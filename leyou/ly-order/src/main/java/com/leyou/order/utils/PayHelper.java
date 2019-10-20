package com.leyou.order.utils;

import com.github.wxpay.sdk.WXPay;

import static com.github.wxpay.sdk.WXPayConstants.*;

import com.github.wxpay.sdk.WXPayUtil;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.order.config.PayProperties;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.enums.PayState;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class PayHelper {

    @Autowired
    private WXPay wxPay;

    @Autowired
    private PayProperties config;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderStatusMapper orderStatusMapper;
    public String createPayUrl(Long orderId, Long totalPay, String description) {

        try {
            HashMap<String, String> data = new HashMap<>();
            //描述
            data.put("body", description);
            //订单号
            data.put("out_trade_no", orderId.toString());
            //货币（默认就是人民币）
            //data.put("fee_type", "CNY");
            //总金额
            data.put("total_fee", totalPay.toString());
            //调用微信支付的终端ip
            data.put("spbill_create_ip", "127.0.0.1");
            //回调地址
            data.put("notify_url", config.getNotifyUrl());
            //交易类型为扫码支付
            data.put("trade_type", "NATIVE");

            Map<String, String> result = wxPay.unifiedOrder(data);

            //信息校验
            isSuccess(result);
            //下单成功 获取支付链接
            return result.get("code_url");
        } catch (Exception e) {
            log.error("【微信下单】创建预交易订单异常", e);
            return null;
        }
    }

    /**
     * 对信息进行校验
     *
     * @param result
     */
    public void isSuccess(Map<String, String> result) {
        //判断通信标识
        String returnCode = result.get("return_code");
        if (FAIL.equals(returnCode)) {
            log.error("[微信下单]：微信下单通信失败，原因：{}", result.get("return_msg"));
            throw new LyException(ExceptionEnum.WXPAY_ORDER_ERROR);
        }
        //判断业务标识
        String resultCode = result.get("result_code");
        if (FAIL.equals(resultCode)) {
            log.error("[微信下单]：微信下单业务失败，错误码：{}，错误原因：{}", result.get("error_code"), result.get("error_code_des"));
            throw new LyException(ExceptionEnum.WXPAY_ORDER_ERROR);
        }
    }

    /**
     * 签名校验
     *
     * @param data
     */
    public void isValidSign(Map<String, String> data) {
        try {
            //重新生成签名
            String sign1 = WXPayUtil.generateSignature(data, config.getKey(), SignType.HMACSHA256);
            String sign2 = WXPayUtil.generateSignature(data, config.getKey(), SignType.MD5);
            //与传过来的签名对比
            String sign = data.get("sign");
            if (!StringUtils.equals(sign1, sign) && !StringUtils.equals(sign2, sign)) {
                //两种算法的签名都不对
                log.error("[微信支付]：无效的签名");
                throw new LyException(ExceptionEnum.INVALID_SIGN_ERROR);
            }
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.INVALID_SIGN_ERROR);
        }

    }

    /**
     * 主动查询微信支付状态
     * @param orderId
     */
    public PayState findPayState(Long orderId) {
        try {
            Map<String, String> data = new HashMap<>();
            data.put("out_trade_no", orderId.toString());
            //其余参数调用方法时候自动封装
            Map<String, String> result = wxPay.orderQuery(data);
            //校验通信状态
            this.isSuccess(result);
            //校验签名
            this.isValidSign(result);
            //校验订单金额
            String totalFeeStr = result.get("total_fee");
            String orderIdStr = result.get("out_trade_no");
            if (StringUtils.isEmpty(totalFeeStr)||StringUtils.isEmpty(orderIdStr)) {
                throw new LyException(ExceptionEnum.INVALID_ORDER_PARAM);
            }
            Long totalFee = Long.valueOf(totalFeeStr);
            Order order = orderMapper.selectByPrimaryKey(orderId);
            Long actualPay = order.getActualPay();
            if (actualPay != totalFee) {
                throw new LyException(ExceptionEnum.INVALID_ORDER_PARAM);
            }

            //判断交易状态
            String state = result.get("trade_state");
            if (SUCCESS.equals(state)) {
                //支付成功
                //修改订单状态
                OrderStatus status = new OrderStatus();
                status.setStatus(OrderStatusEnum.PAYED.value());
                status.setOrderId(orderId);
                status.setPaymentTime(new Date());
                int count = orderStatusMapper.updateByPrimaryKeySelective(status);
                if (count != 1) {
                    throw new LyException(ExceptionEnum.UPDATE_ORDER_STATUS_ERROR);
                }
                //返回支付成功
                return PayState.SUCCESS;
            }

            if ("USERPAYING".equals(state) || "NOTPAY".equals(state)) {
                //未支付或者正在支付中
                return PayState.NOT_PAY;
            }
            //支付失败
            return PayState.FAIL;
        } catch (Exception e) {
            return PayState.NOT_PAY;
        }
    }
}
