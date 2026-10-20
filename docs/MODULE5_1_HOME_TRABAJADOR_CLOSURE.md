# Módulo 5.1 — Home trabajador (proveedor)

> Numeración propuesta: se inserta entre M05 (Ficha del trabajador, vista cliente) y M06 (Pedidos, ya reservado en `MODULE4_CLOSURE.md`). Si preferís otra numeración, se renombra sin problema — no hay otros documentos que lo referencien todavía.

## Criterio de cierre

- Home del trabajador de oficio como pantalla única con estados condicionales (antes el mismo grafo de navegación mostraba Home cliente para ambos roles — bug real detectado y corregido).
- Grafo de navegación separado de verdad: `ClientHome` y `ProviderHome` ya no comparten destino.
- Suite de tests del plan en verde.

## Entregado

### Domain
- `ProviderHomeContent` (nuevo modelo): datos de header, beneficios, % de perfil, turnos próximos, banner de reserva activa.
- `GetProviderHomeUseCase`.
- `HomeRepository` extendido con `getProviderHome()`.

### Data
- `HomeRepositoryImpl.getProviderHome()`: agrega turnos próximos y calcula `hasActiveBooking` (más tarde, ya en Módulo 6, se conectó a datos reales de pedidos en vez de un `false` hardcodeado).

### UI
- `ProviderHomeScreen` / `ProviderHomeRoute` / `ProviderHomeViewModel` / `ProviderHomeEvent`.
- Componentes: header de perfil, banner "AHORA" (reserva activa), sección de beneficios, tarjeta de % de perfil completo, `ProviderAgendaSection` (mini-calendario + próximos turnos).
- Bottom nav propio para el rol trabajador.

### Navegación
- `ProviderHomeDestination` / `ProviderHomeNavigation` como grafo independiente (`providerHome`), separado del grafo `home` (cliente).
- `ResolveInitialDestinationUseCase` corregido para enrutar cada rol a su propio grafo.

## QA manual

1. Login con cuenta trabajador (Matías Federico Britez / Peluquería canina) → aterriza en Home trabajador, no en Home cliente.
2. Login con cuenta cliente → aterriza en Home cliente, sin cruces.
3. Header, beneficios, % de perfil y mini-agenda renderizan con datos reales de `getProviderHome()`.

## Tests automatizados

```bash
./gradlew :feature:home:testDebugUnitTest :core:data:testDebugUnitTest :domain:test
```

## Fuera de alcance

- Datos reales de pedidos en el banner "AHORA" y en `ProviderAgendaSection` — llegaron recién con Módulo 6 (Pedidos).
- Agenda completa / configuración de turnos (`/sesion`, `/agenda/slot`) — Fase 4, todavía no arrancada.
