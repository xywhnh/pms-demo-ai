# Scenario Playbook

Map demo scenarios to expected observability signatures.

## Scenario 1: Dashboard N+1 — 项目仪表盘加载缓慢

- Endpoint: `GET /api/projects/1/dashboard`
- Expected trace: `service-a → service-b` (1 call) + `service-a → service-c` (N calls, N=任务数)
- Key pattern: service-a 发出 15+ 个串行 HTTP Exit span 到 service-c 的 `/api/users/{id}`
- Expected result: code 200, but total latency 750ms+ due to sequential user lookups
- Root cause location: `ProjectService.enrichTasksWithAssigneeInfo()` — 对每个 task 逐一调用 getUser()
- Fix direction: 使用批量接口 `GET /api/users/batch?ids=...` 替换循环调用

## Scenario 2: Silent Error — 创建任务成功但通知未发送

- Endpoint: `POST /api/projects/1/tasks` with `{"title":"修复登录超时","assigneeId":5,"priority":"HIGH"}`
- Expected trace:
  - `service-a → service-b` (create task): code 200
  - `service-b → service-c` (permission check): code 200
  - `service-b → service-c` (notification send): span marked ERROR (NPE)
- Key pattern: 整体返回 200 OK，但 trace 中 notification span 标记 error=true
- Root cause location: `NotificationService.renderTemplate()` — HIGH 优先级未在 PRIORITY_LABEL_MAP 中配置, 导致 NPE
- Error swallow location: `TaskService.createTask()` — try-catch 块静默捕获通知异常，仅 log.warn

## Scenario 3: Cascade Timeout — 项目概览偶尔超时

- Endpoint: `GET /api/projects/1/overview?includeStats=true`
- Expected trace:
  - `service-a → service-b` (task stats): 耗时 4000ms+, 可能 timeout
  - `service-a → service-c` (user batch): 耗时 60ms, 正常完成
- Key pattern: 两个并行 span，stats span 远长于 team span，整体因 allOf() 超时返回 504
- Root cause location: `TaskStatisticsService.calculateWorkloadScore()` — O(n²) 嵌套循环 + 每对任务 15ms 延迟
- Propagation: stats 延迟 → allOf().get(3s) 超时 → service-a 返回 504
- Comparison: `GET /api/projects/1/overview?includeStats=false` 响应正常（仅调用 service-c batch）

## Scenario 4: Partial Failure — 批量导入任务部分失败

- Endpoint: `POST /api/projects/1/tasks/import`
- Request body: tasks 列表中包含不存在的 assigneeId (e.g., 999, 888)
- Expected trace:
  - `service-a → service-b` (batch import): code 200
  - `service-b → service-c` (user exists check): N 个 span，部分 ERROR (404 user not found)
- Key pattern: 顶层 200 OK，但 span 树中有多个 service-c 调用返回 404
- Root cause location: `TaskService.batchImportTasks()` — catch 块仅 failCount++，未保留失败详情
- Response shows: `{"success": 8, "failed": 2}` — 不知道哪条失败、为什么失败

## Scenario 5: Retry Storm — 完成任务触发重复通知

- Endpoint: `POST /api/projects/1/tasks/1/complete`
- Expected trace:
  - `service-a → service-b` (complete task): code 200
  - `service-b → service-c` (notification): 3 个 span
    - 第1次: 2500ms TIMEOUT (service-c 处理 TASK_COMPLETED 需要多渠道推送)
    - 第2次: ~2500ms OK (重试成功)
    - 第3次: ~2500ms OK (多余重试)
- Key pattern: 同一 notification 端点出现 3 个 span，时间上部分重叠
- Root cause location: `ServiceCClient.sendNotificationWithRetry()` — notificationClient 超时 2s < C 端处理 2.5s，触发自动重试
- Impact: 指派人收到 2-3 条重复的"任务完成"通知
- Fix direction: 通知接口增加幂等 key；对非幂等操作禁用重试；调整超时阈值
