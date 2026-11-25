#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
BACKUP_DIR="$ROOT_DIR/gameturbo-admin/backups"
mkdir -p "$BACKUP_DIR"

STAMP="$(date +%Y%m%d_%H%M%S)"
ARCHIVE="$BACKUP_DIR/gameturbo_backup_${STAMP}.tar.gz"

tar -czf "$ARCHIVE" \
  --exclude=".gradle" \
  --exclude="app/build" \
  --exclude="build" \
  --exclude="gameturbo-admin/backups/*.tar.gz" \
  -C "$ROOT_DIR" \
  app gameturbo-admin gradle settings.gradle.kts build.gradle.kts gradle.properties README.md LICENSE

echo "[backup.sh] Copia de seguridad creada: $ARCHIVE"
sha256sum "$ARCHIVE"
