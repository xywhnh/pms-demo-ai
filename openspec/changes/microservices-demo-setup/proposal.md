# Proposal: 微服务演示项目架构改造

## Summary

将当前 pms-demo-ai 单体项目改造为父子模块的微服务架构，创建 demo-service-a 和 demo-service-b 两个独立微服务，并集成 SkyWalking APM 实现分布式链路追踪。

## Problem

当前项目是一个单体 Spring Boot 应用，无法满足以下需求：
1. 无法模拟微服务间的调用场景
2. 缺乏分布式链路追踪能力
3. 无法演示服务调用中的各种异常场景（超时、慢查询、异常等）
4. 无法在 SkyWalking 中进行错误监控和追踪

## Solution

### 1. 项目结构改造
- 将 pms-demo-ai 改造为 Maven 父工程（pom 类型）
- 创建 demo-service-a 子模块：作为上游服务，使用 OkHttp3 调用下游服务
- 创建 demo-service-b 子模块：作为下游服务，提供各种场景模拟端点

### 2. demo-service-b 功能
提供 ScenarioController 和 ScenarioService，支持以下场景：
- 正常返回场景
- 接口超时场景（延迟5秒返回）
- 慢查询场景（执行复杂计算）
- 空指针异常场景（抛出 NPE）

### 3. demo-service-a 功能
提供统一接口，通过参数控制不同场景：
- 本服务正常访问
- 本服务内部出错
- 调用外部接口成功
- 调用外部接口超时
- 调用外部接口异常

### 4. 可观测性
- 所有服务日志输出包含 traceId
- 集成 SkyWalking APM 系统

## Non-goals

- 不包含数据库实际连接（仅模拟慢查询场景）
- 不包含完整的业务逻辑实现
- 不包含生产环境部署配置

## Success Criteria

- [ ] 两个子模块可独立启动运行
- [ ] demo-service-a 能成功调用 demo-service-b 的各个场景接口
- [ ] 所有日志输出包含 traceId
- [ ] 各种异常场景能在 SkyWalking 中正确追踪显示
