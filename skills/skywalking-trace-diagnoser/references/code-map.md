# Code Map

Use this map when converting traces into concrete code locations.

## service-a

- Entry API:
  - `demo-service-a/src/main/java/com/glodon/pms/demo/controller/DemoController.java`
- Scenario dispatch and remote-call behavior:
  - `demo-service-a/src/main/java/com/glodon/pms/demo/service/DemoService.java`
- Downstream HTTP call:
  - `demo-service-a/src/main/java/com/glodon/pms/demo/client/ServiceBClient.java`
- Exception mapping:
  - `demo-service-a/src/main/java/com/glodon/pms/demo/common/GlobalExceptionHandler.java`
- Trace header and MDC:
  - `demo-service-a/src/main/java/com/glodon/pms/demo/common/TraceIdFilter.java`

## service-b

- Entry APIs:
  - `demo-service-b/src/main/java/com/glodon/pms/demo/controller/ScenarioController.java`
- Scenario implementations:
  - `demo-service-b/src/main/java/com/glodon/pms/demo/service/ScenarioService.java`
- Exception mapping:
  - `demo-service-b/src/main/java/com/glodon/pms/demo/common/GlobalExceptionHandler.java`
- Trace header and MDC:
  - `demo-service-b/src/main/java/com/glodon/pms/demo/common/TraceIdFilter.java`
