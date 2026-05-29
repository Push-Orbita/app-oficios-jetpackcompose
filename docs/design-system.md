# Design System

El Design System vive en `:core:designsystem` y está basado en **Material 3** con tokens propios para mantener consistencia y facilitar white-labeling.

## Tema

```kotlin
AppTheme(darkTheme = isDark) {
    // contenido
}
```

`AppTheme` configura:

- `ColorScheme` claro y oscuro
- `AppTypography`
- `AppShapes`

El modo oscuro se controla desde Settings vía DataStore y se aplica en `:app` (`OficiosRoot`).

## Tokens

| Token | Uso |
|-------|-----|
| `AppSpacing` | Padding, gaps (`xs` → `xxxl`) |
| `AppDimens` | Alturas, iconos, max width |
| `AppElevation` | Elevación de cards/superficies |
| `Color.kt` | Paleta semántica (primary, surface, error…) |

### Reglas

- **No** usar `Color(0xFF...)` en pantallas
- **No** repetir `16.dp` — usar `AppSpacing.lg`
- Preferir `MaterialTheme.colorScheme.*` en componentes

## Componentes base

| Componente | Descripción |
|------------|-------------|
| `AppButton` | Botón filled primario |
| `AppOutlinedButton` | Botón outlined |
| `AppTextButton` | Botón texto |
| `AppTextField` | Campo de texto |
| `AppPasswordField` | Campo password |
| `AppTopBar` | Top bar centrada |
| `AppScaffold` | Scaffold estándar |
| `AppCard` | Card con padding interno |
| `AppLoading` | Estado de carga |
| `AppErrorState` | Error con retry |
| `AppEmptyState` | Estado vacío |
| `AppDialog` | Diálogo de confirmación |
| `AppSnackbarHost` | Host de snackbars |
| `AppIconButton` | Icon button |

## Agregar un componente

1. Crear archivo en `core/designsystem/component/`.
2. Usar tokens del tema (`MaterialTheme`, `AppSpacing`, etc.).
3. Mantener el composable **stateless** — recibe datos y callbacks.
4. Agregar `@Preview` si aporta valor.
5. (Opcional) Agregar test en `src/androidTest`.

Ejemplo mínimo:

```kotlin
@Composable
fun AppBadge(
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = AppSpacing.sm,
                vertical = AppSpacing.xs,
            ),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}
```

## `:core:ui` vs `:core:designsystem`

| Módulo | Contenido |
|--------|-----------|
| `designsystem` | Tokens + componentes atómicos del DS |
| `ui` | Composiciones reutilizables (layouts, containers) que usan el DS |

Ejemplo en `ui`: `CenterContent` — layout con max width, no un token de diseño.

## Dark mode

Paleta definida explícitamente en `Color.kt` (light + dark). No se usa dynamic color para que el starter sea predecible en cualquier app derivada.

## Previews

Agregar previews en componentes del DS:

```kotlin
@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AppButtonPreview() {
    AppTheme {
        AppButton(text = "Continue", onClick = {})
    }
}
```
