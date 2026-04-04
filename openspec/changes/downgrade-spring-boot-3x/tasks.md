# Tasks: Spring Boot 降级到 3.x

## Task 1: 修改父工程 POM 版本
**文件**: `pom.xml`

- [x] 将 `spring-boot-starter-parent` 版本从 `4.0.5` 改为 `3.4.4`

## Task 2: 修改 demo-service-a 依赖名称
**文件**: `demo-service-a/pom.xml`

- [x] 将 `spring-boot-starter-webmvc` 改为 `spring-boot-starter-web`
- [x] 将 `spring-boot-starter-webmvc-test` 改为 `spring-boot-starter-test`

## Task 3: 修改 demo-service-b 依赖名称
**文件**: `demo-service-b/pom.xml`

- [x] 将 `spring-boot-starter-webmvc` 改为 `spring-boot-starter-web`
- [x] 将 `spring-boot-starter-webmvc-test` 改为 `spring-boot-starter-test`

## Task 4: 编译验证
- [x] 执行 `mvn clean compile` 验证编译通过
- [x] 确保两个子模块都能正常编译
