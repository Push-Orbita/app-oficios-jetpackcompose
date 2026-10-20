# Módulo 1.1 — Rediseño visual (Design System v2)

## Contexto

Revisión del Design System base (M01) para adoptar la paleta y tipografía del handoff Figma nuevo (`design_handoff_oficios_formosa`), manteniendo un solo tema (sin variante dark/light — el handoff usa un único "Mode 1").

## Entregado

- **Paleta reescrita** en `core/designsystem/theme/Color.kt` (146 líneas modificadas):
  - Fondo `rgb(0,53,139)` (navy).
  - Outline / fill de tarjetas `rgb(0,77,168)`.
  - Texto secundario `rgb(127,154,197)`.
  - Acento dorado `rgb(248,174,26)` con stroke `rgb(228,158,27)` (CTA "ahora", validaciones).
  - Azul de acción `rgb(0,109,239)` (botones primarios).
  - "Unavailable" `rgb(0,60,154)`.
  - Alerta / dar de baja `rgb(186,99,128)` con stroke `rgb(209,151,171)`.
- Mapeo a `ColorScheme` de Material 3 (`primary` = azul acción, `background` = navy, `outline` = outline-fill).
- Ajuste de `Type.kt`: jerarquía Bold 700 (36/28/24px) para títulos/CTA, Regular 400 (16px) cuerpo. Se usa `FontFamily.Default` como sustituto (el handoff no trae archivos `.ttf/.otf` de DIN Pro/Satoshi, solo PNGs) — reemplazable después sin romper la API de `OficiosTypography`.
- Revisión de **todos** los componentes compartidos de `core/designsystem/component/` (botones, cards, chips, inputs, checkboxes, feedback, navigation, avatar, brand) contra las specs de interacción del README del handoff (radio 8dp inputs/botones, 16dp cards, sombra `0 4px 10px rgba(0,0,0,.15)`).
- Plomería de Google Maps API key: `local.properties` (gitignorado) → `manifestPlaceholders["mapsApiKey"]` en `app/build.gradle.kts` → `<meta-data android:name="com.google.android.geo.API_KEY">` en `AndroidManifest.xml`. **Sin** la dependencia `maps-compose` todavía (se agrega recién cuando se construyan pantallas de mapa, Fase 6).
- `.gitignore` reforzado (`*.hprof`, `.vscode/`, etc.) sin tocar `google-services.json` (ya trackeado intencionalmente).

## QA manual

1. Splash → Login → Home cliente con la paleta nueva, sin regresiones visuales en componentes reutilizados por otras features.
2. Recorrido visual de botones/cards/inputs/checkboxes en Auth, Onboarding y Home para confirmar que ningún consumidor rompió su contrato de API pública.

## Tests automatizados

```bash
./gradlew :core:designsystem:build :app:assembleDebug
```

Se corrió build completo (no solo del módulo) porque un cambio de tema global toca todas las features que consumen `core/designsystem`.

## Fuera de alcance

- Fuentes reales DIN Pro / Satoshi (pendiente de que el usuario consiga los archivos).
- Dependencia `maps-compose` (diferida a Fase 6).
- Variante dark/light (el diseño no la contempla).
