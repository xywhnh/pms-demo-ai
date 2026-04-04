package com.glodon.pms.demo.service;

import com.glodon.pms.demo.client.ServiceBClient;
import com.glodon.pms.demo.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.SocketTimeoutException;

/**
 * Demo服务 - 根据场景参数执行不同逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DemoService {

    private final ServiceBClient serviceBClient;

    /**
     * 场景类型枚举
     */
    public enum ScenarioType {
        LOCAL_SUCCESS,    // 本服务正常访问
        LOCAL_ERROR,      // 本服务内部出错
        REMOTE_SUCCESS,   // 调用外部接口成功
        REMOTE_TIMEOUT,   // 调用外部接口超时
        REMOTE_NPE        // 调用外部接口NPE
    }

    /**
     * 根据场景参数执行不同逻辑
     *
     * @param scenario 场景类型
     * @return 响应结果
     */
    public ApiResponse<String> executeScenario(String scenario) {
        log.info("执行场景: {}", scenario);

        ScenarioType scenarioType;
        try {
            scenarioType = ScenarioType.valueOf(scenario.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("未知场景类型: {}", scenario);
            return ApiResponse.error(400, "未知场景类型: " + scenario +
                    "。支持的场景: LOCAL_SUCCESS, LOCAL_ERROR, REMOTE_SUCCESS, REMOTE_TIMEOUT, REMOTE_NPE");
        }

        return switch (scenarioType) {
            case LOCAL_SUCCESS -> handleLocalSuccess();
            case LOCAL_ERROR -> handleLocalError();
            case REMOTE_SUCCESS -> handleRemoteSuccess();
            case REMOTE_TIMEOUT -> handleRemoteTimeout();
            case REMOTE_NPE -> handleRemoteNpe();
        };
    }

    /**
     * 本服务正常访问
     */
    private ApiResponse<String> handleLocalSuccess() {
        log.info("执行本服务正常访问场景");
        // 执行一些本地业务逻辑
        String result = processLocalBusinessLogic();
        return ApiResponse.success("本服务正常处理完成", result);
    }

    /**
     * 本服务内部出错
     */
    private ApiResponse<String> handleLocalError() {
        log.info("执行本服务内部出错场景");
        // 模拟业务异常
        throw new RuntimeException("本服务内部业务处理异常");
    }

    /**
     * 调用外部接口成功
     */
    private ApiResponse<String> handleRemoteSuccess() {
        log.info("执行调用外部接口成功场景");
        try {
            // 调用 Service B 的正常接口
            ApiResponse<String> response = serviceBClient.callNormal();
            log.info("Service B 返回: {}", response);
            return ApiResponse.success("远程调用成功", "Service A 处理完成，Service B 返回: " + response.getData());
        } catch (IOException e) {
            log.error("调用 Service B 失败", e);
            return ApiResponse.error("调用外部服务失败: " + e.getMessage());
        }
    }

    /**
     * 调用外部接口超时
     */
    private ApiResponse<String> handleRemoteTimeout() {
        log.info("执行调用外部接口超时场景");
        try {
            // 调用 Service B 的超时接口（延迟5秒，但客户端超时3秒）
            ApiResponse<String> response = serviceBClient.callTimeout(5000);
            log.info("Service B 返回: {}", response);
            return ApiResponse.success("远程调用完成", "Service B 返回: " + response.getData());
        } catch (SocketTimeoutException e) {
            log.error("调用 Service B 超时", e);
            return ApiResponse.error(504, "调用外部服务超时: " + e.getMessage());
        } catch (IOException e) {
            log.error("调用 Service B 失败", e);
            return ApiResponse.error("调用外部服务失败: " + e.getMessage());
        }
    }

    /**
     * 调用外部接口NPE
     */
    private ApiResponse<String> handleRemoteNpe() {
        log.info("执行调用外部接口NPE场景");
        try {
            // 调用 Service B 的NPE接口
            ApiResponse<String> response = serviceBClient.callNpe();
            log.info("Service B 返回: {}", response);

            // Service B 会返回500错误
            if (response.getCode() != 200) {
                return ApiResponse.error(response.getCode(),
                        "Service B 返回错误: " + response.getMessage());
            }
            return response;
        } catch (IOException e) {
            log.error("调用 Service B 失败", e);
            return ApiResponse.error("调用外部服务失败: " + e.getMessage());
        }
    }

    /**
     * 模拟本地业务逻辑处理
     */
    private String processLocalBusinessLogic() {
        log.debug("执行本地业务逻辑处理");
        // 模拟一些计算
        int sum = 0;
        for (int i = 0; i < 1000; i++) {
            sum += i;
        }
        return "本地处理结果: " + sum;
    }
}
