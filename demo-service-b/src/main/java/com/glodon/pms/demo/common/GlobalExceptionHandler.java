package com.glodon.pms.demo.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    public ApiResponse<Void> handleNullPointerException(NullPointerException e) {
        log.error("发生空指针异常: ", e);
        return ApiResponse.error(500, "空指针异常: " + e.getMessage());
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("发生非法参数异常: ", e);
        return ApiResponse.error(400, "参数错误: " + e.getMessage());
    }

    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e) {
        log.error("发生未知异常: ", e);
        return ApiResponse.error(500, "服务器内部错误: " + e.getMessage());
    }
}
