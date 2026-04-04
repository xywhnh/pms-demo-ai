package com.glodon.pms.demo.service;

import com.glodon.pms.demo.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 场景模拟服务
 */
@Slf4j
@Service
public class ScenarioService {

    /**
     * 正常场景 - 直接返回成功响应
     */
    public ApiResponse<String> normalScenario() {
        log.info("执行正常场景");
        return ApiResponse.success("正常场景执行成功", "Hello from Service B");
    }

    /**
     * 超时场景 - 延迟指定时间后返回
     *
     * @param delayMs 延迟毫秒数
     */
    public ApiResponse<String> timeoutScenario(long delayMs) {
        log.info("执行超时场景，延迟 {} 毫秒", delayMs);
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("超时场景被中断", e);
            return ApiResponse.error("超时场景执行被中断");
        }
        log.info("超时场景执行完成");
        return ApiResponse.success("超时场景执行成功", "延迟 " + delayMs + " 毫秒后返回");
    }

    /**
     * 慢查询场景 - 执行复杂计算模拟慢查询
     *
     * @param iterations 迭代次数
     */
    public ApiResponse<String> slowQueryScenario(int iterations) {
        log.info("执行慢查询场景，迭代 {} 次", iterations);
        long startTime = System.currentTimeMillis();

        // 执行复杂计算模拟慢查询
        double result = 0;
        for (int i = 0; i < iterations; i++) {
            result += Math.sqrt(i) * Math.sin(i) * Math.cos(i);
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("慢查询场景执行完成，耗时 {} 毫秒，计算结果 {}", duration, result);
        return ApiResponse.success("慢查询场景执行成功",
                String.format("迭代 %d 次，耗时 %d 毫秒", iterations, duration));
    }

    /**
     * 空指针异常场景 - 主动抛出NullPointerException
     */
    public ApiResponse<String> npeScenario() {
        log.info("执行空指针异常场景");
        String nullString = null;
        // 主动触发NPE
        return ApiResponse.success(nullString.length() + "");
    }
}
