package com.leyou.search.web;

import com.leyou.common.vo.PageResult;
import com.leyou.domain.Category;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SearchController {

    @Autowired
    private GoodsService goodsService;

    @PostMapping("/page")
    public ResponseEntity<PageResult<Goods>> search(@RequestBody SearchRequest searchRequest) {
        return ResponseEntity.ok(goodsService.search(searchRequest));
    }
    @GetMapping("/category")
    public ResponseEntity<List<Category>> findCategoriesByCid3(@RequestParam("id")Long id) {
        return ResponseEntity.ok(goodsService.findCategoriesByCid3(id));
    }
}
