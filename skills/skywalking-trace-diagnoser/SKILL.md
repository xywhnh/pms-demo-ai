---
name: skywalking-trace-diagnoser
description: Analyze API failures by querying SkyWalking traces and logs through MCP, then correlate evidence with local Java code for root-cause diagnosis. Use when users ask why a specific endpoint failed, timed out, degraded, or returned unexpected responses in service-a/service-b scenarios (LOCAL_SUCCESS, LOCAL_ERROR, REMOTE_SUCCESS, REMOTE_TIMEOUT, REMOTE_NPE).
---

# SkyWalking Trace Diagnoser

## Overview

Diagnose interface exceptions with evidence-first workflow: trace tree + span status + related logs + code location.  
Output a stable incident report including root cause, impact path, confidence, and concrete fix direction.

## Workflow

1. Clarify target
- Capture `endpoint`, `time_range`, and optional `trace_id`.
- If no endpoint is provided, ask for exact API path and HTTP method.

2. Query observability evidence through MCP
- Read [mcp-contract.md](references/mcp-contract.md) to map required MCP methods.
- Pull failed/slow traces for the endpoint in the window.
- Select top 1-3 representative traces (prioritize ERROR and highest latency).
- Fetch full span tree and related logs for each selected trace.

3. Correlate with code
- Read [code-map.md](references/code-map.md) to map endpoint and scenario to code paths.
- Use local code to identify where exception/timeout is generated and where it is transformed.
- Use [scenario-playbook.md](references/scenario-playbook.md) to match known demo scenarios.

4. Produce diagnosis
- Follow [analysis-template.md](references/analysis-template.md).
- Include only evidence-backed conclusions.
- If evidence is missing, state exactly which MCP query/log/trace is missing.

## Decision Rules

- Never conclude from response code alone; always inspect spans + logs.
- Prefer one complete failing trace over many partial traces.
- Separate root cause from propagation:
  - Root cause: where failure originates.
  - Propagation: where failure is transformed and returned.
- Always include:
  - trace identifier
  - failing span(operation/service)
  - at least one corroborating log line
  - code file location for fix entry

## References To Load

- [mcp-contract.md](references/mcp-contract.md): Required MCP capability checklist and mapping placeholders.
- [scenario-playbook.md](references/scenario-playbook.md): Expected trace/log signatures for demo scenarios.
- [code-map.md](references/code-map.md): Endpoint-to-code correlation map.
- [analysis-template.md](references/analysis-template.md): Standard output format.
