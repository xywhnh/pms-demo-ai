#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
cd "$ROOT_DIR"

AGENT_VERSION="${AGENT_VERSION:-9.6.0}"
AGENT_URL="${AGENT_URL:-http://infra-jijianjindukeshi.oss-cn-beijing.aliyuncs.com/dev/test/apache-skywalking-java-agent-${AGENT_VERSION}.tgz}"
OPTIONAL_PLUGINS="${OPTIONAL_PLUGINS:-apm-springmvc-annotation-6.x-plugin-${AGENT_VERSION}.jar}"

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

for plugin in $OPTIONAL_PLUGINS; do
  if [ -f "$TARGET_DIR/optional-plugins/$plugin" ]; then
    cp "$TARGET_DIR/optional-plugins/$plugin" "$TARGET_DIR/plugins/"
    echo "Activated optional plugin: $plugin"
  else
    echo "WARN: optional plugin not found: $plugin"
  fi
done

test -f "$TARGET_DIR/skywalking-agent.jar"
echo "SkyWalking agent installed at: $TARGET_DIR"
