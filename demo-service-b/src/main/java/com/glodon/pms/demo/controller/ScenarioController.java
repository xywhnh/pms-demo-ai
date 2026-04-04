package com.glodon.pms.demo.controller;

import com.glodon.pms.demo.common.ApiResponse;
import com.glodon.pms.demo.service.ScenarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 场景模拟控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/scenario")
@RequiredArgsConstructor
public class ScenarioController {

    private final ScenarioService scenarioService;

    /**
     * 正常返回场景
     */
    @GetMapping("/normal")
    public ApiResponse<String> normal() {
        log.debug("收到正常场景请求");
        return scenarioService.normalScenario();
    }

    /**
     * 超时场景
     *
     * @param delay 延迟毫秒数，默认5000ms
     */
    @GetMapping("/timeout")
    public ApiResponse<String> timeout(@RequestParam(defaultValue = "5000") long delay) {
        log.debug("收到超时场景请求，延迟参数: {} 毫秒", delay);
        return scenarioService.timeoutScenario(delay);
    }

    /**
     * 慢查询场景
     *
     * @param iterations 迭代次数，默认1000000次
     */
    @GetMapping("/slow-query")
    public ApiResponse<String> slowQuery(@RequestParam(defaultValue = "1000000") int iterations) {
        log.debug("收到慢查询场景请求，迭代次数: {}", iterations);
        return scenarioService.slowQueryScenario(iterations);
    }

    /**
     * 空指针异常场景
     */
    @GetMapping("/npe")
    public ApiResponse<String> npe() {
        log.debug("收到空指针异常场景请求");
        return scenarioService.npeScenario();
    }
}
