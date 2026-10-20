#!/bin/bash
# Script auxiliar para capturar screenshot del emulador/dispositivo conectado

set -e

OUTPUT_FILE="${1:-screenshot.png}"

echo "Capturando screenshot del dispositivo..."

# Verificar que hay un dispositivo conectado
if ! adb devices | grep -q "device$"; then
    echo "Error: No hay dispositivos conectados. Conecta un emulador o dispositivo físico." >&2
    exit 1
fi

# Capturar screenshot usando screencap
adb exec-out screencap -p > "$OUTPUT_FILE"

echo "Screenshot guardado en: $OUTPUT_FILE"
