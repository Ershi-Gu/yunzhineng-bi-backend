package com.ershi.bibackend.mapper;

import com.ershi.bibackend.model.entity.Chart;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
* @author qingtian_jun
* @description 针对表【chart(图标信息表)】的数据库操作Mapper
* @createDate 2024-05-15 01:37:03
* @Entity com.ershi.bibackend.model.entity.Chart
*/
public interface ChartMapper extends BaseMapper<Chart> {

    /**
     * 创建 ChartData 分表
     * @param tableName 分表名
     * @param columns ”列名 类型“
     */
    void createTable(@Param("tableName") String tableName, @Param("columns") Map<String, String> columns);

    /**
     * 向 ChartData 分表插入数据
     * @param tableName 分表名
     * @param data 原始数据
     */
    void insertData(@Param("tableName") String tableName, @Param("data") List<Map<Integer, String>> data);
}




