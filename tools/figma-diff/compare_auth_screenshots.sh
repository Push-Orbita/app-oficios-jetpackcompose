#!/usr/bin/env bash
# Compara capturas Roborazzi de feature/auth contra PNG locales de Figma.
# Los PNG de referencia no se commitean; busca en screens/flujos o FIGMA_REFS_DIR.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
CAPTURE_DIR="${CAPTURE_DIR:-$ROOT/feature/auth/build/outputs/roborazzi}"
REFS_DIR="${FIGMA_REFS_DIR:-$ROOT/screens/flujos}"
OUT_DIR="${FIGMA_DIFF_OUT:-$ROOT/qa-artifacts/figma-diff}"
THRESHOLD="${THRESHOLD:-2.0}"

mkdir -p "$OUT_DIR"

if [[ ! -d "$CAPTURE_DIR" ]]; then
  echo "No hay capturas en $CAPTURE_DIR. Corré:"
  echo "  ./gradlew :feature:auth:testDebugUnitTest --tests com.cocido.oficios.feature.auth.screenshot.AuthScreenshotTest"
  exit 1
fi

compare() {
  local name="$1"
  local capture_glob="$2"
  local ref_name="$3"
  local capture
  capture="$(find "$CAPTURE_DIR" -name "*${capture_glob}" | head -n 1 || true)"
  local ref="$REFS_DIR/$ref_name"
  if [[ -z "$capture" || ! -f "$capture" ]]; then
    echo "SKIP $name: falta captura ($capture_glob)"
    return 0
  fi
  if [[ ! -f "$ref" ]]; then
    echo "SKIP $name: falta referencia $ref"
    return 0
  fi
  echo "=== $name ==="
  python3 "$ROOT/tools/figma-diff/figma_diff.py" \
    "$ref" \
    "$capture" \
    --output "$OUT_DIR/${name}_diff.png" \
    --threshold "$THRESHOLD" \
    --no-crop || true
}

compare splash "AuthScreenshotTest.splash.png" "01-oficios-formosa.png"
compare login "AuthScreenshotTest.login.png" "02-ingrese-sus-datos-para-iniciar-sesion.png"
compare login_error "AuthScreenshotTest.loginError.png" "04-los-datos-de-sesion-no-coinciden.png"
compare role "AuthScreenshotTest.role.png" "13-elegi-como-registrarte.png"
compare register "AuthScreenshotTest.register.png" "07-registro-de-usuario.png"
