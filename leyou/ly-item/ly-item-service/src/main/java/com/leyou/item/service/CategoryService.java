package com.leyou.item.service;

import com.leyou.domain.Category;

import java.util.List;

/**
 * 商品类业务层
 */
public interface CategoryService {
    List<Category> findCategoryListByPid(Long pid);

    String findName(Long id);

    List<Category> findByIds(List<Long> ids);

    List<Category> findByCid3(Long id);
}
