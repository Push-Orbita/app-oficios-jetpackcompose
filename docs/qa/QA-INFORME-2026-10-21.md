# Informe QA exhaustivo — 2026-10-21

**Alcance:** testear e informar. No se corrigió producto (salvo agregar tests).  
**Rama / PR:** `cursor/estado-plan-ee07` · [PR #21](https://github.com/Push-Orbita/app-oficios-jetpackcompose/pull/21)  
**HEAD al momento del informe:** ver commit de este archivo.  
**API:** producción `apioficios.pushsoftware.com.ar` — **no se mutó ni se leyó con credenciales**.  
**Emulador `emulator-5554`:** no había dispositivos (`adb devices` vacío). Navegación real: **no verificable aquí**.

Umbral visual acordado: pixel-diff &lt; 2 % (Roborazzi P9 vs PNG Figma), layout ±3 dp. File Figma `hhKrcrusbr3h6qJxqYBiTd`.

---

## Resumen ejecutivo

| Métrica | Valor |
|---|---|
| Flujos PASS (automatizado / mock) | 18 |
| Flujos FAIL (funcional bloqueante) | 0 |
| Flujos FAIL visual vs umbral 2 % | 8 pantallas clave |
| Flujos no verificable aquí | 5 |
| Bugs bloqueantes | 0 |
| Bugs alta | 2 (fidelidad visual ficha/detalle) |
| Bugs media | 6 |
| Bugs baja | 3 |
| Unit + MockWebServer corridos | **512** tests, **0** fail, **39** skip (Roborazzi sin golden + `@Ignore("QA-FAIL")` a11y) |
| Instrumented E2E en emulador | **no corrido** (sin device) |
| `assembleRelease` + R8 minify + `lintVitalRelease` | **PASS** (`app-release-unsigned.apk`) |
| Jacoco / % líneas | no hay plugin Jacoco en el repo |

**Conclusión:** la app **no se cae** en unit/MockWebServer ni en R8. Los huecos grandes son fidelidad Figma (6–36 % vs 2 %), a11y de botones chicos, y todo lo que exige dispositivo real / credenciales QA / FCM.

---

## Tabla por flujo

| Flujo | Resultado | Cómo se verificó | Notas |
|---|---|---|---|
| Splash | **PASS** | `AuthFlowComposeTest`, `SplashScreenTest` | Loading con marca; error + Reintentar. 2 nodos `Oficios Formosa` (logo + wordmark). |
| Login | **PASS** | `LoginViewModelTest`, `AuthApiIntegrationTest`, `AuthFlowComposeTest`, `LoginScreenTest` | 200 tokens; 401 → banner genérico *Los datos de sesión no coinciden*; 500/timeout/sin red → `AppError.Network`. |
| Registro cliente (Nombres + Apellido) | **PASS** | `RegisterViewModelTest`, `RegisterFormScreenTest`, signup MockWebServer | Campos separados; “Matias Federico” no se parte. |
| Registro trabajador (Nombres + Apellido) | **PASS** | mismo + `RegisterTradeScreenTest` | Título *Registro de trabajador*; DNI obligatorio. |
| Recuperar contraseña | **PASS** | `ForgotPasswordViewModelTest`, `ForgotPasswordScreenTest`, `AuthApiIntegrationTest` | Request 201; 404 mapea NotFound. Envío real de mail: no verificable. |
| Onboarding | **PASS** (unit) | tests existentes client/provider/verify | Checklist UI instrumentada no se ejecutó (sin emulador). |
| Home cliente | **PASS** | `HomeFlowComposeTest`, `HomeViewModelTest` | Loading CD, error+retry, buscador + mic CD. Visual vs Figma: **FAIL 9.92 %**. |
| Buscador texto | **PASS** | `WorkerSearchApiIntegrationTest`, `SearchResultsViewModelTest` | Success, empty, 401, 500. |
| Buscador voz | **no verificable aquí** | `VoiceSearchTest` (unit) PASS | Sin RecognizerIntent / dispositivo real. Mic visible en home. |
| Resultados / filtros | **PASS** | `SearchResultsScreenTest`, `HomeFlowComposeTest` | Badge filtros; empty/loading. Error de red reusa copy de home (bug media). |
| Ficha trabajador | **PASS** (datos/mock) / **FAIL** visual | `WorkerProfileApiIntegrationTest`, `WorkerProfileViewModelTest` | Pixel **33.89 %**. |
| Favoritos | **PASS** (mock) | `FavoritesApiIntegrationTest`, `FavoritesScreenTest` | List/empty/PUT+Idempotency/DELETE 204/404/500. **No se tocó Favorito en prod.** |
| Solicitud / reserva | **PASS** (mock) | `CreateOrderViewModelTest`, `OrdersApiIntegrationTest` create | Idempotency-Key. **No se creó solicitud real.** |
| Mis pedidos Activos/Historial (regla fecha) | **PASS** | `OrdersListClassificationTest`, `OrdersListViewModelTest`, `OrdersListScreenTest` | PENDIENTE/CONFIRMADA pasados → Historial; EN_CAMINO/EN_CURSO siguen Activos. |
| Detalle pedido + cancelar | **PASS** (mock) / **FAIL** visual | `OrderDetailScreenTest`, cancel 400 `motivoCancelacion` | **No se canceló en prod.** Pixel **36.02 %**. |
| Home trabajador (AHORA/HOY, Completa Tu Perfil) | **PASS** (unit) / **FAIL** visual | `ProviderHomeViewModelTest`, `AgendaAppointmentTagTest` | Visual 15.93 % (incompleto) / 33.57 % (completo). |
| Servicios alta/edición | **PASS** (unit) | `ServicesViewModelTest`, `ProfileScreensTest` | Persistencia DELETE+POST (BK-01). **No se editó en prod.** |
| Galería | **PASS** (unit) | `PortfolioViewModelTest` | Mutación solo mock conceptual. |
| Certificados | **PASS** (unit) | `ProfileScreensTest` (sin badge admin) | BK-11. |
| Local comercial | **PASS** (unit) | `CommercialLocalViewModelTest` | |
| Perfil / Ajustes / Ayuda | **PASS** | `ProfileScreensTest`, `HelpScreenTest` | Una fila Editar Perfil; logout visible. Visual perfil **29.00 %**. |
| Notificaciones inbox | **PASS** (mock) | `NotificationsApiIntegrationTest`, `NotificationsScreenTest` | Empty/loading/list. Push FCM: no verificable. |
| Logout | **PASS** (mock) | `AuthApiIntegrationTest` 204, UI *Cerrar sesión* | |
| Sesión expirada | **PASS** (mapeo) | `NetworkErrorMapperTest` 401 | `TokenAuthenticator` sin test E2E; refresh fallido → 401 al caller. |
| Rotación / process death | **PASS** parcial | `SearchResultsViewModel` + `SavedStateHandle` | Login/Register **no** persisten campos. |
| Lint / R8 release | **PASS** | `./gradlew :app:assembleRelease` | minify + shrink + lintVitalRelease. |

---

## Bugs (producto — no corregidos)

### Alta

| ID | Qué | Repro | Evidencia | Causa probable | Fix sugerido |
|---|---|---|---|---|---|
| QA-V01 | Ficha trabajador lejos del umbral 2 % | Abrir ficha vs Figma `40:559` | `docs/qa/diffs-2026-10-21/ficha-trabajador-diff.png` **33.89 %** | Nav 4 vs 5, sin foto real, D-DIN, P9 427 vs 440 | Aceptar adaptaciones o rehacer layout contra el frame; no mezclar datos reales en el golden. |
| QA-V02 | Detalle pedido lejos del umbral 2 % | Abrir detalle vs `198:697` | `docs/qa/diffs-2026-10-21/detalle-pedido-diff.png` **36.02 %** | Mapa placeholder, tipografía, chrome | Maps key + golden con mapa apagado; alinear filas icon-box. |

### Media

| ID | Qué | Repro | Evidencia | Causa probable | Fix sugerido |
|---|---|---|---|---|---|
| QA-V03 | Home trabajador (completo) 33.57 % vs `259:534` | Home trabajador con agenda | `home-trabajador-completo-diff.png` | Figma sin bloque Beneficios; nav distinta | Golden del frame que sí incluye Beneficios, o ocultar el bloque cuando el perfil está 100 %. |
| QA-V04 | Perfil / Editar Perfil 29.00 % vs `231:15` | Ajustes → Editar Perfil | `perfil-diff.png` | Fotos, nested nav | Golden con placeholders; no 5 tabs Figma. |
| QA-V05 | Agenda 28.72 % vs `239:475` | Configurar agenda | `agenda-diff.png` | CTA Guardar extra, D-DIN | Dejar Guardar (necesario) y excluirlo del pixel-diff, o documentar adaptación. |
| QA-V06 | Solicitud 18.47 % / home cliente 9.92 % / Mis pedidos 6.60 % | Pantallas vs Figma | diffs homónimos | Nav acordada, placeholder usted, datos | Seguir bajando layout ±3 dp; umbral 2 % sigue FAIL. |
| QA-A01 | `OficiosButton` Small=36 dp y Medium=44 dp; se desactiva el mínimo táctil M3 | Cualquier CTA Small (login *Enviar instrucciones*, auth primary) | `OficiosButtonA11yTest` `@Ignore("QA-FAIL")`; `LocalMinimumInteractiveComponentSize = Unspecified` | Fidelidad visual Figma vs WCAG 48 dp | Mantener visual 36/44 y **expandir hit-box a 48 dp** (padding invisible), o usar Large en CTAs. |
| QA-A02 | Contraste: texto secundario Figma `#7F9AC5` 3.90:1 sobre navy; disabled `#918E9B` 3.21:1 sobre blanco; success `#4CAF7D` 2.71:1 sobre blanco / 4.11:1 sobre navy | Tokens en `Color.kt` | `ColorContrastTest` (2 `@Ignore`) | Tokens Figma vs AA 4.5:1 | Ya existe `#8EABD4` (4.74:1). No usar `#7F9AC5` en texto chico. Badge verde: usar `OficiosColorSuccessText`. |
| QA-C01 | Error de resultados de búsqueda usa el título de home | Forzar error de red en resultados | `SearchResultsScreen.kt` → `R.string.home_error_title` = *No pudimos cargar el inicio* | Copy reutilizado | String propio: *No pudimos cargar los resultados*. |

### Baja

| ID | Qué | Repro | Evidencia | Causa probable | Fix sugerido |
|---|---|---|---|---|---|
| QA-B01 | `ClientHomeBottomBar` llama `hiltViewModel<HomeChromeViewModel>()` si `showFavorites == null` | Compose test de `HomeScreen` sin override | `HomeFlowComposeTest` (IllegalStateException Hilt en host sin Hilt) | Acoplamiento chrome/Hilt en el composable de pantalla | Inyectar `showFavorites` desde el Route; no resolver Hilt dentro del bottom bar. |
| QA-B02 | Login/Register no sobreviven process death | Matar proceso a mitad del form | No hay `SavedStateHandle` en esos VM | Form en memoria | `rememberSaveable` / SavedStateHandle para email y flags, no para password. |
| QA-B03 | `SUPPORT_PHONE` sigue en placeholder | Home/Ayuda *Contactar* | `FASE_9_RELEASE_READINESS.md` | Dato de negocio | Reemplazar `+5491112345678`. |

---

## Visual vs Figma (recomputado 2026-10-21)

Comparador: `tools/figma-diff/figma_diff.py` (440×980, crop status 48 + nav 24, umbral 10 de gris). Fuentes: `docs/qa-1a1/sources/*-figma.png` vs `*-p9.png`. Diffs nuevos: `docs/qa/diffs-2026-10-21/`.

| Pantalla | Nodo | Pixel-diff | Umbral 2 % | Captura |
|---|---|---|---|---|
| Home cliente | `25:95` | **9.92 %** | FAIL | `diffs-2026-10-21/home-cliente-diff.png` |
| Home trabajador (incompleto) | `191:11` | **15.93 %** | FAIL | `home-trabajador-diff.png` |
| Home trabajador (completo) | `259:534` | **33.57 %** | FAIL | `home-trabajador-completo-diff.png` |
| Solicitud / Agendar Cita | `613:1373` | **18.47 %** | FAIL | `solicitud-diff.png` |
| Agenda | `239:475` | **28.72 %** | FAIL | `agenda-diff.png` |
| Perfil | `231:15` | **29.00 %** | FAIL | `perfil-diff.png` |
| Ficha trabajador | `40:559` | **33.89 %** | FAIL | `ficha-trabajador-diff.png` |
| Detalle pedido | `198:697` | **36.02 %** | FAIL | `detalle-pedido-diff.png` |
| Mis pedidos | (placeholder) | **6.60 %** | FAIL | `mis-pedidos-diff.png` |

Roborazzi P9 está opt-in (`-Proborazzi.test.record=true`); **no se regeneraron goldens** en esta corrida. Los % coinciden con `docs/ESTADO_PLAN.md` ±0.5 pp (recorte del comparador).

Paparazzi: no está en el proyecto (solo Roborazzi).

---

## Accesibilidad

| Check | Resultado |
|---|---|
| `contentDescription` marca splash | PASS (`Oficios Formosa`, 2 nodos) |
| Mic buscador | PASS (`Buscar por voz`) |
| Filtros resultados | PASS (`Filtros` / `Filtros (N)`) |
| Notificaciones | PASS (`Notificaciones`) |
| Loading skeletons | PASS (home, pedidos, resultados) |
| Touch target ≥ 48 dp | **FAIL** Small 36 / Medium 44; Large 52 PASS |
| Contraste AA texto chico | PASS white/navy 11.16, action/white 4.74, secondary DS 4.74; **FAIL** Figma secondary 3.90, disabled/white 3.21, success/white 2.71 |
| TalkBack E2E | no verificable aquí |

---

## Red (MockWebServer, JSON alineado a DTOs/OpenAPI)

Cubierto en `core/network` (86 tests): login/signup/me/logout/reset, mis-solicitudes (favorito TINYINT), search trabajador nested `{data:{trabajador,solicitudes}}`, notifications bilingüe type/tipo, favoritos, catálogo, agenda slots, search workers.

Por cada API nueva: **200/201, lista vacía, 4xx, 5xx, timeout (OkHttp 80 ms + `NO_RESPONSE`), sin red (`DISCONNECT_AT_START`)**.

Mutaciones (create/cancel/favorito add-remove) **solo contra mock**.

---

## No verificable aquí

| Ítem | Por qué |
|---|---|
| Emulador 5554 navegación real | `adb devices` vacío |
| Login/GET contra API prod | Sin credenciales QA; política de no mutar prod |
| Contactar / Solicitar / Favorito en prod | Prohibido |
| Voz (RecognizerIntent es-AR) | Sin motor STT / dispositivo |
| Push FCM foreground/background/cold start | `FIREBASE_ENABLED` apagado (BK-09); sin `google-services.json` real |
| Maps en detalle | Sin `MAPS_API_KEY` |
| Instrumented androidTest (20 archivos, varios nuevos) | Requieren device; se agregaron pero **no se ejecutaron** |
| Contraste medido sobre captura real | Solo tokens; no screenshot WCAG overlay |

---

## Cobertura alcanzada

No hay Jacoco. Proxy: tests JVM ejecutados en esta sesión.

| Módulo | Tests | Fail | Skip |
|---|---:|---:|---:|
| domain | 76 | 0 | 0 |
| core/network | 86 | 0 | 0 |
| core/data | 64 | 0 | 0 |
| core/designsystem | 39 | 0 | 4 |
| feature/auth | 40 | 0 | 11 |
| feature/home | 151 | 0 | 23 |
| feature/onboarding | 41 | 0 | 0 |
| feature/settings | 13 | 0 | 1 |
| app | 2 | 0 | 0 |
| **Total** | **512** | **0** | **39** |

Skips: Roborazzi sin PNG committed + `@Ignore("QA-FAIL")` de a11y (botón Small/Medium y 2 pares de contraste).

**Agregado en este QA (tests, no producto):**

- Fixtures OpenAPI-like: `auth/`, `orders/`, `notifications/`, `favorites/`, `catalog/`, `agenda/`, `errors/`, `search/workers_search_empty.json`
- MockWebServer: Auth, Orders, Notifications, Favorites, Catalog, Agenda + errores en WorkerSearch
- `NetworkErrorMapperTest` Http 400/401/403/404/500
- Compose Robolectric: `AuthFlowComposeTest`, `HomeFlowComposeTest`
- androidTest (no ejecutados): splash, register Nombres/Apellido, forgot, home, orders list, search empty/error, settings logout
- A11y: `OficiosButtonA11yTest`, `ColorContrastTest`

**Huecos de cobertura que quedan:**

- E2E Compose contra backend mockeado con Hilt (hoy las pantallas se testean con `UiState` fake)
- `TokenAuthenticator` (refresh 401)
- Instrumented en device
- Roborazzi record + commit de goldens P9

---

## Comandos corridos

```bash
./gradlew :core:network:testDebugUnitTest
./gradlew :core:designsystem:testDebugUnitTest
./gradlew :domain:test :core:data:testDebugUnitTest
./gradlew :feature:auth:testDebugUnitTest :feature:home:testDebugUnitTest
./gradlew :feature:onboarding:testDebugUnitTest :feature:settings:testDebugUnitTest
./gradlew :app:assembleRelease   # R8 minify + lintVitalRelease PASS
adb devices                      # vacío
```

---

## Decisión pedida a Matías

Nada mergeado. Priorizar:

1. ¿Se acepta el umbral visual &gt; 2 % por adaptaciones (nav 4 tabs, D-DIN, sin fotos) o hay que bajar ficha/detalle (~34–36 %)?
2. ¿Hit-box 48 dp en botones Small/Medium aunque el fill siga 36/44?
3. ¿Correr androidTest + voz + push en un Pixel real con credenciales QA cuando las tenga?
