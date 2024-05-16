package com.ershi.bibackend.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * Bi 的返回结果
 * @author Ershi
 * @date 2024/05/16
 */
@Data
public class BiResponse implements Serializable {

    private Long id;

    private String genChart;

    private String genResult;

    private static final long serialVersionUID = 384969924709186467L;
}
