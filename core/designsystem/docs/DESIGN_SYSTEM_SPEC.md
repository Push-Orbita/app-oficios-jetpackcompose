# Design System Spec — Oficios Formosa

> Fuente de verdad visual: Figma (ver FIGMA_NODE_MAPPING.md)
> Rama Git: `feature/shared/design-system-base`

---

## Colores

### Paleta primitiva

| Token | Valor | Uso |
|---|---|---|
| `OficiosColorBackground` | `#231D38` | Fondo de pantalla principal |
| `OficiosColorSurface` | `#2A2340` | Superficies secundarias |
| `OficiosColorSurfaceVariant` | `#332A4E` | Superficies terciarias / hover |
| `OficiosColorContainer` | `#413764` | Contenedores, chips, BottomNav indicator |
| `OficiosColorOutline` | `#413764` | Bordes de inputs, cards, separadores |
| `OficiosColorPrimary` | `#A32F56` | Botón primario, color de acento principal |
| `OficiosColorPrimaryBorder` | `rgba(209,151,171,0.5)` | Borde del botón primario |
| `OficiosColorWhite` | `#FFFFFF` | Texto sobre fondo oscuro |
| `OficiosColorMutedText` | `#918E9B` | Texto secundario / placeholder / metadatos |
| `OficiosColorTertiary` | `#F4A426` | Casco del logo → estrellas de rating, advertencias |
| `OficiosColorSuccess` | `#4CAF7D` | Estado confirmado / aprobado |
| `OficiosColorError` | `#CF6679` | Estado rechazado / cancelado / errores |

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
| `buttonMinHeight` | 36dp |
| `buttonHeight` | 48dp |
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
