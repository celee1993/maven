package com.leyou.item.mapper;

import com.leyou.common.mapper.BaseMapper;
import com.leyou.domain.Category;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 持久层 通用mapper
 */
public interface CategoryMapper extends BaseMapper<Category> {

    @Select("SELECT brand_id FROM tb_category_brand WHERE category_id = #{id}")
    List<Long> findBrandIds(@Param("id") Long id);
}
