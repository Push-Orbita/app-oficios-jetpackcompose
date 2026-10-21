# BK-15 — Tipo de solicitud (Presupuesto / Trabajo)

**Pantalla Figma:** Completá tu Solicitud `455:27` / Solicitar Servicio `468:1657`.

## Qué falta en la API de producción

`CreateSolicitudServicioRequestDto` no tiene `tipo` (`PRESUPUESTO` | `TRABAJO`). El subrubro tampoco expone `requierePresupuesto`.

## Impacto en la app

El segmentado **Presupuesto | Trabajo** y el alerta “Este trabajador requiere un presupuesto…” quedan ocultos (**[adaptación]**) hasta que el backend publique el campo.

## Pedido

1. Agregar `requierePresupuesto: boolean` en trabajador-oficio-subrubro.
2. Agregar `tipo: PRESUPUESTO | TRABAJO` en `POST /solicitud-servicio`.
3. Si `requierePresupuesto = true`, rechazar `tipo = TRABAJO` con 400 claro.
