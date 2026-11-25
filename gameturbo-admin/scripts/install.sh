#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
TARGET_BIN_DIR="${PREFIX:-$HOME/.local}/bin"
mkdir -p "$TARGET_BIN_DIR"

chmod +x "$ROOT_DIR/gameturbo-admin/gameturbo-admin"
chmod +x "$ROOT_DIR/gameturbo-admin/scripts/"*.sh

ln -sf "$ROOT_DIR/gameturbo-admin/gameturbo-admin" "$TARGET_BIN_DIR/gameturbo-admin"
echo "[install.sh] Enlace simbólico instalado en: $TARGET_BIN_DIR/gameturbo-admin"
echo "[install.sh] Ejecuta: gameturbo-admin help"
