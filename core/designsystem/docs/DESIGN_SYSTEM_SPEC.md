# Design System Spec — Oficios Formosa

> Fuente de verdad visual: Figma (ver FIGMA_NODE_MAPPING.md)
> Rama Git: `feature/shared/design-system-base`

---

## Colores

### Paleta primitiva

| Token | Valor | Uso |
|---|---|---|
| `OficiosColorBackground` | `#00358B` | Fondo de pantalla |
| `OficiosColorOutlineFill` | `#004DA8` | Borde e inset de cards |
| `OficiosColorSecondaryText` | `#7F9AC5` | Texto secundario e inactivo |
| `OficiosColorAction` | `#006DEF` | CTA azul, jerarquía media |
| `OficiosColorUnavailable` | `#003C9A` | Turnos no disponibles |
| `OficiosColorWhite` | `#FFFFFF` | Texto sobre navy y CTA de máxima énfasis |
| `OficiosColorBrandMark` | `#006AF0` | Casco del logo (Figma `666:2`). No usar en UI de acento |
| `OficiosColorAccent` | `#F8AE1A` | Badges “AHORA” y acentos de UI. No es el casco |
| `OficiosColorAccentBorder` | `#E49E1B` | Borde del acento |
| `OficiosColorAlert` | `#BA6380` | Peligro |
| `OficiosColorAlertText` | `#D197AB` | Texto de peligro sobre navy |

Los alias `OficiosColorPrimary`, `OficiosColorContainer`, `OficiosColorMutedText` y `OficiosColorTertiary` están deprecados y apuntan a la paleta navy. No usar la paleta violeta (`#231D38`, `#A32F56`).

### Jerarquía de botones (frames de producto, página `3:3`)

| Variante | Fill medido | Texto | Cuándo |
|---|---|---|---|
| `Secondary` (énfasis) | `#FFFFFF` | `#00358B` | CTA dominante: Aceptar, Estoy en camino, Registrarse, Contactar de login |
| `Primary` (acción) | `#006DEF` | `#FFFFFF` | Acción media: Reagendar, Contactar en card de trabajador |
| `Outlined` | transparente + borde `#004DA8` | blanco | Acción terciaria |
| `OutlinedDanger` | transparente + borde alerta | `#D197AB` | Rechazar / cancelar |
| `Ghost` | fondo | blanco | Enlaces de formulario (olvidé mi contraseña) |

Altura: los frames de CTA miden **36dp** (`210:29` Aceptar, `198:697` Estoy en camino). `Prominent` usa esa altura (`buttonMinHeight`). Radius de botón: **8dp**.

### Marca

- Logotipo (`12:101` y pantallas de producto) sigue dibujado en dorado `#F8AE1A` dentro de varios frames.
- El casco azul de la página Thumbnail es el vector `666:2`: fill `#006AF0` (rgb 0, 106, 240) y cuerpo blanco `#FFFFFF`.
- `ic_casco` del registro de rol queda en blanco: es la variante sobre navy, no el logotipo de marca.

### Esquemas M3

- **DarkColorScheme** (por defecto) — dark-first como diseñó Figma
- **LightColorScheme** — preparado, aún no refinado

### Reglas
- No usar valores hex hardcodeados en componentes → siempre `MaterialTheme.colorScheme.*` o primitivos `OficiosColor*`
- Usar `copy(alpha = OficiosAlpha.*)` para opacidades; nunca valores float literales

---

## Tipografía

Fuente de diseño: **DIN Pro** (Bold/Medium/Regular) + **Satoshi Medium** (navigation labels).
Fallback actual: `FontFamily.SansSerif`.

### Extensión futura
1. Agregar archivos `.ttf` en `res/font/`
2. Crear `FontFamily` con `FontWeight.Bold`, `Medium`, `Normal`
3. Reemplazar `FontFamily.SansSerif` en `Type.kt`

### Roles tipográficos

| Rol | Tamaño | Peso | Uso |
|---|---|---|---|
| `displayLarge` | 48sp | Bold | Splash, nombre app |
| `headlineLarge` | 36sp | Bold | Título de pantalla principal |
| `headlineMedium` | 28sp | Bold | Títulos intermedios |
| `titleLarge` | 22sp | Bold | Sección, modal title |
| `titleMedium` | 16sp | Bold | Nombre de usuario, label activo |
| `bodyLarge` | 16sp | Normal | Texto principal de contenido |
| `bodyMedium` | 14sp | Normal | Texto secundario, descripciones |
| `labelLarge` | 16sp | Medium | Texto en botones |
| `labelMedium` | 12sp | Medium | Chips, badges |
| `labelSmall` | 11sp | Normal | Caption, footnote |

---

## Espaciado

`OficiosSpacing` — usar siempre estos valores, nunca `.dp` literal:

| Token | Valor | Uso |
|---|---|---|
| `none` | 0dp | |
| `xxxs` | 2dp | Separación mínima, punto de rating |
| `xxs` | 4dp | Gap entre ícono y texto en chips |
| `xs` | 8dp | Padding interno de chips, gap estándar |
| `sm` | 12dp | Padding vertical de inputs |
| `md` | 16dp | Padding de cards, separación estándar |
| `lg` | 24dp | Separación entre secciones |
| `xl` | 32dp | Padding horizontal de screens |
| `xxl` | 48dp | Espaciado hero |
| `xxxl` | 64dp | Espaciado extralarge |

---

## Radio de esquinas

`OficiosRadius` → mapeado a `AppShapes` de M3:

| Token | Valor | Uso |
|---|---|---|
| `xs` (extraSmall) | 4dp | Badges, puntos |
| `sm` (small) | 8dp | Botones, inputs, chips, avatares |
| `md` (medium) | 12dp | Diálogos pequeños |
| `lg` (large) | 16dp | Cards principales, bottom sheets |
| `xl` (extraLarge) | 24dp | Modales grandes, paneles |
| `pill` | 100dp | Botones píldora |

---

## Elevación

`OficiosElevation`:

| Token | Valor | Uso |
|---|---|---|
| `none` | 0dp | Cards (bordes reemplazan la sombra) |
| `sm` | 2dp | Dropdown menus |
| `md` | 4dp | Botones primary (shadow) |
| `lg` | 8dp | Modales, dialogs |

---

## Alpha

`OficiosAlpha`:

| Token | Valor | Uso |
|---|---|---|
| `disabled` | 0.38f | Cualquier elemento deshabilitado |
| `medium` | 0.60f | Placeholder text |
| `high` | 0.87f | Texto secundario |
| `overlay` | 0.70f | Loading overlay |

---

## Tamaños de componentes

`OficiosComponentSize`:

| Token | Valor |
|---|---|
| `textFieldHeight` | 48dp |
| `buttonMinHeight` | 36dp (`Compact` y `Prominent`) |
| `buttonHeight` | 48dp (filas e íconos, no el CTA) |
| `iconSm` | 16dp |
| `iconMd` | 24dp |
| `iconLg` | 32dp |
| `checkboxSize` | 18dp |
| `bottomBarHeight` | 64dp |
| `topBarHeight` | 56dp |
| `avatarSm` | 48dp |
| `avatarMd` | 64dp |

---

## Reglas de diseño

### QUÉ NO HACER
- No usar `Color(0xFF...)` hardcodeado en componentes
- No usar `.dp` literales para spacing — usar `OficiosSpacing.*`
- No crear nuevos colores primitivos sin actualizar este spec y Figma
- No modificar `OficiosTheme.kt` para casos de pantalla específica — extender con parámetros
- No usar `AppTheme` (deprecated) en código nuevo

### CÓMO EXTENDER
- Agregar primitivo en `Color.kt`
- Agregarlo a `DarkColorScheme` y `LightColorScheme` si corresponde al sistema M3
- Documentar en esta spec y en `FIGMA_NODE_MAPPING.md`
- Crear un Preview en el componente

---

## Sistema de componentes

Ver `COMPONENTS_CATALOG.md` para la lista completa con parámetros y variantes.

---

## Verificación

```bash
./gradlew :core:designsystem:assembleDebug
./gradlew :app:assembleDebug
```

Ambos deben compilar sin errores. Previews verificables desde Android Studio (Design panel).
