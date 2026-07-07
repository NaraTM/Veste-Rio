#!/usr/bin/env bash
set -euo pipefail

ACTION="${1:-up}"
URL="http://localhost:8080/erp-web-war/login.xhtml"
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT"

SUDO=()
if [[ "$(id -u)" -ne 0 ]] && command -v sudo >/dev/null 2>&1; then
  SUDO=(sudo)
fi

DOCKER_CMD=(docker)
if ! docker info >/dev/null 2>&1; then
  DOCKER_CMD=("${SUDO[@]}" docker)
fi

configure_docker_dns() {
  local daemon_json="/etc/docker/daemon.json"
  local tmpfile
  tmpfile="$(mktemp)"

  "${SUDO[@]}" mkdir -p /etc/docker
  if "${SUDO[@]}" test -f "$daemon_json"; then
    "${SUDO[@]}" cat "$daemon_json" > "$tmpfile"
  else
    printf '{}' > "$tmpfile"
  fi

  python3 - "$tmpfile" <<'PY2'
import json
import pathlib
import sys

path = pathlib.Path(sys.argv[1])
try:
    data = json.loads(path.read_text(encoding="utf-8") or "{}")
except Exception:
    data = {}
if not isinstance(data, dict):
    data = {}
data["dns"] = ["1.1.1.1", "8.8.8.8"]
path.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
PY2

  "${SUDO[@]}" cp "$tmpfile" "$daemon_json"
  rm -f "$tmpfile"
}

restart_services() {
  if command -v systemctl >/dev/null 2>&1; then
    "${SUDO[@]}" systemctl restart systemd-resolved >/dev/null 2>&1 || true
    "${SUDO[@]}" systemctl restart docker >/dev/null 2>&1 || "${SUDO[@]}" service docker restart >/dev/null 2>&1 || true
  else
    "${SUDO[@]}" service docker restart >/dev/null 2>&1 || true
  fi
  sleep 3
}

compose_up() {
  local started=0
  for _ in 1 2 3; do
    if "${DOCKER_CMD[@]}" compose up --build -d; then
      started=1
      break
    fi
    configure_docker_dns
    restart_services
  done
  if [[ "$started" -ne 1 ]]; then
    return 1
  fi
}

open_url() {
  for _ in $(seq 1 60); do
    if curl -fsSI "$URL" >/dev/null 2>&1; then
      break
    fi
    sleep 1
  done
  ( xdg-open "$URL" >/dev/null 2>&1 || gio open "$URL" >/dev/null 2>&1 || python3 -m webbrowser "$URL" >/dev/null 2>&1 || true ) &
  echo "$URL"
}

case "$ACTION" in
  up)
    compose_up
    open_url
    ;;
  reset|restart)
    "${DOCKER_CMD[@]}" compose down -v --remove-orphans >/dev/null 2>&1 || true
    compose_up
    open_url
    ;;
  down)
    "${DOCKER_CMD[@]}" compose down -v --remove-orphans
    ;;
  logs)
    "${DOCKER_CMD[@]}" compose logs -f
    ;;
  open)
    open_url
    ;;
  ps)
    "${DOCKER_CMD[@]}" compose ps
    ;;
  *)
    exit 2
    ;;
esac
