package com.ershi.bibackend.service.impl;

import java.util.Arrays;
import java.util.Date;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ershi.bibackend.common.ErrorCode;
import com.ershi.bibackend.common.ResultUtils;
import com.ershi.bibackend.constant.CommonConstant;
import com.ershi.bibackend.exception.BusinessException;
import com.ershi.bibackend.exception.ThrowUtils;
import com.ershi.bibackend.manager.AIManager;
import com.ershi.bibackend.model.dto.chart.ChartQueryRequest;
import com.ershi.bibackend.model.dto.chart.GenChartByAIRequest;
import com.ershi.bibackend.model.entity.*;
import com.ershi.bibackend.model.enums.FileUploadBizEnum;
import com.ershi.bibackend.model.vo.BiResponse;
import com.ershi.bibackend.model.vo.ChartVO;
import com.ershi.bibackend.model.vo.PostVO;
import com.ershi.bibackend.model.vo.UserVO;
import com.ershi.bibackend.service.ChartService;
import com.ershi.bibackend.mapper.ChartMapper;
import com.ershi.bibackend.utils.ExcelUtils;
import com.ershi.bibackend.utils.SqlUtils;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatResponse;
import jdk.nashorn.internal.ir.annotations.Reference;
import netscape.security.PrivilegeManager;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author qingtian_jun
 * @description 针对表【chart(图标信息表)】的数据库操作Service实现
 * @createDate 2024-05-15 01:37:04
 */
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart> implements ChartService {

    @Resource
    private AIManager aiManager;

    @Override
    public void validChart(Chart chart, boolean add) {
        if (chart == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
    }


    @Override
    public void validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum) {
        // 文件大小
        long fileSize = multipartFile.getSize();
        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final long ONE_M = 1024 * 1024L;
        if (FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)) {
            if (fileSize > ONE_M) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 1M");
            }
            if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp").contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        }
    }

    @Override
    public QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        if (chartQueryRequest == null) {
            return queryWrapper;
        }

        Long id = chartQueryRequest.getId();
        String name = chartQueryRequest.getName();
        String goal = chartQueryRequest.getGoal();
        String chartType = chartQueryRequest.getChartType();
        Long userId = chartQueryRequest.getUserId();
        int current = chartQueryRequest.getCurrent();
        int pageSize = chartQueryRequest.getPageSize();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();

        // 拼接查询条件
        queryWrapper.eq(id != null && id > 0, "id", id);
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.eq(StringUtils.isNotBlank(goal), "goal", goal);
        queryWrapper.eq(ObjectUtils.isNotEmpty(chartType), "chartType", chartType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        return queryWrapper;
    }

    @Override
    public Page<Chart> searchFromEs(ChartQueryRequest chartQueryRequest) {
        return null;
    }

    @Override
    public ChartVO getChartVO(Chart chart, HttpServletRequest request) {
        return ChartVO.objToVo(chart);
    }

    @Override
    public Page<ChartVO> getChartVOPage(Page<Chart> chartPage, HttpServletRequest request) {
        return null;
    }

    @Override
    public BiResponse genChartByAI(MultipartFile multipartFile, GenChartByAIRequest genChartByAIRequest, User loginUser) {

        // 参数检验
        String name = genChartByAIRequest.getName();
        String goal = genChartByAIRequest.getGoal();
        String chartType = genChartByAIRequest.getChartType();
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "图表名称过长");
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "分析目标为空");

        // 用户输入
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求:").append("\n");
        if (StringUtils.isNotBlank(chartType)){
            goal = goal + "请使用" + chartType;
        }
        userInput.append("数据:").append("\n");
        userInput.append(csvData).append("\n");

        // 调用 AI 服务生成图表和分析
        String result = aiManager.doChat(userInput.toString());
        String[] splits = result.split("【【【【【");
        if (splits.length != 3) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 分析异常");
        }
        String genChart = splits[1];
        String genResult = splits[2];
        // 格式处理
        genChart = RemoveFirstNewline(genChart);
        genResult = RemoveFirstNewline(genResult);

        // 保存图表数据
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setGenChart(genChart);
        chart.setGenResult(genResult);
        chart.setUserId(loginUser.getId());
        boolean save = this.save(chart);
        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR, "图表保存失败");

        BiResponse biResponse = new BiResponse();
        biResponse.setId(chart.getId());
        biResponse.setGenChart(genChart);
        biResponse.setGenResult(genResult);

        return biResponse;
    }

    /**
     * 移除首行换行符
     *
     * @param string
     * @return {@link String}
     */
    public String RemoveFirstNewline(String string) {
        // 找到第一个换行符的位置
        int firstNewlineIndex = string.indexOf('\n');
        if (firstNewlineIndex != -1) {
            // 移除第一个换行符
            string = string.substring(0, firstNewlineIndex) +
                    string.substring(firstNewlineIndex + 1);
        }

        // 找到最后一个换行符的位置
        int lastNewlineIndex = string.lastIndexOf('\n');
        if (lastNewlineIndex != -1) {
            // 移除最后一个换行符
            string = string.substring(0, lastNewlineIndex) +
                    string.substring(lastNewlineIndex + 1);
        }

        return string;
    }
}




