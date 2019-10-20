package com.leyou.order.service;

import com.leyou.order.dto.OrderDto;
import com.leyou.order.enums.PayState;
import com.leyou.order.pojo.Order;

import java.util.Map;


public interface OrderService {
    Long createOrder(OrderDto order);

    Order findOrderById(Long orderId);

    String findWxPayUrl(Long orderId);

    void handleNotify(Map<String, String> result);

    PayState findOrderState(Long orderId);
}
