package com.glodon.pms.demo.service;

import com.glodon.pms.demo.client.ServiceCClient;
import com.glodon.pms.demo.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 任务服务 - 任务增删改查及业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final ServiceCClient serviceCClient;

    private final AtomicLong idGenerator = new AtomicLong(100);
    private final Map<Long, Map<String, Object>> taskStore = new ConcurrentHashMap<>();

    /**
     * 初始化模拟任务数据
     */
    {
        initSampleTasks();
    }

    /**
     * 查询项目下的任务列表
     */
    public List<Map<String, Object>> getTasksByProjectId(Long projectId) {
        log.info("查询项目任务列表: projectId={}", projectId);
        return taskStore.values().stream()
                .filter(t -> projectId.equals(((Number) t.get("projectId")).longValue()))
                .map(HashMap::new)
                .collect(Collectors.toList());
    }

    /**
     * 创建任务（包含权限校验和通知发送）
     */
    public Map<String, Object> createTask(Map<String, Object> request) {
        Long projectId = ((Number) request.get("projectId")).longValue();
        String title = (String) request.get("title");
        Long assigneeId = ((Number) request.get("assigneeId")).longValue();
        String priority = (String) request.getOrDefault("priority", "MEDIUM");

        log.info("创建任务: projectId={}, title={}, assigneeId={}, priority={}",
                projectId, title, assigneeId, priority);

        // 第一步：校验指派人权限
        try {
            ApiResponse<?> permResult = serviceCClient.getUserPermissions(assigneeId);
            if (permResult.getCode() != 200) {
                throw new IllegalArgumentException("指派人无效: " + permResult.getMessage());
            }
            log.info("权限校验通过: assigneeId={}", assigneeId);
        } catch (Exception e) {
            log.error("权限校验失败: assigneeId={}", assigneeId, e);
            throw new IllegalArgumentException("权限校验失败: " + e.getMessage());
        }

        // 第二步：创建任务记录
        Long taskId = idGenerator.incrementAndGet();
        Map<String, Object> task = new HashMap<>();
        task.put("id", taskId);
        task.put("projectId", projectId);
        task.put("title", title);
        task.put("assigneeId", assigneeId);
        task.put("priority", priority);
        task.put("status", "TODO");
        task.put("createdAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        taskStore.put(taskId, task);
        log.info("任务创建成功: taskId={}", taskId);

        // 第三步：发送指派通知（非关键路径，失败不影响任务创建）
        try {
            serviceCClient.sendNotification(
                    assigneeId, "TASK_ASSIGNED",
                    "你有新的任务: " + title,
                    "任务 [" + title + "] 已分配给你，优先级: " + priority,
                    priority
            );
            log.info("指派通知发送成功: assigneeId={}", assigneeId);
        } catch (Exception e) {
            // 通知发送失败不影响任务创建，仅记录警告
            log.warn("指派通知发送失败，不影响任务创建: {}", e.getMessage());
        }

        return new HashMap<>(task);
    }

    /**
     * 批量导入任务
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> batchImportTasks(Map<String, Object> request) {
        Long projectId = ((Number) request.get("projectId")).longValue();
        List<Map<String, Object>> tasks = (List<Map<String, Object>>) request.get("tasks");

        log.info("批量导入任务: projectId={}, count={}", projectId, tasks.size());

        int successCount = 0;
        int failCount = 0;

        for (Map<String, Object> taskData : tasks) {
            Long assigneeId = ((Number) taskData.get("assigneeId")).longValue();

            // 校验指派人是否存在
            try {
                ApiResponse<?> existsResult = serviceCClient.checkUserExists(assigneeId);
                if (existsResult.getCode() != 200) {
                    log.warn("批量导入: 用户不存在, assigneeId={}", assigneeId);
                    failCount++;
                    continue;
                }
            } catch (Exception e) {
                log.warn("批量导入: 用户校验失败, assigneeId={}", assigneeId);
                failCount++;
                continue;
            }

            // 创建任务记录
            Long taskId = idGenerator.incrementAndGet();
            Map<String, Object> task = new HashMap<>();
            task.put("id", taskId);
            task.put("projectId", projectId);
            task.put("title", taskData.get("title"));
            task.put("assigneeId", assigneeId);
            task.put("priority", taskData.getOrDefault("priority", "MEDIUM"));
            task.put("status", "TODO");
            task.put("createdAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            taskStore.put(taskId, task);

            successCount++;
        }

        log.info("批量导入完成: success={}, failed={}", successCount, failCount);

        Map<String, Object> result = new HashMap<>();
        result.put("total", tasks.size());
        result.put("success", successCount);
        result.put("failed", failCount);
        return result;
    }

    /**
     * 标记任务完成
     */
    public Map<String, Object> completeTask(Long taskId) {
        log.info("标记任务完成: taskId={}", taskId);

        Map<String, Object> task = taskStore.get(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在: " + taskId);
        }

        // 更新任务状态
        task.put("status", "DONE");
        task.put("completedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        log.info("任务状态更新为DONE: taskId={}", taskId);

        // 发送完成通知给指派人
        Long assigneeId = ((Number) task.get("assigneeId")).longValue();
        String title = (String) task.get("title");

        serviceCClient.sendNotificationWithRetry(
                assigneeId, "TASK_COMPLETED",
                "任务已完成: " + title,
                "任务 [" + title + "] 已标记为完成",
                "MEDIUM"
        );

        return new HashMap<>(task);
    }

    /**
     * 初始化模拟数据
     */
    private void initSampleTasks() {
        long[][] sampleData = {
                {1, 1, 3},  // projectId, taskId, assigneeId
                {1, 2, 5},
                {1, 3, 7},
                {1, 4, 1},
                {1, 5, 2},
                {1, 6, 8},
                {1, 7, 10},
                {1, 8, 12},
                {1, 9, 14},
                {1, 10, 4},
                {1, 11, 6},
                {1, 12, 9},
                {1, 13, 11},
                {1, 14, 13},
                {1, 15, 15},
        };

        String[] titles = {
                "用户登录接口优化", "首页加载性能调优", "权限管理重构",
                "数据导出功能", "日志系统升级", "缓存策略调整",
                "API文档更新", "单元测试补充", "数据库索引优化",
                "前端组件重构", "消息队列集成", "监控告警配置",
                "CI/CD流水线优化", "代码审查流程", "安全漏洞修复"
        };

        String[] statuses = {"TODO", "TODO", "IN_PROGRESS", "DONE", "TODO",
                "IN_PROGRESS", "TODO", "DONE", "IN_PROGRESS", "TODO",
                "TODO", "DONE", "IN_PROGRESS", "TODO", "TODO"};

        for (int i = 0; i < sampleData.length; i++) {
            long taskId = sampleData[i][1];
            Map<String, Object> task = new HashMap<>();
            task.put("id", taskId);
            task.put("projectId", sampleData[i][0]);
            task.put("title", titles[i]);
            task.put("assigneeId", sampleData[i][2]);
            task.put("priority", i % 3 == 0 ? "HIGH" : (i % 3 == 1 ? "MEDIUM" : "LOW"));
            task.put("status", statuses[i]);
            task.put("createdAt", "2026-03-15T09:00:00");
            if ("DONE".equals(statuses[i])) {
                task.put("completedAt", "2026-03-28T17:30:00");
            }
            taskStore.put(taskId, task);
        }
    }
}
