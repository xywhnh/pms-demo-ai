package com.glodon.pms.demo.controller;

import com.glodon.pms.demo.common.ApiResponse;
import com.glodon.pms.demo.service.DemoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Demo控制器 - 统一入口
 */
@Slf4j
@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
public class DemoController {

    private final DemoService demoService;

    /**
     * 统一执行入口
     * <p>
     * 支持的场景参数:
     * - LOCAL_SUCCESS: 本服务正常访问
     * - LOCAL_ERROR: 本服务内部出错
     * - REMOTE_SUCCESS: 调用外部接口成功
     * - REMOTE_TIMEOUT: 调用外部接口超时
     * - REMOTE_NPE: 调用外部接口NPE
     *
     * @param scenario 场景类型
     * @return 响应结果
     */
    @GetMapping("/execute")
    public ApiResponse<String> execute(@RequestParam(defaultValue = "LOCAL_SUCCESS") String scenario) {
        log.debug("收到执行请求，场景参数: {}", scenario);
        return demoService.executeScenario(scenario);
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("Service A is healthy");
    }
}
