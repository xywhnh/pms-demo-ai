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
import java.util.concurrent.TimeUnit;

/**
 * Service B HTTP客户端
 */
@Slf4j
@Component
public class ServiceBClient {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String TRACE_ID_KEY = "traceId";

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String serviceBBaseUrl;

    public ServiceBClient(@Value("${service-b.base-url}") String serviceBBaseUrl) {
        this.serviceBBaseUrl = serviceBBaseUrl;
        this.objectMapper = new ObjectMapper();
        // 配置超时时间：连接3秒，读取3秒，写入3秒
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(3, TimeUnit.SECONDS)
                .writeTimeout(3, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 调用正常场景接口
     */
    public ApiResponse<String> callNormal() throws IOException {
        String url = serviceBBaseUrl + "/api/scenario/normal";
        log.info("调用 Service B 正常场景接口: {}", url);
        return executeRequest(url);
    }

    /**
     * 调用超时场景接口
     *
     * @param delay 延迟毫秒数
     */
    public ApiResponse<String> callTimeout(long delay) throws IOException {
        String url = serviceBBaseUrl + "/api/scenario/timeout?delay=" + delay;
        log.info("调用 Service B 超时场景接口: {}", url);
        return executeRequest(url);
    }

    /**
     * 调用慢查询场景接口
     *
     * @param iterations 迭代次数
     */
    public ApiResponse<String> callSlowQuery(int iterations) throws IOException {
        String url = serviceBBaseUrl + "/api/scenario/slow-query?iterations=" + iterations;
        log.info("调用 Service B 慢查询场景接口: {}", url);
        return executeRequest(url);
    }

    /**
     * 调用空指针异常场景接口
     */
    public ApiResponse<String> callNpe() throws IOException {
        String url = serviceBBaseUrl + "/api/scenario/npe";
        log.info("调用 Service B 空指针异常场景接口: {}", url);
        return executeRequest(url);
    }

    /**
     * 执行HTTP请求
     */
    @SuppressWarnings("unchecked")
    private ApiResponse<String> executeRequest(String url) throws IOException {
        // 获取当前的traceId
        String traceId = MDC.get(TRACE_ID_KEY);

        Request.Builder requestBuilder = new Request.Builder().url(url).get();

        // 在请求头中传递traceId
        if (traceId != null && !traceId.isEmpty()) {
            requestBuilder.addHeader(TRACE_ID_HEADER, traceId);
        }

        Request request = requestBuilder.build();

        try (Response response = httpClient.newCall(request).execute()) {
            ResponseBody body = response.body();
            if (body != null) {
                String responseBody = body.string();
                log.debug("Service B 响应: {}", responseBody);
                return objectMapper.readValue(responseBody, ApiResponse.class);
            }
            return ApiResponse.error(response.code(), "响应体为空");
        }
    }
}
