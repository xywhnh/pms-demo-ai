package com.glodon.pms.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户服务 - 模拟用户数据查询
 */
@Slf4j
@Service
public class UserService {

    /**
     * 模拟用户数据库
     */
    private static final Map<Long, Map<String, Object>> USER_DB = new HashMap<>();

    static {
        USER_DB.put(1L, createUser(1L, "张三", "zhangsan@company.com", "研发部"));
        USER_DB.put(2L, createUser(2L, "李四", "lisi@company.com", "研发部"));
        USER_DB.put(3L, createUser(3L, "王五", "wangwu@company.com", "产品部"));
        USER_DB.put(4L, createUser(4L, "赵六", "zhaoliu@company.com", "测试部"));
        USER_DB.put(5L, createUser(5L, "孙七", "sunqi@company.com", "研发部"));
        USER_DB.put(6L, createUser(6L, "周八", "zhouba@company.com", "设计部"));
        USER_DB.put(7L, createUser(7L, "吴九", "wujiu@company.com", "研发部"));
        USER_DB.put(8L, createUser(8L, "郑十", "zhengshi@company.com", "产品部"));
        USER_DB.put(9L, createUser(9L, "钱十一", "qiansy@company.com", "测试部"));
        USER_DB.put(10L, createUser(10L, "陈十二", "chense@company.com", "研发部"));
        USER_DB.put(11L, createUser(11L, "林十三", "linss@company.com", "运维部"));
        USER_DB.put(12L, createUser(12L, "黄十四", "huangss@company.com", "研发部"));
        USER_DB.put(13L, createUser(13L, "刘十五", "liusw@company.com", "产品部"));
        USER_DB.put(14L, createUser(14L, "杨十六", "yangsl@company.com", "研发部"));
        USER_DB.put(15L, createUser(15L, "胡十七", "husq@company.com", "测试部"));
    }

    private static Map<String, Object> createUser(Long id, String name, String email, String department) {
        Map<String, Object> user = new HashMap<>();
        user.put("id", id);
        user.put("name", name);
        user.put("email", email);
        user.put("department", department);
        user.put("avatar", "https://avatar.example.com/" + id + ".png");
        return user;
    }

    /**
     * 根据ID查询单个用户，模拟数据库查询延迟
     */
    public Map<String, Object> getUserById(Long id) {
        log.debug("查询用户信息, userId={}", id);
        // 模拟数据库查询延迟
        simulateDbQuery(45);

        Map<String, Object> user = USER_DB.get(id);
        if (user == null) {
            log.warn("用户不存在, userId={}", id);
            return null;
        }
        return new HashMap<>(user);
    }

    /**
     * 批量查询用户
     */
    public List<Map<String, Object>> getUsersByIds(List<Long> ids) {
        log.debug("批量查询用户信息, userIds={}", ids);
        // 批量查询只有一次DB延迟
        simulateDbQuery(60);

        return ids.stream()
                .map(USER_DB::get)
                .filter(u -> u != null)
                .map(HashMap::new)
                .collect(Collectors.toList());
    }

    /**
     * 检查用户是否存在
     */
    public boolean userExists(Long id) {
        log.debug("检查用户是否存在, userId={}", id);
        simulateDbQuery(20);
        return USER_DB.containsKey(id);
    }

    /**
     * 查询用户权限
     */
    public Map<String, Object> getUserPermissions(Long userId) {
        log.debug("查询用户权限, userId={}", userId);
        simulateDbQuery(30);

        Map<String, Object> user = USER_DB.get(userId);
        if (user == null) {
            return null;
        }

        Map<String, Object> permissions = new HashMap<>();
        permissions.put("userId", userId);
        permissions.put("canCreateTask", true);
        permissions.put("canAssignTask", true);
        permissions.put("canDeleteProject", "研发部".equals(user.get("department")));
        permissions.put("role", "研发部".equals(user.get("department")) ? "developer" : "member");
        return permissions;
    }

    /**
     * 模拟数据库查询延迟
     */
    private void simulateDbQuery(long baseMillis) {
        try {
            // 基础延迟 + 随机波动
            long delay = baseMillis + (long) (Math.random() * 15);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
