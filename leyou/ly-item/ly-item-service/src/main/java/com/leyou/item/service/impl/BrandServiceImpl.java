package com.leyou.item.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.domain.Brand;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.service.BrandService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandMapper brandMapper;
    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 分页搜索查询
     * @param page
     * @param rows
     * @param sortBy
     * @param desc
     * @param key
     * @return
     */
    @Override
    public PageResult<Brand> findBrandByPage(Integer page, Integer rows, String sortBy, boolean desc, String key) {
        //分页
        PageHelper.startPage(page, rows);
        //过滤
        Example example = new Example(Brand.class);
        if (StringUtils.isNotBlank(key)) {
            //过滤条件
            example.createCriteria().orLike("name", "%" + key + "%")
                    .orEqualTo("letter", key.toUpperCase());
        }
        //排序
        if (StringUtils.isNotBlank(sortBy)) {
            String orderByClause=sortBy+(desc ? " DESC" : " ASC");
            example.setOrderByClause(orderByClause);
        }
        //查询
        List<Brand> list = brandMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        //解析分页助手
        PageInfo<Brand> info = new PageInfo<>(list);

        return new PageResult<>(info.getTotal(),list);
    }

    /**
     * 添加品牌以及品牌商品中间表
     * @param brand
     * @param cids
     */
    @Override
    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {
        //品牌id为null
        brand.setId(null);
        //保存品牌的操作
        int i = brandMapper.insert(brand);
        if (i != 1) {
            //新增失败
            throw new LyException(ExceptionEnum.BRAND_SAVE_ERROR);
        }
        //新增中间表
        for (Long cid : cids) {
            i = brandMapper.insertCategoryBrand(cid, brand.getId());
            if (i != 1) {
                //新增失败
                throw new LyException(ExceptionEnum.BRAND_SAVE_ERROR);
            }
        }
    }

    /**
     * 根据品牌id查询品牌
     * @param id
     * @return
     */
    @Override
    public Brand findBrandById(Long id) {
        Brand brand = new Brand();
        brand.setId(id);
        Brand b = brandMapper.selectByPrimaryKey(brand);
        if (b == null) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return b;
    }

    /**
     * 根据分类id查询品牌集
     * @param id
     * @return
     */
    @Override
    public List<Brand> findBrandsById(Long id) {
        //先查中间表 获得brand_ids
        List<Long> brand_ids = categoryMapper.findBrandIds(id);
        //根据ids获取品牌集合
        List<Brand> brands = brandMapper.selectByIdList(brand_ids);
        if (CollectionUtils.isEmpty(brands)) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brands;
    }


    /**
     * 根据商品品牌ids查询品牌集合
     * @param ids
     * @return
     */
    @Override
    public List<Brand> findBrandsByIds(List<Long> ids) {
        List<Brand> brands = brandMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(brands)) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brands;
    }
}
