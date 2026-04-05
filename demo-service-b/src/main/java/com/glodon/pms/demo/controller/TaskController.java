package com.glodon.pms.demo.controller;

import com.glodon.pms.demo.common.ApiResponse;
import com.glodon.pms.demo.service.TaskService;
import com.glodon.pms.demo.service.TaskStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 任务控制器 - 任务增删改查及统计
 */
@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final TaskStatisticsService taskStatisticsService;

    /**
     * 查询项目下的任务列表
     */
    @GetMapping
    public ApiResponse<List<Map<String, Object>>> getTasksByProject(
            @RequestParam Long projectId) {
        log.debug("查询项目任务列表: projectId={}", projectId);
        List<Map<String, Object>> tasks = taskService.getTasksByProjectId(projectId);
        return ApiResponse.success(tasks);
    }

    /**
     * 查询项目任务统计
     */
    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getTaskStatistics(
            @RequestParam Long projectId) {
        log.debug("查询项目任务统计: projectId={}", projectId);
        Map<String, Object> stats = taskStatisticsService.calculateStatistics(projectId);
        return ApiResponse.success(stats);
    }

    /**
     * 创建任务
     */
    @PostMapping
    public ApiResponse<Map<String, Object>> createTask(@RequestBody Map<String, Object> request) {
        log.debug("创建任务: {}", request);
        Map<String, Object> task = taskService.createTask(request);
        return ApiResponse.success("任务创建成功", task);
    }

    /**
     * 批量导入任务
     */
    @PostMapping("/batch")
    public ApiResponse<Map<String, Object>> batchImportTasks(@RequestBody Map<String, Object> request) {
        log.debug("批量导入任务");
        Map<String, Object> result = taskService.batchImportTasks(request);
        return ApiResponse.success("批量导入完成", result);
    }

    /**
     * 标记任务完成
     */
    @PutMapping("/{id}/complete")
    public ApiResponse<Map<String, Object>> completeTask(@PathVariable Long id) {
        log.debug("标记任务完成: taskId={}", id);
        Map<String, Object> result = taskService.completeTask(id);
        return ApiResponse.success("任务已完成", result);
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("Service B is healthy");
    }
}
