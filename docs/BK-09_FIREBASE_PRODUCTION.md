# BK-09 — Firebase de producción

**Estado:** bloqueado por config. La app **no** activa push hasta que haya un `google-services.json` real.

## Qué hay hoy

- `app/google-services.json` es un **stub** (`project_id` = `oficios-stub`). No commitear claves reales.
- `app/build.gradle.kts` calcula `BuildConfig.FIREBASE_ENABLED`:
  - `false` si el archivo falta, no parsea, o `project_id` contiene `stub`
  - `true` solo con un `project_id` productivo
- Con `FIREBASE_ENABLED = false`:
  - `FcmDeviceTokenRegistrar.syncCurrentToken()` no pide token
  - `OficiosFirebaseMessagingService` ignora `onNewToken` / `onMessageReceived`
  - el inbox (`GET /notifications`) y el polling de detalle de pedido (12 s, PR-6) siguen funcionando

## Qué falta (DevOps / Firebase)

1. Crear el proyecto Firebase **Oficios Formosa** y la app Android `com.cocido.oficios`.
2. Descargar `google-services.json` real. **No** commitearlo si la política del equipo lo prohíbe: inyectarlo en CI.
3. Reemplazar el stub en `app/` (local o pipeline). El próximo assemble pone `FIREBASE_ENABLED = true`.
4. Probar `POST /notifications/device-token` post-login.
5. Recorrer `docs/qa/PUSH_NOTIFICATION_MATRIX.md` y el catálogo `docs/backend/FCM_EVENT_CATALOG.md`.

## Qué no cambia en backend

El registro de token y el inbox ya existen. BK-09 es **config de Firebase**, no un endpoint nuevo. Coordinar payloads `type`, `deep_link` / `deepLink`, `notificacionId` con el catálogo.

## Fallback hasta que se resuelva

| Superficie | Respaldo sin push |
|---|---|
| Inbox | polling al abrir Notificaciones |
| Detalle de pedido | polling 12 s (PR-6) |
| KYC / checklist | polling al reanudar |

Sin Firebase de prod no hay crash ni secretos extra. El código de push queda compilado y listo.
