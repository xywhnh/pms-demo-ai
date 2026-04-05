# MCP Contract Checklist

Use this file to map actual SkyWalking MCP methods before production use.

## Required Capabilities

1. Query endpoint-level traces by time range and status
- Inputs: `service`, `endpoint`, `start_time`, `end_time`, optional `status`.
- Output: list of traces with duration/error markers.

2. Query full trace details by trace id
- Inputs: `trace_id`.
- Output: full span tree with service/operation/duration/error.

3. Query logs correlated by trace id or service+time window
- Inputs: `trace_id` OR (`service`, `start_time`, `end_time`).
- Output: log entries with timestamp, level, content, trace correlation fields.

4. Query service/endpoint catalog
- Inputs: optional filters.
- Output: discoverable service and endpoint names for input normalization.

## Fill-In Mapping (TODO)

Replace placeholders below with actual MCP method names and parameters.

- `find_traces_by_endpoint(...)` -> `<method_name>`
- `get_trace_detail(...)` -> `<method_name>`
- `query_trace_logs(...)` -> `<method_name>`
- `list_services_or_endpoints(...)` -> `<method_name>`

## Validation Steps

1. Select a known failing request window.
2. Run endpoint trace query and confirm at least one failing trace is returned.
3. Fetch one trace detail and verify parent-child span links.
4. Fetch logs by the same trace id and verify timestamps overlap the span timeline.
