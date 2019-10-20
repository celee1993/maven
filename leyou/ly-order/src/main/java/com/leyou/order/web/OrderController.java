package com.leyou.order.web;


import com.leyou.order.dto.OrderDto;
import com.leyou.order.pojo.Order;
import com.leyou.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;


    /**
     * 创建订单
     * @param order
     * @return
     */
    @PostMapping
    public ResponseEntity<Long> createOrder(@RequestBody OrderDto order) {
        return ResponseEntity.ok(orderService.createOrder(order));
    }

    /**
     * 查询订单
     * @param orderId
     * @return
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> findOrderById(@PathVariable("orderId") Long orderId) {
        return ResponseEntity.ok(orderService.findOrderById(orderId));
    }

    /**
     * 获取微信支付链接
     * @param orderId
     * @return
     */
    @GetMapping("url/{orderId}")
    public ResponseEntity<String> findWxPayUrl(@PathVariable("orderId") Long orderId) {
        return ResponseEntity.ok(orderService.findWxPayUrl(orderId));
    }

    /**
     * 查询订单状态
     * @param orderId
     * @return
     */
    @GetMapping("state/{id}")
    public ResponseEntity<Integer> findOrderState(@PathVariable("id") Long orderId) {
        return ResponseEntity.ok(orderService.findOrderState(orderId).getValue());
    }
}
