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

echo "Using compose command: ${COMPOSE_CMD[*]}"
"${COMPOSE_CMD[@]}" down
