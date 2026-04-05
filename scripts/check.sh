#!/usr/bin/env bash
set -euo pipefail

SERVER_IP="${1:-127.0.0.1}"

echo "Check service-c health..."
curl -fsS "http://${SERVER_IP}:8083/api/users/health"
echo

echo "Check service-b health..."
curl -fsS "http://${SERVER_IP}:8082/api/tasks/health"
echo

echo "Check service-a health..."
curl -fsS "http://${SERVER_IP}:8081/api/projects/health"
echo

echo "Check cross-service call (service-a -> service-b -> service-c)..."
curl -fsS "http://${SERVER_IP}:8081/api/projects/1"
echo

echo "All checks passed."
