package com.leyou.item.web;

import com.leyou.common.dto.CartDto;
import com.leyou.common.vo.PageResult;
import com.leyou.domain.Sku;
import com.leyou.domain.Spu;
import com.leyou.domain.SpuDetail;
import com.leyou.item.service.GoodsService;
import com.leyou.vo.SpuVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping
public class GoodsController {

    @Autowired
    private GoodsService goodsService;


    /**
     * 分页查询商品spu
     * @param page
     * @param rows
     * @param saleable
     * @param key
     * @return
     */
    @GetMapping("/spu/page")
    public ResponseEntity<PageResult<SpuVO>> findSpuByPage(
            @RequestParam(value = "page",defaultValue = "1") Integer page,
            @RequestParam(value = "rows",defaultValue = "5") Integer rows,
            @RequestParam(value = "saleable",required = false) Boolean saleable,
            @RequestParam(value = "key",required = false) String key
    ) {
        PageResult<SpuVO> result= goodsService.findSpuByPage(page, rows, saleable, key);
        return ResponseEntity.ok(result);
    }

    /**
     * 新增商品
     * @param
     * @param
     * @return
     */
    @PostMapping("/goods")
    public ResponseEntity<Void> saveGoods(@RequestBody Spu spu){
        goodsService.saveGoods(spu);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据商品ID查询商品详情
     * @param spuId
     * @return
     */
    @GetMapping("/spu/detail/{spuId}")
    public ResponseEntity<SpuDetail> findDetail(@PathVariable("spuId")Long spuId) {
       SpuDetail spuDetail= goodsService.findDetailBySpuId(spuId);
        return ResponseEntity.ok(spuDetail);
    }

    /**
     * 根据spuId查询详情
     * @param spuId
     * @return
     */
    @GetMapping("/sku/list")
    public ResponseEntity<List<Sku>> findSku(@RequestParam("id") Long spuId) {
        List<Sku> skus = goodsService.findSkuListBySpuId(spuId);
        return ResponseEntity.ok(skus);
    }

    @PutMapping("/goods")
    public ResponseEntity<Void> updateGoods(@RequestBody Spu spu){
        goodsService.updateGoods(spu);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 根据spuId查询单个spu
     * @param spuId
     * @return
     */
    @GetMapping("/spu/{spuId}")
    public ResponseEntity<Spu>  findSpuById(@PathVariable("spuId")Long spuId){
        return ResponseEntity.ok(goodsService.findSpuById(spuId));
    }

    /**
     * 根据skuId集合批量查询sku集合
     * @param ids
     * @return
     */
    @GetMapping("/sku/list/ids")
    public ResponseEntity<List<Sku>> findSkuListByIds(@RequestParam("ids") List<Long> ids){
        return ResponseEntity.ok(goodsService.findSkuListByIds(ids));
    }

    /**
     * 根据购物车的id查询SKU信息
     * @param id
     * @return
     */
    @GetMapping("/sku/ids")
    public ResponseEntity<Sku> findSkuById(@RequestParam("id") Long id){
        return ResponseEntity.ok(goodsService.findSkuById(id));
    }

    /**
     * 减库存
     * @param carts
     * @return
     */
    @PostMapping("/stock/decrease")
    public ResponseEntity<Void> decreaseStock(@RequestBody List<CartDto> carts){
        goodsService.decreaseStock(carts);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
