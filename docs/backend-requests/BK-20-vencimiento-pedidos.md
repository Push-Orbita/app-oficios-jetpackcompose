# BK-20 — Auto-expiración de pedidos vencidos

**Pantalla:** Mis pedidos (cliente y trabajador), pestaña Activos.
**Captura QA (21 oct 2026):** Activos mostraba Plomería del **30 de septiembre 10:00** y del **5 de octubre 12:00** (Av. 25 de Mayo 450).

## Hallazgo

El listado sale de un único GET:

- Cliente: `GET /solicitud-servicio/mis-solicitudes-cliente`
- Trabajador: `GET /solicitud-servicio/search`

No hay pestaña Activos/Historial en el backend. La app parte por estado.

Estados reales (OpenAPI):

| Entidad | Enum |
|---|---|
| Solicitud | `PENDIENTE`, `CONTRAPROPUESTA`, `ACEPTADA`, `CANCELADA`, `EXPIRADA` |
| Booking | `PENDIENTE`, `CONFIRMADA`, `EN_CAMINO`, `EN_CURSO`, `FINALIZADA`, `CANCELADA` |

No existe `VENCIDO`. `EXPIRADA` está en la solicitud, pero **ningún job/cron** la aplica cuando pasa `inicioConfirmado` / `finConfirmado`. `canceladoPor` solo admite `CLIENTE` | `TRABAJADOR`. El PATCH de finalizar es solo del trabajador con código, y no hay endpoint de “marcar vencido”.

Causa: **backend no vence pedidos**. La app, hasta este fix, también los dejaba en Activos porque `isHistoryOrder` solo miraba CANCELADA/EXPIRADA/FINALIZADA. Workaround en app (PR #21): si el slot ya terminó y el booking no está EN_CAMINO/EN_CURSO, va a Historial **sin cambiar el estado**.

## Pedido

Job periódico (cada 5–15 min, timezone `America/Argentina/Buenos_Aires`) sobre bookings cuyo `finConfirmado` (o `inicioConfirmado` + duración del slot, default 60 min) ya pasó:

1. Solicitud `PENDIENTE` o `CONTRAPROPUESTA` → `EXPIRADA`. Liberar el slot si estaba reservado.
2. Booking `PENDIENTE` o `CONFIRMADA` (no iniciado) → no usar CANCELADA de parte. Opciones:
   - **Preferida:** agregar `EXPIRADA` al enum del booking (simétrico a la solicitud), o
   - `CANCELADA` con `canceladoPor: SISTEMA` (hay que ampliar el enum; la app ya contempla “SISTEMA”).
3. **No tocar de inmediato** `EN_CAMINO` / `EN_CURSO` / `LLEGUE`: el trabajo está en marcha.
4. **Cerrar o escalar bookings abiertos de más:** si un booking sigue en `EN_CAMINO`, `EN_CURSO` o `LLEGUE` más de **X horas después de `finConfirmado`** (propuesta: **24 h**, configurable por admin), el backend debe cerrarlo o escalarlo (p. ej. `FINALIZADA` automática, o alerta a soporte). La app **no** mueve esos pedidos a Historial por tiempo: se quedan en Activos hasta que el backend los cierre.
5. Notificar a ambas partes (`type` de FCM, p. ej. `SOLICITUD_EXPIRADA` / `BOOKING_EXPIRADO`) con el `solicitudId` / `bookingId`.

Sin este job, Historial en la app es una vista derivada: el detalle sigue mostrando Confirmada/Pendiente.
