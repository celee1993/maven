package com.leyou.item.mapper;

import com.leyou.common.mapper.BaseMapper;
import com.leyou.domain.Brand;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

public interface BrandMapper extends BaseMapper<Brand> {

    @Insert("INSERT INTO tb_category_brand values(#{cid},#{bid})")
    int insertCategoryBrand(@Param("cid") Long cid, @Param("bid") Long bid);
}
