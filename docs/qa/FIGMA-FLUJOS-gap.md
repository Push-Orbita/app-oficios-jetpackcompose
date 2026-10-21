# Inventario y gap — Figma página Flujos

Fecha: 2026-10-21. File `hhKrcrusbr3h6qJxqYBiTd`, página **Flujos** (`3:2`).
Fuente: metadata XML + prototype reactions (`ON_CLICK` / `AFTER_TIMEOUT` → `NAVIGATE`).
No hay nodos `CONNECTOR`; las flechas del diagrama son prototipo, no líneas sueltas.

Convención: cada `screen` es un frame 440×980 llamado `screen`. Los textos listados son los primeros visibles (sin status bar).

## Resumen

| | Cantidad |
|---|---|
| Screens 440×980 en Flujos | 81 |
| Diagramas (sections) | 6 + 2 huérfanos |
| Transiciones de prototipo | 116 (112 a otro screen de Flujos) |
| Destino fuera de Flujos | `40:784` (variante de galería en Rediseño, `CHANGE_TO`) |

Muchas screens son **estados** del mismo flujo (login vacío / filled / error; OTP omitir / inválido / validado), no pantallas de producto distintas.

---

## 1. Diagrama de registro e Inicio de sesión #1 (`356:18`) — 26 screens

| Node | Rol (copy) | App actual |
|---|---|---|
| `356:22` | Splash | `AuthDestination.SPLASH` |
| `356:49` | Login vacío | `LOGIN` |
| `356:279` | Login con datos | mismo Login |
| `356:343` | Login error «Los datos de sesión no coinciden» | mismo Login + error |
| `366:1865` | Login validación «Complete los datos para ingresar» | mismo Login + campos vacíos |
| `356:104` | Home cliente (Nicolás Ayala) | `HomeDestination.inicio` |
| `356:482` | Elegí cómo registrarte | `REGISTER_ROLE` |
| `356:422` | Registro de usuario (cliente, Nombre completo) | `REGISTER_CLIENT` — app usa **Nombres + Apellido** (adaptación #21) |
| `366:1210` | Registro de cliente error contraseñas | mismo registro |
| `359:789` | Contacto + domicilio (alta) | onboarding `CLIENT_CONTACT_DOMICILIO` |
| `359:859` | Validación correo — Omitir | `VERIFY_EMAIL` |
| `363:911` | Validación correo — Código validado | mismo |
| `366:964` | Validación — Código inválido | mismo |
| `382:297` | Validación (otra variante Omitir) | mismo |
| `356:596` | Registro de oficio (categoría + rubro + especialización **en un frame**) | App **parte** en Elegí categoría + Elegí Rubro + especializaciones (`526:1026`, `526:903`) |
| `356:680` | Validación de Identidad (DNI frente/dorso) | `PROVIDER_DNI` + selfie |
| `366:1030` | Home cliente post-registro (grilla categorías) | Home / Oficios |
| `366:1317` | Home trabajador **Pendiente de validación** + Beneficios | `ProviderHome` + banner pendiente |
| `366:1553` / `376:2044` | Calendario general (config inicial) | `AgendaDestination.config` |
| `366:1737` | Home trabajador completo (oficios + beneficios) | `ProviderHome` éxito |
| `382:24` | Registro de usuario (rama trabajador) | `REGISTER_PROVIDER` |
| `382:71` | Contacto trabajador | onboarding contacto |
| `382:110` / `382:187` / `382:141` | OTP omitir / reenviar / validado | `VERIFY_EMAIL` |

### Transiciones clave

- Splash `AFTER_TIMEOUT` → Login.
- Login vacío: tap campos → filled; tap ingresar sin datos → «Complete los datos»; Registrarse → rol.
- Login filled: ingresar OK → Home cliente; ingresar mal → error; Registrarse → rol.
- Rol: Cliente → registro usuario; Trabajador → `356:596` oficio; Iniciar sesión → Login.
- Registro cliente Continuar → Contacto → OTP → Home / Oficios.
- Oficio Continuar → DNI → Calendario general → Home pendiente → (config) Home completo.

**Gap navegación:** Figma mete calendario general **en el alta** antes del home pendiente. La app deja Configurar Agenda como tile de Inicio (acordado: Agenda dentro de Inicio, no tab).

---

## 2. Completación de Perfil de Cliente (`383:334`) — 8 screens

Repite splash/login/rol/home. Lo específico:

| Node | Rol | App |
|---|---|---|
| `447:543` | Contacto **para poder continuar** (gate post-login) | `OnboardingGate` / `CLIENT_CONTACT_DOMICILIO` |
| `447:450` | Home cliente una vez completo | Home |

**Flujo:** login OK **sin** contacto → `447:543` (no home). Continuar → Home.

**App:** coincide en espíritu (gate de onboarding). Copy Figma voseo en el gate vs usted en app (adaptación).

---

## 3. Completación de Perfil de Trabajador (`447:665`) — 14 screens + 2 huérfanos

| Node | Rol | App |
|---|---|---|
| `447:1044` | Contacto para continuar | gate onboarding |
| `447:951` | Home **cliente** (Nicolás) — frame reutilizado en este diagrama | n/a en rama trabajador |
| `526:1026`…`527:294` | Elegí tu categoría (4 estados de selección) | onboarding / settings categoría |
| `526:1118` | Validación de Identidad | DNI |
| `526:1169` | Calendario general | Agenda config |
| `526:903` / `526:986` | **Elegí Rubro** (huérfanos, fuera de section) | settings / onboarding rubro |

**Flujo categoría:** Continuar → DNI → Calendario → Home pendiente (`366:1317`).

**Gap:** Figma muestra home cliente (`447:951`) como destino de un login en el diagrama de trabajador (probable copy-paste). La app enruta por rol.

---

## 4. Diagrama de Búsqueda (`451:1146`) — 14 screens

| Node | Rol | App |
|---|---|---|
| `454:467` | Home cliente (buscador) | Home |
| `453:21` | Oficios | `categories` |
| `453:99` | Rubros (Construcción) | `rubros` |
| `453:206` | Resultados (Albañiles) + aviso 3/10 min | `searchResults` |
| `453:343` | Ficha completa | `workerProfile` |
| `454:583` | Completa tus datos (gate al contactar) | onboarding contact si perfil < 100 % |
| `460:1017` / `455:27` | Completá tu Solicitud (con/sin ubicación) | `createOrder` |
| `468:1657` | **Solicitar Servicio** + alerta «requiere presupuesto» | **oculto** — [BK-15](../backend-requests/BK-15-tipo-solicitud.md) |
| `460:1245` | **Mis Ubicaciones** vacía + «Agrega la nueva ubicación» | **difiere** — la app abre `settings/client-data` (contactos + domicilios mezclados) |
| `460:1483` / `468:1578` | Mis Ubicaciones lista + favorito (estrella) | mismo gap |
| `455:423` | Resumen Solicitud | `createOrder` paso resumen |
| `475:65` | Home + toast «Solicitud enviada» | toast en home (no es pantalla nueva) |

### Transiciones

- Home Recientes/categoría → Oficios → Rubro → Resultados.
- Resultados **Contactar** → Completa tus datos (`454:583`) si faltan datos; no va directo a ficha.
- Ficha CTA → Completá tu Solicitud.
- Solicitud «ubicación» → Mis Ubicaciones → (añadir / favorito) → volver a Solicitud → Resumen.

**Gaps de navegación**

1. **Mis Ubicaciones** no existe como pantalla propia; `CreateOrder` navega a Ajustes → Datos de cliente.
2. Resultados Contactar → ficha o create-order según la app; Figma Búsqueda manda Contactar al **gate de datos**.
3. Segmento Presupuesto/Trabajo y `468:1657` sin backend (BK-15). No inventar.

---

## 5. Diagrama Configuración de Agenda (`526:15`) — 3 screens

| Node | Rol | App |
|---|---|---|
| `526:31` | Home trabajador (beneficios, oficios, **sin** lista de turnos) | `ProviderHome` |
| `526:667` | Home trabajador **con bloque Agenda** (marzo 2026, HOY) | `ProviderAgendaSection` **dentro de Inicio** (acordado; no tab Agenda) |
| `526:494` | Calendario general (Días de Atención + grilla) | `AgendaDestination.config` |

Figma no tiene tab «Agenda». Coincide con la regla del plan: Agenda dentro de Inicio.

---

## 6. Diagrama Reserva con Agenda (`613:34`) — 14 screens

| Node | Rol | App |
|---|---|---|
| `613:40` | Home cliente | Home |
| `613:134` / `613:214` | Oficios / Rubros | categories / rubros |
| `613:323` | Resultados | searchResults |
| `613:460` | Ficha | workerProfile |
| `613:1373` | **Agendar cita** (mini-card + calendario + horario) | `createOrder` cuando el trabajador tiene agenda |
| `613:572` | **Pedido Confirmado** | `orderDetail` título según estado |
| `613:637` / `613:704` / `613:906` | **Pedido Pendiente** (variantes) | mismo `orderDetail` |
| `613:982`…`613:1249` | **Terminar Pedido** (4 estados: código / confirmación) | `orderDetail` + diálogo código (`orders_title_finish`) |

### Transiciones

- Home Reciente → Resultados (saltea Oficios).
- Resultados Contactar → **ficha** (a diferencia del diagrama Búsqueda).
- Ficha → Agendar cita → Pedido.

**Gaps**

- Reciente en Figma salta categorías; la app puede ir a resultados o ficha según el recent guardado.
- Pedido Confirmado / Pendiente / Terminar son **un** composable con título dinámico, no 8 rutas. Estructura visual es el gap de Etapa 4, no de navegación.
- Ficha galería `CHANGE_TO` `40:784` (Rediseño).

---

## Contraste app vs Flujos (pantallas de producto)

Leyenda: **OK** existe y se navega; **DIFF** existe pero UI/flujo distinto; **MISS** no hay pantalla; **BK** falta backend.

| Pantalla Figma | Estado | Notas |
|---|---|---|
| Splash, Login, Rol, Registro, OTP, DNI | OK | Estados de error = misma ruta |
| Home cliente / Oficios / Rubros / Resultados / Ficha | OK | Etapa 4 itera ficha |
| Home trabajador + Agenda in-home | OK | Completa Tu Perfil solo &lt;100 % |
| Calendario general | OK | CTA Guardar (Figma no lo pinta) |
| Completá tu Solicitud / Agendar cita / Resumen | OK | Títulos Figma en voseo |
| Pedido Confirmado / Pendiente / Terminar | DIFF | Una ruta `orderDetail`; Etapa 4 |
| Gate «Completa tus datos» | OK | Onboarding / contacto |
| **Mis Ubicaciones** (3 estados) | MISS/DIFF | Backend de domicilios **sí**. Etapa 5: picker dedicado desde solicitud |
| Solicitar Servicio + presupuesto | BK | BK-15 |
| Forgot password / Ajustes / Notif / Favoritos / Ayuda / Pedidos lista | — | No están en Flujos (sí en Rediseño o app-only) |
| Editar Perfil `231:15` | — | **No está en Flujos**; vive en Rediseño. Etapa 4 usa Rediseño + Ajustes=Perfil |

### Navegación que no sigue el prototipo

| Acción Figma | Destino Figma | App |
|---|---|---|
| Login OK sin contacto | Gate contacto, no home | Gate onboarding (OK) |
| Resultados → Contactar (Búsqueda) | Completa tus datos | Ficha o create-order |
| Resultados → Contactar (Reserva) | Ficha | Ficha (OK en esta rama) |
| Solicitud → ubicación | Mis Ubicaciones | `settings/client-data` |
| Resumen OK | Home + toast | Home + toast (OK) |
| Alta trabajador | Oficio unificado → DNI → Calendario → Home | Categoría y rubro separados; agenda desde Inicio |
| Tab Agenda | No existe | No existe (OK) |

---

## Etapa 4 — fuente de verdad (estas 5)

| Plan | Flujos (preferido) | Rediseño (histórico) | Antes (QA 21/10) |
|---|---|---|---|
| Detalle pedido | `613:572` Confirmado; `613:637` Pendiente; `613:982` Terminar | `198:697` | ~36 % |
| Ficha trabajador | `613:460` / `453:343` | `40:559` | ~34 % |
| Home trabajador | `526:31` / `366:1737` + agenda `526:667` | `191:11` / `259:534` | ~34 % |
| Agenda | `526:494` (config) + bloque `526:667` | `239:475` | ~29 % |
| Perfil | no está en Flujos | `231:15` Editar Perfil | ~29 % |

Residual esperado aunque el layout cierre: D-DIN vs DIN Pro, usted vs voseo de títulos Figma, nav 4 tabs, datos de fixture vs copy de Figma.

---

## Etapa 5 — implementar vs documentar

**Implementado (hay backend):**

1. **Mis Ubicaciones** (`460:1245` / `460:1483` / `468:1578`) — picker dedicado desde Completá tu Solicitud.
   - Ruta `orders/locations?selectedId=`.
   - Lista, favorito (`setFavoriteDomicilio`), alta estructurada (barrio + calle + altura + mapa) y Guardar que vuelve a la solicitud.
   - No mezcla contactos (eso sigue en Ajustes → Datos de cliente).
   - El campo libre «Agrega la nueva ubicación» de Figma no alcanza: el API exige `barrioId` + calle + altura. Alta = formulario, no geocoder de texto libre.

**No inventar (pedido backend):**

- Presupuesto/Trabajo y `requierePresupuesto` → ya [BK-15](../backend-requests/BK-15-tipo-solicitud.md).
- Chip **Efectivo** / medio de pago en detalle de pedido → [BK-21](../backend-requests/BK-21-metodo-pago-pedido.md). El monto solo se muestra si hay `nuevoPrecio` de contrapropuesta.
- Cualquier campo de solicitud que no esté en OpenAPI.

**Fuera de alcance de Flujos (no son pantallas faltantes):** Forgot, lista de pedidos, notificaciones, favoritos, ayuda, Ajustes.
