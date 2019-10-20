package com.leyou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 自定义枚举
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ExceptionEnum {
    ORDER_STATUS_ERROR("订单状态异常",400),
    STOCK_NOT_ENOUGH("库存量不足", 500),
    DATA_ERROR("参数有误", 400),
    DELETE_CART_ERROR("商品删除失败", 400),
    INVALID_TOKEN_CODE("身份认证有误", 400),
    INVALID_VERIFY_CODE("验证码有误", 400),
    GOODS_NOT_FOUND("商品未查询到", 404),
    ORDER_NOT_FOUND("订单未查询到", 404),
    ORDER_STATUS_NOT_FOUND("订单状态未查询到", 404),
    ORDER_DETAIL_NOT_FOUND("订单详情未查询到", 404),
    CATEGORY_NOT_FOUND("商品分类未查询到", 404),
    SKU_NOT_FOUND("SKU属性未查询到", 404),
    BRAND_NOT_FOUND("品牌分类未查询到", 404),
    CART_NOT_FOUND("购物车为空", 404),
    SPU_NOT_FOUND("SPU属性未查询到", 404),
    SPEC_GROUP_NOT_FOUND("规格组未查询到", 404),
    GROUP_PARAM_NOT_FOUND("组属性未查询到", 404),
    BRAND_SAVE_ERROR("品牌新增失败", 500),
    SPU_SAVE_ERROR("SPU新增失败", 500),
    SPU_UPDATE_ERROR("SPU更新失败", 500),
    SPU_DETAIL_SAVE_ERROR("商品详情新增失败", 500),
    SPU_DETAIL_UPDATE_ERROR("商品详情更新失败", 500),
    STOCK_SAVE_ERROR("库存新增失败", 500),
    STOCK_NOT_FOUND("库存查询失败", 500),
    UPLOAD_FILE_ERROR("文件上传失败", 500),
    INVALID_FILE_TYPE("文件类型不匹配", 400),
    INVALID_USERNAME_OR_PASSWORD("用户名或密码错误", 400),
    INVALID_SIGN_ERROR("无效的签名", 400),
    INVALID_ORDER_PARAM("订单参数有误", 400),
    GOODS_ID_CAN_NOT_BE_NULL("商品id不能为空", 400),
    CREATE_TOKEN_ERROR("用户凭证生成失败", 500),
    CREATE_ORDER_ERROR("订单生成失败", 500),
    CREATE_ORDER_DETAIL_ERROR("订单生成失败", 500),
    UPDATE_ORDER_STATUS_ERROR("订单状态修改失败", 500),
    WXPAY_ORDER_ERROR("微信下单失败",500)
    ;

    private String message;
    private int code;
}
