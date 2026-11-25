#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
VARIANT="${1:-debug}"

cd "$ROOT_DIR"

GRADLE_BIN="gradle"
if [[ -x "./gradlew" ]]; then
  GRADLE_BIN="./gradlew"
fi

case "$VARIANT" in
  debug)
    echo "[build.sh] Compilando APK Debug..."
    "$GRADLE_BIN" :app:assembleDebug
    APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
    ;;
  release)
    echo "[build.sh] Compilando APK Release..."
    if [[ -z "${KEYSTORE_PATH:-}" || -z "${STORE_PASSWORD:-}" || -z "${KEY_PASSWORD:-}" ]]; then
      echo "[build.sh] ADVERTENCIA: Variables KEYSTORE_PATH/STORE_PASSWORD/KEY_PASSWORD no definidas."
      echo "[build.sh] Se requiere un keystore protegido para firma de producción."
    fi
    "$GRADLE_BIN" :app:assembleRelease
    APK_PATH="app/build/outputs/apk/release/app-release.apk"
    ;;
  *)
    echo "[build.sh] Error: variante no válida '$VARIANT'. Usa 'debug' o 'release'." >&2
    exit 1
    ;;
esac

if [[ -f "$APK_PATH" ]]; then
  echo "[build.sh] APK generado: $APK_PATH"
  sha256sum "$APK_PATH"
else
  echo "[build.sh] Error: no se encontró el APK esperado en $APK_PATH" >&2
  exit 1
fi
