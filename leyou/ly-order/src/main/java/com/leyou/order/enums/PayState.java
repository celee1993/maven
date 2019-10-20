package com.leyou.order.enums;

/**
 * 订单状态返回到页面的枚举
 */
public enum PayState {
    NOT_PAY(0), SUCCESS(1), FAIL(2),
    ;
    private int value;

    PayState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
