package com.glodon.pms.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/**
 * 通知服务 - 处理各类通知发送
 */
@Slf4j
@Service
public class NotificationService {

    /**
     * 通知模板中的优先级标签映射
     * 注意: HIGH 优先级故意未配置，模拟线上模板配置遗漏
     */
    private static final Map<String, String> PRIORITY_LABEL_MAP = Map.of(
            "LOW", "普通",
            "MEDIUM", "重要",
            "CRITICAL", "紧急"
    );

    /**
     * 发送通知
     *
     * @param userId   接收人ID
     * @param type     通知类型 (TASK_ASSIGNED, TASK_COMPLETED, etc.)
     * @param title    通知标题
     * @param content  通知内容
     * @param priority 优先级 (LOW, MEDIUM, HIGH, CRITICAL)
     * @return 通知发送结果
     */
    public Map<String, Object> sendNotification(Long userId, String type, String title,
                                                 String content, String priority) {
        log.info("发送通知: userId={}, type={}, priority={}, title={}", userId, type, priority, title);

        // 模拟通知发送延迟
        simulateSendDelay(type, priority);

        // 渲染通知模板 — 此处存在隐藏bug
        String renderedContent = renderTemplate(title, content, priority);

        String notificationId = UUID.randomUUID().toString().substring(0, 8);
        log.info("通知发送成功: notificationId={}, userId={}", notificationId, userId);

        return Map.of(
                "notificationId", notificationId,
                "status", "sent",
                "userId", userId
        );
    }

    /**
     * 渲染通知模板
     * <p>
     * 根据优先级添加标签前缀，用于邮件和消息推送的格式化显示
     */
    private String renderTemplate(String title, String content, String priority) {
        // 从映射表获取优先级标签 — HIGH 未配置，返回 null
        String priorityLabel = PRIORITY_LABEL_MAP.get(priority);

        // 此处当 priority=HIGH 时 priorityLabel 为 null，触发 NPE
        String formattedTitle = "[" + priorityLabel.toUpperCase() + "] " + title;

        log.debug("渲染通知模板: {}", formattedTitle);
        return formattedTitle + "\n\n" + content;
    }

    /**
     * 模拟通知发送延迟，不同类型和渠道耗时不同
     */
    private void simulateSendDelay(String type, String priority) {
        try {
            long delay;
            if ("TASK_COMPLETED".equals(type)) {
                // 任务完成通知推送到多个渠道（邮件+IM+webhook），耗时较长
                delay = 2500;
            } else {
                delay = "CRITICAL".equals(priority) ? 30 : 50;
            }
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
