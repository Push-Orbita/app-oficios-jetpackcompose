# Design System

El design system vive en `:core:designsystem`. Hay **dos temas** (`OficiosTheme` + `OficiosTheme.colors`).

## Tema

```kotlin
OficiosTheme { /* claro, default */ }
OficiosTheme(darkTheme = true) { /* navy Figma Mode 1 */ }
OficiosTheme.colors.success
```

- **Claro (DEFAULT):** fondo hueso **`#F4F5F7`** (rango BBVA `#F5F5F2`–`#F4F5F7`), cards blancas, texto `#0D1B2A`. El azul de marca Figma `#00358B` solo en botones primarios, títulos destacados, links, círculos de ícono, chip/tab seleccionado e indicador del bottom nav.
- **Oscuro:** el Mode 1 actual (`#00358B` full-bleed). No se altera.

Preferencia en Ajustes → Configuración → Tema (Claro / Oscuro / Según el sistema), persistida en DataStore. Default: Claro.

## Tokens

Usar `MaterialTheme.colorScheme` y `OficiosTheme.colors`. Los primitivos `OficiosColor*` viven solo en `Color.kt` / `OficiosColors.kt` para armar los schemes.

| Token semántico | Claro | Oscuro |
|---|---|---|
| `background` | `#F4F5F7` | `#00358B` |
| `surface` | `#FFFFFF` | `#00358B` |
| `onSurface` | `#0D1B2A` | `#FFFFFF` |
| `onSurfaceVariant` | `#4A5568` | `#8EABD4` |
| `primary` | `#00358B` | `#006DEF` |
| `success` | `#146C43` | `#B7E4C7` |

Tipografía de Figma: DIN Pro y Satoshi. No están licenciadas en el repo; `Type.kt` usa `FontFamily.SansSerif` con los tamaños y pesos ya definidos. No inventar otra escala.

## Botones

`OficiosButton`:

- `Secondary`: fondo blanco, texto navy. CTA dominante.
- `Primary`: fondo `#006DEF`. Acción media.
- `Outlined`, `OutlinedDanger`, `Ghost`, `Danger`.

## Componentes

`OficiosButton`, `OficiosSearchField`, `OficiosCategoriesGrid`, `OficiosCalendarDay`, `OficiosLogo`, cards, navegación e inputs. El catálogo está en `core/designsystem/docs/COMPONENTS_CATALOG.md`.

## Reglas

- No usar hex sueltos en features: `MaterialTheme.colorScheme` / `OficiosTheme.colors`.
- No usar `AppTheme`, `AppButton` ni `AppSpacing` en código nuevo.
- El acento dorado no pinta el casco de marca.
