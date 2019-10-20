package com.leyou.item.service;


import com.leyou.common.vo.PageResult;
import com.leyou.domain.Brand;

import java.util.List;

public interface BrandService {
    PageResult<Brand> findBrandByPage(Integer page, Integer rows, String sortBy, boolean desc, String key);

    void saveBrand(Brand brand, List<Long> cids);

    public Brand findBrandById(Long id);

    List<Brand> findBrandsById(Long id);

    List<Brand> findBrandsByIds(List<Long> ids);
}
