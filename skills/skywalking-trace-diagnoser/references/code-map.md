# Code Map

Use this map when converting traces into concrete code locations.

## service-a (Gateway / BFF, port 8081)

- Entry API:
  - `demo-service-a/src/main/java/com/glodon/pms/demo/controller/ProjectController.java`
- Business orchestration:
  - `demo-service-a/src/main/java/com/glodon/pms/demo/service/ProjectService.java`
  - Key methods: `getDashboard()`, `getOverview()`, `createTask()`, `importTasks()`, `completeTask()`
  - N+1 location: `enrichTasksWithAssigneeInfo()` — 逐个调用 service-c 获取用户信息
- Downstream HTTP clients:
  - `demo-service-a/src/main/java/com/glodon/pms/demo/client/TaskServiceClient.java` (→ service-b)
  - `demo-service-a/src/main/java/com/glodon/pms/demo/client/PlatformServiceClient.java` (→ service-c)
- Exception mapping:
  - `demo-service-a/src/main/java/com/glodon/pms/demo/common/GlobalExceptionHandler.java`
- Trace header and MDC:
  - `demo-service-a/src/main/java/com/glodon/pms/demo/common/TraceIdFilter.java`

## service-b (Task Service, port 8082)

- Entry API:
  - `demo-service-b/src/main/java/com/glodon/pms/demo/controller/TaskController.java`
- Task CRUD and business logic:
  - `demo-service-b/src/main/java/com/glodon/pms/demo/service/TaskService.java`
  - Key methods: `createTask()` (静默吞错), `batchImportTasks()` (丢失错误详情), `completeTask()` (触发重试)
- Statistics computation:
  - `demo-service-b/src/main/java/com/glodon/pms/demo/service/TaskStatisticsService.java`
  - Key method: `calculateWorkloadScore()` — O(n²) 复杂度, 数据量大时超时
- Downstream HTTP client:
  - `demo-service-b/src/main/java/com/glodon/pms/demo/client/ServiceCClient.java` (→ service-c)
  - Key: `sendNotificationWithRetry()` — notificationClient 超时 2s, 重试 2 次
- Exception mapping:
  - `demo-service-b/src/main/java/com/glodon/pms/demo/common/GlobalExceptionHandler.java`

## service-c (Platform Service, port 8083)

- User API:
  - `demo-service-c/src/main/java/com/glodon/pms/demo/controller/UserController.java`
- Notification API:
  - `demo-service-c/src/main/java/com/glodon/pms/demo/controller/NotificationController.java`
- User data and queries:
  - `demo-service-c/src/main/java/com/glodon/pms/demo/service/UserService.java`
  - 每次查询有 45-60ms 模拟延迟 (DB)
- Notification sending:
  - `demo-service-c/src/main/java/com/glodon/pms/demo/service/NotificationService.java`
  - Bug: `renderTemplate()` — PRIORITY_LABEL_MAP 缺少 "HIGH" 映射，导致 NPE
  - Slow path: TASK_COMPLETED 类型通知需要 2500ms（多渠道推送）
- Exception mapping:
  - `demo-service-c/src/main/java/com/glodon/pms/demo/common/GlobalExceptionHandler.java`
