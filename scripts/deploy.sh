#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if ! command -v docker >/dev/null 2>&1; then
  echo "ERROR: docker is not installed."
  exit 1
fi

if ! docker compose version >/dev/null 2>&1; then
  echo "ERROR: docker compose plugin is not available."
  exit 1
fi

if [ -d ".git" ]; then
  echo "[1/5] Pull latest code..."
  git pull --rebase
else
  echo "[1/5] Skip git pull (.git not found)."
fi

echo "[2/5] Prepare log directories..."
mkdir -p logs/service-a logs/service-b

echo "[3/5] Build images..."
docker compose build

echo "[4/5] Start containers..."
docker compose up -d --remove-orphans

echo "[5/5] Show running status..."
docker compose ps

HOST_IP="$(hostname -I 2>/dev/null | awk '{print $1}')"
if [ -n "${HOST_IP:-}" ]; then
  echo "Service A health: http://${HOST_IP}:8081/api/demo/health"
  echo "Service B normal: http://${HOST_IP}:8082/api/scenario/normal"
else
  echo "Service A health: http://<SERVER_IP>:8081/api/demo/health"
  echo "Service B normal: http://<SERVER_IP>:8082/api/scenario/normal"
fi
