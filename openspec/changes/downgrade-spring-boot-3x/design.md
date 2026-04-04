# Design: Spring Boot 降级到 3.x

## 变更范围

本次变更仅涉及 POM 文件中的版本号和 artifact 名称修改，**不涉及任何 Java 源代码变更**。

## 受影响的文件

| 文件 | 变更内容 |
|------|---------|
| `pom.xml` (父工程) | Spring Boot Parent 版本 4.0.5 → 3.4.4 |
| `demo-service-a/pom.xml` | starter artifact 名称恢复为 3.x 标准 |
| `demo-service-b/pom.xml` | starter artifact 名称恢复为 3.x 标准 |

## 详细变更

### 1. 父工程 pom.xml

```xml
<!-- 变更前 -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.0.5</version>
</parent>

<!-- 变更后 -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.4.4</version>
</parent>
```

### 2. 子模块 POM (demo-service-a & demo-service-b)

**Web Starter**:
```xml
<!-- 变更前 (4.x) -->
<artifactId>spring-boot-starter-webmvc</artifactId>

<!-- 变更后 (3.x) -->
<artifactId>spring-boot-starter-web</artifactId>
```

**Test Starter**:
```xml
<!-- 变更前 (4.x) -->
<artifactId>spring-boot-starter-webmvc-test</artifactId>

<!-- 变更后 (3.x) -->
<artifactId>spring-boot-starter-test</artifactId>
```

## 不需要变更的部分

| 项目 | 原因 |
|------|------|
| Java 源代码 | Spring Boot 3.x 和 4.x 在应用层 API 兼容 |
| `jakarta.*` imports | Spring Boot 3.x 已使用 jakarta 命名空间 |
| OkHttp3 (4.12.0) | 与 Spring Boot 版本无关 |
| SkyWalking (9.1.0) | 与 Spring Boot 版本无关 |
| Lombok | 由 spring-boot-starter-parent 管理版本，自动适配 |
| logback-spring.xml | 日志配置格式在 3.x/4.x 间兼容 |
| application.yaml | 配置项在 3.x/4.x 间兼容 |
