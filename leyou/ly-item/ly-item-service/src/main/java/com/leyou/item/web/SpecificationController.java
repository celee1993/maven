package com.leyou.item.web;

import com.leyou.domain.SpecGroup;
import com.leyou.domain.SpecParam;
import com.leyou.item.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/spec")
@RestController
public class SpecificationController {

    @Autowired
    private SpecificationService specificationService;

    /**
     * 根据分类id查询规格组
     * @param cid
     * @return
     */
    @GetMapping("/groups/{cid}")
    public ResponseEntity<List<SpecGroup>> findSpecGroupByCid(@PathVariable("cid") Long cid) {
        return ResponseEntity.ok(specificationService.findSpecGroupByCid(cid));
    }

    /**
     * 根据条件查询规格参数
     *
     * @param gid
     * @return
     */
    @GetMapping("/params")
    public ResponseEntity<List<SpecParam>> findParamsList(
            @RequestParam(value = "gid",required = false) Long gid,
            @RequestParam(value = "cid",required = false) Long cid,
            @RequestParam(value = "searching",required = false) Boolean searching
    ) {
        return ResponseEntity.ok(specificationService.findParamsList(gid,cid,searching));
    }


    /**
     * 查询商品的规格参数组和组内属性
     * @param cid
     * @return
     */
    @GetMapping("/group")
    ResponseEntity<List<SpecGroup>>  findGroupsAndParams(@RequestParam(value = "cid",required = false) Long cid){
        return ResponseEntity.ok(specificationService.findGroupsAndParams(cid));
    }
}
