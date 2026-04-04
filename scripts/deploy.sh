#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

# Some minimal Linux images do not provide C.UTF-8 locale.
if [ "${LC_ALL:-}" = "C.UTF-8" ] && ! locale -a 2>/dev/null | grep -qi '^c\.utf-8$'; then
  unset LC_ALL
fi

if ! command -v docker >/dev/null 2>&1; then
  echo "ERROR: docker is not installed."
  exit 1
fi

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

if [ -d ".git" ]; then
  echo "[1/5] Pull latest code..."
  git pull --rebase
else
  echo "[1/5] Skip git pull (.git not found)."
fi

echo "[2/5] Prepare log directories..."
mkdir -p logs/service-a logs/service-b

if [ -n "${MVN_IMAGE:-}" ] || [ -n "${JRE_IMAGE:-}" ]; then
  echo "Using custom base images:"
  echo "  MVN_IMAGE=${MVN_IMAGE:-<default>}"
  echo "  JRE_IMAGE=${JRE_IMAGE:-<default>}"
fi

echo "[3/5] Build images..."
"${COMPOSE_CMD[@]}" build

echo "[4/5] Start containers..."
"${COMPOSE_CMD[@]}" up -d --remove-orphans

echo "[5/5] Show running status..."
"${COMPOSE_CMD[@]}" ps

HOST_IP="$(hostname -I 2>/dev/null | awk '{print $1}')"
if [ -n "${HOST_IP:-}" ]; then
  echo "Service A health: http://${HOST_IP}:8081/api/demo/health"
  echo "Service B normal: http://${HOST_IP}:8082/api/scenario/normal"
else
  echo "Service A health: http://<SERVER_IP>:8081/api/demo/health"
  echo "Service B normal: http://<SERVER_IP>:8082/api/scenario/normal"
fi
