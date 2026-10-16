# Fase 8 — Pedidos E2E y reserva por turnos

**Estado:** Android parcial — UI slots + código finalización listos; E2E API bloqueado por gates backend

## Android entregado

| Ítem | Ubicación |
|------|-----------|
| `GET /agenda/slot/disponibles` | `AgendaApi`, `AgendaRepository` |
| Selector turnos CreateOrder | `CreateOrderScreen`, `CreateOrderViewModel` |
| `slotTurnoId` en POST solicitud | `CreateSolicitudServicioRequestDto` |
| UI código finalización | `OrderDetailScreen` — cliente ve código, trabajador ingresa código |
| Body opcional `code` en finalizar | `FinalizeBookingRequestDto` (BACK-ORDERS-001) |
| Script contrato Fase 8 | `scripts/contract/verify-fase8-backend.ps1` |
| Script E2E pedidos | `scripts/e2e/fase8-pedidos-e2e.ps1` |

## Gates backend

- **Twilio OTP** — onboarding cliente nuevo
- **BACK-AGENDA-002** — reserva atómica + 409
- **BACK-ORDERS-001** — validación código en backend
- **BACK-ORDERS-003** — nombre trabajador en listado

## Próximo paso E2E

Extender `fase8-pedidos-e2e.ps1` con IDs reales de staging cuando el equipo los provea.
