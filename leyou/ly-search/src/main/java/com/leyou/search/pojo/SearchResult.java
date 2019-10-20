package com.leyou.search.pojo;

import com.leyou.common.vo.PageResult;
import com.leyou.domain.Brand;
import com.leyou.domain.Category;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SearchResult extends PageResult<Goods> {

    private List<Category> categories;//分类待选项

    private List<Brand> brands;//品牌待选项

    private List<Map<String,Object>> specs; // 规格参数过滤条件
    public SearchResult(Long total, Long totalPage, List<Goods> items, List<Category> categories, List<Brand> brands) {
        super(total, totalPage, items);
        this.categories = categories;
        this.brands = brands;
    }

    public SearchResult(List<Category> categories, List<Brand> brands, List<Map<String, Object>> specs) {
        this.categories = categories;
        this.brands = brands;
        this.specs = specs;
    }

    public SearchResult(Long total, List<Goods> items, List<Category> categories, List<Brand> brands, List<Map<String, Object>> specs) {
        super(total, items);
        this.categories = categories;
        this.brands = brands;
        this.specs = specs;
    }

    public SearchResult(Long total, Long totalPage, List<Goods> items, List<Category> categories, List<Brand> brands, List<Map<String, Object>> specs) {
        super(total, totalPage, items);
        this.categories = categories;
        this.brands = brands;
        this.specs = specs;
    }

    public SearchResult() {
    }

}