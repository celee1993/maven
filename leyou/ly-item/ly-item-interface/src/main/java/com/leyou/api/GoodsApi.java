package com.leyou.api;

import com.leyou.common.dto.CartDto;
import com.leyou.common.vo.PageResult;
import com.leyou.domain.Sku;
import com.leyou.domain.Spu;
import com.leyou.domain.SpuDetail;
import com.leyou.vo.SpuVO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GoodsApi {

    @GetMapping("/spu/page")
    PageResult<SpuVO> findSpuByPage(
            @RequestParam(value = "page",defaultValue = "1") Integer page,
            @RequestParam(value = "rows",defaultValue = "5") Integer rows,
            @RequestParam(value = "saleable",required = false) Boolean saleable,
            @RequestParam(value = "key",required = false) String key
    );

    @GetMapping("/spu/detail/{spuId}")
    SpuDetail findDetail(@PathVariable("spuId")Long spuId);

    @GetMapping("/sku/list")
    List<Sku> findSku(@RequestParam("id") Long spuId);


    @GetMapping("/spu/{spuId}")
    Spu findSpuById(@PathVariable("spuId")Long spuId);

    @PostMapping("/stock/decrease")
    void decreaseStock(@RequestBody List<CartDto> carts);
}
