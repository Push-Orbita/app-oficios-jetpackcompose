# Matriz QA — Push notifications (Fase 7C)

> Probar en dispositivo real con `google-services.json` productivo. Emulador con Google Play + FCM staging opcional.

## Canales Android

| Canal ID | Categoría | Importance |
|----------|-----------|------------|
| `kyc_revision` | KYC / documentación | HIGH |
| `orders` | Solicitudes y pedidos | DEFAULT |
| `booking` | Citas y finalización | DEFAULT |
| `tracking` | En camino / llegada | HIGH |

## Matriz type → deep link → pantalla

| FCM `type` | Canal | `deep_link` ejemplo | Ruta interna |
|------------|-------|---------------------|--------------|
| `KYC_REVISION_DOCUMENTACION` | kyc_revision | `oficios://onboarding/kyc` | Onboarding KYC |
| `ORDER_NEW_REQUEST` | orders | `oficios://orders/{id}` | Detalle pedido |
| `ORDER_ACCEPTED` | orders | `oficios://orders/{id}` | Detalle pedido |
| `ORDER_COUNTER_OFFER` | orders | `oficios://orders/{id}` | Detalle pedido |
| `BOOKING_EN_CAMINO` | tracking | `oficios://orders/{id}` | Detalle pedido + mapa |
| `BOOKING_EN_CURSO` | booking | `oficios://orders/{id}` | Detalle + código cliente |
| `BOOKING_FINALIZADA` | booking | `oficios://orders/{id}` | Detalle + calificar |
| `NOTIFICATION_GENERIC` | orders | `oficios://notifications` | Inbox |

Mapper: `core/common/navigation/PushDeepLinkMapper.kt`

## Checklist manual

- [ ] App en foreground: tap en notificación navega al destino correcto
- [ ] App en background: tap abre `MainActivity` y navega
- [ ] App cerrada: cold start con deep link
- [ ] `notificacionId` en payload: al abrir, se marca leída (`MainActivity`)
- [ ] Token FCM se registra post-login (`FcmDeviceTokenRegistrar`)
- [ ] Permiso POST_NOTIFICATIONS (API 33+)

## Payload mínimo esperado

```json
{
  "type": "ORDER_NEW_REQUEST",
  "title": "Nueva solicitud",
  "body": "Tenés un pedido pendiente",
  "deep_link": "oficios://orders/123",
  "notificacionId": "456"
}
```

Ver catálogo completo: `docs/backend/FCM_EVENT_CATALOG.md`
