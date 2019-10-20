package com.leyou.common.vo;

import com.leyou.common.enums.ExceptionEnum;
import lombok.Data;

import java.util.Date;

/**
 * 自定义异常结果封装
 */
@Data
public class ExceptionResult {

    private int status;
    private String message;
    private Date tiemstamp;

    public ExceptionResult(ExceptionEnum e) {
        this.message = e.getMessage();
        this.status = e.getCode();
        this.tiemstamp = new Date();
    }

}
