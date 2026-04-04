# Tasks: 微服务演示项目架构改造

## Phase 1: 项目结构改造

### Task 1.1: 改造父工程 POM
**文件**: `pom.xml`

- [x] 修改 packaging 为 `pom`
- [x] 添加 `<modules>` 配置，声明子模块 demo-service-a 和 demo-service-b
- [x] 配置 `<dependencyManagement>` 统一管理依赖版本
- [x] 添加 OkHttp3、Lombok、SkyWalking 依赖版本管理
- [x] 移除原有的 `src` 目录相关配置（父工程不编译代码）

### Task 1.2: 创建 demo-service-b 子模块结构
**目录**: `demo-service-b/`

- [x] 创建子模块目录结构
- [x] 创建 `demo-service-b/pom.xml`，继承父 POM
- [x] 添加 spring-boot-starter-web 依赖
- [x] 添加 SkyWalking 相关依赖
- [x] 创建 `src/main/java/com/glodon/pms/demo/` 目录结构
- [x] 创建 `src/main/resources/application.yaml` 配置文件（端口 8082）

### Task 1.3: 创建 demo-service-a 子模块结构
**目录**: `demo-service-a/`

- [x] 创建子模块目录结构
- [x] 创建 `demo-service-a/pom.xml`，继承父 POM
- [x] 添加 spring-boot-starter-web 依赖
- [x] 添加 OkHttp3 依赖
- [x] 添加 SkyWalking 相关依赖
- [x] 创建 `src/main/java/com/glodon/pms/demo/` 目录结构
- [x] 创建 `src/main/resources/application.yaml` 配置文件（端口 8081）

---

## Phase 2: 公共组件开发

### Task 2.1: 实现统一响应类 ApiResponse
**文件**: 两个子模块各自的 `common/ApiResponse.java`

- [x] 创建泛型类 `ApiResponse<T>`
- [x] 定义字段：code、message、data、traceId
- [x] 实现静态工厂方法：success()、error()
- [x] 使用 Lombok 简化代码

### Task 2.2: 实现 TraceId 过滤器
**文件**: 两个子模块各自的 `common/TraceIdFilter.java`

- [x] 创建 Servlet Filter 实现
- [x] 从请求头 `X-Trace-Id` 获取 traceId，不存在则生成 UUID
- [x] 将 traceId 存入 MDC
- [x] 将 traceId 存入请求属性供后续使用
- [x] 注册 Filter 到 Spring 容器

### Task 2.3: 配置 Logback 日志格式
**文件**: 两个子模块的 `src/main/resources/logback-spring.xml`

- [x] 配置日志格式包含 traceId
- [x] 配置 SkyWalking 日志 Appender
- [x] 设置控制台和文件日志输出

---

## Phase 3: demo-service-b 开发

### Task 3.1: 创建 ServiceBApplication 启动类
**文件**: `demo-service-b/src/main/java/com/glodon/pms/demo/ServiceBApplication.java`

- [x] 创建 Spring Boot 应用启动类
- [x] 添加 @SpringBootApplication 注解
- [x] 配置组件扫描包路径

### Task 3.2: 实现 ScenarioService
**文件**: `demo-service-b/src/main/java/com/glodon/pms/demo/service/ScenarioService.java`

- [x] 实现 `normalScenario()` 方法：直接返回成功响应
- [x] 实现 `timeoutScenario(long delayMs)` 方法：Thread.sleep 模拟延迟
- [x] 实现 `slowQueryScenario(int iterations)` 方法：执行复杂计算模拟慢查询
- [x] 实现 `npeScenario()` 方法：主动抛出 NullPointerException
- [x] 添加日志记录，包含 traceId

### Task 3.3: 实现 ScenarioController
**文件**: `demo-service-b/src/main/java/com/glodon/pms/demo/controller/ScenarioController.java`

- [x] 创建 REST 控制器，路径前缀 `/api/scenario`
- [x] 实现 `GET /normal` 端点
- [x] 实现 `GET /timeout` 端点，接收 delay 参数
- [x] 实现 `GET /slow-query` 端点，接收 iterations 参数
- [x] 实现 `GET /npe` 端点

### Task 3.4: 实现全局异常处理
**文件**: `demo-service-b/src/main/java/com/glodon/pms/demo/common/GlobalExceptionHandler.java`

- [x] 创建 @RestControllerAdvice 类
- [x] 处理 NullPointerException 返回 500 错误
- [x] 处理通用 Exception 返回错误响应
- [x] 记录异常日志

---

## Phase 4: demo-service-a 开发

### Task 4.1: 创建 ServiceAApplication 启动类
**文件**: `demo-service-a/src/main/java/com/glodon/pms/demo/ServiceAApplication.java`

- [x] 创建 Spring Boot 应用启动类
- [x] 添加 @SpringBootApplication 注解

### Task 4.2: 实现 ServiceBClient HTTP 客户端
**文件**: `demo-service-a/src/main/java/com/glodon/pms/demo/client/ServiceBClient.java`

- [x] 创建 OkHttp3 客户端配置
- [x] 配置连接超时、读取超时、写入超时
- [x] 实现 `callNormal()` 方法
- [x] 实现 `callTimeout()` 方法
- [x] 实现 `callSlowQuery()` 方法
- [x] 实现 `callNpe()` 方法
- [x] 在请求头中传递 traceId
- [x] 处理响应和异常

### Task 4.3: 实现 DemoService
**文件**: `demo-service-a/src/main/java/com/glodon/pms/demo/service/DemoService.java`

- [x] 注入 ServiceBClient
- [x] 实现 `executeScenario(String scenario)` 方法
- [x] 根据场景参数分派到不同处理逻辑：
  - LOCAL_SUCCESS：本地直接返回成功
  - LOCAL_ERROR：本地抛出业务异常
  - REMOTE_SUCCESS：调用 service-b 正常接口
  - REMOTE_TIMEOUT：调用 service-b 超时接口
  - REMOTE_NPE：调用 service-b NPE 接口
- [x] 添加业务逻辑处理和日志记录

### Task 4.4: 实现 DemoController
**文件**: `demo-service-a/src/main/java/com/glodon/pms/demo/controller/DemoController.java`

- [x] 创建 REST 控制器，路径前缀 `/api/demo`
- [x] 实现 `GET /execute` 端点，接收 scenario 参数
- [x] 调用 DemoService 处理请求

### Task 4.5: 实现全局异常处理
**文件**: `demo-service-a/src/main/java/com/glodon/pms/demo/common/GlobalExceptionHandler.java`

- [x] 创建 @RestControllerAdvice 类
- [x] 处理业务异常
- [x] 处理 HTTP 调用异常（超时、连接失败等）
- [x] 记录异常日志

---

## Phase 5: SkyWalking 集成配置

### Task 5.1: 配置 SkyWalking 日志集成
**文件**: 两个子模块的 `logback-spring.xml`

- [x] 添加 SkyWalking TraceId 占位符
- [x] 配置 GRPCLogClientAppender（可选）

### Task 5.2: 编写启动脚本
**文件**: 根目录 `scripts/` 或 README 说明

- [ ] 记录 SkyWalking Agent 启动参数配置
- [ ] 记录各服务启动命令示例

---

## Phase 6: 验证测试

### Task 6.1: 编译验证
- [x] 执行 `mvn clean compile` 验证编译通过
- [x] 确保两个子模块都能正常编译

### Task 6.2: 功能测试
- [ ] 启动 demo-service-b，验证各场景接口
- [ ] 启动 demo-service-a，验证统一接口
- [ ] 验证跨服务调用链路
- [ ] 验证日志中 traceId 输出
