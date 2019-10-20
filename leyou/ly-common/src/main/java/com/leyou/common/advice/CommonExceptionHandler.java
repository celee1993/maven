package com.leyou.common.advice;

import com.leyou.common.exception.LyException;
import com.leyou.common.vo.ExceptionResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * 异常通知处理器
 */
@ControllerAdvice
public class CommonExceptionHandler {


    @ExceptionHandler(LyException.class)
    public ResponseEntity<ExceptionResult> handleException(LyException le) {
        ExceptionResult result=new ExceptionResult(le.getExceptionEnum());
        return ResponseEntity.status(result.getStatus()).body(result);
    }
}
