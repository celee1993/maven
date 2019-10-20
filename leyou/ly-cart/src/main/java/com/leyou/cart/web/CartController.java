package com.leyou.cart.web;

import com.leyou.cart.pojo.Cart;
import com.leyou.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 添加购物车
     * @param cart
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> addCart(@RequestBody Cart cart) {
        cartService.addCart(cart);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 查询客服的购物车列表
     * @return
     */
    @GetMapping("/list")
    public ResponseEntity<List<Cart>> findCartList() {
        return ResponseEntity.ok(cartService.findCartList());
    }

    /**
     * 修改购物车对应商品的数量
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> updateNum(@RequestParam("id") Long skuId, @RequestParam("num") Integer num) {
        cartService.updateNum(skuId,num);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 购物车商品删除
     * @param skuId
     * @return
     */
    @DeleteMapping("/{skuId}")
    public ResponseEntity<Void> deleteCart(@PathVariable("skuId") Long skuId) {
        cartService.deleteCart(skuId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


}
