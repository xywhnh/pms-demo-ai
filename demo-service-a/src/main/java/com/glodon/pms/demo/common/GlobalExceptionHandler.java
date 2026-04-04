package com.glodon.pms.demo.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.net.SocketTimeoutException;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理Socket超时异常
     */
    @ExceptionHandler(SocketTimeoutException.class)
    public ApiResponse<Void> handleSocketTimeoutException(SocketTimeoutException e) {
        log.error("发生Socket超时异常: ", e);
        return ApiResponse.error(504, "调用外部服务超时: " + e.getMessage());
    }

    /**
     * 处理IO异常（包括HTTP调用失败）
     */
    @ExceptionHandler(IOException.class)
    public ApiResponse<Void> handleIOException(IOException e) {
        log.error("发生IO异常: ", e);
        return ApiResponse.error(502, "调用外部服务失败: " + e.getMessage());
    }

    /**
     * 处理运行时异常（业务异常）
     */
    @ExceptionHandler(RuntimeException.class)
    public ApiResponse<Void> handleRuntimeException(RuntimeException e) {
        log.error("发生运行时异常: ", e);
        return ApiResponse.error(500, "业务处理异常: " + e.getMessage());
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
