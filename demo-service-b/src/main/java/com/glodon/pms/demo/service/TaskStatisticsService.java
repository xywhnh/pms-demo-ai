package com.glodon.pms.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务统计服务 - 计算项目任务的各类统计指标
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskStatisticsService {

    private final TaskService taskService;

    /**
     * 计算项目任务统计
     * <p>
     * 包含: 总数、各状态数量、完成率、逾期数、优先级分布、工作量评估等
     * 注意: 工作量评估算法复杂度较高，数据量大时会明显变慢
     */
    public Map<String, Object> calculateStatistics(Long projectId) {
        log.info("计算项目统计: projectId={}", projectId);
        long startTime = System.currentTimeMillis();

        List<Map<String, Object>> tasks = taskService.getTasksByProjectId(projectId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("projectId", projectId);
        stats.put("totalTasks", tasks.size());

        // 基础统计
        long todoCount = tasks.stream().filter(t -> "TODO".equals(t.get("status"))).count();
        long inProgressCount = tasks.stream().filter(t -> "IN_PROGRESS".equals(t.get("status"))).count();
        long doneCount = tasks.stream().filter(t -> "DONE".equals(t.get("status"))).count();

        stats.put("todoCount", todoCount);
        stats.put("inProgressCount", inProgressCount);
        stats.put("doneCount", doneCount);
        stats.put("completionRate", tasks.isEmpty() ? 0 : (double) doneCount / tasks.size() * 100);

        // 优先级分布
        long highCount = tasks.stream().filter(t -> "HIGH".equals(t.get("priority"))).count();
        long mediumCount = tasks.stream().filter(t -> "MEDIUM".equals(t.get("priority"))).count();
        long lowCount = tasks.stream().filter(t -> "LOW".equals(t.get("priority"))).count();

        Map<String, Long> priorityDistribution = new HashMap<>();
        priorityDistribution.put("HIGH", highCount);
        priorityDistribution.put("MEDIUM", mediumCount);
        priorityDistribution.put("LOW", lowCount);
        stats.put("priorityDistribution", priorityDistribution);

        // 工作量评估 — 此处存在性能问题
        double workloadScore = calculateWorkloadScore(tasks);
        stats.put("workloadScore", workloadScore);

        long duration = System.currentTimeMillis() - startTime;
        stats.put("calculationTimeMs", duration);
        log.info("统计计算完成: projectId={}, 耗时={}ms", projectId, duration);

        return stats;
    }

    /**
     * 计算工作量评分
     * <p>
     * 基于任务间的优先级关联度和状态权重进行综合评估。
     * 算法对每对任务计算关联因子，时间复杂度 O(n²)。
     */
    private double calculateWorkloadScore(List<Map<String, Object>> tasks) {
        double totalScore = 0;

        // 嵌套循环：对每对任务计算关联因子
        for (int i = 0; i < tasks.size(); i++) {
            Map<String, Object> taskA = tasks.get(i);
            double weightA = getPriorityWeight(taskA);
            double statusFactorA = getStatusFactor(taskA);

            for (int j = 0; j < tasks.size(); j++) {
                if (i == j) continue;
                Map<String, Object> taskB = tasks.get(j);
                double weightB = getPriorityWeight(taskB);
                double statusFactorB = getStatusFactor(taskB);

                // 模拟复杂的关联度计算
                double correlation = Math.sqrt(weightA * weightB)
                        * Math.sin(statusFactorA + statusFactorB)
                        * Math.cos(weightA - weightB);
                totalScore += correlation;

                // 模拟数据库查询延迟（每对任务需要交叉查询依赖关系）
                simulateDependencyLookup();
            }
        }

        return Math.abs(totalScore);
    }

    private double getPriorityWeight(Map<String, Object> task) {
        return switch ((String) task.getOrDefault("priority", "MEDIUM")) {
            case "HIGH" -> 3.0;
            case "MEDIUM" -> 2.0;
            case "LOW" -> 1.0;
            default -> 1.5;
        };
    }

    private double getStatusFactor(Map<String, Object> task) {
        return switch ((String) task.getOrDefault("status", "TODO")) {
            case "DONE" -> 0.2;
            case "IN_PROGRESS" -> 1.5;
            case "TODO" -> 1.0;
            default -> 1.0;
        };
    }

    /**
     * 模拟依赖关系查询延迟
     */
    private void simulateDependencyLookup() {
        try {
            Thread.sleep(15);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
