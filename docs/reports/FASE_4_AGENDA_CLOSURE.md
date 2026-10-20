# Fase 4 — Cierre del módulo Agenda

Fecha: 27 de agosto de 2026

Rama: `feature/full-app/fase-4-agenda`

## Resultado

La Agenda del Trabajador quedó conectada al backend real y validada con la cuenta `trabajador.prueba.agenda@gmail.com`, cuyo estado es `VALIDADO` y cuyo perfil/configuración están completos.

## Implementación

- Calendario mensual alineado con la identidad visual del Figma.
- Vista semanal rápida, selección de día y regreso a “Hoy”.
- Carga mensual acotada mediante `inicioDesde` e `inicioHasta`.
- Filtrado de servicios que realmente tienen `requiereTurnos=true`.
- Creación de disponibilidad con inicio, fin y autoaceptación.
- Edición mediante el endpoint real `PATCH /agenda/slot/{id}`.
- Eliminación con confirmación explícita.
- Slots reservados, bloqueados y pasados son de solo lectura.
- Reserva visible mediante `bookingId`, sin inventar datos del cliente.
- Prevención local de horarios superpuestos y rangos que cruzan de día.
- Errores de formulario visibles en contexto y reintento de carga sin vaciar toda la pantalla.
- Cancelación de requests anteriores al cambiar rápidamente de servicio o mes.
- Actualización automática al volver a Agenda.
- Confirmación antes de eliminar configuraciones de sesión.
- La tarjeta “Completá tu perfil” deja de mostrarse al llegar al 100%.
- La publicación de notificaciones comprueba el permiso de Android 13+ antes de invocar al sistema.
- La cámara queda declarada como capacidad opcional para no excluir dispositivos compatibles sin cámara.

## Decisiones UX

- Se usa mes + detalle del día, siguiendo Figma, y se agrega vista semanal para operaciones frecuentes.
- “Nuevo turno” se renombra a “Agregar disponibilidad”, porque todavía no existe una reserva de cliente.
- No se permite editar ni eliminar un horario reservado desde la Agenda.
- No se simula información del pedido que `SlotTurnoDTO` no devuelve.
- Los horarios nuevos parten de una hora futura razonable y siempre pueden ajustarse antes de guardar.

## Endpoints verificados

- `GET /trabajador-oficio/me`
- `GET /trabajador-oficio/me/estado-perfil`
- `GET /agenda/slot/search`
- `POST /agenda/slot`
- `PATCH /agenda/slot/{id}`
- `GET /agenda/slot/{id}`
- `DELETE /agenda/slot/{id}`
- `GET /sesion/por-subrubro/{trabajadorOficioSubRubroId}`
- `POST /sesion`
- `DELETE /sesion/{id}`

## Diferencias respecto de Figma

- Figma muestra servicios/presupuestos como si fueran eventos completos, pero el endpoint de slots solo garantiza estado, subrubro y `bookingId`.
- Se preserva el calendario y la jerarquía visual, sin inventar nombre de cliente, importe ni tipo de booking.
- Se agregan estados de carga, error, vacío, pasado, reservado y formulario inválido que el prototipo no especifica.
- El layout no depende de un frame fijo de 440×980.

## Pruebas ejecutadas

- `:feature:home:testDebugUnitTest`: verde.
- `:feature:home:compileDebugAndroidTestKotlin`: verde.
- `:app:assembleDebug`: verde.
- `lintDebug build`: verde para todos los módulos y variantes verificadas.
- 13 instrumented tests en Pixel Tablet API 35: verdes.
- E2E automatizado en emulador API 35:
  - login del trabajador;
  - Home del trabajador;
  - apertura de Agenda;
  - calendario mensual;
  - selección de un día con slot real;
  - visualización 10:00–11:00 y estado Disponible;
  - cambio a vista semanal;
  - apertura de configuración;
  - lectura de sesión real de 60 minutos con intervalo de 15 minutos.
- Prueba API controlada: creación, consulta y limpieza de slots de test con verificación posterior.

## Incidencias reales

- `PATCH /agenda/slot/{id}` permite cambiar `autoAceptacion` y conservar las mismas fechas, pero puede rechazar incorrectamente un cambio de hora que continúa dentro de la jornada laboral. Registrado como `BACK-AGENDA-001`.
- Swagger no ofrece edición atómica para una sesión. Registrado como `BACK-AGENDA-003`.
- La prevención definitiva de doble reserva debe ocurrir en backend. Registrado como `BACK-AGENDA-002`.

## Limpieza

Los slots creados durante la verificación (`15`, `16`, `17` y `18`) fueron eliminados y se verificó que ya no aparezcan en la búsqueda.
