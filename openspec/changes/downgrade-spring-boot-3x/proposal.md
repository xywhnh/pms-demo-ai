# Proposal: Spring Boot 降级到 3.x

## Summary

将项目的 Spring Boot 版本从 4.0.5 降级到 3.4.4（最新稳定 3.x），同时修正所有受版本变更影响的依赖 artifact 名称。

## Problem

当前项目使用 Spring Boot 4.0.5，存在以下问题：
1. Spring Boot 4.x 的部分 starter artifact 名称发生了变更（如 `spring-boot-starter-webmvc`），与社区生态和文档中常见的 3.x 命名不一致
2. 部分第三方库和工具链对 Spring Boot 4.x 的兼容性尚不完善
3. 团队更熟悉 Spring Boot 3.x 的 API 和配置方式

## Solution

- 将父 POM 的 `spring-boot-starter-parent` 版本从 `4.0.5` 改为 `3.4.4`
- 将子模块中的 `spring-boot-starter-webmvc` 恢复为 Spring Boot 3.x 的标准名称 `spring-boot-starter-web`
- 将子模块中的 `spring-boot-starter-webmvc-test` 恢复为 `spring-boot-starter-test`
- Java 版本保持 21（Spring Boot 3.2+ 支持）
- `jakarta.*` 包名无需变更（Spring Boot 3.x 已使用 jakarta 命名空间）

## Non-goals

- 不降级到 Spring Boot 2.x（javax 命名空间）
- 不修改业务代码逻辑
- 不变更 Java 版本

## Impact Analysis

| 变更项 | 当前值 (4.x) | 目标值 (3.x) |
|--------|-------------|-------------|
| spring-boot-starter-parent | 4.0.5 | 3.4.4 |
| Web starter artifact | spring-boot-starter-webmvc | spring-boot-starter-web |
| Test starter artifact | spring-boot-starter-webmvc-test | spring-boot-starter-test |
| Java 版本 | 21 | 21 (不变) |
| jakarta.* 包名 | 不变 | 不变 |
| OkHttp3 / Lombok / SkyWalking | 不变 | 不变 |
