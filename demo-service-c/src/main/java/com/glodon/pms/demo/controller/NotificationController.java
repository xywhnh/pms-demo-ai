package com.glodon.pms.demo.controller;

import com.glodon.pms.demo.common.ApiResponse;
import com.glodon.pms.demo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 通知控制器 - 处理通知发送请求
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 发送通知
     */
    @PostMapping("/send")
    public ApiResponse<Map<String, Object>> sendNotification(@RequestBody Map<String, Object> request) {
        Long userId = ((Number) request.get("userId")).longValue();
        String type = (String) request.get("type");
        String title = (String) request.get("title");
        String content = (String) request.get("content");
        String priority = (String) request.getOrDefault("priority", "MEDIUM");

        log.info("接收通知发送请求: userId={}, type={}, priority={}", userId, type, priority);

        Map<String, Object> result = notificationService.sendNotification(userId, type, title, content, priority);
        return ApiResponse.success("通知发送成功", result);
    }
}
