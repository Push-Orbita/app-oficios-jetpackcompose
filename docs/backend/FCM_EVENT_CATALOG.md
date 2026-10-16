# Catálogo de eventos FCM — Oficios Formosa

> Propuesta para cerrar **BACK-NOTIF-001**. Backend debe publicar esta tabla en OpenAPI y respetar el payload `data` en todos los envíos.

## Payload mínimo (campo `data`)

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `type` | string | sí | Identificador del evento (tabla abajo) |
| `notificacionId` | string | sí | ID numérico de la fila en `GET /notifications` |
| `title` | string | sí | Título visible |
| `body` | string | sí | Cuerpo visible |
| `deepLink` | string | no | Ruta app-relative (ej. `orders/42`) |
| `entityType` | string | no | `SOLICITUD`, `BOOKING`, `KYC`, etc. |
| `entityId` | string | no | ID del recurso relacionado |

**Prohibido en push:** teléfono completo, DNI, dirección exacta, tokens.

## Eventos v1

| type | Destinatario | deepLink sugerido | Canal Android |
|------|--------------|-------------------|---------------|
| `KYC_REVISION_DOCUMENTACION` | Trabajador | `onboarding/identidad` | `kyc_revision` |
| `NUEVA_SOLICITUD` | Trabajador | `orders/{solicitudId}` | `orders` |
| `SOLICITUD_ACEPTADA` | Cliente | `orders/{solicitudId}` | `orders` |
| `SOLICITUD_RECHAZADA` | Cliente | `orders/{solicitudId}` | `orders` |
| `CONTRAPROPUESTA_RECIBIDA` | Cliente | `orders/{solicitudId}` | `orders` |
| `CONTRAPROPUESTA_ACEPTADA` | Trabajador | `orders/{solicitudId}` | `orders` |
| `BOOKING_CONFIRMADO` | Ambos | `orders/{solicitudId}` | `booking` |
| `TRABAJADOR_EN_CAMINO` | Cliente | `orders/{solicitudId}` | `tracking` |
| `TRABAJADOR_LLEGO` | Cliente | `orders/{solicitudId}` | `tracking` |
| `SERVICIO_FINALIZADO` | Ambos | `orders/{solicitudId}` | `booking` |
| `CALIFICACION_PENDIENTE` | Cliente | `orders/rate/{bookingId}` | `orders` |

## GET /notifications — DTO propuesto

```json
{
  "data": [
    {
      "id": 1,
      "type": "NUEVA_SOLICITUD",
      "title": "Nuevo pedido",
      "body": "Un cliente solicitó un servicio.",
      "createdAt": "2026-09-02T12:00:00-03:00",
      "readAt": null,
      "leido": false,
      "entityType": "SOLICITUD",
      "entityId": 42,
      "deepLink": "orders/42"
    }
  ],
  "metadata": {
    "count": 1,
    "pageSize": 20,
    "pageNumber": 1,
    "totalPages": 1
  }
}
```

Verificar shape real con:

```powershell
.\scripts\contract\probe-notifications-api.ps1 -Email ... -Password ...
```
