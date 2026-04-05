package com.glodon.pms.demo.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.glodon.pms.demo.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 任务服务HTTP客户端（调用 Service B）
 */
@Slf4j
@Component
public class TaskServiceClient {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String TRACE_ID_KEY = "traceId";
    private static final MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String serviceBBaseUrl;

    public TaskServiceClient(@Value("${service-b.base-url}") String serviceBBaseUrl) {
        this.serviceBBaseUrl = serviceBBaseUrl;
        this.objectMapper = new ObjectMapper();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(3, TimeUnit.SECONDS)
                .writeTimeout(3, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 查询项目任务列表
     */
    public ApiResponse<?> getTasksByProject(Long projectId) throws IOException {
        String url = serviceBBaseUrl + "/api/tasks?projectId=" + projectId;
        log.info("查询项目任务列表: url={}", url);
        return executeGet(url);
    }

    /**
     * 查询项目任务统计
     */
    public ApiResponse<?> getTaskStatistics(Long projectId) throws IOException {
        String url = serviceBBaseUrl + "/api/tasks/stats?projectId=" + projectId;
        log.info("查询项目任务统计: url={}", url);
        return executeGet(url);
    }

    /**
     * 创建任务
     */
    public ApiResponse<?> createTask(Map<String, Object> request) throws IOException {
        String url = serviceBBaseUrl + "/api/tasks";
        log.info("创建任务: url={}", url);
        return executePost(url, request);
    }

    /**
     * 批量导入任务
     */
    public ApiResponse<?> batchImportTasks(Map<String, Object> request) throws IOException {
        String url = serviceBBaseUrl + "/api/tasks/batch";
        log.info("批量导入任务: url={}", url);
        return executePost(url, request);
    }

    /**
     * 标记任务完成
     */
    public ApiResponse<?> completeTask(Long taskId) throws IOException {
        String url = serviceBBaseUrl + "/api/tasks/" + taskId + "/complete";
        log.info("标记任务完成: url={}", url);
        return executePut(url);
    }

    @SuppressWarnings("unchecked")
    private ApiResponse<?> executeGet(String url) throws IOException {
        Request.Builder builder = new Request.Builder().url(url).get();
        addTraceHeader(builder);

        try (Response response = httpClient.newCall(builder.build()).execute()) {
            return parseResponse(response);
        }
    }

    @SuppressWarnings("unchecked")
    private ApiResponse<?> executePost(String url, Map<String, Object> body) throws IOException {
        String json = objectMapper.writeValueAsString(body);
        RequestBody requestBody = RequestBody.create(json, JSON_TYPE);

        Request.Builder builder = new Request.Builder().url(url).post(requestBody);
        addTraceHeader(builder);

        try (Response response = httpClient.newCall(builder.build()).execute()) {
            return parseResponse(response);
        }
    }

    @SuppressWarnings("unchecked")
    private ApiResponse<?> executePut(String url) throws IOException {
        RequestBody emptyBody = RequestBody.create("", JSON_TYPE);
        Request.Builder builder = new Request.Builder().url(url).put(emptyBody);
        addTraceHeader(builder);

        try (Response response = httpClient.newCall(builder.build()).execute()) {
            return parseResponse(response);
        }
    }

    private ApiResponse<?> parseResponse(Response response) throws IOException {
        ResponseBody body = response.body();
        if (body != null) {
            String responseBody = body.string();
            log.debug("Service B 响应: {}", responseBody);
            return objectMapper.readValue(responseBody, ApiResponse.class);
        }
        return ApiResponse.error(response.code(), "响应体为空");
    }

    private void addTraceHeader(Request.Builder builder) {
        String traceId = MDC.get(TRACE_ID_KEY);
        if (traceId != null && !traceId.isEmpty()) {
            builder.addHeader(TRACE_ID_HEADER, traceId);
        }
    }
}
