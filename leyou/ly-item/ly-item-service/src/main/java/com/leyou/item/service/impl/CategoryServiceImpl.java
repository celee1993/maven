package com.leyou.item.service.impl;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.domain.Category;
import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;

/**
 * 业务层实现类
 */
@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<Category> findCategoryListByPid(Long pid) {
        //查询条件 mapper会把对象的非空条件作为条件查询
        Category t = new Category();
        t.setParentId(pid);
        List<Category> list = categoryMapper.select(t);
//        判断list
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return list;
    }

    /**
     * 根据id查询类名
     * @param id
     * @return
     */
    @Override
    public String findName(Long id) {
        Category category = new Category();
        category.setId(id);
        return categoryMapper.selectByPrimaryKey(category).getName();
    }

    /**
     * 根据分类id的数组查询分类
     * @param ids
     * @return
     */
    @Override
    public List<Category> findByIds(List<Long> ids) {
        List<Category> list = categoryMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return list;
    }

    @Override
    public List<Category> findByCid3(Long id) {
        Category c3 = categoryMapper.selectByPrimaryKey(id);
        Category c2 = categoryMapper.selectByPrimaryKey(c3.getParentId());
        Category c1 = categoryMapper.selectByPrimaryKey(c2.getParentId());
        return Arrays.asList(c1, c2, c3);
    }
}
