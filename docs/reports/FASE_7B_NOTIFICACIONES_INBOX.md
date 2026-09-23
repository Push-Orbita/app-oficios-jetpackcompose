# Fase 7B — Centro de notificaciones (inbox)

**Estado:** Cerrada (Android) — gate BACK-NOTIF-001 schema OpenAPI **abierto**

## Capas implementadas

| Capa | Archivos |
|------|----------|
| Network | `NotificationsApi.kt`, `NotificationDtos.kt` |
| Domain | `NotificationModels.kt`, `NotificationsRepository` |
| Data | `NotificationsRepositoryImpl`, `NotificationMapper.kt` |
| Use cases | `GetNotificationsUseCase`, `GetUnreadNotificationCountUseCase` |
| UI | `feature/home/notifications/*` |
| Badge campana | `ClientHomeHeader`, `ProviderHomeHeader` |
| Deep links | `PushDeepLinkMapper` en `core/common` |

## Navegación

- Ruta `notifications` en NavHost
- Campana reemplaza `ShowComingSoon` en ViewModels de home

## Verificación contrato

```powershell
.\scripts\contract\verify-fase7-backend.ps1   # exit 2 si falta schema 200
.\scripts\contract\probe-notifications-api.ps1 -Email ... -Password ...
```

## Tests

- Instrumentado: `NotificationsScreenTest` (loading/empty)

## Pendiente backend

Publicar schema de respuesta paginada en OpenAPI para tipado estable sin inferencia del probe.
