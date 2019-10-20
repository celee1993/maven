package com.leyou.item.web;

import com.leyou.domain.Category;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 根据父节点查询分类
     * @param pid
     * @return
     */
    @GetMapping("/list")
    public ResponseEntity<List<Category>> findCategoryListByPid(@RequestParam("pid") Long pid) {
        return ResponseEntity.ok(categoryService.findCategoryListByPid(pid));
//        return ResponseEntity.status(HttpStatus.OK).body();
    }

    /**
     * 根据商品分类id查询分类
     * @param ids
     * @return
     */
    @GetMapping("/list/ids")
    public ResponseEntity<List<Category>> findCategoryListByIds(@RequestParam("ids") List<Long> ids) {
        return ResponseEntity.ok(categoryService.findByIds(ids));
//        return ResponseEntity.status(HttpStatus.OK).body();
    }

    @GetMapping
    public ResponseEntity<List<Category>> findCategoriesByCid3(@RequestParam("id")Long id){
        return ResponseEntity.ok(categoryService.findByCid3(id));
    }

}
