# Fase 7A — Navegación quick wins

**Rama:** `codex/fase-7-navegacion-notificaciones`  
**Estado:** Cerrada (Android)

## Entregables

| Ítem | Ubicación |
|------|-----------|
| Navigator unificado tabs | `feature/home/common/ClientHomeTabNavigator.kt` |
| Pantalla Ayuda | `feature/home/help/HelpRoute.kt` |
| Pantalla Favoritos (empty) | `feature/home/favorites/FavoritesRoute.kt` |
| Rutas compartidas | `SharedHomeDestination`, `SharedHomeNavigation` |
| Tab Perfil → datos personales | `settings/personal-data` vía `navigateToPersonalData()` |
| Wiring cliente + proveedor | `HomeRoute`, `ProviderHomeRoute` |
| Smoke E2E actualizado | `scripts/e2e/smoke-app.ps1` |

## Tests

- Unit: `ClientHomeTabNavigatorTest`
- Instrumentado: `HelpScreenTest`, `FavoritesScreenTest`

## Decisión de producto

Tab **Favoritos** visible con `OficiosEmptyState` explicando función futura (sin ocultar tab).

## Regresiones evitadas

- Tabs Perfil/Ayuda/Favoritos ya no muestran Toast "Próximamente"
- Bottom nav unificado entre home cliente, categorías y home proveedor
