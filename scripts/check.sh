#!/usr/bin/env bash
set -euo pipefail

SERVER_IP="${1:-127.0.0.1}"

echo "Check service-a health..."
curl -fsS "http://${SERVER_IP}:8081/api/demo/health"
echo

echo "Check service-b normal..."
curl -fsS "http://${SERVER_IP}:8082/api/scenario/normal"
echo

echo "Check cross-service call (service-a -> service-b)..."
curl -fsS "http://${SERVER_IP}:8081/api/demo/execute?scenario=REMOTE_SUCCESS"
echo

echo "All checks passed."
