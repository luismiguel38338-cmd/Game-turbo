#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT_DIR"

echo "[verify.sh] Verificando estructura crítica de Game Turbo AI..."

REQUIRED_PATHS=(
  "app/build.gradle.kts"
  "app/src/main/AndroidManifest.xml"
  "app/src/main/res/values/strings.xml"
  "app/src/main/java/com/example/MainActivity.kt"
  "app/src/main/java/com/gameturbo/ai/MainEntry.kt"
  "app/src/main/java/com/gameturbo/ai/performance/SystemTelemetryReader.kt"
  "app/src/main/java/com/gameturbo/ai/security/SecurityValidator.kt"
  "gameturbo-admin/config/gameturbo.conf"
  ".github/workflows/android.yml"
)

ERRORS=0
for rel in "${REQUIRED_PATHS[@]}"; do
  if [[ ! -e "$ROOT_DIR/$rel" ]]; then
    echo "  [ERROR] Falta archivo requerido: $rel" >&2
    ERRORS=$((ERRORS + 1))
  else
    echo "  [OK] $rel"
  fi
done

echo "[verify.sh] Comprobando ausencia de dependencias o APIs de IA prohibidas..."
if grep -rnE "firebase-ai|generativeai|GEMINI_API_KEY" app/src/main/java/ >/dev/null 2>&1; then
  echo "  [ERROR] Se detectó referencia de IA en el código fuente." >&2
  ERRORS=$((ERRORS + 1))
else
  echo "  [OK] Código fuente 100% libre de SDKs o APIs de IA."
fi

if [[ "$ERRORS" -gt 0 ]]; then
  echo "[verify.sh] Verificación fallida con $ERRORS error(es)." >&2
  exit 1
fi

echo "[verify.sh] Verificación completada correctamente."
