#!/bin/sh
set -eu

APP_PID=""
ARTHAS_DIR="/app/arthas"
ARTHAS_BOOT_JAR="${ARTHAS_DIR}/arthas-boot.jar"
ARTHAS_PACKAGE="/opt/arthas/arthas-packaging.tar"
ARTHAS_ENABLED="${ARTHAS_ENABLED:-true}"
ARTHAS_START_DELAY_SECONDS="${ARTHAS_START_DELAY_SECONDS:-8}"
ARTHAS_TARGET_IP="${ARTHAS_TARGET_IP:-0.0.0.0}"
ARTHAS_TELNET_PORT="${ARTHAS_TELNET_PORT:-3658}"
ARTHAS_HTTP_PORT="${ARTHAS_HTTP_PORT:-8563}"
ARTHAS_LOG_FILE="/app/logs/arthas-boot.log"

log() {
  echo "[entrypoint] $*"
}

forward_signal() {
  if [ -n "${APP_PID}" ] && kill -0 "${APP_PID}" 2>/dev/null; then
    log "forwarding termination signal to app pid ${APP_PID}"
    kill -TERM "${APP_PID}" 2>/dev/null || true
  fi
}

extract_arthas() {
  if [ -f "${ARTHAS_BOOT_JAR}" ]; then
    return 0
  fi

  if [ ! -f "${ARTHAS_PACKAGE}" ]; then
    log "arthas package not found at ${ARTHAS_PACKAGE}, skip arthas startup"
    return 1
  fi

  tmp_dir="$(mktemp -d)"
  rm -rf "${ARTHAS_DIR}"
  mkdir -p "${ARTHAS_DIR}"

  if ! tar -xf "${ARTHAS_PACKAGE}" -C "${tmp_dir}"; then
    rm -rf "${tmp_dir}"
    log "failed to extract arthas package"
    return 1
  fi

  boot_path="$(find "${tmp_dir}" -type f -name arthas-boot.jar | head -n 1)"
  if [ -z "${boot_path}" ]; then
    rm -rf "${tmp_dir}"
    log "arthas-boot.jar not found after extraction"
    return 1
  fi

  boot_dir="$(dirname "${boot_path}")"
  cp -R "${boot_dir}"/. "${ARTHAS_DIR}/"
  rm -rf "${tmp_dir}"
  return 0
}

start_arthas() {
  if [ "${ARTHAS_ENABLED}" != "true" ]; then
    log "arthas disabled by ARTHAS_ENABLED=${ARTHAS_ENABLED}"
    return 0
  fi

  if ! extract_arthas; then
    return 0
  fi

  if [ ! -f "${ARTHAS_BOOT_JAR}" ]; then
    log "arthas boot jar missing at ${ARTHAS_BOOT_JAR}, skip arthas startup"
    return 0
  fi

  if ! kill -0 "${APP_PID}" 2>/dev/null; then
    log "app process ${APP_PID} is not running, skip arthas startup"
    return 0
  fi

  log "starting arthas for app pid ${APP_PID}"
  (
    JAVA_TOOL_OPTIONS= exec java -jar "${ARTHAS_BOOT_JAR}" \
      "${APP_PID}" \
      --target-ip "${ARTHAS_TARGET_IP}" \
      --telnet-port "${ARTHAS_TELNET_PORT}" \
      --http-port "${ARTHAS_HTTP_PORT}"
  ) >> "${ARTHAS_LOG_FILE}" 2>&1 &
}

mkdir -p /app/logs "${ARTHAS_DIR}"

trap forward_signal TERM INT

log "starting application"
java -jar /app/app.jar &
APP_PID=$!

sleep "${ARTHAS_START_DELAY_SECONDS}"
start_arthas || true

wait "${APP_PID}"
