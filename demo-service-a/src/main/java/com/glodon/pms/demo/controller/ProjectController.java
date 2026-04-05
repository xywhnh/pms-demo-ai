package com.glodon.pms.demo.controller;

import com.glodon.pms.demo.common.ApiResponse;
import com.glodon.pms.demo.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 项目控制器 - 项目管理统一入口
 */
@Slf4j
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    /**
     * 获取项目基本信息
     */
    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getProject(@PathVariable Long id) {
        log.debug("获取项目信息: id={}", id);
        return projectService.getProject(id);
    }

    /**
     * 获取项目仪表盘（任务列表 + 指派人详情）
     */
    @GetMapping("/{id}/dashboard")
    public ApiResponse<Map<String, Object>> getDashboard(@PathVariable Long id) {
        log.debug("获取项目仪表盘: id={}", id);
        return projectService.getDashboard(id);
    }

    /**
     * 获取项目概览（含统计和团队信息）
     */
    @GetMapping("/{id}/overview")
    public ApiResponse<Map<String, Object>> getOverview(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean includeStats) {
        log.debug("获取项目概览: id={}, includeStats={}", id, includeStats);
        return projectService.getOverview(id, includeStats);
    }

    /**
     * 创建项目任务
     */
    @PostMapping("/{id}/tasks")
    public ApiResponse<Map<String, Object>> createTask(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.debug("创建项目任务: projectId={}", id);
        request.put("projectId", id);
        return projectService.createTask(request);
    }

    /**
     * 批量导入任务
     */
    @PostMapping("/{id}/tasks/import")
    public ApiResponse<Map<String, Object>> importTasks(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.debug("批量导入任务: projectId={}", id);
        request.put("projectId", id);
        return projectService.importTasks(request);
    }

    /**
     * 标记任务完成
     */
    @PostMapping("/{projectId}/tasks/{taskId}/complete")
    public ApiResponse<Map<String, Object>> completeTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId) {
        log.debug("标记任务完成: projectId={}, taskId={}", projectId, taskId);
        return projectService.completeTask(taskId);
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("Service A is healthy");
    }
}
