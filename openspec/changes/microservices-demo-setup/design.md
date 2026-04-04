# Design: 微服务演示项目架构改造

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    pms-demo-ai (Parent POM)                  │
├─────────────────────────────┬───────────────────────────────┤
│       demo-service-a        │        demo-service-b         │
│       (Port: 8081)          │        (Port: 8082)           │
│                             │                               │
│  ┌─────────────────────┐    │    ┌─────────────────────┐   │
│  │  DemoController     │    │    │  ScenarioController │   │
│  └──────────┬──────────┘    │    └──────────┬──────────┘   │
│             │               │               │               │
│  ┌──────────▼──────────┐    │    ┌──────────▼──────────┐   │
│  │  DemoService        │────┼───▶│  ScenarioService    │   │
│  │  (OkHttp3 Client)   │    │    │  (场景模拟)          │   │
│  └─────────────────────┘    │    └─────────────────────┘   │
└─────────────────────────────┴───────────────────────────────┘
                              │
                    ┌─────────▼─────────┐
                    │   SkyWalking OAP  │
                    │   (APM Server)    │
                    └───────────────────┘
```

## Project Structure

```
pms-demo-ai/                          # Parent POM 项目
├── pom.xml                           # 父 POM (packaging: pom)
├── demo-service-a/                   # 子模块 A
│   ├── pom.xml
│   └── src/main/java/com/glodon/pms/demo/
│       ├── ServiceAApplication.java
│       ├── controller/
│       │   └── DemoController.java
│       ├── service/
│       │   └── DemoService.java
│       ├── client/
│       │   └── ServiceBClient.java
│       └── common/
│           ├── ApiResponse.java
│           └── TraceIdFilter.java
├── demo-service-b/                   # 子模块 B
│   ├── pom.xml
│   └── src/main/java/com/glodon/pms/demo/
│       ├── ServiceBApplication.java
│       ├── controller/
│       │   └── ScenarioController.java
│       ├── service/
│       │   └── ScenarioService.java
│       └── common/
│           ├── ApiResponse.java
│           └── TraceIdFilter.java
└── openspec/                         # OpenSpec 配置
```

## Module Designs

### 1. Parent POM (pms-demo-ai)

**依赖管理**:
- Spring Boot 4.0.5 (Parent)
- Java 21
- SkyWalking Agent 依赖
- 公共依赖版本管理

### 2. demo-service-b (下游服务)

**端口**: 8082

**API 设计**:

| Endpoint | Method | 参数 | 描述 |
|----------|--------|------|------|
| `/api/scenario/normal` | GET | - | 正常返回 |
| `/api/scenario/timeout` | GET | delay=5000 | 超时场景 |
| `/api/scenario/slow-query` | GET | iterations=1000000 | 慢查询模拟 |
| `/api/scenario/npe` | GET | - | 空指针异常 |

**ScenarioService 实现**:
```java
public ApiResponse<String> normalScenario();
public ApiResponse<String> timeoutScenario(long delayMs);
public ApiResponse<String> slowQueryScenario(int iterations);
public ApiResponse<String> npeScenario();  // throws NPE
```

### 3. demo-service-a (上游服务)

**端口**: 8081

**API 设计**:

| Endpoint | Method | 参数 | 描述 |
|----------|--------|------|------|
| `/api/demo/execute` | GET | scenario | 统一入口，根据场景参数执行不同逻辑 |

**场景参数值**:
- `LOCAL_SUCCESS`: 本服务正常访问
- `LOCAL_ERROR`: 本服务内部出错
- `REMOTE_SUCCESS`: 调用外部接口成功
- `REMOTE_TIMEOUT`: 调用外部接口超时
- `REMOTE_NPE`: 调用外部接口 NPE

**ServiceBClient 实现**:
使用 OkHttp3 封装对 demo-service-b 的调用：
- 配置超时时间
- 传递 traceId header
- 处理响应和异常

## Common Components

### 1. ApiResponse 统一响应格式

```java
public class ApiResponse<T> {
    private int code;           // 状态码: 200=成功, 500=错误
    private String message;     // 响应消息
    private T data;             // 响应数据
    private String traceId;     // 链路追踪ID
}
```

### 2. TraceIdFilter 链路追踪过滤器

- 从请求头获取或生成 traceId
- 存入 MDC (Mapped Diagnostic Context)
- 日志模板中输出 traceId

### 3. 日志配置

**Logback 格式**:
```
%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%X{traceId}] %-5level %logger{36} - %msg%n
```

## SkyWalking Integration

### Agent 配置

每个服务启动时添加 JVM 参数：
```bash
-javaagent:/path/to/skywalking-agent.jar
-Dskywalking.agent.service_name=demo-service-a
-Dskywalking.collector.backend_service=localhost:11800
```

### 日志集成

添加 SkyWalking 日志依赖，自动上报日志到 SkyWalking：
- apm-toolkit-logback-1.x
- apm-toolkit-trace

## Error Handling

### GlobalExceptionHandler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e) {
        log.error("发生异常: ", e);
        return ApiResponse.error(500, e.getMessage());
    }
}
```

## Dependencies

### Parent POM 依赖管理

| 依赖 | 版本 | 用途 |
|------|------|------|
| spring-boot-starter-web | 4.0.5 | Web 框架 |
| okhttp3 | 4.12.0 | HTTP 客户端 |
| lombok | - | 代码简化 |
| apm-toolkit-trace | 9.x | SkyWalking 追踪 |
| apm-toolkit-logback-1.x | 9.x | SkyWalking 日志 |
