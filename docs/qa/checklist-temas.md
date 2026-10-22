# Checklist temas — claro (default) / oscuro (Figma)

**Rama / PR:** `cursor/qa-fixes-figma-ee07` · PR #22  
**Tema claro:** fondo hueso `#F4F5F7`, cards blancas, texto navy/casi negro, azul de marca `#00358B` solo en CTA, títulos destacados, links, círculos de ícono, chip/tab seleccionado e indicador del bottom nav.  
**Tema oscuro:** Mode 1 actual de Figma (`#00358B` full).  
**Selector:** Ajustes → Apariencia → Claro (default) / Oscuro / Según el sistema (DataStore, sin restart, sin flash).  
**Roborazzi:** opt-in `-Proborazzi.test.record=true`; sin golden el test se omite. Gate JVM: `assembleDebug`, `testDebugUnitTest`, `lintDebug`, `:domain:test`.

| Pantalla | Claro | Oscuro | Cómo se revisó |
|---|---|---|---|
| Splash | OK | OK | Tokens + `splash` / `splashDark` + `values` / `values-night` |
| Login | OK | OK | Tokens + `login` / `loginDark` |
| Login error | OK | OK | Tokens + fixture `darkTheme` (banner error) |
| Registro / rol | OK | OK | Tokens + `register` / `registerDark` + `role` / `roleDark` |
| Onboarding / forgot | OK | OK | Tokens semánticos (`AuthScreenLayout` / `OficiosScaffold`) |
| Home cliente | OK | OK | Tokens + `home` / `homeDark` |
| Home trabajador (Beneficios, Agenda, Completa tu perfil) | OK | OK | Tokens + `providerHome` / `providerHomeDark` |
| Buscador / resultados | OK | OK | Tokens + `results` / `resultsDark` |
| Ficha pública | OK | OK | Tokens + `ficha` / `fichaDark` + mosaico/visor |
| Solicitud de reserva | OK | OK | Tokens + `createOrder` / `createOrderDark` |
| Mis pedidos | OK | OK | Tokens + `ordersList` / `ordersListDark` |
| Detalle pedido / contrapropuesta | OK | OK | Tokens + `orderDetail` / `orderDetailDark` |
| Calificación | OK | OK | Tokens (`OficiosTheme.colors` / `colorScheme`) |
| Ajustes + Apariencia | OK | OK | Tokens + `settingsAppearance` / `settingsAppearanceDark` |
| Editar perfil / portfolio (galerías) | OK | OK | Tokens + `editProfile` / `editProfileDark` + mosaico |
| Mapas (ubicación local / domicilios) | OK | OK | Tokens + camera gating ola 1 (sin Maps factory antes de `onMapLoaded`) |
| Diálogos, toasts, bottom sheets, cropper | OK | OK | Tokens DS (`OficiosDialog`, alerts, sheets) |
| Empty / loading / error | OK | OK | Tokens (`OficiosEmptyState` / `OficiosLoadingState` / `OficiosErrorState`) |
| Visor de galería | N/A | N/A | Fondo negro fijo (visor fullscreen) |

Logos: claro usa variante con contorno navy; oscuro la actual. Launcher icon no cambia. Íconos de system bars: oscuros en claro, claros en oscuro (`SystemBarStyle`).
