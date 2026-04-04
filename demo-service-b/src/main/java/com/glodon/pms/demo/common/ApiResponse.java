package com.glodon.pms.demo.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.MDC;

/**
 * 统一API响应格式
 *
 * @param <T> 响应数据类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private static final String TRACE_ID_KEY = "traceId";

    /**
     * 状态码: 200=成功, 500=错误
     */
    private int code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 链路追踪ID
     */
    private String traceId;

    /**
     * 创建成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message("success")
                .data(data)
                .traceId(MDC.get(TRACE_ID_KEY))
                .build();
    }

    /**
     * 创建成功响应（带自定义消息）
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message(message)
                .data(data)
                .traceId(MDC.get(TRACE_ID_KEY))
                .build();
    }

    /**
     * 创建错误响应
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .data(null)
                .traceId(MDC.get(TRACE_ID_KEY))
                .build();
    }

    /**
     * 创建服务器内部错误响应
     */
    public static <T> ApiResponse<T> error(String message) {
        return error(500, message);
    }
}
