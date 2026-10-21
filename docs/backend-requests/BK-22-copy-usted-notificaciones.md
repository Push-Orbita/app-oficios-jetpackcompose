# BK-22 — Copy de notificaciones en vos

**Pantalla:** Notificaciones.
**App:** no cambia los textos de cada item.

## Hallazgo

Los títulos y cuerpos de `GET /notifications` llegan en **vos** (“tu pedido”, “aceptaste”, etc.). El resto de la app habla de **usted**.

La app no reescribe esos strings: el payload es la fuente. Si el `type` viene vacío, el ícono queda en campana.

## Pedido

Unificar el copy de notificaciones (título y cuerpo, FCM y persistidas) a **usted**, alineado al resto de Oficios.
