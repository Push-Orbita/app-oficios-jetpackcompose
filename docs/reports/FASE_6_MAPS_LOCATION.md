# Informe modular — Fase 6: ubicación, mapas y tracking

**Rama:** `codex/fase-6-mapas`  
**Fecha de cierre Android:** 28 de agosto de 2026  
**Estado:** implementación Android completa; tracking E2E pendiente de un booking real y del bloqueo backend de métodos de pago.

## Implementaciones realizadas

- Nuevo módulo `core:location` con `FusedLocationProviderClient`, Hilt y una abstracción de dominio independiente de Android.
- Modelo `GeoPoint` con validación de rangos WGS84 y rechazo explícito de la coordenada ficticia `0,0`.
- Obtención puntual de ubicación con timeout, control de GPS apagado, permisos aproximado/preciso y mensajes comprensibles.
- Observación periódica de ubicación precisa para recorridos activos.
- Integración oficial `maps-compose` compatible con Kotlin 2.0.21 (`6.4.1`) y Play Services Location `21.3.0`.
- Selector de ubicación con mapa, marcador, toque manual, centrado inicial en Formosa y CTA “Usar mi ubicación”.
- Domicilios de cliente: creación/edición y onboarding ahora guardan `latitud`/`longitud` reales.
- Local del trabajador y constructor de servicios: si se selecciona atención en local, se requiere confirmar el punto exacto.
- Detalle de pedido con mapa del destino y ubicación más reciente del trabajador.
- Tracking real para `EN_CAMINO` y `LLEGUE`; se eliminaron por completo las coordenadas hardcodeadas.
- Servicio foreground de ubicación con notificación persistente, actualización cada 30 segundos y acción para dejar de compartir.
- Recarga/polling del cliente cada 15 segundos mientras el trabajador está en camino.
- Dirección y contacto exactos ocultos al trabajador antes de aceptar la solicitud.
- Permisos Android declarados conforme al modelo foreground; no se solicita ubicación permanente en segundo plano.

## Correcciones de producción incorporadas

La prueba real del formulario “Métodos de pago” reveló dos problemas:

1. Android reintentaba `POST /trabajador-oficio-rubro/full` para guardar métodos de pago y el backend rechazaba la recreación del rubro principal.
2. El endpoint correcto `PATCH /trabajador-oficio/me` devuelve `200`, pero el backend no persiste `metodosPago`.

Android ahora:

- usa exclusivamente el `PATCH` profesional para métodos de pago;
- no reintenta el draft completo cuando sólo faltan pagos, disponibilidad, descripción o experiencia;
- confirma el resultado contra `estado-perfil` antes de navegar;
- conserva la selección si backend no persistió;
- muestra un error profesional y nunca el texto técnico con rutas/DTO/SQL;
- oculta globalmente mensajes de transporte o implementación que no corresponden al usuario.

## Decisiones UX y de privacidad

- Para domicilios de cliente el mapa mejora precisión, pero el texto sigue permitiendo avanzar si la persona prefiere no compartir GPS.
- Para un local comercial el punto exacto sí es obligatorio porque será visible y utilizado como destino operativo.
- El tracking requiere ubicación precisa y sólo arranca desde una acción visible del trabajador sobre un booking real.
- No se solicita `ACCESS_BACKGROUND_LOCATION`: el servicio foreground visible es suficiente y más respetuoso de la privacidad.
- La app nunca muestra `0,0`, no inventa una ubicación y no inicia tracking si no existe `bookingId` válido.
- El cliente ve “esperando la primera actualización” en lugar de un marcador falso.
- Al finalizar/cancelar o cerrar sesión se detiene el servicio.

## Endpoints consumidos y verificados

- `GET /domicilios/mis`
- `POST /domicilios/mi`
- `PATCH /domicilios/{id}`
- `GET /tracking-booking/booking/{bookingId}`
- `POST /tracking-booking/booking/{bookingId}/marcar`
- `PATCH /trabajador-oficio/me`
- `GET /trabajador-oficio/me`
- `GET /trabajador-oficio/me/estado-perfil`

El script `scripts/contract/verify-fase6-backend.ps1` valida operaciones, autenticación, schemas, coordenadas y enums directamente contra OpenAPI.

## Diferencias y decisiones respecto de Figma

- Figma muestra el mapa asociado al pedido y al estado “Estoy en camino”, pero no especifica permisos, GPS apagado, ausencia de primera ubicación ni caducidad. Se agregaron esos estados por necesidad de producto real.
- Se incorporó selección manual en mapa para corregir GPS impreciso, además del CTA de ubicación actual.
- No se implementó tracking silencioso ni coordenadas simuladas aunque el mockup no detalle el mecanismo.
- No se mostró contacto exacto antes de aceptar el pedido, siguiendo la decisión de privacidad del plan maestro.

## Pruebas ejecutadas

### JVM y contrato

- `:core:common:test`
- `:domain:test`
- `:core:data:testDebugUnitTest`
- `:feature:onboarding:testDebugUnitTest`
- `:feature:home:testDebugUnitTest`
- regresión de errores técnicos;
- regresión de métodos de pago sin recrear rubro;
- falso `200` sin persistencia;
- coordenadas válidas, corruptas, fuera de rango y `0,0`;
- `scripts/contract/verify-fase6-backend.ps1`: correcto.

### Compose UI en emulador API 35

- Design system: 3/3.
- Auth/registro: 5/5.
- Home/agenda/pedidos/búsqueda: 13/13.
- Onboarding: 4/4.
- Perfil/mapa: 5/5.
- Total: **30/30 pruebas instrumentadas verdes**.

El test de mapa renderiza el editor de domicilio con coordenadas confirmadas y verifica el mapa, el estado de confirmación y el CTA de ubicación actual.

### Smoke y E2E automatizado

- `scripts/e2e/fase6-location-maps.ps1`:
  - instala el APK;
  - concede permisos coarse/fine;
  - inyecta ubicación simulada en Formosa;
  - inicia la app y verifica ausencia de crash;
  - confirma preparación del servicio foreground.
- UIAutomator sobre trabajador validado:
  - confirmó `PATCH /trabajador-oficio/me`;
  - confirmó ausencia de `POST /full`;
  - confirmó conservación de selección y error profesional ante falso éxito backend.
- `lintDebug build`: **BUILD SUCCESSFUL**, 1.233 tareas; Debug y Release verificados.

## Incidencias y limitaciones comprobadas

- El AVD API 37 ejecuta la app, pero la versión actual de Espresso usa una API interna eliminada por ese preview. El checkpoint contractual se aisló y completó en Pixel Tablet API 35.
- La cuenta validada de prueba tiene cero solicitudes (`ORDER_COUNT=0`), por lo que no fue posible activar un recorrido real sin inventar o alterar un pedido.
- Backend no persiste `metodosPago` aunque responde `200`; esto impide completar el perfil operativo y crear el escenario E2E de contratación/tracking de forma limpia.

## Requerimientos backend acumulados en esta fase

- `BACK-PROFILE-002`: persistencia transaccional y verificable de métodos de pago.
- `BACK-TRACKING-001`: frecuencia, secuencia, precisión, caducidad, autorización, privacidad e idempotencia del tracking.
- `BACK-PROFILE-001`: edición atómica de un servicio existente, incluida la ubicación del local.

El detalle ejecutable, ejemplos JSON, `curl` y criterios de aceptación están en `docs/backend/BACKEND_IMPLEMENTATION_REQUIREMENTS.md`.

## Criterio de cierre

La parte Android de Fase 6 queda cerrada: no existen coordenadas ficticias, los mapas y permisos son reales, el servicio foreground está implementado y las regresiones están verdes. El E2E de ubicación en movimiento se reabrirá apenas backend persista métodos de pago y exista un booking de prueba en estado compatible.
