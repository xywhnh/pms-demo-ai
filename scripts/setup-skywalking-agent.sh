#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

AGENT_VERSION="${AGENT_VERSION:-9.4.0}"
AGENT_URL="${AGENT_URL:-https://archive.apache.org/dist/skywalking/java-agent/${AGENT_VERSION}/apache-skywalking-java-agent-${AGENT_VERSION}.tgz}"

TARGET_DIR="$ROOT_DIR/skywalking/agent"
TMP_DIR="$(mktemp -d)"
ARCHIVE="$TMP_DIR/skywalking-agent.tgz"

cleanup() {
  rm -rf "$TMP_DIR"
}
trap cleanup EXIT

echo "Preparing SkyWalking Java Agent ${AGENT_VERSION}"
mkdir -p "$TARGET_DIR"

if command -v curl >/dev/null 2>&1; then
  curl -fL "$AGENT_URL" -o "$ARCHIVE"
elif command -v wget >/dev/null 2>&1; then
  wget -O "$ARCHIVE" "$AGENT_URL"
else
  echo "ERROR: curl/wget not found."
  exit 1
fi

tar -xzf "$ARCHIVE" -C "$TMP_DIR"

AGENT_JAR="$(find "$TMP_DIR" -type f -name skywalking-agent.jar | head -n1 || true)"
if [ -z "$AGENT_JAR" ]; then
  echo "ERROR: skywalking-agent.jar not found in archive."
  exit 1
fi

AGENT_HOME="$(dirname "$AGENT_JAR")"
rm -rf "$TARGET_DIR"
mkdir -p "$TARGET_DIR"
cp -r "$AGENT_HOME"/. "$TARGET_DIR"/

test -f "$TARGET_DIR/skywalking-agent.jar"
echo "SkyWalking agent installed at: $TARGET_DIR"
