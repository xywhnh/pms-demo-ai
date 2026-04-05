#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

COMPOSE_CMD=()
if command -v docker-compose >/dev/null 2>&1; then
  COMPOSE_CMD=(docker-compose)
elif docker compose version 2>/dev/null | grep -qi "Docker Compose"; then
  COMPOSE_CMD=(docker compose)
else
  echo "ERROR: neither 'docker compose' nor 'docker-compose' is available."
  exit 1
fi

mkdir -p logs/service-a logs/service-b

if [ ! -f "skywalking/agent/skywalking-agent.jar" ]; then
  bash scripts/setup-skywalking-agent.sh
fi

echo "Using compose command: ${COMPOSE_CMD[*]}"
"${COMPOSE_CMD[@]}" -f docker-compose.yml -f docker-compose.skywalking.yml pull
"${COMPOSE_CMD[@]}" -f docker-compose.yml -f docker-compose.skywalking.yml up -d
"${COMPOSE_CMD[@]}" -f docker-compose.yml -f docker-compose.skywalking.yml ps

echo "SkyWalking UI: http://<SERVER_IP>:18080"
