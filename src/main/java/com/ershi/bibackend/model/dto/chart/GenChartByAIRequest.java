package com.ershi.bibackend.model.dto.chart;

import lombok.Data;

import java.io.Serializable;

/**
 * AI 生成请求
 * @author Ershi
 * @date 2024/05/16
 */
@Data
public class GenChartByAIRequest implements Serializable {

    /**
     * 图表名称
     */
    private String name;

    /**
     * 分析目标
     */
    private String goal;

    /**
     * 图表类型
     */
    private String chartType;


    private static final long serialVersionUID = -7572359661344874517L;

}
