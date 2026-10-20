# Mapa Figma ↔ App (Oficios Formosa)

Archivo Figma `hhKrcrusbr3h6qJxqYBiTd`. Repo `Push-Orbita/app-oficios-jetpackcompose` `main` @ `51f80de` (sin cambios después del `git fetch`, 20/10/2026). Las rutas son relativas al repo.

**Totales:** 4 páginas (Thumbnail 0:1, Rediseño 3:3, Flujos 3:2, Re-Coloring 537:17). Hay 122 ítems en el manifiesto: 112 frames de pantalla y 10 assets o diagramas. Por página: Rediseño, 30 pantallas + 2 frames auxiliares; Flujos, 82 pantallas + 1 frame + 7 diagramas/imágenes. Los frames se agrupan en **33 diseños únicos**; el resto son estados o duplicados entre flujos.

**Estado por frame:** matches 7 · differs 98 · missing in app 7. Aparte hay 16 pantallas **app-only** (ver al final).

Criterio: *matches* = misma estructura, copy y componentes; solo cambia la fuente o algún token global. *differs* = estructura, copy o componentes distintos. *missing in app* = no hay un Composable equivalente.

Contact sheet: `contact-sheet.png`. PNG de cada frame en `screens/<page>/<nn>-<slug>.png`.

## Diseños únicos → Composable

| Tipo | Frames (node ids) | Composable | Estado | Nota |
|---|---|---|---|---|
| login | R01 `16:517`, R03 `16:245`, F02 `356:49`, F03 `356:279`, F30 `447:193`, F31 `447:250`, F38 `447:694`, F39 `447:751` | `feature/auth/src/main/java/com/cocido/oficios/feature/auth/login/LoginScreen.kt` | **differs** | Copy “Ingrese sus datos…” (Figma) vs “Ingresá tus datos…”, “Registrarse” vs “Registrate”. Wordmark 48 DIN Bold. Campos de 48 dp (app 56). Figma R01 usa D.N.I. y Flujos usa Correo: confirmar cuál. |
| splash | R02 `16:2`, F01 `356:22`, F29 `447:166`, F37 `447:667` | `feature/auth/src/main/java/com/cocido/oficios/feature/auth/splash/SplashScreen.kt` | **matches** | Solo cambia la fuente (DIN Pro falta). |
| reg_user | R04 `16:572`, F07 `356:422`, F08 `366:1210`, F22 `382:24` | `feature/auth/src/main/java/com/cocido/oficios/feature/auth/register/RegisterScreens.kt#RegisterFormScreen` | **differs** | Copy “Ingrese…” vs “Ingresá…”; error “Las contraseñas no coinciden” bajo el campo; altura de campo 48. |
| home_cli | R05 `25:95`, F06 `356:104`, F34 `447:450`, F42 `447:951`, F59 `454:467`, F77 `613:40` | `feature/home/src/main/java/com/cocido/oficios/feature/home/HomeScreen.kt` | **differs** | Ver diff detallado: falta “Recientes” como cards de rubro, card de ayuda “Contactar”, mic; sobran “Mis pedidos” y “Destacados”; padding 32 vs 16. |
| oficios | R06 `25:258`, F16 `366:1030`, F55 `453:21`, F78 `613:134` | `feature/home/src/main/java/com/cocido/oficios/feature/home/CategoriesScreen.kt` | **differs** | Grilla 2 col con ilustraciones propias (no Material icons), cards de 16 dp de radio y borde #004DA8; título 36 + chevron; buscador “¿Qué oficio estás buscando?”. |
| construccion | R07 `35:2`, F56 `453:99`, F79 `613:214` | `feature/home/src/main/java/com/cocido/oficios/feature/home/rubros/RubrosScreen.kt` | **differs** | En Figma es una grilla 2 col de cards con ilustración por rubro; la app muestra una lista. Título 36. |
| resultados | R08 `40:309`, F57 `453:206`, F80 `613:323` | `feature/home/src/main/java/com/cocido/oficios/feature/home/search/SearchResultsScreen.kt + core/.../cards/WorkerCard.kt` | **differs** | Header con logo y título “Albañiles de Oficios Formosa” 36, aviso de límite de contacto, buscador “¿Buscas algún albañil en específico?”, cards con 5 estrellas, “Contactar” azul y “Ver ficha completa” blanco. La app tiene chips/filtros y no tiene ni el header ni el aviso. |
| ficha | R09 `40:559`, F58 `453:343`, F81 `613:460` | `feature/home/src/main/java/com/cocido/oficios/feature/home/worker/WorkerProfileScreen.kt` | **differs** | Título “Ficha Completa” 36, avatar 64 r8, 5 estrellas + corazón, bio, “Oficios:”, “Especializado en:”, Contactar/Calificar (Rediseño) o “Solicitar Servicios” (Flujos), “Trabajos realizados” y certificados. La app usa topbar M3, “Contratar” y “Denunciar”. |
| reg_trade | R10 `25:337`, F14 `356:596` | `feature/auth/src/main/java/com/cocido/oficios/feature/auth/register/RegisterTradeScreen.kt` | **differs** | Figma Rediseño/F14 usa selects; la versión nueva de Flujos (“Elegí tu categoría” 526:1026…) usa una grilla de categorías multiselección + radios de especialización. La app usa dropdowns. |
| identidad | R11 `25:767`, F15 `356:680`, F50 `526:1118` | `feature/onboarding/src/main/java/com/cocido/oficios/feature/onboarding/steps/OnboardingStepScreens.kt#ProviderDniStepScreen + ProviderSelfieStepScreen` | **differs** | Figma tiene una sola pantalla con Frente, Dorso, Foto del rostro y Certificado (opcional) + “Continuar”. La app lo reparte en 2–3 pasos con cards “Tocá para cargar”. |
| home_trab | R13 `191:11`, R20 `259:17`, R21 `259:254`, F17 `366:1317`, F20 `366:1737`, F73 `526:31` | `feature/home/src/main/java/com/cocido/oficios/feature/home/providerhome/ProviderHomeScreen.kt` | **differs** | Beneficios (3 cards), “Completa Tu Perfil” con barra 80 % + 3 tiles (galería/local/certificado), Agenda calendario + lista por fecha con chevron, “ver agenda completa”. En Flujos aparece una card “Calendario → Configurar Agenda” y el badge “Pendiente de validación”. |
| pedido_conf | R14 `198:697`, F83 `613:572` | `feature/home/src/main/java/com/cocido/oficios/feature/home/orders/OrderDetailScreen.kt` | **differs** | Título por estado 36, mapa embebido 248 dp, filas con icon box 40 dp #004DA8, chip “Efectivo”, CTAs Contacto/“Estoy en camino”. La app: topbar, timeline de progreso, Material icons, mapa por intent. |
| pedido_pend | R15 `210:29`, R26 `210:531`, R27 `215:769`, F84 `613:637`, F85 `613:704`, F86 `613:906` | `feature/home/src/main/java/com/cocido/oficios/feature/home/orders/OrderDetailScreen.kt + orders/CounterOfferScreen.kt` | **differs** | Worker: Rechazar (texto) / Reagendar (azul) / Aceptar (blanco). Reagendar abre un bottom sheet (calendario + hora ± + tiempo actual/propuesto + Cancelar/Enviar Reagenda). La app va a CounterOfferScreen full-screen. |
| perfil | R16 `198:373` | `feature/settings/src/main/java/com/cocido/oficios/feature/settings/provider/ProfessionalProfileScreen.kt + PortfolioScreen.kt` | **differs** | Figma “Perfil” es una sola pantalla (foto, descripción, oficios, especializado en, trabajos realizados, certificados) con “Editar”. La app lo reparte en Ajustes → Presentación profesional / Galería y certificados. |
| editar_perfil | R17 `224:1077`, R18 `230:20`, R19 `231:15` | `feature/settings/src/main/java/com/cocido/oficios/feature/settings/provider/ProfessionalProfileScreen.kt + PortfolioScreen.kt + ServicesScreens.kt` | **differs** | Edición inline: foto (Eliminar/Inspeccionar), descripción, oficios, “Editar Rubro o Especialidad”, galería con “+ Añadir”, certificados con estado Validado/Pendiente y “Darme de baja”. |
| agenda_trab | R22 `259:534`, F74 `526:667` | `feature/home/src/main/java/com/cocido/oficios/feature/home/agenda/AgendaListScreen.kt` | **differs** | Agenda con calendario y lista agrupada por fecha (cards con barra lateral #006DEF “hoy”). |
| agenda_completa | R24 `210:106` | `feature/home/src/main/java/com/cocido/oficios/feature/home/agenda/AgendaListScreen.kt` | **differs** | “Agenda Completa”: calendario + lista con checkboxes por servicio (Servicio de Albañilería / Presupuesto…) + “ajustar calendario”. |
| calendario | R25 `239:475`, F18 `366:1553`, F19 `376:2044`, F51 `526:1169`, F75 `526:494` | `feature/home/src/main/java/com/cocido/oficios/feature/home/agenda/AgendaConfigScreen.kt + agenda/AgendaListScreen.kt` | **differs** | Mayor diferencia: Días de Atención (7 chips), División de Horarios (30 min/1 h/2 h/custom), grilla de horas 4×6 seleccionables (máx. 12 h/día), “Calendario Por Oficio” con checkbox por oficio. La app: campos numéricos de duración/intervalo + CRUD de slots. |
| terminar | R28 `255:17`, R29 `255:101`, R30 `255:199`, R31 `255:331`, R32 `255:452`, F87 `613:982`, F88 `613:1049`, F89 `613:1132`, F90 `613:1249` | `feature/home/src/main/java/com/cocido/oficios/feature/home/orders/OrderDetailScreen.kt (CompletionCodeCard/FinalizeCodeDialog) + orders/RateWorkerScreen.kt` | **differs** | Worker: input “Ingresar código del cliente” + “Validar”. Cliente: “Código: 3456” + diálogo informativo con “No volver a mostrar”. Calificación en un bottom sheet (5 estrellas 40 dp + comentario + Enviar/Enviado). La app: diálogo + pantalla RateWorker separada con 2 criterios (calidad/puntualidad). |
| login_err | F04 `356:343`, F05 `366:1865`, F32 `447:315`, F33 `447:383`, F40 `447:816`, F41 `447:884` | `feature/auth/src/main/java/com/cocido/oficios/feature/auth/login/LoginScreen.kt` | **differs** | Error arriba de los campos con lucide/circle-alert y borde #A32F56; la app usa el error inline M3 (#BA6380). |
| contacto | F09 `359:789`, F23 `382:71`, F35 `447:543`, F43 `447:1044` | `feature/onboarding/src/main/java/com/cocido/oficios/feature/onboarding/steps/OnboardingStepScreens.kt#ClientContactDomicilioStepScreen` | **differs** | En Figma es una sola pantalla “Contacto” (Teléfono + Domicilio + Barrio/Piso/Dpto) con “Continuar”; la app la arma por pasos del checklist y con topbar M3. |
| validacion | F10 `359:859`, F11 `363:911`, F12 `366:964`, F24 `382:110`, F25 `382:187`, F26 `382:141`, F27 `382:297` | `feature/onboarding/src/main/java/com/cocido/oficios/feature/onboarding/verify/VerifyEmailStepScreen.kt` | **differs** | Figma: 4 casillas de código, “Abrir correo”, “Omitir”, cuenta regresiva “Reenviar código en 00:56”, estados validado e inválido. La app usa 6 dígitos (la API pide 6: mantener 6, adaptación). No tiene “Abrir correo” ni “Omitir”. |
| reg_role | F13 `356:482`, F36 `447:573`, F44 `447:1074` | `feature/auth/src/main/java/com/cocido/oficios/feature/auth/register/RegisterScreens.kt#RegisterRoleSelectionScreen` | **matches** | Medidas ya tomadas de 356:482 (ComponentSize.registerRoleCard*). |
| elegi_cat | F46 `526:1026`, F47 `527:57`, F48 `527:168`, F49 `527:294` | `feature/auth/src/main/java/com/cocido/oficios/feature/auth/register/RegisterTradeScreen.kt / feature/onboarding/src/main/java/com/cocido/oficios/feature/onboarding/steps (ProviderRubrosStepScreen)` | **missing in app** | Grilla 2 columnas de categorías seleccionables (multi), después rubro y especialización con radios y “Otro:”. Hoy son dropdowns. |
| elegi_rubro | F52 `526:903`, F53 `526:986` | `feature/settings/src/main/java/com/cocido/oficios/feature/settings/provider/ServicesScreens.kt#AddServiceScreen (paso trade)` | **missing in app** | “Elegí Rubro”: grilla de categorías, seleccionada en #004DA8, y “Continuar” deshabilitado/habilitado. |
| home_cli_toast | F60 `475:65` | `feature/home/src/main/java/com/cocido/oficios/feature/home/HomeScreen.kt + core/designsystem/.../feedback/OficiosSnackbar.kt` | **differs** | Snackbar “Solicitud enviada:” (lucide/circle-check + x) al volver a Home después de enviar. Hoy no se muestra (2e). |
| completa_datos | F61 `454:583` | `feature/onboarding/src/main/java/com/cocido/oficios/feature/onboarding/steps/OnboardingStepScreens.kt#ClientContactDomicilioStepScreen (gate de CreateOrder)` | **differs** | Se muestra en el flujo de búsqueda cuando faltan contacto/domicilio. En la app, CreateOrder muestra un InlineAlert (create_order_profile_required). |
| solicitud_form | F62 `455:27`, F63 `460:1017` | `feature/home/src/main/java/com/cocido/oficios/feature/home/orders/CreateOrderScreen.kt` | **differs** | Figma: Modalidad (Presupuesto/Trabajo segmentado), descripción, Ubicación (lista de direcciones con estrella de favorita + “Añadir nueva ubicación”), Fecha y hora (calendario + grilla de horas 4 col), “Continuar”. La app: dropdowns + slots + DateTimePickerButton. |
| solicitar_serv | F64 `468:1657` | `feature/home/src/main/java/com/cocido/oficios/feature/home/orders/CreateOrderScreen.kt` | **differs** | Variante con presupuesto obligatorio (InlineAlert “Este trabajador requiere un presupuesto…”) y días deshabilitados #231D38. |
| ubicaciones | F65 `460:1245`, F66 `460:1483`, F67 `468:1578` | `feature/settings/src/main/java/com/cocido/oficios/feature/settings/client/ClientDataScreen.kt` | **differs** | “Mis Ubicaciones”: lista con estrella de favorita, “Marcar en el mapa” (mapa 475:14), “Guardar”. La app: “Contactos y domicilios” sin mapa ni favorita visible. |
| solicitud_resumen | F68 `455:423` | `feature/home/src/main/java/com/cocido/oficios/feature/home/orders/CreateOrderScreen.kt` | **missing in app** | Pantalla resumen “Solicitud” (mapa + Detalle/Dirección/Tiempo de visita + “Realizar cambios”/“Enviar Solicitud”). La app envía directo. |
| reagendar | F71 `455:488` | `feature/home/src/main/java/com/cocido/oficios/feature/home/orders/CounterOfferScreen.kt` | **differs** | En Figma es un bottom sheet, no una pantalla. |
| agendar | F82 `613:1373` | `feature/home/src/main/java/com/cocido/oficios/feature/home/orders/CreateOrderScreen.kt` | **differs** | “Agendar Cita” (reserva con agenda): card del trabajador, calendario mensual, grilla de horarios del día. La app lista slots en una LazyColumn. |

## Frame por frame

### Rediseño (3:3)

| # | Node | Título | Sección | PNG | Tipo | Estado |
|---|---|---|---|---|---|---|
| 01 | `16:517` | Ingrese sus datos para iniciar sesión |  | `screens/redisenio/01-ingrese-sus-datos-para-iniciar-sesion.png` | login | differs |
| 02 | `16:2` | oficios formosa |  | `screens/redisenio/02-oficios-formosa.png` | splash | matches |
| 03 | `16:245` | Ingrese sus datos para iniciar sesión |  | `screens/redisenio/03-ingrese-sus-datos-para-iniciar-sesion.png` | login | differs |
| 04 | `16:572` | Ingrese sus datos para crear su cuenta |  | `screens/redisenio/04-ingrese-sus-datos-para-crear-su-cuenta.png` | reg_user | differs |
| 05 | `25:95` | Nicolas Ayala |  | `screens/redisenio/05-nicolas-ayala.png` | home_cli | differs |
| 06 | `25:258` | Oficios |  | `screens/redisenio/06-oficios.png` | oficios | differs |
| 07 | `35:2` | Construcción |  | `screens/redisenio/07-construccion.png` | construccion | differs |
| 08 | `40:309` | Albañiles de oficios formosa |  | `screens/redisenio/08-albaniles-de-oficios-formosa.png` | resultados | differs |
| 09 | `40:559` | ficha completa |  | `screens/redisenio/09-ficha-completa.png` | ficha | differs |
| 10 | `25:337` | Registro de oficio |  | `screens/redisenio/10-registro-de-oficio.png` | reg_trade | differs |
| 11 | `25:767` | Validación de Identidad |  | `screens/redisenio/11-validacion-de-identidad.png` | identidad | differs |
| 12 | `40:752` | Frame 929 |  | `screens/redisenio/12-frame-929.png` | asset | n/a |
| 13 | `191:11` | Emanuel Molina |  | `screens/redisenio/13-emanuel-molina.png` | home_trab | differs |
| 14 | `198:697` | Pedido Confirmado |  | `screens/redisenio/14-pedido-confirmado.png` | pedido_conf | differs |
| 15 | `210:29` | Pedido Pendiente |  | `screens/redisenio/15-pedido-pendiente.png` | pedido_pend | differs |
| 16 | `198:373` | Perfil |  | `screens/redisenio/16-perfil.png` | perfil | differs |
| 17 | `224:1077` | Editar Perfil |  | `screens/redisenio/17-editar-perfil.png` | editar_perfil | differs |
| 18 | `230:20` | Editar Perfil |  | `screens/redisenio/18-editar-perfil.png` | editar_perfil | differs |
| 19 | `231:15` | Editar Perfil |  | `screens/redisenio/19-editar-perfil.png` | editar_perfil | differs |
| 20 | `259:17` | Emanuel Molina |  | `screens/redisenio/20-emanuel-molina.png` | home_trab | differs |
| 21 | `259:254` | Emanuel Molina |  | `screens/redisenio/21-emanuel-molina.png` | home_trab | differs |
| 22 | `259:534` | Emanuel Molina |  | `screens/redisenio/22-emanuel-molina.png` | agenda_trab | differs |
| 23 | `40:783` | Frame 954 |  | `screens/redisenio/23-frame-954.png` | asset | n/a |
| 24 | `210:106` | Agenda completa |  | `screens/redisenio/24-agenda-completa.png` | agenda_completa | differs |
| 25 | `239:475` | Calendario general |  | `screens/redisenio/25-calendario-general.png` | calendario | differs |
| 26 | `210:531` | Pedido Pendiente |  | `screens/redisenio/26-pedido-pendiente.png` | pedido_pend | differs |
| 27 | `215:769` | Pedido Pendiente |  | `screens/redisenio/27-pedido-pendiente.png` | pedido_pend | differs |
| 28 | `255:17` | Terminar Pedido |  | `screens/redisenio/28-terminar-pedido.png` | terminar | differs |
| 29 | `255:101` | Terminar Pedido |  | `screens/redisenio/29-terminar-pedido.png` | terminar | differs |
| 30 | `255:199` | Terminar Pedido |  | `screens/redisenio/30-terminar-pedido.png` | terminar | differs |
| 31 | `255:331` | Terminar Pedido |  | `screens/redisenio/31-terminar-pedido.png` | terminar | differs |
| 32 | `255:452` | Terminar Pedido |  | `screens/redisenio/32-terminar-pedido.png` | terminar | differs |

### Flujos (3:2)

| # | Node | Título | Sección | PNG | Tipo | Estado |
|---|---|---|---|---|---|---|
| 01 | `356:22` | oficios formosa | Diagrama de registro e Inicio de sesión  | `screens/flujos/01-oficios-formosa.png` | splash | matches |
| 02 | `356:49` | Ingrese sus datos para iniciar sesión | Diagrama de registro e Inicio de sesión  | `screens/flujos/02-ingrese-sus-datos-para-iniciar-sesion.png` | login | differs |
| 03 | `356:279` | Ingrese sus datos para iniciar sesión | Diagrama de registro e Inicio de sesión  | `screens/flujos/03-ingrese-sus-datos-para-iniciar-sesion.png` | login | differs |
| 04 | `356:343` | Los datos de sesión no coinciden | Diagrama de registro e Inicio de sesión  | `screens/flujos/04-los-datos-de-sesion-no-coinciden.png` | login_err | differs |
| 05 | `366:1865` | Complete los datos para ingresar | Diagrama de registro e Inicio de sesión  | `screens/flujos/05-complete-los-datos-para-ingresar.png` | login_err | differs |
| 06 | `356:104` | Nicolas Ayala | Diagrama de registro e Inicio de sesión  | `screens/flujos/06-nicolas-ayala.png` | home_cli | differs |
| 07 | `356:422` | Registro de usuario | Diagrama de registro e Inicio de sesión  | `screens/flujos/07-registro-de-usuario.png` | reg_user | differs |
| 08 | `366:1210` | Registro de cliente | Diagrama de registro e Inicio de sesión  | `screens/flujos/08-registro-de-cliente.png` | reg_user | differs |
| 09 | `359:789` | Contacto | Diagrama de registro e Inicio de sesión  | `screens/flujos/09-contacto.png` | contacto | differs |
| 10 | `359:859` | Validación | Diagrama de registro e Inicio de sesión  | `screens/flujos/10-validacion.png` | validacion | differs |
| 11 | `363:911` | Validación | Diagrama de registro e Inicio de sesión  | `screens/flujos/11-validacion.png` | validacion | differs |
| 12 | `366:964` | Registro de cliente | Diagrama de registro e Inicio de sesión  | `screens/flujos/12-registro-de-cliente.png` | validacion | differs |
| 13 | `356:482` | Elegí cómo registrarte | Diagrama de registro e Inicio de sesión  | `screens/flujos/13-elegi-como-registrarte.png` | reg_role | matches |
| 14 | `356:596` | Registro de oficio | Diagrama de registro e Inicio de sesión  | `screens/flujos/14-registro-de-oficio.png` | reg_trade | differs |
| 15 | `356:680` | Validación de Identidad | Diagrama de registro e Inicio de sesión  | `screens/flujos/15-validacion-de-identidad.png` | identidad | differs |
| 16 | `366:1030` | Nicolas Cabrayala | Diagrama de registro e Inicio de sesión  | `screens/flujos/16-nicolas-cabrayala.png` | oficios | differs |
| 17 | `366:1317` | Emanuel Molina | Diagrama de registro e Inicio de sesión  | `screens/flujos/17-emanuel-molina.png` | home_trab | differs |
| 18 | `366:1553` | Calendario general | Diagrama de registro e Inicio de sesión  | `screens/flujos/18-calendario-general.png` | calendario | differs |
| 19 | `376:2044` | Calendario general | Diagrama de registro e Inicio de sesión  | `screens/flujos/19-calendario-general.png` | calendario | differs |
| 20 | `366:1737` | Emanuel Molina | Diagrama de registro e Inicio de sesión  | `screens/flujos/20-emanuel-molina.png` | home_trab | differs |
| 21 | `382:22` | image 1 | Diagrama de registro e Inicio de sesión  | `screens/flujos/21-image-1.png` | asset | n/a |
| 22 | `382:24` | Registro de usuario | Diagrama de registro e Inicio de sesión  | `screens/flujos/22-registro-de-usuario.png` | reg_user | differs |
| 23 | `382:71` | Contacto | Diagrama de registro e Inicio de sesión  | `screens/flujos/23-contacto.png` | contacto | differs |
| 24 | `382:110` | Validación | Diagrama de registro e Inicio de sesión  | `screens/flujos/24-validacion.png` | validacion | differs |
| 25 | `382:187` | Validación | Diagrama de registro e Inicio de sesión  | `screens/flujos/25-validacion.png` | validacion | differs |
| 26 | `382:141` | Validación | Diagrama de registro e Inicio de sesión  | `screens/flujos/26-validacion.png` | validacion | differs |
| 27 | `382:297` | Validación | Diagrama de registro e Inicio de sesión  | `screens/flujos/27-validacion.png` | validacion | differs |
| 28 | `445:6` | completar-perfil-cliente 1 | Diagrama de Completación de Perfil de Cl | `screens/flujos/28-completar-perfil-cliente-1.png` | asset | n/a |
| 29 | `447:166` | oficios formosa | Diagrama de Completación de Perfil de Cl | `screens/flujos/29-oficios-formosa.png` | splash | matches |
| 30 | `447:193` | Ingrese sus datos para iniciar sesión | Diagrama de Completación de Perfil de Cl | `screens/flujos/30-ingrese-sus-datos-para-iniciar-sesion.png` | login | differs |
| 31 | `447:250` | Ingrese sus datos para iniciar sesión | Diagrama de Completación de Perfil de Cl | `screens/flujos/31-ingrese-sus-datos-para-iniciar-sesion.png` | login | differs |
| 32 | `447:315` | Los datos de sesión no coinciden | Diagrama de Completación de Perfil de Cl | `screens/flujos/32-los-datos-de-sesion-no-coinciden.png` | login_err | differs |
| 33 | `447:383` | Complete los datos para ingresar | Diagrama de Completación de Perfil de Cl | `screens/flujos/33-complete-los-datos-para-ingresar.png` | login_err | differs |
| 34 | `447:450` | Nicolas Ayala | Diagrama de Completación de Perfil de Cl | `screens/flujos/34-nicolas-ayala.png` | home_cli | differs |
| 35 | `447:543` | Contacto | Diagrama de Completación de Perfil de Cl | `screens/flujos/35-contacto.png` | contacto | differs |
| 36 | `447:573` | Elegí cómo registrarte | Diagrama de Completación de Perfil de Cl | `screens/flujos/36-elegi-como-registrarte.png` | reg_role | matches |
| 37 | `447:667` | oficios formosa | Diagrama de Completación de Perfil de Tr | `screens/flujos/37-oficios-formosa.png` | splash | matches |
| 38 | `447:694` | Ingrese sus datos para iniciar sesión | Diagrama de Completación de Perfil de Tr | `screens/flujos/38-ingrese-sus-datos-para-iniciar-sesion.png` | login | differs |
| 39 | `447:751` | Ingrese sus datos para iniciar sesión | Diagrama de Completación de Perfil de Tr | `screens/flujos/39-ingrese-sus-datos-para-iniciar-sesion.png` | login | differs |
| 40 | `447:816` | Los datos de sesión no coinciden | Diagrama de Completación de Perfil de Tr | `screens/flujos/40-los-datos-de-sesion-no-coinciden.png` | login_err | differs |
| 41 | `447:884` | Complete los datos para ingresar | Diagrama de Completación de Perfil de Tr | `screens/flujos/41-complete-los-datos-para-ingresar.png` | login_err | differs |
| 42 | `447:951` | Nicolas Ayala | Diagrama de Completación de Perfil de Tr | `screens/flujos/42-nicolas-ayala.png` | home_cli | differs |
| 43 | `447:1044` | Contacto | Diagrama de Completación de Perfil de Tr | `screens/flujos/43-contacto.png` | contacto | differs |
| 44 | `447:1074` | Elegí cómo registrarte | Diagrama de Completación de Perfil de Tr | `screens/flujos/44-elegi-como-registrarte.png` | reg_role | matches |
| 45 | `447:1134` | completar-perfil-to 1 | Diagrama de Completación de Perfil de Tr | `screens/flujos/45-completar-perfil-to-1.png` | asset | n/a |
| 46 | `526:1026` | Elegí tu categoría | Diagrama de Completación de Perfil de Tr | `screens/flujos/46-elegi-tu-categoria.png` | elegi_cat | missing in app |
| 47 | `527:57` | Elegí tu categoría | Diagrama de Completación de Perfil de Tr | `screens/flujos/47-elegi-tu-categoria.png` | elegi_cat | missing in app |
| 48 | `527:168` | Elegí tu categoría | Diagrama de Completación de Perfil de Tr | `screens/flujos/48-elegi-tu-categoria.png` | elegi_cat | missing in app |
| 49 | `527:294` | Elegí tu categoría | Diagrama de Completación de Perfil de Tr | `screens/flujos/49-elegi-tu-categoria.png` | elegi_cat | missing in app |
| 50 | `526:1118` | Validación de Identidad | Diagrama de Completación de Perfil de Tr | `screens/flujos/50-validacion-de-identidad.png` | identidad | differs |
| 51 | `526:1169` | Calendario general | Diagrama de Completación de Perfil de Tr | `screens/flujos/51-calendario-general.png` | calendario | differs |
| 52 | `526:903` | Elegí Rubro |  | `screens/flujos/52-elegi-rubro.png` | elegi_rubro | missing in app |
| 53 | `526:986` | Elegí Rubro |  | `screens/flujos/53-elegi-rubro.png` | elegi_rubro | missing in app |
| 54 | `451:1145` | busqueda 1 | Diagrama de Búsqueda | `screens/flujos/54-busqueda-1.png` | asset | n/a |
| 55 | `453:21` | Oficios | Diagrama de Búsqueda | `screens/flujos/55-oficios.png` | oficios | differs |
| 56 | `453:99` | Construcción | Diagrama de Búsqueda | `screens/flujos/56-construccion.png` | construccion | differs |
| 57 | `453:206` | Albañiles de oficios formosa | Diagrama de Búsqueda | `screens/flujos/57-albaniles-de-oficios-formosa.png` | resultados | differs |
| 58 | `453:343` | ficha completa | Diagrama de Búsqueda | `screens/flujos/58-ficha-completa.png` | ficha | differs |
| 59 | `454:467` | Nicolas Ayala | Diagrama de Búsqueda | `screens/flujos/59-nicolas-ayala.png` | home_cli | differs |
| 60 | `475:65` | Nicolas Ayala | Diagrama de Búsqueda | `screens/flujos/60-nicolas-ayala.png` | home_cli_toast | differs |
| 61 | `454:583` | Completa tus datos | Diagrama de Búsqueda | `screens/flujos/61-completa-tus-datos.png` | completa_datos | differs |
| 62 | `455:27` | Completá tu Solicitud | Diagrama de Búsqueda | `screens/flujos/62-completa-tu-solicitud.png` | solicitud_form | differs |
| 63 | `460:1017` | Completá tu Solicitud | Diagrama de Búsqueda | `screens/flujos/63-completa-tu-solicitud.png` | solicitud_form | differs |
| 64 | `468:1657` | Solicitar Servicio | Diagrama de Búsqueda | `screens/flujos/64-solicitar-servicio.png` | solicitar_serv | differs |
| 65 | `460:1245` | Mis Ubicaciones | Diagrama de Búsqueda | `screens/flujos/65-mis-ubicaciones.png` | ubicaciones | differs |
| 66 | `460:1483` | Mis Ubicaciones | Diagrama de Búsqueda | `screens/flujos/66-mis-ubicaciones.png` | ubicaciones | differs |
| 67 | `468:1578` | Mis Ubicaciones | Diagrama de Búsqueda | `screens/flujos/67-mis-ubicaciones.png` | ubicaciones | differs |
| 68 | `455:423` | Solicitud | Diagrama de Búsqueda | `screens/flujos/68-solicitud.png` | solicitud_resumen | missing in app |
| 69 | `464:1577` | Frame 1030 | Diagrama de Búsqueda | `screens/flujos/69-frame-1030.png` | asset | n/a |
| 70 | `475:14` | snazzy-image (4) 1 | Diagrama de Búsqueda | `screens/flujos/70-snazzy-image-4-1.png` | asset | n/a |
| 71 | `455:488` | Reagendar pedido |  | `screens/flujos/71-reagendar-pedido.png` | reagendar | differs |
| 72 | `526:13` | configurar-agenda 1 | Diagrama Configuración de Agenda | `screens/flujos/72-configurar-agenda-1.png` | asset | n/a |
| 73 | `526:31` | Emanuel Molina | Diagrama Configuración de Agenda | `screens/flujos/73-emanuel-molina.png` | home_trab | differs |
| 74 | `526:667` | Emanuel Molina | Diagrama Configuración de Agenda | `screens/flujos/74-emanuel-molina.png` | agenda_trab | differs |
| 75 | `526:494` | Calendario general | Diagrama Configuración de Agenda | `screens/flujos/75-calendario-general.png` | calendario | differs |
| 76 | `613:33` | reserva-con-agenda-cliente 1 | Diagrama Reserva con Agenda | `screens/flujos/76-reserva-con-agenda-cliente-1.png` | asset | n/a |
| 77 | `613:40` | Nicolas Ayala | Diagrama Reserva con Agenda | `screens/flujos/77-nicolas-ayala.png` | home_cli | differs |
| 78 | `613:134` | Oficios | Diagrama Reserva con Agenda | `screens/flujos/78-oficios.png` | oficios | differs |
| 79 | `613:214` | Construcción | Diagrama Reserva con Agenda | `screens/flujos/79-construccion.png` | construccion | differs |
| 80 | `613:323` | Albañiles de oficios formosa | Diagrama Reserva con Agenda | `screens/flujos/80-albaniles-de-oficios-formosa.png` | resultados | differs |
| 81 | `613:460` | ficha completa | Diagrama Reserva con Agenda | `screens/flujos/81-ficha-completa.png` | ficha | differs |
| 82 | `613:1373` | Agendar cita | Diagrama Reserva con Agenda | `screens/flujos/82-agendar-cita.png` | agendar | differs |
| 83 | `613:572` | Pedido Confirmado | Diagrama Reserva con Agenda | `screens/flujos/83-pedido-confirmado.png` | pedido_conf | differs |
| 84 | `613:637` | Pedido Pendiente | Diagrama Reserva con Agenda | `screens/flujos/84-pedido-pendiente.png` | pedido_pend | differs |
| 85 | `613:704` | Pedido Pendiente | Diagrama Reserva con Agenda | `screens/flujos/85-pedido-pendiente.png` | pedido_pend | differs |
| 86 | `613:906` | Pedido Pendiente | Diagrama Reserva con Agenda | `screens/flujos/86-pedido-pendiente.png` | pedido_pend | differs |
| 87 | `613:982` | Terminar Pedido | Diagrama Reserva con Agenda | `screens/flujos/87-terminar-pedido.png` | terminar | differs |
| 88 | `613:1049` | Terminar Pedido | Diagrama Reserva con Agenda | `screens/flujos/88-terminar-pedido.png` | terminar | differs |
| 89 | `613:1132` | Terminar Pedido | Diagrama Reserva con Agenda | `screens/flujos/89-terminar-pedido.png` | terminar | differs |
| 90 | `613:1249` | Terminar Pedido | Diagrama Reserva con Agenda | `screens/flujos/90-terminar-pedido.png` | terminar | differs |

## App-only (no están en Figma)
| Composable | Nota |
|---|---|
| `feature/auth/.../forgot/ForgotPasswordScreens.kt` (Forgot + Reset) | Figma no tiene recuperar contraseña. Mantener y aplicar el estilo de login |
| `core/ui/.../legal/TermsScreen.kt` | Términos. Mantener |
| `feature/onboarding/.../OnboardingScreens.kt` (Client/Provider checklist, PendingApproval, Rejected, AccountStatus Blocked/Inactive) | Figma resuelve "pendiente de validación" con un badge en la Home del trabajador (366:1317). Mantener los estados y aplicar el estilo |
| `feature/onboarding/.../verify/VerifyPhoneStepScreen.kt` | Figma menciona validar el teléfono en el texto de "Contacto" pero no lo dibuja. Reusar el layout de "Validación" |
| `feature/home/.../orders/OrdersListScreen.kt` | No hay "Mis pedidos" en Figma. La entrada desde la Home cliente (ActionCard) no está en el diseño. **Decidir** dónde va (¿tab Perfil?) |
| `feature/home/.../notifications/NotificationsScreen.kt` | La campana existe en Figma pero no la pantalla |
| `feature/home/.../favorites/FavoritesScreen.kt` | Tab "Favoritos" sin diseño |
| `feature/home/.../help/HelpRoute.kt` | Tab "Ayuda" sin diseño (solo la card "Contactar") |
| `feature/home/.../worker/report/WorkerReportRoute.kt` | "Denunciar perfil": no está en Figma |
| `feature/home/.../search/SearchFilterBottomSheet` | Filtros (modalidad, barrio, fecha): Figma no tiene filtros |
| `feature/home/.../orders/RateWorkerScreen.kt` | En Figma es un sheet dentro de "Terminar Pedido" |
| `feature/settings/.../SettingsScreen.kt` | Tab "Ajustes" sin diseño |
| `feature/settings/.../account/PersonalDataScreen.kt`, `ChangePasswordScreen.kt`, `DeleteAccountRoute.kt` | Sin diseño. Figma tiene "Darme de baja" dentro de Editar Perfil |
| `feature/settings/.../provider/ServicesScreens.kt` (lista + wizard de alta) | Solo "Elegí Rubro" (526:903) y "Editar Rubro o Especialidad" lo cubren en parte |
| `feature/home/.../agenda/AgendaConfigScreen.kt` | Duración e intervalo con campos numéricos. Figma lo resuelve con "División de Horarios" en Calendario general |
| `feature/home/HomeScreen.kt` → sección "Trabajadores destacados" | No está en Figma |
