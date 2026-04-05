package com.glodon.pms.demo.controller;

import com.glodon.pms.demo.common.ApiResponse;
import com.glodon.pms.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户控制器 - 用户信息查询和权限管理
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 查询单个用户信息
     */
    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getUser(@PathVariable Long id) {
        log.debug("查询用户信息: id={}", id);
        Map<String, Object> user = userService.getUserById(id);
        if (user == null) {
            return ApiResponse.error(404, "用户不存在: " + id);
        }
        return ApiResponse.success(user);
    }

    /**
     * 批量查询用户信息
     */
    @GetMapping("/batch")
    public ApiResponse<List<Map<String, Object>>> getUserBatch(@RequestParam List<Long> ids) {
        log.debug("批量查询用户信息: ids={}", ids);
        List<Map<String, Object>> users = userService.getUsersByIds(ids);
        return ApiResponse.success(users);
    }

    /**
     * 检查用户是否存在
     */
    @GetMapping("/{id}/exists")
    public ApiResponse<Boolean> checkUserExists(@PathVariable Long id) {
        log.debug("检查用户是否存在: id={}", id);
        boolean exists = userService.userExists(id);
        if (!exists) {
            return ApiResponse.error(404, "用户不存在: " + id);
        }
        return ApiResponse.success(exists);
    }

    /**
     * 查询用户权限
     */
    @GetMapping("/{id}/permissions")
    public ApiResponse<Map<String, Object>> getUserPermissions(@PathVariable Long id) {
        log.debug("查询用户权限: id={}", id);
        Map<String, Object> permissions = userService.getUserPermissions(id);
        if (permissions == null) {
            return ApiResponse.error(404, "用户不存在: " + id);
        }
        return ApiResponse.success(permissions);
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("Service C is healthy");
    }
}
