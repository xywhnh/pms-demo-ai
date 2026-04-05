package com.glodon.pms.demo.service;

import com.glodon.pms.demo.client.PlatformServiceClient;
import com.glodon.pms.demo.client.TaskServiceClient;
import com.glodon.pms.demo.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.*;

/**
 * 项目服务 - 编排下游服务调用，聚合项目数据
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final TaskServiceClient taskServiceClient;
    private final PlatformServiceClient platformServiceClient;

    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    /**
     * 项目信息（模拟数据）
     */
    private static final Map<Long, Map<String, Object>> PROJECT_STORE = new HashMap<>();

    static {
        PROJECT_STORE.put(1L, Map.of(
                "id", 1L,
                "name", "Q2 产品发布",
                "description", "2026年Q2核心产品迭代",
                "memberIds", List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L)
        ));
    }

    /**
     * 获取项目基本信息
     */
    public ApiResponse<Map<String, Object>> getProject(Long projectId) {
        Map<String, Object> project = PROJECT_STORE.get(projectId);
        if (project == null) {
            return ApiResponse.error(404, "项目不存在: " + projectId);
        }
        return ApiResponse.success(new HashMap<>(project));
    }

    /**
     * 获取项目仪表盘
     * <p>
     * 聚合任务列表并补充每个任务的指派人详细信息
     */
    public ApiResponse<Map<String, Object>> getDashboard(Long projectId) {
        Map<String, Object> project = PROJECT_STORE.get(projectId);
        if (project == null) {
            return ApiResponse.error(404, "项目不存在: " + projectId);
        }

        try {
            // 获取项目任务列表
            ApiResponse<?> taskResponse = taskServiceClient.getTasksByProject(projectId);
            if (taskResponse.getCode() != 200) {
                return ApiResponse.error(taskResponse.getCode(), "获取任务列表失败: " + taskResponse.getMessage());
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> tasks = (List<Map<String, Object>>) taskResponse.getData();

            // 补充每个任务的指派人详细信息
            enrichTasksWithAssigneeInfo(tasks);

            Map<String, Object> dashboard = new HashMap<>();
            dashboard.put("project", project.get("name"));
            dashboard.put("taskCount", tasks.size());
            dashboard.put("tasks", tasks);

            return ApiResponse.success(dashboard);
        } catch (SocketTimeoutException e) {
            log.error("获取仪表盘超时: projectId={}", projectId, e);
            return ApiResponse.error(504, "获取仪表盘超时: " + e.getMessage());
        } catch (IOException e) {
            log.error("获取仪表盘失败: projectId={}", projectId, e);
            return ApiResponse.error(500, "获取仪表盘失败: " + e.getMessage());
        }
    }

    /**
     * 获取项目概览（包含统计和团队信息）
     * <p>
     * 并行获取统计数据和团队成员信息以提升响应速度
     */
    @SuppressWarnings("unchecked")
    public ApiResponse<Map<String, Object>> getOverview(Long projectId, boolean includeStats) {
        Map<String, Object> project = PROJECT_STORE.get(projectId);
        if (project == null) {
            return ApiResponse.error(404, "项目不存在: " + projectId);
        }

        try {
            List<Long> memberIds = (List<Long>) project.get("memberIds");

            // 并行获取团队信息和任务统计
            CompletableFuture<List<Map<String, Object>>> teamFuture =
                    CompletableFuture.supplyAsync(() -> {
                        try {
                            return platformServiceClient.getUserBatch(memberIds);
                        } catch (IOException e) {
                            throw new CompletionException(e);
                        }
                    }, executor);

            CompletableFuture<ApiResponse<?>> statsFuture = null;
            if (includeStats) {
                statsFuture = CompletableFuture.supplyAsync(() -> {
                    try {
                        return taskServiceClient.getTaskStatistics(projectId);
                    } catch (IOException e) {
                        throw new CompletionException(e);
                    }
                }, executor);
            }

            // 等待所有异步任务完成
            if (statsFuture != null) {
                CompletableFuture.allOf(teamFuture, statsFuture).get(3, TimeUnit.SECONDS);
            } else {
                teamFuture.get(3, TimeUnit.SECONDS);
            }

            Map<String, Object> overview = new HashMap<>();
            overview.put("project", project.get("name"));
            overview.put("team", teamFuture.get());

            if (statsFuture != null) {
                ApiResponse<?> statsResponse = statsFuture.get();
                overview.put("statistics", statsResponse.getData());
            }

            return ApiResponse.success(overview);
        } catch (TimeoutException e) {
            log.error("获取项目概览超时: projectId={}", projectId, e);
            return ApiResponse.error(504, "获取项目概览超时");
        } catch (ExecutionException e) {
            log.error("获取项目概览失败: projectId={}", projectId, e);
            return ApiResponse.error(500, "获取项目概览失败: " + e.getCause().getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ApiResponse.error(500, "获取项目概览被中断");
        }
    }

    /**
     * 创建项目任务
     */
    public ApiResponse<Map<String, Object>> createTask(Map<String, Object> request) {
        try {
            ApiResponse<?> response = taskServiceClient.createTask(request);
            if (response.getCode() != 200) {
                return ApiResponse.error(response.getCode(), response.getMessage());
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> task = (Map<String, Object>) response.getData();
            return ApiResponse.success(response.getMessage(), task);
        } catch (SocketTimeoutException e) {
            log.error("创建任务超时", e);
            return ApiResponse.error(504, "创建任务超时: " + e.getMessage());
        } catch (IOException e) {
            log.error("创建任务失败", e);
            return ApiResponse.error(500, "创建任务失败: " + e.getMessage());
        }
    }

    /**
     * 批量导入任务
     */
    public ApiResponse<Map<String, Object>> importTasks(Map<String, Object> request) {
        try {
            ApiResponse<?> response = taskServiceClient.batchImportTasks(request);
            if (response.getCode() != 200) {
                return ApiResponse.error(response.getCode(), response.getMessage());
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) response.getData();
            return ApiResponse.success(response.getMessage(), result);
        } catch (SocketTimeoutException e) {
            log.error("批量导入超时", e);
            return ApiResponse.error(504, "批量导入超时: " + e.getMessage());
        } catch (IOException e) {
            log.error("批量导入失败", e);
            return ApiResponse.error(500, "批量导入失败: " + e.getMessage());
        }
    }

    /**
     * 标记任务完成
     */
    public ApiResponse<Map<String, Object>> completeTask(Long taskId) {
        try {
            ApiResponse<?> response = taskServiceClient.completeTask(taskId);
            if (response.getCode() != 200) {
                return ApiResponse.error(response.getCode(), response.getMessage());
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> task = (Map<String, Object>) response.getData();
            return ApiResponse.success(response.getMessage(), task);
        } catch (SocketTimeoutException e) {
            log.error("标记任务完成超时", e);
            return ApiResponse.error(504, "标记任务完成超时: " + e.getMessage());
        } catch (IOException e) {
            log.error("标记任务完成失败", e);
            return ApiResponse.error(500, "标记任务完成失败: " + e.getMessage());
        }
    }

    /**
     * 补充任务的指派人详细信息
     * <p>
     * 为每个任务获取指派人的姓名、邮箱、部门等信息，
     * 用于仪表盘的完整展示。
     */
    private void enrichTasksWithAssigneeInfo(List<Map<String, Object>> tasks) {
        for (Map<String, Object> task : tasks) {
            Long assigneeId = ((Number) task.get("assigneeId")).longValue();
            try {
                Map<String, Object> userInfo = platformServiceClient.getUser(assigneeId);
                if (userInfo != null) {
                    task.put("assigneeName", userInfo.get("name"));
                    task.put("assigneeEmail", userInfo.get("email"));
                    task.put("assigneeDepartment", userInfo.get("department"));
                }
            } catch (IOException e) {
                log.warn("获取用户信息失败: assigneeId={}", assigneeId, e);
                task.put("assigneeName", "未知用户");
            }
        }
    }
}
