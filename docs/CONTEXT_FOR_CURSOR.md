# Contexto completo del proyecto — Oficios Formosa (para continuar en Cursor)

> Este documento es un prompt/contexto pensado para pegarle directo a Cursor al empezar una sesión nueva. Resume todo lo construido hasta ahora, las convenciones del repo, el estado real de git, y lo que falta. Está escrito para que quien lo lea (humano o IA) pueda retomar el desarrollo sin tener que releer todo el historial de commits ni redescubrir las convenciones desde cero.

## 1. Qué es la app

**Oficios Formosa** es una app Android nativa (Kotlin + Jetpack Compose) que conecta **clientes** con **trabajadores de oficio** (plomeros, electricistas, peluqueros caninos, etc.) en Formosa, Argentina. Dos roles con flujos y homes completamente distintos: `CLIENTE` y `TRABAJADOR_DE_OFICIOS`.

Backend real (NestJS, no es un mock): `https://apioficios.pushsoftware.com.ar` — swagger/OpenAPI vivo en `https://apioficios.pushsoftware.com.ar/api-json`. **Siempre verificar contratos contra ese swagger antes de asumir un endpoint o shape de respuesta** — varias veces en este proyecto la documentación o el swagger mismo no coincidían con el comportamiento real (ver sección 5).

Flujo de negocio central: el cliente busca por categoría/rubro, ve el perfil de un trabajador, le pide un servicio (con fecha propuesta), el trabajador acepta/rechaza/contraoferta, se genera un booking, hay seguimiento de ubicación en vivo mientras el trabajador viaja, se finaliza el servicio y el cliente califica.

## 2. Stack técnico y arquitectura

- **UI:** Jetpack Compose, Material 3, sin Material You dinámico (paleta propia fija, un solo modo visual — no hay claro/oscuro real pese a que quedó un toggle legado en Ajustes).
- **Arquitectura:** MVVM + Clean Architecture pragmática (documentada en `docs/architecture.md`). Capas: UI (`app`, `feature:*`) → Domain (`domain`, sin ninguna dependencia de Android) → Data (`core:data`, `core:network`, `core:database`, `core:datastore`) → Core compartido (`core:common`, `core:model`, `core:designsystem`, `core:ui`, `core:location`).
- **DI:** Hilt. Un módulo por capa de infraestructura (`NetworkModule` en `core:network`, `DatabaseModule` en `core:database`, `PreferencesModule` en `core:datastore`, `DataModule`/`CoreModule` en `core:data`).
- **Red:** Retrofit + `retrofit-kotlinx-serialization` (NO Gson/Moshi). `BASE_URL` real está en `core/network/build.gradle.kts` como `BuildConfig` (`https://apioficios.pushsoftware.com.ar/`). Ojo: hay un `DEMO_BASE_URL` (jsonplaceholder) que parece resto de un template, revisar si sigue en uso.
- **Persistencia:** Room (`core:database`), DataStore + `androidx.security-crypto` para sesión/token cifrados (`core:datastore`).
- **Manejo de errores:** patrón `AppResult<T>` (`Success`/`Error`) + `AppError` sealed interface (`Network`/`Validation`/`Unknown`/etc.), con `safeApiCall{}` envolviendo las llamadas Retrofit y `toAppError()` mapeando excepciones. Las ViewModels nunca tocan Retrofit/Room directo — todo pasa por `AppResult`.
- **Módulos Gradle reales** (`settings.gradle.kts`): `app`, `domain`, `core:common`, `core:model`, `core:database`, `core:datastore`, `core:network`, `core:data`, `core:designsystem`, `core:ui`, `core:location`, `feature:auth`, `feature:onboarding`, `feature:home`, `feature:settings`. **No existen módulos separados `feature:agenda` ni `feature:account`/`feature:profile`** — Agenda vive dentro de `feature:home` (paquete `agenda/`), y Perfil/Cuenta vive dentro de `feature:settings`.
- **Patrón de pantalla:** `*UiState` (inmutable) + `*Event` (sealed interface) + `*ViewModel` (expone `StateFlow<UiState>` + efectos one-shot vía `Channel`/`SharedFlow`) + `*Screen` (composable **stateless**, sin lógica) + `*Route` (wiring: `hiltViewModel()`, `collectAsStateWithLifecycle()`, colecta efectos y navega). Ver `docs/create-new-feature.md` para el checklist completo paso a paso de cómo armar un feature module nuevo (incluye plantilla de `build.gradle.kts`, estructura de carpetas, wiring de navegación, tests mínimos).
- **Testing:** unit tests con fakes para ViewModels/use cases/mappers (`src/test`), Compose UI tests para pantallas clave (`src/androidTest`). **Regla aprendida en esta sesión: preferir tests instrumentados sobre testeo manual con adb+capturas siempre que se pueda** — mucho más rápido y confiable. Para testeo manual en vivo que sí sea necesario (contra backend real), usar un script de tap-por-texto basado en `uiautomator dump` en vez de adivinar coordenadas de pantalla.
- **Emulador:** ⚠️ el AVD `Pixel_9_Pro`/`Pixel` corre una imagen preview de Android (API 37) que rompe la sincronización interna de Espresso (`NoSuchMethodException` en `InputManager`). Usar el AVD `Pixel_Tablet` (API 35, estable) para `connectedAndroidTest`.

### Convención de nombres del design system — IMPORTANTE

`docs/design-system.md` describe el tema navy v2 y los componentes `Oficios*`. La lista viva está en `core/designsystem/src/main/java/com/cocido/oficios/core/designsystem/component/`.

### Reglas de Compose/insets — IMPORTANTE (bug real ya corregido, no reintroducir)

`MainActivity` usa `enableEdgeToEdge()`. `OficiosScaffold` ya define `contentWindowInsets = WindowInsets.safeDrawing`, que **ya incluye el IME (teclado)**. Agregar `imePadding()` manualmente ENCIMA de eso duplica el padding inferior y deja un hueco vacío del alto del teclado — esto pasó de verdad en `AuthScreenLayout.kt` y `AuthRegisterLayout.kt` y se corrigió en esta sesión (commit "Fix: no duplicar padding del teclado en pantallas de auth"). **Nunca agregar `imePadding()`/`navigationBarsPadding()` manual — todo el manejo de insets está centralizado en `OficiosScaffold`.**

### Regla "un solo patrón por intención de producto"

Documentada en `.cursor/rules/android-standardization.mdc` (`alwaysApply: true`, se aplica a cada request en Cursor automáticamente). Resumen: si la misma lógica/estado/efecto/UI aparece 2+ veces (aunque con datos distintos) → extraer a una capa compartida antes de copiar y pegar. Flujos gemelos cliente/trabajador (onboarding, auth, checklist, confirmación) deben compartir tipos, un navigator/helper y hosts compuestos — no ViewModels casi idénticos por rol. Ejemplos reales ya en el repo: `OnboardingChecklistUiState`/`OnboardingCompletionPhase`/`OnboardingChecklistEffect` + `OnboardingCompletionNavigator`/`OnboardingChecklistRouteHost`.

## 3. Estado actual — qué ya está construido (todo contra backend real, no mockeado)

Cronología completa (dos esquemas de numeración usados en distintos momentos — "Módulo N" al principio, "Fase N" desde mediados de agosto 2026 — son el mismo proyecto continuo, no dos líneas paralelas):

| # | Nombre | Qué incluye | Estado |
|---|---|---|---|
| Módulo 01 | Design System base | Componentes `Oficios*`, tokens, tema | ✅ Cerrado |
| Módulo 02 | Auth + Onboarding | Login, registro cliente/trabajador, checklist DNI | ✅ Cerrado |
| Módulo 03 + 3.1 | Integración backend de identidad | Selfie KYC, OTP teléfono, push FCM para revisión de DNI | ✅ Cerrado (con QA manual pendiente de confirmar, ver `MODULE3_1_INTEGRATION.md`) |
| Módulo 04 | Home / Búsqueda cliente | Feed, categorías→rubros→resultados, filtros, paginación | ✅ Cerrado |
| Módulo 05 | Ficha del trabajador (vista cliente) | Especialidades, disponibilidad, galería, certificados, reseñas | ✅ Cerrado |
| Fase 1 | Rediseño visual v2 + auditoría | Nueva paleta Figma, alineación de pantallas M02-M05 | ✅ Cerrado |
| Módulo 5.1 | Home trabajador | Home separado por rol (bug real corregido: ambos roles caían al mismo home) | ✅ Cerrado |
| Módulo 6 / Pedidos | Ciclo de vida de pedido completo | Solicitud→contraoferta→booking→tracking→calificación | ⚠️ **Construido pero bloqueado** — nunca se corrió el flujo E2E completo por el bug de Twilio (ver sección 5) |
| Fase 4 | Agenda | Configurar disponibilidad, crear/editar/eliminar turnos, calendario mensual + vista semanal | ✅ Cerrado y validado con cuenta real |
| Fase 5 | Perfil y cuenta | Centro de cuenta: editar nombre/email/foto/contraseña; cliente administra domicilios/contactos; trabajador administra servicios/certificados/galería | ✅ Cerrado y validado con cuenta real |
| Fase 6 | Mapas y ubicación en tiempo real | Selector de domicilio en mapa, tracking en vivo (en camino/llegó), servicio foreground de ubicación | ✅ Cerrado (parte Android); E2E de tracking real pendiente de un booking real (bloqueado por lo mismo que Pedidos) |
| **Fase 7** | **Navegación y notificaciones** | — | 🔵 **Rama creada (`codex/fase-7-navegacion-notificaciones`), CERO commits todavía. Es el punto de partida para Cursor.** |

### Qué significa "Fase 7" en concreto — pistas de lo que falta

A lo largo de todo el desarrollo quedaron varios puntos de navegación/notificaciones como placeholders "Próximamente" sin implementar de verdad:

- El ícono de campana (🔔) de notificaciones en Home (cliente y trabajador) y en Categorías — siempre dispara un mensaje "Próximamente", nunca abrió una pantalla real de notificaciones.
- Los tabs "Perfil" (ya resuelto en Fase 5), "Favoritos" y "Ayuda" del bottom nav — Favoritos y Ayuda siguen siendo "Próximamente" en varios puntos.
- El sistema de push FCM para KYC (Módulo 3.1) ya está construido (`OficiosFirebaseMessagingService`, `OnboardingDeepLinkMapper`) pero usa un `google-services.json` **stub** (no productivo) — ver `docs/MODULE3_FCM_STAGING.md`. Habría que expandir esto a un sistema de notificaciones genérico (nuevo pedido, contraoferta, booking confirmado, trabajador en camino, etc.), no solo KYC.
- `BACK-NOTIF-001` (ver sección 5) señala que `GET /notifications` ni siquiera tiene un schema de respuesta publicado en el swagger — antes de construir una pantalla de notificaciones real hay que validar/negociar ese contrato con backend.
- Favoritos (cliente↔trabajador) no tiene ningún endpoint en el backend (`BACK-SOCIAL-001`) — está deshabilitado del lado de la app a propósito.

Esto sugiere que "Fase 7" debería cubrir: (a) una pantalla real de centro de notificaciones, (b) push notifications genéricas más allá de KYC, y posiblemente (c) pulir navegación general (Favoritos, Ayuda) según lo que decida el equipo — pero **esto es una inferencia mía a partir del nombre de rama y los placeholders encontrados, no una especificación cerrada. Vale la pena confirmar el alcance real de "Fase 7" antes de arrancar a construir**, ya que no hay un documento de planificación para esta fase todavía (a diferencia de Fases 4-6, que sí tienen `docs/reports/FASE_N_*.md` de cierre pero fueron planificadas antes de empezar).

## 4. Estado de git — IMPORTANTE antes de que Cursor arranque

- Rama actual: `codex/fase-7-navegacion-notificaciones`, recién creada desde el tip de Fase 6, **sin ningún commit propio todavía**.
- `main` está **desactualizado** — solo tiene el "first commit" inicial del template. **Todo el desarrollo real (22+ commits, todas las fases) vive fuera de `main`**, en esta cadena de branches. No branchear desde `main`.
- Convención de branch-por-fase: `feature/full-app/fase-4-agenda`, `codex/fase-5-perfil-cuenta`, `codex/fase-6-mapas`, `codex/fase-7-navegacion-notificaciones` — Cursor debería seguir el mismo patrón (una rama por fase, nombrada `codex/fase-N-tema` o similar).
- Mensajes de commit: español, formato libre pero descriptivo (`feat: ...`, `Fix: ...`, `Fase N: ...`). No hay Conventional Commits estricto.
- Idioma: **todo el código de UI, strings, comentarios de negocio, y documentación está en español (Argentina)**. Mantener esa convención.

## 5. Gaps y bloqueos de backend conocidos — HOY, no históricos

Documento maestro con el detalle completo (request/response de ejemplo, permisos, errores, criterios de aceptación): **`docs/backend/BACKEND_IMPLEMENTATION_REQUIREMENTS.md`**. Ningún ítem de esa lista está marcado como resuelto — todos siguen abiertos a la fecha del último commit que la tocó (28 de agosto de 2026). Resumen por prioridad:

**Bloqueante (impide flujos completos hoy):**
- **Verificación de teléfono (Twilio) — `docs/BACKEND_OTP_TWILIO_ISSUE.md`.** El envío de código OTP (WhatsApp y SMS) falla siempre para números argentinos reales, confirmado con múltiples reintentos y contactos nuevos sin historial. Esto bloquea que **cualquier cuenta de cliente nueva llegue a `ClientHome`** (el backend exige teléfono verificado para `perfilCompleto`), lo que a su vez bloquea la validación E2E completa de Pedidos (Módulo 6) y del tracking real (Fase 6). Es el bloqueo más importante del proyecto ahora mismo — del lado del proveedor de mensajería, no de la app.
- **BACK-PROFILE-002** — `PATCH /trabajador-oficio/me` con `metodosPago` devuelve `200` pero **no persiste el valor** (confirmado con curl contra el ambiente real el 28/08). Traba a los trabajadores en 50% de perfil completo.
- **BACK-AGENDA-002** — no hay reserva atómica de turnos en el backend; posible condición de carrera si dos clientes reservan el mismo slot casi simultáneamente.
- **BACK-ORDERS-001** — `PATCH /booking/{id}/finalizar` no tiene código de finalización real; cualquiera puede finalizar sin prueba de que ambas partes estuvieron presentes.
- **BACK-ORDERS-002** — el modelo de booking no soporta tipo de contrato (presupuesto/trabajo/ambos) ni documenta bien monto/método de pago.
- **BACK-NOTIF-001** — `GET /notifications` no tiene schema de respuesta publicado en OpenAPI. **Relevante directo para Fase 7.**
- **BACK-ACCOUNT-001** — no existe `DELETE /usuario/me` seguro basado en identidad autenticada (solo `DELETE /usuario/{id}` genérico por ID editable). Por esto "Eliminar cuenta" está visible en la UI pero deshabilitado a propósito, con explicación.

**Alta prioridad (gaps de contrato, no bloquean pero limitan funcionalidad):**
BACK-AGENDA-001 (edición de horario de turno rechaza incorrectamente rangos válidos), BACK-AGENDA-003 (no hay `PATCH /sesion/{id}` atómico), BACK-ORDERS-003 (el listado de pedidos del cliente no trae los datos del trabajador asignado, fuerza N+1 requests), BACK-RATING-001 (solo existe calificación cliente→trabajador, no la inversa, si es que se necesita), BACK-ACCOUNT-002 (`PATCH /usuario/{id}` depende de un ID de path en vez de la identidad del token), BACK-CONTACT-001 (creación de contacto no es idempotente — una cuenta de prueba acumuló 7 contactos duplicados por reintentos), BACK-PROFILE-001 (no hay edición atómica de un servicio ya publicado, solo `autoAceptacion`/`principal`), BACK-TRACKING-001 (endpoints de tracking funcionan pero sin contrato de frecuencia/precisión/expiración documentado), BACK-CERT-001 (certificados no tienen estado administrativo — la app nunca muestra un certificado como "verificado" porque el backend no lo informa), BACK-API-001 (errores inconsistentes, paginación no uniforme, falta de documentación de query params).

**Media prioridad / features deshabilitadas a propósito hasta que exista backend:**
BACK-GALLERY-001 (sin caption/orden en galería), BACK-SOCIAL-001 (favoritos cliente↔trabajador no existe, feature deshabilitada), BACK-SAFETY-001 (denuncias/moderación no existe, feature deshabilitada), BACK-PHONE-001 (soporte multi-país, no urgente).

**Ya resueltos por backend (confirmados):**
- `docs/BACKEND_BARRIOS_SEARCH_AUTH_ISSUE.md` → **RESUELTO**, confirmado por retest directo: `GET /barrios/search` ya no exige `Authorization`. (Ojo: el fix se pidió también para `/localidades/search`, `/provincias/search`, `/paises/search` pero el documento no confirma si esos también se corrigieron — revisar si se usan en algún flujo).

**Estado ambiguo — revisar antes de asumir:**
- `docs/BACKEND_SOFT_DELETE_DNI_LOCKED_ISSUE.md` (el soft-delete de un usuario no libera su DNI para un registro nuevo) — el documento en sí **no tiene ningún banner de "resuelto"**, se lee como un issue abierto de punta a punta. Sin embargo, en el desarrollo de este proyecto hubo una confirmación verbal/de backend de que esto se había arreglado. **Recomiendo re-verificar con un test directo contra la API real antes de dar por cerrado o abierto este issue** — no confiar ni en la memoria de la sesión anterior ni en el documento sin volver a probar.

## 6. Cómo trabajar en este repo (para Cursor)

1. **No branchear desde `main`** — está desactualizado. Crear la rama nueva desde el HEAD actual de `codex/fase-7-navegacion-notificaciones`.
2. **Verificar contratos de backend en vivo** contra `https://apioficios.pushsoftware.com.ar/api-json` antes de asumir un endpoint, especialmente shapes de respuesta y si un DELETE devuelve body vacío (ver el bug real de abajo).
3. **Bug real ya encontrado y corregido dos veces en este proyecto — repetir el chequeo en cualquier endpoint nuevo:** varios DELETE del backend (`agenda/slot/{id}`, `sesion/{id}`, `galeria-trabajo/{id}`, `domicilios/{id}`, `contacto/{id}`, `trabajador-oficio-rubro/{id}`, `certificado-trabajo/{id}`) devuelven `200` con **body vacío**. Si el método Retrofit correspondiente está tipado para devolver `JsonElement` (o cualquier DTO), el parseo de un body vacío tira una excepción que `safeApiCall` atrapa y reporta como error genérico — **aunque el borrado sí haya funcionado en el servidor**. Antes de tipar un método DELETE, chequear el swagger: si `hasContent: false` en la respuesta 200, tipar el método Retrofit como `Unit` (sin tipo de retorno), no como `JsonElement`.
4. **Testing:** preferir tests instrumentados (`src/androidTest`, Compose UI test) sobre testeo manual. Para lo que sí requiera probarse en vivo contra el backend real, usar un script de tap-por-texto vía `uiautomator dump` en vez de coordenadas fijas adivinadas de una captura.
5. **Revisar proactivamente la lógica de UX** en cualquier pantalla que se toque — no limitarse a lo pedido explícitamente. Ejemplo real de esta sesión: una tarjeta "Completá tu perfil" seguía mostrando el botón de completar aunque el perfil ya estuviera al 100%; se corrigió ocultando la tarjeta entera al llegar a 100%.
6. **Idioma:** todo en español (código de negocio, strings, commits, documentación).
7. Al cerrar una fase, seguir el patrón ya establecido: un doc de cierre en `docs/reports/FASE_N_<TEMA>.md` con: qué se implementó, decisiones de UX, endpoints consumidos/verificados, diferencias vs. Figma, pruebas ejecutadas, incidencias/requerimientos de backend nuevos.

## 7. Qué pedirle a Cursor que haga ahora

Con todo este contexto:
1. Crear una rama nueva desde el estado actual (`codex/fase-7-navegacion-notificaciones` o una nueva rama a partir de ella).
2. Armar un plan concreto para **Fase 7: Navegación y notificaciones**, confirmando primero el alcance real (centro de notificaciones, push genérico más allá de KYC, Favoritos/Ayuda, lo que corresponda) ya que no hay una especificación previa escrita para esta fase, solo el nombre de la rama y los placeholders "Próximamente" detectados.
3. Seguir las convenciones de arquitectura, diseño y testing documentadas arriba.
4. Verificar cualquier endpoint nuevo contra el swagger real antes de implementarlo, y documentar cualquier gap de backend nuevo con el mismo formato que los `BACK-*` ya existentes en `docs/backend/BACKEND_IMPLEMENTATION_REQUIREMENTS.md`.
