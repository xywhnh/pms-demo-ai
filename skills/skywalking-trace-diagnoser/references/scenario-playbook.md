# Scenario Playbook

Map demo scenarios to expected observability signatures.

## LOCAL_SUCCESS

- Endpoint: `GET /api/demo/execute?scenario=LOCAL_SUCCESS`
- Expected trace: only `demo-service-a` internal spans, no remote call to service-b.
- Expected result: code 200.

## LOCAL_ERROR

- Endpoint: `GET /api/demo/execute?scenario=LOCAL_ERROR`
- Expected trace: failure in `demo-service-a`; entry span marked error.
- Likely code anchor: Runtime exception branch in `DemoService`.
- Expected result: code 500 with business error message.

## REMOTE_SUCCESS

- Endpoint: `GET /api/demo/execute?scenario=REMOTE_SUCCESS`
- Expected trace: cross-service path `demo-service-a -> demo-service-b`.
- Expected result: code 200 and successful downstream response.

## REMOTE_TIMEOUT

- Endpoint: `GET /api/demo/execute?scenario=REMOTE_TIMEOUT`
- Expected trace:
  - `service-a` outbound call fails around client timeout threshold.
  - `service-b` timeout scenario may still run longer.
- Expected result: code 504 (timeout).

## REMOTE_NPE

- Endpoint: `GET /api/demo/execute?scenario=REMOTE_NPE`
- Expected trace:
  - `service-b` span marked error with NPE.
  - `service-a` receives downstream error and returns mapped failure.
- Expected result: non-200 with error from downstream.
