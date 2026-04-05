package com.glodon.pms.demo.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.glodon.pms.demo.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 平台服务HTTP客户端（调用 Service C）
 */
@Slf4j
@Component
public class PlatformServiceClient {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String TRACE_ID_KEY = "traceId";

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String serviceCBaseUrl;

    public PlatformServiceClient(@Value("${service-c.base-url}") String serviceCBaseUrl) {
        this.serviceCBaseUrl = serviceCBaseUrl;
        this.objectMapper = new ObjectMapper();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(3, TimeUnit.SECONDS)
                .writeTimeout(3, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 查询单个用户信息
     */
    public Map<String, Object> getUser(Long userId) throws IOException {
        String url = serviceCBaseUrl + "/api/users/" + userId;
        log.debug("查询用户信息: url={}", url);

        ApiResponse<?> response = executeGet(url);
        if (response.getCode() == 200 && response.getData() != null) {
            return castToMap(response.getData());
        }
        return null;
    }

    /**
     * 批量查询用户信息
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getUserBatch(List<Long> userIds) throws IOException {
        String ids = userIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        String url = serviceCBaseUrl + "/api/users/batch?ids=" + ids;
        log.debug("批量查询用户信息: url={}", url);

        ApiResponse<?> response = executeGet(url);
        if (response.getCode() == 200 && response.getData() != null) {
            return (List<Map<String, Object>>) response.getData();
        }
        return List.of();
    }

    @SuppressWarnings("unchecked")
    private ApiResponse<?> executeGet(String url) throws IOException {
        Request.Builder builder = new Request.Builder().url(url).get();
        addTraceHeader(builder);

        try (Response response = httpClient.newCall(builder.build()).execute()) {
            ResponseBody body = response.body();
            if (body != null) {
                String responseBody = body.string();
                log.debug("Service C 响应: {}", responseBody);
                return objectMapper.readValue(responseBody, ApiResponse.class);
            }
            return ApiResponse.error(response.code(), "响应体为空");
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castToMap(Object data) {
        if (data instanceof Map) {
            return (Map<String, Object>) data;
        }
        return null;
    }

    private void addTraceHeader(Request.Builder builder) {
        String traceId = MDC.get(TRACE_ID_KEY);
        if (traceId != null && !traceId.isEmpty()) {
            builder.addHeader(TRACE_ID_HEADER, traceId);
        }
    }
}
