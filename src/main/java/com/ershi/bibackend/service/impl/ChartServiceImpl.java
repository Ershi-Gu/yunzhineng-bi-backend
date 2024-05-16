package com.ershi.bibackend.service.impl;

import java.util.Arrays;
import java.util.Date;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ershi.bibackend.common.BaseResponse;
import com.ershi.bibackend.common.ErrorCode;
import com.ershi.bibackend.constant.CommonConstant;
import com.ershi.bibackend.exception.BusinessException;
import com.ershi.bibackend.exception.ThrowUtils;
import com.ershi.bibackend.model.dto.chart.ChartQueryRequest;
import com.ershi.bibackend.model.dto.chart.GenChartByAIRequest;
import com.ershi.bibackend.model.entity.*;
import com.ershi.bibackend.model.enums.FileUploadBizEnum;
import com.ershi.bibackend.model.vo.ChartVO;
import com.ershi.bibackend.model.vo.PostVO;
import com.ershi.bibackend.model.vo.UserVO;
import com.ershi.bibackend.service.ChartService;
import com.ershi.bibackend.mapper.ChartMapper;
import com.ershi.bibackend.utils.ExcelUtils;
import com.ershi.bibackend.utils.SqlUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author qingtian_jun
 * @description 针对表【chart(图标信息表)】的数据库操作Service实现
 * @createDate 2024-05-15 01:37:04
 */
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
        implements ChartService {

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
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
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
    public String genChartByAI(MultipartFile multipartFile,
                               GenChartByAIRequest genChartByAIRequest, HttpServletRequest request) {

        // 参数检验
        String name = genChartByAIRequest.getName();
        String goal = genChartByAIRequest.getGoal();
        String chartType = genChartByAIRequest.getChartType();
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "图表名称过长");
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "分析目标为空");

        // 用户输入
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        StringBuilder userInput = new StringBuilder();
        userInput.append("你是一位专业的数据分析师，接下来我会给你我的分析目标和原始数据，请你根据分析目标告诉我分析结论。").append("\n");
        userInput.append("分析目标:").append(goal).append("\n");
        userInput.append("数据:").append(csvData).append("\n");


        return userInput.toString();
    }
}




