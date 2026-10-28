# BK-21 — Medio de pago en el pedido

**Pantalla Figma:** Pedido Confirmado `613:572` (Flujos). Fila Monto + chip **Efectivo**.

## Qué falta en la API de producción

`SolicitudServicioDto` / `BookingSimpleDto` no exponen medio de pago (`EFECTIVO`, etc.). El único monto mapeable es `nuevoPrecio` de una contrapropuesta.

## Impacto en la app

- **Monto** se muestra solo si hay `nuevoPrecio` pendiente o aceptado.
- El chip **Efectivo** no se pinta. No se inventa un medio de pago.

## Pedido

1. Exponer `medioPago` (o reutilizar `metodosPago` del oficio) en GET solicitud/booking.
2. Si el monto acordado vive fuera de la contrapropuesta, exponerlo en el booking.
