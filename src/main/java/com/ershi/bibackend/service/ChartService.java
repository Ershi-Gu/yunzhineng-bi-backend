package com.ershi.bibackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ershi.bibackend.common.BaseResponse;
import com.ershi.bibackend.model.dto.chart.ChartQueryRequest;
import com.ershi.bibackend.model.dto.chart.GenChartByAIRequest;
import com.ershi.bibackend.model.entity.Chart;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ershi.bibackend.model.entity.Chart;
import com.ershi.bibackend.model.entity.User;
import com.ershi.bibackend.model.enums.FileUploadBizEnum;
import com.ershi.bibackend.model.vo.BiResponse;
import com.ershi.bibackend.model.vo.ChartVO;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * @author qingtian_jun
 * @description 针对表【chart(图标信息表)】的数据库操作Service
 * @createDate 2024-05-15 01:37:04
 */
public interface ChartService extends IService<Chart> {

    /**
     * 校验
     *
     * @param chart
     * @param add
     */
    void validChart(Chart chart, boolean add);

    /**
     * 上传文件校验
     *
     * @param multipartFile
     * @param fileUploadBizEnum
     */
    void validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum);


    /**
     * 获取查询条件
     *
     * @param chartQueryRequest
     * @return
     */
    QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest);

    /**
     * 从 ES 查询
     *
     * @param chartQueryRequest
     * @return
     */
    Page<Chart> searchFromEs(ChartQueryRequest chartQueryRequest);

    /**
     * 获取帖子封装
     *
     * @param chart
     * @param request
     * @return
     */
    ChartVO getChartVO(Chart chart, HttpServletRequest request);

    /**
     * 分页获取帖子封装
     *
     * @param chartPage
     * @param request
     * @return
     */
    Page<ChartVO> getChartVOPage(Page<Chart> chartPage, HttpServletRequest request);


    /**
     * BI 智能数据分析
     *
     * @param multipartFile
     * @param genChartByAIRequest
     * @param loginUser
     * @return
     */
    BiResponse genChartByAI(@RequestPart("file") MultipartFile multipartFile,
                            GenChartByAIRequest genChartByAIRequest, User loginUser);

}
