package com.leyou.cart.service.impl;

import com.leyou.cart.interceptor.UserInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.cart.service.CartService;
import com.leyou.common.entity.UserInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 购物车业务实现类
 */
@Service
public class CartServiceImpl implements CartService {
    private static final String CART_PREFIX = "cart:uid";
    @Autowired
    private StringRedisTemplate template;

    /**
     * .添加购物车
     *
     * @param cart
     */
    @Override
    public void addCart(Cart cart) {
        //获取登陆的用户
        UserInfo user = UserInterceptor.getUser();
        //redis的key
        String key = CART_PREFIX + user.getId();
        //商品的hashKey
        String hashKey = cart.getSkuId().toString();
        BoundHashOperations<String, Object, Object> operation = template.boundHashOps(key);
        //记录下传进来的购物车的商品数量
        Integer num = cart.getNum();
        //判断购物车
        if (operation.hasKey(hashKey)) {
            //已存在则修改数量 并且覆盖以前的cart
            //从redis中取出已存在的购物车的商品
            String json_cart = operation.get(hashKey).toString();
            cart = JsonUtils.parse(json_cart, Cart.class);
            cart.setNum(cart.getNum() + num);//购物车里本来存的数据加上新传过来的商品的数据
        }
        //写回redis
        operation.put(hashKey, JsonUtils.serialize(cart));
    }

    /**
     * 查询客户的购物车列表
     *
     * @return
     */
    @Override
    public List<Cart> findCartList() {
        //获取登陆的用户
        UserInfo user = UserInterceptor.getUser();
        //redis的key
        String key = CART_PREFIX + user.getId();
        if (!template.hasKey(key)) {
            //用户没有购物车
            throw new LyException(ExceptionEnum.CART_NOT_FOUND);
        }
        BoundHashOperations<String, Object, Object> operation = template.boundHashOps(key);
        //取出所有的值 就是carts
        List<Object> values = operation.values();
        //将json转换为对象
        List<Cart> carts = values.stream().map(v -> JsonUtils.parse(v.toString(), Cart.class)).collect(Collectors.toList());
        return carts;
    }

    /**
     * 修改购物车对应商品的数量
     *
     * @param skuId
     * @param num
     */
    @Override
    public void updateNum(Long skuId, Integer num) {
        //获取登陆的用户
        UserInfo user = UserInterceptor.getUser();
        //redis的key
        String key = CART_PREFIX + user.getId();
        BoundHashOperations<String, Object, Object> operation = template.boundHashOps(key);
        if (!operation.hasKey(skuId.toString())) {
            //购物车里不存在该商品
            throw new LyException(ExceptionEnum.CART_NOT_FOUND);
        }
        //查询购物车
        String json_cart = operation.get(skuId.toString()).toString();
        Cart cart = JsonUtils.parse(json_cart, Cart.class);
        //修改数量
        cart.setNum(num);
        //写回redis
        json_cart = JsonUtils.serialize(cart);
        operation.put(skuId.toString(), json_cart);
    }

    /**
     * 购物车商品删除
     * @param skuId
     */
    @Override
    public void deleteCart(Long skuId) {
        //获取登陆的用户
        UserInfo user = UserInterceptor.getUser();
        //redis的key
        String key = CART_PREFIX + user.getId();
        //删除购物车
        Long delete = template.opsForHash().delete(key, skuId.toString());
        if (!(delete == 1)) {
            //删除失败
            throw new LyException(ExceptionEnum.DELETE_CART_ERROR);
        }
    }
}
