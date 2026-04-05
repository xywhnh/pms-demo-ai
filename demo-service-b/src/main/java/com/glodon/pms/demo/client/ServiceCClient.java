package com.glodon.pms.demo.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.glodon.pms.demo.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Service C HTTP客户端
 */
@Slf4j
@Component
public class ServiceCClient {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String TRACE_ID_KEY = "traceId";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    /**
     * 通知发送的最大重试次数
     */
    private static final int NOTIFICATION_MAX_RETRIES = 2;

    private final OkHttpClient httpClient;
    private final OkHttpClient notificationClient;
    private final ObjectMapper objectMapper;
    private final String serviceCBaseUrl;

    public ServiceCClient(@Value("${service-c.base-url}") String serviceCBaseUrl) {
        this.serviceCBaseUrl = serviceCBaseUrl;
        this.objectMapper = new ObjectMapper();

        // 通用客户端：3秒超时
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(3, TimeUnit.SECONDS)
                .writeTimeout(3, TimeUnit.SECONDS)
                .build();

        // 通知客户端：2秒超时（用于非关键通知场景）
        this.notificationClient = new OkHttpClient.Builder()
                .connectTimeout(2, TimeUnit.SECONDS)
                .readTimeout(2, TimeUnit.SECONDS)
                .writeTimeout(2, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 检查用户是否存在
     */
    public ApiResponse<?> checkUserExists(Long userId) throws IOException {
        String url = serviceCBaseUrl + "/api/users/" + userId + "/exists";
        log.debug("检查用户是否存在: url={}", url);
        return executeGet(httpClient, url);
    }

    /**
     * 查询用户权限
     */
    public ApiResponse<?> getUserPermissions(Long userId) throws IOException {
        String url = serviceCBaseUrl + "/api/users/" + userId + "/permissions";
        log.debug("查询用户权限: url={}", url);
        return executeGet(httpClient, url);
    }

    /**
     * 发送通知（无重试）
     */
    public void sendNotification(Long userId, String type, String title,
                                 String content, String priority) throws IOException {
        String url = serviceCBaseUrl + "/api/notifications/send";
        Map<String, Object> body = Map.of(
                "userId", userId,
                "type", type,
                "title", title,
                "content", content,
                "priority", priority
        );
        log.info("发送通知: url={}, userId={}, type={}", url, userId, type);
        executePost(httpClient, url, body);
    }

    /**
     * 发送通知（带重试机制）
     * <p>
     * 对于非关键通知，使用较短超时并在超时时自动重试，
     * 确保通知尽量送达。
     */
    public void sendNotificationWithRetry(Long userId, String type, String title,
                                           String content, String priority) {
        String url = serviceCBaseUrl + "/api/notifications/send";
        Map<String, Object> body = Map.of(
                "userId", userId,
                "type", type,
                "title", title,
                "content", content,
                "priority", priority
        );

        for (int attempt = 0; attempt <= NOTIFICATION_MAX_RETRIES; attempt++) {
            try {
                log.info("发送通知: userId={}, type={}, attempt={}", userId, type, attempt + 1);
                executePost(notificationClient, url, body);
                log.info("通知发送成功: userId={}", userId);
                return;
            } catch (SocketTimeoutException e) {
                log.warn("通知发送超时, attempt={}/{}: {}", attempt + 1, NOTIFICATION_MAX_RETRIES + 1, e.getMessage());
                if (attempt == NOTIFICATION_MAX_RETRIES) {
                    log.error("通知发送最终失败（已重试{}次）: userId={}", NOTIFICATION_MAX_RETRIES, userId);
                }
            } catch (IOException e) {
                log.error("通知发送IO异常: userId={}", userId, e);
                return;
            }
        }
    }

    /**
     * 执行GET请求
     */
    @SuppressWarnings("unchecked")
    private ApiResponse<?> executeGet(OkHttpClient client, String url) throws IOException {
        Request.Builder requestBuilder = new Request.Builder().url(url).get();
        addTraceHeader(requestBuilder);

        try (Response response = client.newCall(requestBuilder.build()).execute()) {
            ResponseBody body = response.body();
            if (body != null) {
                String responseBody = body.string();
                log.debug("Service C 响应: {}", responseBody);
                return objectMapper.readValue(responseBody, ApiResponse.class);
            }
            return ApiResponse.error(response.code(), "响应体为空");
        }
    }

    /**
     * 执行POST请求
     */
    @SuppressWarnings("unchecked")
    private ApiResponse<?> executePost(OkHttpClient client, String url,
                                        Map<String, Object> requestBody) throws IOException {
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        RequestBody body = RequestBody.create(jsonBody, JSON);

        Request.Builder requestBuilder = new Request.Builder().url(url).post(body);
        addTraceHeader(requestBuilder);

        try (Response response = client.newCall(requestBuilder.build()).execute()) {
            ResponseBody respBody = response.body();
            if (respBody != null) {
                String responseStr = respBody.string();
                log.debug("Service C 响应: {}", responseStr);
                return objectMapper.readValue(responseStr, ApiResponse.class);
            }
            return ApiResponse.error(response.code(), "响应体为空");
        }
    }

    /**
     * 添加链路追踪请求头
     */
    private void addTraceHeader(Request.Builder builder) {
        String traceId = MDC.get(TRACE_ID_KEY);
        if (traceId != null && !traceId.isEmpty()) {
            builder.addHeader(TRACE_ID_HEADER, traceId);
        }
    }
}
