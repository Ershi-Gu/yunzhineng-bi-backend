package com.ershi.bibackend.service.impl;

import java.time.Duration;
import java.util.*;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ershi.bibackend.common.ErrorCode;
import com.ershi.bibackend.constant.CommonConstant;
import com.ershi.bibackend.exception.BusinessException;
import com.ershi.bibackend.exception.ThrowUtils;
import com.ershi.bibackend.manager.AIManager;
import com.ershi.bibackend.model.dto.chart.ChartQueryRequest;
import com.ershi.bibackend.model.dto.chart.GenChartByAIRequest;
import com.ershi.bibackend.model.entity.*;
import com.ershi.bibackend.model.enums.ChartStatusEnum;
import com.ershi.bibackend.model.enums.FileUploadBizEnum;
import com.ershi.bibackend.model.vo.BiResponse;
import com.ershi.bibackend.model.vo.ChartVO;
import com.ershi.bibackend.service.ChartService;
import com.ershi.bibackend.mapper.ChartMapper;
import com.ershi.bibackend.utils.ExcelUtils;
import com.ershi.bibackend.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author qingtian_jun
 * @description 针对表【chart(图标信息表)】的数据库操作Service实现
 * @createDate 2024-05-15 01:37:04
 */
@Service
@Slf4j
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart> implements ChartService {

    @Resource
    private ChartMapper chartMapper;

    @Resource
    private AIManager aiManager;

    @Resource
    private ThreadPoolExecutor myThreadPoolExecutor;

    @Resource
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    // 用于保存执行任务的线程
    private static final ConcurrentHashMap<String, Thread> threadMap = new ConcurrentHashMap<>();

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
        if (chart == null) {
            return null;
        }
        ChartVO chartVO = new ChartVO();
        BeanUtils.copyProperties(chart, chartVO);
        return chartVO;
    }

    @Override
    public Page<ChartVO> getChartVOPage(Page<Chart> chartPage, HttpServletRequest request) {
        List<Chart> ChartList = chartPage.getRecords();
        Page<ChartVO> ChartrVOPage = new Page<>(chartPage.getCurrent(),
                chartPage.getSize(), chartPage.getTotal());
        if (CollUtil.isEmpty(ChartList)) {
            return ChartrVOPage;
        }
        // 填充信息
        List<ChartVO> ChartVOList = ChartList.stream().map(ChartVO::objToVo).collect(Collectors.toList());
        ChartrVOPage.setRecords(ChartVOList);
        return ChartrVOPage;
    }

    @Override
    public BiResponse genChartByAI(MultipartFile multipartFile, GenChartByAIRequest genChartByAIRequest, User loginUser) {

        String chartName = genChartByAIRequest.getName();
        String goal = genChartByAIRequest.getGoal();
        String chartType = genChartByAIRequest.getChartType();

        // 参数检验
        ThrowUtils.throwIf(StringUtils.isNotBlank(chartName) && chartName.length() > 100, ErrorCode.PARAMS_ERROR, "图表名称过长");
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "分析目标为空");

        // 文件校验
        long size = multipartFile.getSize();
        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件超过 1M");
        String originalFilename = multipartFile.getOriginalFilename();
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFileSuffixList = Arrays.asList("xlsx");
        ThrowUtils.throwIf(!validFileSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");

        // 用户输入数据处理
        List<Map<Integer, String>> csvDataList = ExcelUtils.excelToCsv(multipartFile);
        String csvData = ExcelUtils.csvToString(csvDataList);
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求:").append("\n");
        if (StringUtils.isNotBlank(chartType)) {
            goal = goal + "，请使用" + chartType;
        }
        userInput.append(goal).append("\n");
        userInput.append("原始数据:").append("\n");
        userInput.append(csvData).append("\n");

        // 调用 AI 服务生成图表和分析
        String result = aiManager.doChat(userInput.toString());
        String[] splits = result.split("【【【【【");
        if (splits.length != 3) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 分析异常");
        }

        // 返回数据格式处理
        String genChart = splits[1];
        String genResult = splits[2];
        genChart = RemoveFirstNewline(genChart);
        genResult = RemoveFirstNewline(genResult);
        // todo 生成输入的校验，使用正则处理不合法的字符，应对 AI 智障

        // 保存图表数据
        Chart chart = new Chart();
        chart.setName(chartName);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setGenChart(genChart);
        chart.setGenResult(genResult);
        chart.setUserId(loginUser.getId());
        boolean save = this.save(chart);
        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR, "图表保存失败");

        // 分表保存图表原始数据
//        String chartDataTableName = "chart_" + chart.getId();
//        createChartDataTableAndInsertData(chartDataTableName, csvDataList);

        BiResponse biResponse = new BiResponse();
        biResponse.setId(chart.getId());
        biResponse.setGenChart(genChart);
        biResponse.setGenResult(genResult);

        return biResponse;
    }


    @Override
    public BiResponse genChartByAIAsync(MultipartFile multipartFile, GenChartByAIRequest genChartByAIRequest, User loginUser) {

        String chartName = genChartByAIRequest.getName();
        String goal = genChartByAIRequest.getGoal();
        String chartType = genChartByAIRequest.getChartType();

        // 参数检验
        ThrowUtils.throwIf(StringUtils.isNotBlank(chartName) && chartName.length() > 100, ErrorCode.PARAMS_ERROR, "图表名称过长");
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "分析目标为空");

        // 文件校验
        long size = multipartFile.getSize();
        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件超过 1M");
        String originalFilename = multipartFile.getOriginalFilename();
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFileSuffixList = Arrays.asList("xlsx");
        ThrowUtils.throwIf(!validFileSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");

        // 用户输入数据处理
        List<Map<Integer, String>> csvDataList = ExcelUtils.excelToCsv(multipartFile);
        String csvData = ExcelUtils.csvToString(csvDataList);
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求:").append("\n");
        if (StringUtils.isNotBlank(chartType)) {
            goal = goal + "，请使用" + chartType;
        }
        userInput.append(goal).append("\n");
        userInput.append("原始数据:").append("\n");
        userInput.append(csvData).append("\n");

        // 保存任务记录到数据库
        Chart chart = new Chart();
        chart.setName(chartName);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setStatus(ChartStatusEnum.WAIT.getValue());
        chart.setUserId(loginUser.getId());
        boolean save = this.save(chart);
        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR, "生成图表任务记录保存失败");


        // 异步调用 AI 服务生成图表和分析
        CompletableFuture.runAsync(() -> {

            // 修改任务状态
            Chart updateChart = new Chart();
            updateChart.setId(chart.getId());
            updateChart.setStatus(ChartStatusEnum.RUNNING.getValue());
            updateChart.setExecuteMessage(ChartStatusEnum.RUNNING.getText());
            boolean updateResult = this.updateById(updateChart);
            if (!updateResult) {
                handlerChartUpdateError(updateChart.getId(), "更新 Chart 任务状态为进行中失败");
                return;
            }

            // AI 服务
            String result = aiManager.doChat(userInput.toString());
            String[] splits = result.split("【【【【【");
            if (splits.length != 3) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 分析异常");
            }

            // 返回数据格式处理
            String genChart = splits[1];
            String genResult = splits[2];
            genChart = RemoveFirstNewline(genChart);
            genResult = RemoveFirstNewline(genResult);
            // todo 生成输入的校验，使用正则处理不合法的字符，应对 AI 智障


            // 保存结果到数据库，并修改任务状态
            updateChart.setGenChart(genChart);
            updateChart.setGenResult(genResult);
            updateChart.setStatus(ChartStatusEnum.SUCCEED.getValue());
            updateChart.setExecuteMessage(ChartStatusEnum.SUCCEED.getText());
            boolean overBiUpdateResult = this.updateById(updateChart);
            if (!overBiUpdateResult) {
                handlerChartUpdateError(updateChart.getId(), "保存 Chart 生成任务执行结果数据库失败");
                return;
            }

        }, myThreadPoolExecutor);

        // 返回用户结果 => 异步处理（这里不直接返回生成结果），用户直接去图标管理界面查询处理进度
        BiResponse biResponse = new BiResponse();
        biResponse.setId(chart.getId());

        return biResponse;
    }


    /**
     * 更新 Chart 记录表错误处理工具
     *
     * @param updateChartId
     */
    public void handlerChartUpdateError(Long updateChartId, String executeMessage) {
        Chart updateChart = new Chart();
        updateChart.setId(updateChartId);
        updateChart.setStatus(ChartStatusEnum.FAILED.getValue());
        updateChart.setExecuteMessage(executeMessage);
        boolean updateResult = this.updateById(updateChart);
        if (!updateResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新任务记录数据库失败-"
                    + updateChart.getId() + "-" + executeMessage);
        }
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


    /**
     * 原始数据分表存储，该方法会创建一个 chart_图表id 的新表，并将原始数据按列与列对应插入
     *
     * @param tableName 分表名
     * @param data      原始数据
     */
    public void createChartDataTableAndInsertData(String tableName, List<Map<Integer, String>> data) {
        if (data == null || data.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分析数据为空");
        }
        // 根据第一行数据表头动态生成表结构
        Map<String, String> columns = new HashMap<>();
        Map<Integer, String> firstRow = data.get(0);
        firstRow.forEach((key, value) -> {
            String columnType = "VARCHAR(1024)";
            // 对列名进行处理
            value = "`" + StringUtils.deleteWhitespace(value) + "`";
            columns.put(value, columnType);
        });

        chartMapper.createTable(tableName, columns);

        data.remove(0);
        chartMapper.insertData(tableName, data);
    }
}




