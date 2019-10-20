package com.leyou.api;

import com.leyou.domain.Category;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface CategoryApi {
    @GetMapping("/category/list/ids")
    List<Category> findCategoryListByIds(@RequestParam("ids") List<Long> ids);

    @GetMapping("/category")
    List<Category> findCategoriesByCid3(@RequestParam("id")Long id);
}
