# Módulo 6 — Pedidos

> **Estado: EN PROGRESO / BLOQUEADO.** No es un cierre — falta el checkpoint de flujo completo real en emulador (cliente crea pedido → trabajador acepta), bloqueado por un problema de backend (ver "Bloqueante actual" abajo). Se deja documentado el trabajo entregado hasta ahora para no perder trazabilidad.

## Alcance

Reemplaza el stub `WorkerHireStubScreen` por el módulo completo de solicitud de servicio → contrapropuesta → booking → tracking → calificación, contra los tags reales del Swagger: **Solicitud de servicio**, **Contrapropuesta**, **Booking (cita)**, **Tracking (en camino/llegada)**, **Calificaciones**.

## Entregado

### Domain
- `ServiceOrder`, `ServiceRequestStatus`, `BookingStatus`, `CounterOfferStatus`, `ServiceOrderListItem`, `CreateServiceRequestParams`.
- `OrderRepository` (13 métodos) + `OrderUseCases` / `CreateServiceRequestUseCase`.
- `WorkerProfile` extendido con `WorkerSubRubroOption` (necesario porque `POST /solicitud-servicio` pide `trabajadorOficioSubRubroId`, que el modelo anterior descartaba).

### Network
- `OrdersApi` (Retrofit): crear solicitud, listar (cliente/trabajador), detalle, aceptar/rechazar, contrapropuesta (crear/aceptar/rechazar), cancelar, booking (detalle/finalizar), tracking (marcar en camino/llegada), calificación.
- DTOs completos en `OrderDtos.kt`, reutilizando catálogos y DTOs existentes (`RubroDto`, `ContactoSinPersonaDto`, `UsuarioDto`) en vez de duplicarlos.

### Data
- `OrderRemoteDataSource` (wrapea todo con `safeApiCall`).
- `OrderMapper`: mapeo de estados, fechas (`OffsetDateTime` → label), y `SolicitudServicioSimpleDto.toListItem(isWorkerViewing)`.
- `OrderRepositoryImpl`.

### UI (paquete `feature/home/orders/`)
- **Crear pedido**: `CreateOrderScreen` con sub-rubro, domicilio, contacto, fecha/hora (`DateTimePickerButton` reutilizable), descripción.
- **Detalle de pedido**: `OrderDetailScreen` — **una sola pantalla adaptativa** (no 5 pantallas casi duplicadas) que cubre pendiente/contrapropuesta/confirmado/en camino/en curso/finalizado/cancelado según `(status, bookingStatus, viewerRole)`.
- **Reagendar**: `CounterOfferScreen` (contrapropuesta con fecha/hora + precio opcional).
- **Puntuar trabajador**: `RateWorkerScreen` con selector de estrellas 1-5 (el diseño pide un único selector; el backend separa calidad 1-10 y puntualidad 1-5 — calidad se deriva como estrellas×2, documentado en código).
- **Mis pedidos**: `OrdersListScreen`, según rol (cliente ve `getClientOrders()`, trabajador ve `getWorkerOrders()`).
- Integración en Home cliente ("Mis pedidos") y Home trabajador (banner "AHORA" + agenda con pedidos reales).

### Navegación
- `OrdersDestination` / `OrdersNavigation` como rutas top-level (no anidadas), alcanzables desde ambos grafos de rol.
- Eliminado `WorkerHireStubRoute/Screen/ViewModel` y strings huérfanos asociados.

## Decisiones de diseño/backend documentadas

- **Finalizar servicio sin código**: el diseño sugería un código de 4 dígitos, pero `POST /booking/{id}/finalizar` no tiene ese parámetro en el backend real. Se implementó un botón "Finalizar servicio" único, disponible para ambos roles cuando el booking está EN_CURSO — se priorizó coherencia de flujo por sobre fidelidad al diseño (criterio acordado con el usuario).
- **`counterpartName` en la lista de pedidos**: `SolicitudServicioSimpleDto` nunca incluye `trabajadorOficio` (solo `cliente`), así que la lista del cliente no puede mostrar el nombre del trabajador — gap real de backend, documentado en `OrderMapper`.
- **Lat/lng en tracking**: `markEnRoute`/`markArrived` usan placeholder `"0"/"0"` — GPS real diferido a Fase 6 (Maps).

## Bugs reales encontrados y corregidos (durante el checkpoint de esta fase)

1. **Formato de teléfono no persistía en Contacto/Domicilio** (`ClientContactDomicilioViewModel.kt`): el campo se precargaba con el formato normalizado del backend (`+543705123456`) y se reenviaba tal cual al guardar, pero el backend rechaza `POST /contacto` si el número trae `+`/código de país (pide solo el número local). Esto rompía el guardado de Contacto **y**, en cascada, el de Domicilio (nunca se llegaba a esa llamada porque el flujo aborta si Contacto falla). Corregido con `removePrefix("+54")` al precargar. Confirmado con `POST /contacto` devolviendo `201` en vez de `400`.
2. **Parser de errores del backend perdía el mensaje real** (`NetworkErrorMapper.kt`): cuando `errorDetails.message` viene como array JSON (formato típico de class-validator/NestJS), el parser lanzaba una excepción silenciosa y caía al mensaje genérico "Revisá los datos ingresados", ocultando el motivo real (ej. "Ingresá solo el número local..."). Corregido para manejar arrays y aislar cada candidato de parseo; agregado test de regresión en `NetworkErrorMapperTest`.

## Bloqueante actual (backend, no de la app)

**Verificación de teléfono por OTP no funciona en el backend real, ni por WhatsApp ni por SMS.** Confirmado llamando directo a la API (no vía la app):

```
POST /contacto/8/enviar-codigo-telefono
→ 400 { code: "VF06", details: { detail: "No se pudo entregar el código por el canal configurado", canal: "sms" } }
```

Probado con un número argentino real del usuario (no solo con números de prueba del emulador). El endpoint no expone ningún parámetro de canal al cliente — el backend decide internamente WhatsApp vs. SMS (vía Twilio) y ambos fallan. Esto bloquea que **cualquier** cuenta cliente nueva llegue a `ClientHome`, ya que `ResolveInitialDestinationUseCase` exige `perfilCompleto` (que el propio backend calcula, incluyendo teléfono verificado). Requiere revisión del equipo de backend (credenciales/saldo/config de Twilio o Messaging Service para Argentina).

## Pendiente para cerrar Módulo 6

- Flujo real end-to-end: cliente crea solicitud → trabajador acepta → confirmado → en camino → finalizar → puntuar (bloqueado por el punto anterior).
- Build + tests ya corren en verde; falta la corrida completa en emulador con ambos roles una vez se resuelva el bloqueante de Twilio.

## Tests automatizados

```bash
./gradlew :feature:home:testDebugUnitTest :core:network:testDebugUnitTest \
  :core:data:testDebugUnitTest :domain:test
```
