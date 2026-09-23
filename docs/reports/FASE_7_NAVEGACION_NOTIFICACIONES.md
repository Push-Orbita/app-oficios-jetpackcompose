# Fase 7 — Navegación y notificaciones (cierre)

**Estado:** Cerrada (Android)

## Subfases

| Fase | Doc |
|------|-----|
| 7A Quick wins navegación | `FASE_7A_NAVEGACION_QUICK_WINS.md` |
| 7B Inbox notificaciones | `FASE_7B_NOTIFICACIONES_INBOX.md` |
| 7C Push genérico | Este documento (push) + `docs/qa/PUSH_NOTIFICATION_MATRIX.md` |

## 7C — Push genérico

- `OficiosFirebaseMessagingService`: canales por categoría (kyc, orders, booking, tracking)
- `PushDeepLinkMapper`: rutas pedidos, onboarding, inbox
- `PushNavigationCoordinatorImpl`: extendido para pedidos
- `MainActivity`: marca leída al abrir push con `EXTRA_NOTIFICATION_ID`
- Catálogo eventos: `docs/backend/FCM_EVENT_CATALOG.md`

## Bloqueante externo

Reemplazar stub `app/google-services.json` con proyecto Firebase productivo (`com.cocido.oficios`). Ver `docs/MODULE3_FCM_STAGING.md`.

## Gates backend

Ver `docs/backend/GATE_STATUS.md`.
