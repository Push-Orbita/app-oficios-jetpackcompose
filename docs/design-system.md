# Design System

El design system vive en `:core:designsystem`. El tema activo es navy v2 (`OficiosTheme`), no la paleta violeta ni los nombres `App*`.

## Tema

```kotlin
OficiosTheme {
    // contenido
}
```

`OficiosTheme` configura `ColorScheme`, `OficiosTypography` y formas. El fondo de producto es `#00358B`.

## Tokens

| Token | Hex | Uso |
|---|---|---|
| `OficiosColorBackground` | `#00358B` | Fondo |
| `OficiosColorOutlineFill` | `#004DA8` | Borde de cards |
| `OficiosColorSecondaryText` | `#7F9AC5` | Texto secundario |
| `OficiosColorAction` | `#006DEF` | CTA azul (jerarquía media) |
| `OficiosColorWhite` | `#FFFFFF` | Texto y CTA de máxima énfasis |
| `OficiosColorBrandMark` | `#006AF0` | Casco del logo |
| `OficiosColorAccent` | `#F8AE1A` | Acento de UI, no el casco |
| `OficiosSpacing` | 4–64dp | Padding y gaps |
| `OficiosRadius` | 8 / 16dp | Botones y cards |
| `OficiosComponentSize` | 36 / 48dp | Alto de botón compacto y de pantalla |

Tipografía de Figma: DIN Pro y Satoshi. No están licenciadas en el repo; `Type.kt` usa `FontFamily.SansSerif` con los tamaños y pesos ya definidos. No inventar otra escala.

## Botones

`OficiosButton`:

- `Secondary`: fondo blanco, texto navy. CTA dominante.
- `Primary`: fondo `#006DEF`. Acción media.
- `Outlined`, `OutlinedDanger`, `Ghost`, `Danger`.

## Componentes

`OficiosButton`, `OficiosSearchField`, `OficiosCategoriesGrid`, `OficiosCalendarDay`, `OficiosLogo`, cards, navegación e inputs. El catálogo está en `core/designsystem/docs/COMPONENTS_CATALOG.md`.

## Reglas

- No usar hex sueltos en features: tokens `OficiosColor*`.
- No usar `AppTheme`, `AppButton` ni `AppSpacing` en código nuevo.
- El acento dorado no pinta el casco de marca.
