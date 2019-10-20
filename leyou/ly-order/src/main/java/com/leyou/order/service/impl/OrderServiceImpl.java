package com.leyou.order.service.impl;

import com.leyou.common.dto.CartDto;
import com.leyou.common.entity.UserInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.IdWorker;
import com.leyou.domain.Sku;
import com.leyou.order.client.AddressClient;
import com.leyou.order.client.GoodsClient;
import com.leyou.order.config.IdWorkerProperties;
import com.leyou.order.dto.AddressDTO;
import com.leyou.order.dto.OrderDto;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.enums.PayState;
import com.leyou.order.interceptor.UserInterceptor;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderDetail;
import com.leyou.order.pojo.OrderStatus;
import com.leyou.order.service.OrderService;
import com.leyou.order.utils.PayHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@EnableConfigurationProperties(IdWorkerProperties.class)
public class OrderServiceImpl implements OrderService {
    @Autowired
    private IdWorker idWorker;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private OrderStatusMapper orderStatusMapper;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private PayHelper payHelper;


    /**
     * 创建订单
     *
     * @param orderDto
     * @return
     */
    @Transactional
    @Override
    public Long createOrder(OrderDto orderDto) {
        //新增订单数据
        Order order = new Order();
        //1.1 订单编号，基本信息
        long orderId = idWorker.nextId();
        order.setOrderId(orderId);
        order.setCreateTime(new Date());
        order.setPaymentType(orderDto.getPaymentType());
        //1.2 用户信息
        UserInfo user = UserInterceptor.getUser();
        order.setUserId(user.getId());
        order.setBuyerRate(false);
        order.setBuyerNick(user.getUsername());
        //1.3 收货人地址
        //模拟
        AddressDTO address = AddressClient.findById(orderDto.getAddressId());
        order.setReceiver(address.getName());
        order.setReceiverAddress(address.getAddress());
        order.setReceiverCity(address.getCity());
        order.setReceiverDistrict(address.getDistrict());
        order.setReceiverMobile(address.getPhone());
        order.setReceiverState(address.getState());
        order.setReceiverZip(address.getZipCode());
        //1.4 金额
        //将cartDto集合变为map id为key 数量为值
        Map<Long, Integer> numCart = orderDto.getCarts().stream().collect(Collectors.toMap(CartDto::getSkuId, CartDto::getNum));
        Set<Long> ids = numCart.keySet();
        //批量查询skus
        List<Sku> skuList = goodsClient.findSkuListByIds(new ArrayList<>(ids));
        Long totalPrice = 0L;
        //准备orderDetail订单详情集合
        List<OrderDetail> details = new ArrayList<>();
        for (Sku sku : skuList) {
            //计算总价
            totalPrice += sku.getPrice() * numCart.get(sku.getId());
            //封装订单商品详情
            OrderDetail detail = new OrderDetail();
            detail.setImage(StringUtils.substringBefore(sku.getImages(), ","));
            detail.setNum(numCart.get(sku.getId()));
            detail.setOrderId(orderId);
            detail.setTitle(sku.getTitle());
            detail.setSkuId(sku.getId());
            detail.setOwnSpec(sku.getOwnSpec());
            detail.setPrice(sku.getPrice());
            details.add(detail);
        }
        //订单金额
        order.setTotalPay(totalPrice);
        //实付金额 =订单金额+邮费-优惠
        order.setActualPay(totalPrice + order.getPostFee() - 0);
        //1.5 写入数据库
        int count_order = orderMapper.insertSelective(order);
        if (count_order != 1) {
            //新增失败
            log.error("[订单服务]：订单创建失败,orderId:{}", orderId);
            throw new LyException(ExceptionEnum.CREATE_ORDER_ERROR);
        }


        //新增订单详情
        int count_detail = orderDetailMapper.insertList(details);
        if (count_detail != details.size()) {
            //新增失败
            log.error("[订单服务]：订单详情生成失败,orderId:{}", orderId);
            throw new LyException(ExceptionEnum.CREATE_ORDER_DETAIL_ERROR);
        }


        //新增订单状态
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderId);
        orderStatus.setCreateTime(order.getCreateTime());
        orderStatus.setStatus(OrderStatusEnum.UNPAY.value());
        int count_status = orderStatusMapper.insertSelective(orderStatus);
        if (count_status != 1) {
            //新增失败
            log.error("[订单状态服务]：订单状态生成失败,orderId:{}", orderId);
            throw new LyException(ExceptionEnum.CREATE_ORDER_DETAIL_ERROR);
        }


        //减库存（通过feign同步调用）
        List<CartDto> carts = orderDto.getCarts();
        goodsClient.decreaseStock(carts);
        return orderId;
    }

    /**
     * 查询订单
     *
     * @param orderId
     * @return
     */
    @Override
    public Order findOrderById(Long orderId) {
        //查询订单
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            log.error("[订单不存在]");
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }
        //查询订单详情
        OrderDetail detail = new OrderDetail();
        detail.setOrderId(orderId);
        List<OrderDetail> details = orderDetailMapper.select(detail);
        if (CollectionUtils.isEmpty(details)) {
            log.error("[订单详情不存在]");
            throw new LyException(ExceptionEnum.ORDER_DETAIL_NOT_FOUND);
        }
        order.setOrderDetails(details);
        //查询订单状态
        OrderStatus status = orderStatusMapper.selectByPrimaryKey(orderId);
        if (status == null) {
            log.error("[订单状态不存在]");
            throw new LyException(ExceptionEnum.ORDER_STATUS_NOT_FOUND);
        }
        order.setOrderStatus(status);
        return order;
    }

    /**
     * 获取微信支付链接
     *
     * @param orderId
     * @return
     */
    @Override
    public String findWxPayUrl(Long orderId) {
        //查询订单
        Order order = this.findOrderById(orderId);
        //判断订单状态
        Integer status = order.getOrderStatus().getStatus();
        if (status != OrderStatusEnum.UNPAY.value()) {
            //订单状态异常
            log.error("[订单支付]：订单状态异常");
            throw new LyException(ExceptionEnum.ORDER_STATUS_ERROR);
        }
        //实付金额
        Long pay = order.getActualPay();
        //商品描述
        OrderDetail detail = order.getOrderDetails().get(0);
        String des = detail.getTitle();
        String url = payHelper.createPayUrl(orderId, pay, des);
        if (StringUtils.isBlank(url)) {
            throw new LyException(ExceptionEnum.WXPAY_ORDER_ERROR);
        }
        return url;
    }


    /**
     * 处理微信支付的回调函数
     * @param result
     */
    @Override
    public void handleNotify(Map<String, String> result) {
        //信息校验
        payHelper.isSuccess(result);
        //签名校验
        payHelper.isValidSign(result);
        //校验订单金额
        String totalFeeStr = result.get("total_fee");
        String orderIdStr = result.get("out_trade_no");
        if (StringUtils.isEmpty(totalFeeStr)||StringUtils.isEmpty(orderIdStr)) {
            throw new LyException(ExceptionEnum.INVALID_ORDER_PARAM);
        }
        Long totalFee = Long.valueOf(totalFeeStr);
        Long orderId = Long.valueOf(orderIdStr);
        Order order = orderMapper.selectByPrimaryKey(orderId);
        Long actualPay = order.getActualPay();
        if (actualPay != totalFee) {
            throw new LyException(ExceptionEnum.INVALID_ORDER_PARAM);
        }

        //修改订单状态
        OrderStatus status = new OrderStatus();
        status.setStatus(OrderStatusEnum.PAYED.value());
        status.setOrderId(orderId);
        status.setPaymentTime(new Date());
        int count = orderStatusMapper.updateByPrimaryKeySelective(status);
        if (count != 1) {
            throw new LyException(ExceptionEnum.UPDATE_ORDER_STATUS_ERROR);
        }

        log.info("[订单支付]：订单支付成功！订单编号：{}",orderId);
    }

    /**
     * 查询订单状态
     * @param orderId
     * @return
     */
    @Override
    public PayState findOrderState(Long orderId) {
        int status = orderStatusMapper.selectByPrimaryKey(orderId).getStatus();
        if (status != OrderStatusEnum.UNPAY.value()) {
            //状态码改变 支付成功
            return PayState.SUCCESS;
        }
        //如果状态码没改变 还有种情况就是支付成功了 但是微信通知15秒后才到达 状态码未来得及修改 此时需要主动去查询微信通知
        return payHelper.findPayState(orderId);
    }
}
