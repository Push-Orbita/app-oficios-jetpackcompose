# Components Catalog — Oficios Formosa Design System

> Módulo: `core:designsystem`
> Namespace: `com.cocido.oficios.core.designsystem`

---

## Tema

### `OficiosTheme`
**Archivo:** `theme/OficiosTheme.kt`

| Parámetro | Tipo | Default | Descripción |
|---|---|---|---|
| `darkTheme` | `Boolean` | `true` | Dark-first por diseño Figma |
| `content` | `@Composable () -> Unit` | — | Árbol de UI |

Configura `MaterialTheme`, maneja status bar / nav bar.

---

## Botones — `component/buttons/`

### `OficiosButton`
**Archivo:** `buttons/OficiosButton.kt`

| Parámetro | Tipo | Default |
|---|---|---|
| `text` | `String` | — |
| `onClick` | `() -> Unit` | — |
| `variant` | `OficiosButtonVariant` | `Primary` |
| `enabled` | `Boolean` | `true` |
| `loading` | `Boolean` | `false` |
| `leadingIcon` | `ImageVector?` | `null` |
| `trailingIcon` | `ImageVector?` | `null` |

**Variantes:** `Primary`, `Secondary`, `Outlined`, `Ghost`, `Danger`, `Success`

### `OficiosIconButton`
**Archivo:** `buttons/OficiosIconButton.kt`

Botón cuadrado con ícono centrado, container `surfaceContainer`, radius `small`.

---

## Inputs — `component/inputs/`

### `OficiosTextField`
**Archivo:** `inputs/OficiosTextField.kt`

Override de colores: `unfocused→outline`, `focused→primary`, `error→error`, `container→Transparent`. Height mínima 48dp.

### `OficiosPasswordField`
**Archivo:** `inputs/OficiosPasswordField.kt`

Wrapper de `OficiosTextField` + toggle visibilidad de contraseña.

### `OficiosSearchField`
**Archivo:** `inputs/OficiosSearchField.kt`

`BasicTextField` custom con ícono mic opcional + ícono search. Border `outline`.

### `OficiosDropdownField`
**Archivo:** `inputs/OficiosDropdownField.kt`

`ExposedDropdownMenuBox` usando `OficiosTextField` como anchor.

### `OficiosCheckbox`
**Archivo:** `inputs/OficiosCheckbox.kt`

Checkbox 18dp + label clicable. Color checked: `primary`.

---

## Cards — `component/cards/`

### `OficiosCard` (base)
Border 1dp `outline`, radius 16dp, bg `background`, padding 16dp. Variante clicable y seleccionada.

### `OficiosSectionCard`
Card con header: título + acción (label + callback).

### `OficiosActionCard`
Card con título, descripción, botón primario y botón secundario opcional.

### `WorkerCard`
Avatar 64dp, nombre + ícono verificado, rating con estrella ámbar, specialty muted, botones "Contactar" + "Ver perfil".

### `OrderCard`
StatusBadge + monto + descripción + metadatos (dirección, cliente, horario, pago) + acciones.

### `CategoryCard`
Ícono 32dp + label, estado selected cambia border/color a `primary`.

### `ProfileCompletionCard`
Barra de progreso `LinearProgressIndicator`, porcentaje, items faltantes, CTA.

### `CertificateCard`
Ícono `WorkspacePremium` ámbar + título + emisor + vencimiento + badge verificado.

### `WorkGalleryCard`
Imagen 4:3 placeholder + caption. Listo para Coil `AsyncImage`.

### `BenefitCard`
Ícono ámbar + título + descuento + descripción + CTA opcional.

---

## Chips — `component/chips/`

### `OficiosChip`
Chip base con colores custom. Clicable opcional.

### `OficiosFilterChip`
Estado `selected/unselected`. Muestra checkmark cuando está seleccionado.

### `OficiosStatusChip`
Mapping semántico `OrderStatus → color` sin hardcode:
- `Pending` → `tertiary`
- `Confirmed/Approved` → `OficiosColorSuccess`
- `Rejected/Cancelled` → `error`
- `InReview` → `onSurfaceVariant`
- `Completed` → `surfaceContainer`

---

## Badges — `component/badges/`

### `OficiosBadge`
Etiqueta mínima 20×20dp, radius `extraSmall`.

### `OficiosNotificationBadge`
Wrapper que superpone badge numérico en `TopEnd`. Soporta `maxCount` (default 99 → "99+").

---

## Navegación — `component/navigation/`

### `OficiosBottomNavigationBar`
`NavigationBarItem` M3 con `indicatorColor = surfaceContainer`. Border-top `outline`. Soporta `badgeCount` por ítem.

**Modelo:** `OficiosNavItem(route, label, icon, badgeCount)`

### `OficiosTopAppBar`
`CenterAlignedTopAppBar`, subtitle opcional, divider inferior opcional.

### `OficiosBackTopBar`
Wrapper de `OficiosTopAppBar` con back arrow `AutoMirrored`.

---

## Scaffold — `component/scaffold/`

### `OficiosScaffold`
Wrapper de M3 `Scaffold` + overlay de loading animado (`AnimatedVisibility` fadeIn/Out).

### `OficiosScreen`
Atajo: `OficiosScaffold` + `Box` con padding aplicado automáticamente.

---

## Estados de feedback — `component/feedback/`

### `OficiosLoadingState`
`CircularProgressIndicator` + mensaje opcional, centrado en `fillMaxSize`.

### `OficiosEmptyState`
Ícono 72dp + título + descripción + CTA opcional. Variantes: `NoOrders`, `NoWorkers`, `NoMessages`, `NoResults`, `Generic`.

### `OficiosErrorState`
Ícono error/wifi-off + título + descripción + botón "Reintentar" opcional.

### `OficiosSuccessState`
Ícono `CheckCircle` verde + título + descripción + CTA opcional.

---

## Calendario — `component/calendar/`

### `OficiosCalendarDay`
Celda 40dp ancho. Estados: `Normal`, `Today` (border primary), `Selected` (bg primary), `HasEvent` (dot tertiary), `Disabled`.

### `OficiosScheduleSlot`
Franja horaria. Estados: `Available`, `Booked` (bg primaryContainer), `Blocked` (bg errorContainer), `Past` (opacidad reducida).
**Modelo:** `ScheduleSlotData(startTime, endTime, clientName?, serviceTitle?, state)`

---

## Modelos de dominio — `component/cards/OrderStatus.kt`

```kotlin
sealed class OrderStatus {
    object Pending, Confirmed, InReview, Approved, Completed, Rejected, Cancelled
}
```

Usado por: `OrderCard`, `OficiosStatusChip`, `ScheduleSlotData`.

---

## Tokens de tema disponibles en cualquier componente

```kotlin
// Colores
MaterialTheme.colorScheme.primary          // #006DEF
MaterialTheme.colorScheme.background       // #00358B
MaterialTheme.colorScheme.outline          // #413764
MaterialTheme.colorScheme.tertiary         // #F4A426
OficiosColorSuccess                        // #4CAF7D
OficiosColorMutedText                      // #918E9B

// Spacing
OficiosSpacing.md                          // 16dp

// Radius (a través de shapes)
MaterialTheme.shapes.small                 // 8dp
MaterialTheme.shapes.large                 // 16dp

// Alpha
OficiosAlpha.disabled                      // 0.38f

// Tamaños
OficiosComponentSize.textFieldHeight       // 48dp
```
