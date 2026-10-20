# Tokens de diseño: Figma "Oficios" (hhKrcrusbr3h6qJxqYBiTd)

Fuentes:
- `get_variable_defs` en los frames 25:95, 16:245, 40:309, 198:697, 239:475, 455:27, 191:11, 356:343 y 613:1373.
- Estadística de clases sobre 26 respuestas de `get_design_context` (en `ctx/*.txt`; esquemas en `ctx/*.outline`).

> Hay un error de acceso que se reporta tal cual. Llamar `get_variable_defs` sobre la página 3:3 devolvió: "You currently have nothing selected. You need to select a layer first before using this tool. Figma Debug UUID: dd55f3cc-cd35-40d8-8466-dc71c73323cb". Por eso se llamó sobre frames.

## 1. Variables de color (las únicas variables definidas en el archivo)
| Variable Figma | Hex | Uso | App (`core/designsystem/theme/Color.kt`) | Estado |
|---|---|---|---|---|
| `Background` | `#00358B` | Fondo de todas las pantallas | `OficiosColorBackground` 0xFF00358B | ✅ |
| `Outlines & Fill` | `#004DA8` | Bordes de 1 px, fondo de icon-box, chip seleccionado, indicador de la nav | `OficiosColorOutlineFill` 0xFF004DA8 | ✅ |
| `Secondary` | `#006DEF` | CTA azul (Contactar, Ingresar, Reagendar), día seleccionado | `OficiosColorAction` 0xFF006DEF | ✅ |
| `Secondary Text Color` | `#7F9AC5` | Textos secundarios (valores de fila, especialidad) | `OficiosColorSecondaryText` **0xFF8EABD4** | ⚠️ La app lo aclaró a propósito por AA (4.7:1). Para 1:1 → `#7F9AC5` (≈3.9:1, AA solo para texto grande). **Decisión de Matías** |
| `Buttons & Texts` | `#FFFFFF` | Texto principal y botón blanco | `OficiosColorWhite` | ✅ |

### Colores sueltos (hardcodeados en Figma, sin variable)
| Hex | Dónde | App |
|---|---|---|
| `rgba(255,255,255,0.5)` | Placeholders | `OficiosColorPlaceholder` ✅ |
| `#E2E2E2` | Borde del botón blanco | En `OficiosButton` Secondary ✅ |
| `#918E9B` | Días fuera de mes/deshabilitados en el calendario y textos apagados del sheet Reagendar | ❌ falta (`OficiosColorDisabledText`) |
| `#231D38` | Celda de hora deshabilitada (468:1657) | ❌ es del tema violeta viejo. Probable resto de diseño: confirmar con diseño |
| `#A32F56` | Borde de input con error (356:343) | ❌ la app usa `#BA6380` (`OficiosColorAlert`). Para 1:1 → token `OficiosColorErrorBorder = #A32F56` |
| `#BA6380` | Borde del círculo "hoy" en el sheet Reagendar | `OficiosColorAlert` ✅ |
| `rgba(209,151,171,0.5)` + sombra `rgba(163,47,86,0.15)` | Borde y sombra del CTA azul (Contactar) | ❌ la app pone el borde transparente y la sombra negra 15 % (`DESIGN_ALIGNMENT.md`). Es un resto del tema violeta: **confirmar con diseño** antes de copiarlo |
| `rgba(0,0,0,0.25)` 0/4/2 | drop-shadow del contenedor de contenido | No aplica en Compose (sin efecto visible sobre fondo liso) |

## 2. Tipografía
Fuentes usadas (cantidad de nodos):

| Fuente | Nodos |
|---|---|
| DIN Pro Regular | 460 |
| DIN Pro Bold | 203 |
| Satoshi Medium (labels de la bottom nav) | 78 |
| DIN Pro Medium (botones) | 46 |
| Roboto Medium 14 (status bar, se ignora) | 25 |

`line-height: normal` y `letter-spacing: 0` en todos los textos.

**La app no tiene DIN Pro ni Satoshi: usa `FontFamily.SansSerif`** (`Type.kt`). Es la diferencia visual número 1.

| Rol Figma | Fuente / tamaño | Ejemplo | Token Compose propuesto | Hoy en la app |
|---|---|---|---|---|
| Wordmark | DIN Pro Bold 48 | "Oficios Formosa" (login/splash) | `displayLarge` 48/normal | 48/56 SansSerif |
| Título de pantalla | DIN Pro Bold 36 | "Recientes", "Pedido Confirmado", "Calendario general" | `headlineLarge` 36 | 36/44 ✅ tamaño, ❌ fuente. **Pero las pantallas usan TopAppBar `titleMedium` 16** |
| Título de sección | DIN Pro Bold 32 | "Trabajos realizados", "Certificado(s)" | `headlineMedium` **32** | 28 ❌ |
| Label / nombre / título de fila | DIN Pro Bold 16 | "Detalle:", "Emanuel Molina" | `titleMedium` 16, ls 0 | 16/24, ls 0.15 ❌ |
| Body | DIN Pro Regular 16 | Todo el cuerpo y los placeholders | `bodyLarge` 16, ls 0 | 16/24, ls 0.5 ❌ |
| Botón | DIN Pro Medium 16 | Contactar, Ingresar | `labelLarge` 16 | 16/20 ✅ (fuente ❌) |
| Label de la nav | Satoshi Medium 16 | Inicio, Perfil… | `labelNav` 16 Satoshi | `labelMedium` **12** ❌ |
| Status bar | Roboto 14 | — | n/a | n/a |

Figma **no usa** 12, 14 (salvo la status bar), 20, 22, 24, 28 ni 40. Las escalas `headlineSmall`, `titleLarge`, `bodyMedium`, `bodySmall` y `displayMedium` de la app no tienen equivalente: hay que llevar sus usos a 16 o, donde haga falta jerarquía, a 32/36.

Line-height "normal" de DIN Pro ≈ 1.2 × tamaño. Para el 1:1: `lineHeight = 19.sp` en 16, `43.sp` en 36, `38.sp` en 32 y `58.sp` en 48, con `LineHeightStyle(trim = None)` e `includeFontPadding = false`.

## 3. Espaciado (cantidad de usos)
- gap: 8 (115), 10 (112), 16 (111), 32 (22), 4 (10), 6 (8), 24 (2).
- padding: p-8 (110), p-16 (96), py-10 (57), py-8 (54), p-10 (54), px-12 (50), py-12 (31), px-16 (29), px-20/px-32 (25).
- **Contenedor de pantalla: `pt-16 px-32 pb-32` con `gap-16`.** La app usa `horizontal = 16` y `vertical = 24` con `spacedBy(24)` (ver HomeScreen). ❌
- Nav inferior: `p-16 gap-16` y border-top de 1 px `#004DA8`. Items `gap-8`, icon-box `p-8` r8.
- Botón: `px-12 py-8` → alto ≈ 35 dp. La app tiene `buttonHeightSmall = 36` ✅ (Medium 44 y Large 52 no existen en Figma).
- Input: `h-48 px-16 py-12`, r8, borde 1 px. La app tiene `textFieldHeight = 56` ❌ → 48.
- Falta en `OficiosSpacing`: **10 dp** (muy usado) y **6 dp**. Proponer `OficiosSpacing.s10` y `s6`.

## 4. Radios
| Radio | Usos | Dónde |
|---|---|---|
| 8 | 290 | Botones, inputs, icon-box, avatar 64 y 48, indicador de la nav |
| 16 | 160 | Cards, contenedores de sección, frame |
| 7 | 60 | Celdas del calendario y chips de hora (≈ 8 → usar 8) |
| 12 | 25 | Handle |
| 100 | 23 | Circulares / pill |
| 2 | 23 | Checkbox |
| 32 | 12 | Día seleccionado del calendario (círculo de 32) |

`OficiosRadius` tiene 4/8/12/16/24/100, pero la nav usa `pill` (Figma 8) y el avatar `OficiosAvatar` es circular (Figma: cuadrado r8). ❌

## 5. Tamaños de componentes
- Status bar 48 (sistema).
- Header avatar 48 r8 + nombre Bold 16 + `lucide/badge-check` 24.
- Campana 48×48 con fondo `#004DA8` y r8.
- Icon-box de fila: 40 (24 + p8), fondo `#004DA8`, r8.
- Card de categoría/rubro: cuadrada (aspect 1), p16, r16, borde 1 px, ilustración de 44–100 px.
- Avatar de la card de trabajador: 64 r8 con borde. Estrellas `lucide/star` 24, ×5 (+ `star-half`).
- Mapa del pedido: 376×248, r16, borde 1 px.
- Calendario: celdas de 32, día seleccionado `#006DEF` círculo de 32, día con evento `#004DA8` r7.
- Chips de hora: h32, ancho flex (4 columnas), r8. Seleccionado: fondo `#004DA8` + Bold.
- Handle de navegación 140×4 (sistema).

## 6. Iconos
Figma usa **Lucide**:

| Icono | Usos |
|---|---|
| `star` | 34 |
| `chevron-right` | 25 |
| `chevron-left` | 18 |
| `user` | 18 |
| `heart` | 17 |
| `house` | 15 |
| `circle-question-mark` | 15 |
| `menu` | 15 |
| `chevron-down` | 13 |
| `search` | 7 |
| `clock` | 6 |
| `shield-check` | 6 |
| `bell-dot` | 5 |
| `mic` | 5 |
| `circle-alert` | 5 |
| `receipt-text` | 4 |
| `map-pin-house` | 4 |
| `circle-plus` | 4 |
| `badge-check` | 3 |
| `dollar-sign` | 3 |
| `banknote` | 3 |
| `circle-dashed` | 3 |
| `eye`/`eye-closed` | 3 |
| `x` | 2 |
| `circle-check` | 2 |
| `users` | 2 |
| `scroll-text` | 2 |
| `redo` | 1 |
| `clock-check` | 1 |
| `pencil` | 1 |
| `asterisk` | 1 |
| `book-user` | 1 |
| `hammer` | 1 |

La app usa **Material Icons Extended (155 referencias)** ❌. Propuesta:
- importar los ~35 SVG de Lucide como VectorDrawable en `core/designsystem/res/drawable/ic_lucide_*.xml` (licencia ISC, compatible);
- exponerlos como `OficiosIcons.*`.

Las ilustraciones de categoría y rubro (casa/escudo, herramientas, pala, llave, taladro, etc.) son assets propios del Figma: exportar SVG o PNG con `get_design_context` (assets) o `download_assets`.
