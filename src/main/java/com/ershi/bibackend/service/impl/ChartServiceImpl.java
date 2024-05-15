package com.ershi.bibackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ershi.bibackend.constant.CommonConstant;
import com.ershi.bibackend.model.dto.chart.ChartQueryRequest;
import com.ershi.bibackend.model.entity.*;
import com.ershi.bibackend.model.vo.ChartVO;
import com.ershi.bibackend.model.vo.PostVO;
import com.ershi.bibackend.model.vo.UserVO;
import com.ershi.bibackend.service.ChartService;
import com.ershi.bibackend.mapper.ChartMapper;
import com.ershi.bibackend.utils.SqlUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

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

    }

    @Override
    public QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        if (chartQueryRequest == null) {
            return queryWrapper;
        }

        Long id = chartQueryRequest.getId();
        String goal = chartQueryRequest.getGoal();
        String chartType = chartQueryRequest.getChartType();
        Long userId = chartQueryRequest.getUserId();
        int current = chartQueryRequest.getCurrent();
        int pageSize = chartQueryRequest.getPageSize();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();

        // 拼接查询条件
        queryWrapper.eq(id != null && id > 0, "id", id);
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
        ChartVO chartVO = ChartVO.objToVo(chart);


        return chartVO;
    }

    @Override
    public Page<ChartVO> getChartVOPage(Page<Chart> chartPage, HttpServletRequest request) {
        return null;
    }
}




