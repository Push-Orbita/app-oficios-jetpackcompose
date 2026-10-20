# Herramienta de Diff de Screenshots vs Figma

Esta herramienta permite comparar capturas de pantalla del emulador/dispositivo con los diseños de referencia de Figma para validar la fidelidad 1:1 de la implementación.

## Requisitos

- Python 3.7+
- Pillow (PIL Fork): `pip install Pillow`
- ADB (Android Debug Bridge) para capturas desde dispositivos

## Instalación

```bash
# Instalar dependencias de Python
pip install Pillow
```

## Uso

### 1. Capturar screenshot del dispositivo

Asegúrate de tener un emulador ejecutándose o un dispositivo físico conectado:

```bash
# Capturar screenshot automáticamente
./tools/figma-diff/capture_screenshot.sh mi_captura.png

# O manualmente con adb
adb exec-out screencap -p > screenshot.png
```

### 2. Comparar con referencia de Figma

```bash
python tools/figma-diff/figma_diff.py \
    <referencia_figma.png> \
    <screenshot_capturado.png> \
    --output diff_output.png \
    --threshold 2.0
```

**Parámetros:**

- `figma_png`: Archivo PNG de referencia exportado de Figma (debe ser de 440x980px idealmente)
- `screenshot_png`: Screenshot capturado del dispositivo/emulador
- `--output` / `-o`: (Opcional) Archivo donde guardar la imagen de diferencia con overlay
- `--threshold` / `-t`: (Opcional) Umbral de diferencia en porcentaje (default: 2.0%)
- `--no-crop`: (Opcional) No recortar barra de estado ni indicador de navegación

### 3. Interpretar resultados

La herramienta reporta:

- **Porcentaje de píxeles diferentes**: Un valor entre 0-100%
- **PASS/FAIL**: Basado en el umbral configurado (default 2%)
- **Imagen de diferencia**: Los píxeles diferentes se marcan en rojo sobre el screenshot

**Códigos de salida:**

- `0`: PASS - Las imágenes son suficientemente similares
- `1`: FAIL - Las imágenes difieren más del umbral o hubo un error

## Workflow típico

### Comparar pantalla de Login

```bash
# 1. Navegar a la pantalla de login en el emulador
# 2. Capturar
./tools/figma-diff/capture_screenshot.sh login_actual.png

# 3. Comparar con referencia de Figma
python tools/figma-diff/figma_diff.py \
    design-baselines/screens/flujos/02-ingrese-sus-datos-para-iniciar-sesion.png \
    login_actual.png \
    --output diff_login.png
```

### Uso en CI

```bash
# Script de ejemplo para CI (verificar todas las pantallas críticas)
#!/bin/bash
set -e

BASELINES_DIR="design-baselines/screens"
SCREENSHOTS_DIR="test-screenshots"

# Capturar screenshots de las pantallas implementadas
# (esto requeriría automatización con UI tests)

# Comparar cada screenshot
for baseline in $BASELINES_DIR/**/*.png; do
    screen_name=$(basename "$baseline" .png)
    screenshot="$SCREENSHOTS_DIR/${screen_name}.png"
    
    if [ -f "$screenshot" ]; then
        echo "Verificando $screen_name..."
        python tools/figma-diff/figma_diff.py \
            "$baseline" \
            "$screenshot" \
            --output "diff-results/${screen_name}_diff.png" \
            || echo "FAIL: $screen_name"
    fi
done
```

## Notas técnicas

### Normalización de imágenes

La herramienta automáticamente:

1. **Normaliza ambas imágenes a 440x980px** (dimensiones del frame de Figma)
   - Mantiene aspect ratio
   - Centra la imagen si no coincide exactamente

2. **Recorta el chrome del sistema** (puede deshabilitarse con `--no-crop`)
   - Barra de estado superior: 48px
   - Indicador de navegación inferior: 24px
   - Esto permite comparar solo el contenido de la app

3. **Calcula diferencia píxel por píxel**
   - Usa un umbral de sensibilidad de 10/255 para tolerar pequeñas variaciones de antialiasing
   - Reporta el porcentaje total de píxeles que difieren

### Umbrales recomendados

- **≤ 1%**: Prácticamente idéntico (mismo diseño, posibles diferencias de rendering)
- **1-2%**: Muy similar (pequeñas diferencias de fuente/spacing aceptables)
- **2-5%**: Similar (diferencias visuales menores pero notorias)
- **> 5%**: Diferente (requiere revisión manual)

El umbral por defecto de **2%** es apropiado para verificar fidelidad 1:1 tolerando pequeñas variaciones de rendering entre Figma y Android.

## Limitaciones conocidas

- **Fuentes**: Si no se usa la fuente exacta de Figma (DIN Pro), habrá diferencias en el rendering de texto
- **Antialiasing**: Pequeñas diferencias de suavizado pueden sumar al porcentaje
- **Contenido dinámico**: Imágenes de usuario, fechas, nombres variables aumentarán la diferencia
- **Animaciones**: Capturar en estados intermedios de animación causará diferencias

Para pantallas con contenido dinámico, considera:
- Usar datos de prueba fijos
- Capturar en estados estables (sin animaciones en curso)
- Aumentar el umbral o usar máscaras para regiones dinámicas

## Futuras mejoras

- [ ] Integración con Paparazzi/Roborazzi para screenshots automatizados
- [ ] Máscaras de regiones ignorables (contenido dinámico)
- [ ] Comparación por regiones (header, body, footer)
- [ ] Generación de reportes HTML con side-by-side
- [ ] Integración con GitHub Actions para PRs
