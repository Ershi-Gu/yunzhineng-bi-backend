package com.ershi.bibackend.model.vo;

import cn.hutool.json.JSONUtil;
import com.ershi.bibackend.model.entity.Chart;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.List;

/**
 * 用户视图（脱敏）
 *
 * @author <a href="https://github.com/guershi">贰拾</a>
 * 
 */
@Data
public class ChartVO implements Serializable {

    /**
     * 分析目标
     */
    private String goal;

    /**
     * 图表数据
     */
    private String chartData;

    /**
     * 图表类型
     */
    private String chartType;

    /**
     * 生成的图表数据
     */
    private String genChart;

    /**
     * 生成的分析结论
     */
    private String genResult;

    /**
     *创建用户 id
     */
    private Long userId;

    private static final long serialVersionUID = 1L;

    /**
     * 包装类转对象
     *
     * @param chartVO
     * @return
     */
    public static Chart voToObj(ChartVO chartVO) {
        if (chartVO == null) {
            return null;
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartVO, chart);
        return chart;
    }

    /**
     * 对象转包装类
     *
     * @param chart
     * @return
     */
    public static ChartVO objToVo(Chart chart) {
        if (chart == null) {
            return null;
        }
        ChartVO chartVO = new ChartVO();
        BeanUtils.copyProperties(chart, chartVO);
        return chartVO;
    }
}