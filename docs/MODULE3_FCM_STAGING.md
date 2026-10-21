# Módulo 3 — FCM (staging y producción)

## Staging / desarrollo

No hay `google-services.json` real en el repo para producción. La app incluye un **stub** en `app/google-services.json` para compilar.

**Firebase opcional (v1.0):** La app detecta automáticamente si `google-services.json` es stub (contiene `"oficios-stub"` en `project_id`). Cuando es stub:
- `BuildConfig.FIREBASE_ENABLED = false`
- `FcmDeviceTokenRegistrar.syncCurrentToken()` retorna inmediatamente sin intentar obtener token
- La app funciona normalmente sin push — útil para desarrollo local sin Firebase configurado

La app usa **polling al reanudar** el checklist proveedor como respaldo si FCM falla o no está disponible.

## v1 sin Firebase (no bloquea release)

Push notifications no funcionarán en dispositivo hasta que DevOps provea `google-services.json` real. Sin embargo:
- **Inbox polling funciona**: el usuario ve notificaciones al abrir la app
- **No hay crash**: la app detecta el stub y desactiva registro de token
- Cuando se reemplace el stub por config real, push funcionará sin cambios de código

Checklist de producción: [BK-09_FIREBASE_PRODUCTION.md](BK-09_FIREBASE_PRODUCTION.md).

## Producción — checklist DevOps

1. Crear proyecto Firebase **Oficios Formosa** y app Android `com.cocido.oficios`.
2. Descargar `google-services.json` y colocarlo en `app/` (no commitear secretos sensibles si la política del equipo lo prohíbe — usar CI secret).
3. Verificar plugin Google Services en `app/build.gradle.kts`.
4. Probar registro token: `POST /notifications/device-token` post-login.
5. Validar matriz manual: `docs/qa/PUSH_NOTIFICATION_MATRIX.md`.

## Para habilitar push KYC (staging)

1. Crear proyecto Firebase staging y agregar app Android `com.cocido.oficios`.
2. Colocar `google-services.json` en `app/`.
3. Implementar registro de token: `POST /notifications/device-token` (ya implementado en `FcmDeviceTokenRegistrar`).

## Catálogo de eventos

Ver `docs/backend/FCM_EVENT_CATALOG.md` — coordinar con backend para payloads `type`, `deep_link`, `notificacionId`.

## Cuentas staging recomendadas

- Cliente de prueba en `apioficios.pushsoftware.com.ar`
- Proveedor de prueba con flujo DNI pendiente → aprobado (admin)

