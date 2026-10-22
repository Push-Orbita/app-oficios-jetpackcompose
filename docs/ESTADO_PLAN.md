# Estado del plan 1:1 Figma → app

Fecha: 2026-10-21. Stack abierto vs `main` (`288befd`, PR #10 merged). **Ningún PR de este stack está mergeado.**

Fuente: plan 1:1 + mapa Figma. File Figma `hhKrcrusbr3h6qJxqYBiTd`. API solo prod (`apioficios.pushsoftware.com.ar`). Sin mutar datos reales.

## Orden de merge (lineal)

`#11 → #12 → #13 → #14 → #15 → #16 → #17 → #18 → #19 → #20 → #21` (este PR)

Cada uno declara `Requires PR #N` y está stacked sobre el anterior. No mergear #11 ni ningún otro hasta que Matías lo pida.

## Tabla de PRs

| Plan | GitHub | Rama | Estado | Una línea |
|---|---|---|---|---|
| PR-4 Buscador | [#11](https://github.com/Push-Orbita/app-oficios-jetpackcompose/pull/11) | `cursor/buscador-cliente-figma-ee07` | draft, mergeable | Home / Oficios / Rubros / Resultados 1:1; Recientes local |
| PR-5 Ficha | [#12](https://github.com/Push-Orbita/app-oficios-jetpackcompose/pull/12) | `cursor/ficha-trabajador-ee07` | draft | Ficha `40:559`: galería, certificados, reseñas, contratar |
| PR-6 Detalle | [#13](https://github.com/Push-Orbita/app-oficios-jetpackcompose/pull/13) | `cursor/detalle-pedido-ee07` | draft | Cancelar con `motivoCancelacion` (400 si falta) + polling 12 s |
| PR-7 Solicitud | [#14](https://github.com/Push-Orbita/app-oficios-jetpackcompose/pull/14) | `cursor/solicitud-reserva-ee07` | draft | Form + resumen + toast home; calendario/horas; Presupuesto/Trabajo oculto |
| PR-8 Home trabajador | [#15](https://github.com/Push-Orbita/app-oficios-jetpackcompose/pull/15) | `cursor/home-trabajador-ee07` | draft | 3 beneficios, % perfil, tiles, Configurar Agenda |
| PR-9 Calendario | [#16](https://github.com/Push-Orbita/app-oficios-jetpackcompose/pull/16) | `cursor/calendario-general-ee07` | draft | Días + división + grilla; borrar/recrear `sesion` |
| PR-10 Perfil | [#17](https://github.com/Push-Orbita/app-oficios-jetpackcompose/pull/17) | `cursor/perfil-servicio-ee07` | draft | Elegí Rubro + Editar Perfil; tab Perfil = Ajustes |
| PR-11 Categoría | [#18](https://github.com/Push-Orbita/app-oficios-jetpackcompose/pull/18) | `cursor/elegi-categoria-ee07` | draft | Grilla 2 col. en registro/onboarding |
| PR-13 Copy / app-only | [#19](https://github.com/Push-Orbita/app-oficios-jetpackcompose/pull/19) | `cursor/copy-usted-app-ee07` | draft | Usted fuera de auth; padding 32/16/32/32 |
| PR-12 Push | [#20](https://github.com/Push-Orbita/app-oficios-jetpackcompose/pull/20) | `cursor/push-firebase-config-ee07` | draft | FCM detrás de `FIREBASE_ENABLED`; docs BK-09 |
| Cierre | [#21](https://github.com/Push-Orbita/app-oficios-jetpackcompose/pull/21) | `cursor/estado-plan-ee07` | draft | ESTADO_PLAN + Nombres/Apellido + logo + rewrite 1:1 pantallas |

PR-0…PR-3 ya están en `main` (#7, #8, #9, #10).

## Diffs por pantalla y adaptaciones

| Pantalla | Nodo | PR | Diff / notas | Adaptación |
|---|---|---|---|---|
| Home cliente | `25:95` | #21 | Recientes sin "Ver todos"; QA `docs/qa-1a1/home-cliente.png` ~9.6% | Recientes DataStore; placeholder usted; nav 4 tabs |
| Oficios | `25:258` | #11 | Grilla + ilustraciones locales | BK-10: assets locales |
| Rubros | `35:2` | #11 | Grilla | — |
| Resultados | `40:309` | #11 | Header, aviso 3/10 min, buscador | BK-12, BK-14 (aviso estático) |
| Ficha | `40:559` | #21 | Estructura card 1:1; QA `docs/qa-1a1/ficha-trabajador.png` ~33.7% | Certs/reseñas vacíos no ocupan; nav 4 tabs |
| Detalle / cancelar | `198:697`… | #21 | Mapa primero + filas Figma; QA `docs/qa-1a1/detalle-pedido.png` ~35.8% | Motivo cancelar de #13; mapa placeholder sin key |
| Completá tu Solicitud | `455:27` | #14/#21 | Form + radios de ubicación | Título Figma en voseo; Presupuesto/Trabajo oculto (BK-15) |
| Agendar Cita | `613:1373` | #21 | Mini-card + Fecha + calendario 5 semanas + Horario. QA `docs/qa-1a1/solicitud.png` ~18.3% | Días con cupo en azul; nav 4 tabs |
| Resumen Solicitud | `455:423` | #14 | Toast `Solicitud enviada` → home | Sin shell intermedio |
| Home trabajador | `191:11` + lista `259:534` | #21 | Agenda **dentro de Inicio**. QA incompleto `docs/qa-1a1/home-trabajador.png` **15.6%** vs `191:11`. Completo `home-trabajador-completo.png` **33.5%** vs `259:534` (Figma sin Beneficios + 5 tabs). Sin tab Agenda. | Nav acordada; Completa Tu Perfil solo &lt;100%; % BK-19 |
| Calendario general | `239:475` | #21 | Pills + división + grilla + oficio. QA `docs/qa-1a1/agenda.png` ~28.5% | Editar `sesion` = delete+recreate (BK-02/03); CTA Guardar (Figma no lo muestra) |
| Agenda completa | `210:106` | #16 | — | `diasLaborales` por día en alta |
| Editar Perfil | `231:15` | #21 | Oficios/galería/certs/Darme De Baja 1:1. QA `docs/qa-1a1/perfil.png` ~28.8%. Ajustes: 1 fila (no 3 hermanas) | PATCH limitado (BK-01); tab Perfil = Ajustes; servicios/galería anidados |
| Elegí Rubro | `526:903` | #17 | Grilla 2 col. | Título Figma en voseo |
| Elegí tu categoría | `526:1026` | #18 | Grilla 2 col. | Checkboxes (API multi), no radios; título Figma |
| Forgot / Settings / Pedidos / Notif / Favoritos / Ayuda | — | #19 | Lenguaje DS + usted + `OficiosScreenPadding` | Sin baseline Figma |
| Push | — | #20 | Código listo, **apagado** sin config | BK-09 stub |

Pixel-diff &lt;2 % (Roborazzi P9 vs PNG) salvo D-DIN, datos reales y adaptaciones acordadas. Layout ±3 dp.

## Copy

Decisión 20/10/2026: **usted** (como Figma). Auth ya estaba (#10). PR-13 pasa el resto.

Títulos Figma que se conservan en voseo (adaptación): **Completá tu Solicitud**, **Elegí Rubro**, **Elegí tu categoría**.

### Fix en #21 — Nombres y Apellido

Registro cliente/trabajador y Datos personales usan dos campos (**Nombres**, **Apellido**), copy en usted, validación no vacío. Se envían tal cual a `nombres`/`apellido` (SwaggerSignup / PersonaDTO / UpdateUsuario). Se borró `splitFullName` (el primer espacio partía “Matias Federico Britez”).

### Fix en #21 — Logo Figma (casco azul)

El casco amarillo (`#F8AE1A`, nodo `12:101`) no se usa. Marca vigente exportada como vector desde Figma Thumbnail:

| Asset | Nodo | Uso |
|---|---|---|
| `ic_oficios_logotipo` | `666:2` plano | Splash, login, resultados, BrandBlock |
| `ic_oficios_logotipo_outline` | `736:40` contorno navy | Launcher adaptive (safe zone 66dp) |
| `ic_oficios_logotipo_title` | `583:39` plano + wordmark | `OficiosLogoVariant.WithTitle` |
| `ic_oficios_logotipo_title_outline` | `736:72` contorno + wordmark | Variante de lockup |
| `ic_launcher_foreground` / monochrome | outline en 108dp | Adaptive + Android 13 |
| `ic_stat_oficios` | silueta blanca 24dp | Notificaciones |
| `ic_launcher-playstore.png` | 512 | Play Store |
| `logo_oficios.png` | outline | Fixture e2e (ya no es el amarillo) |

`OficiosColorBrandMark` = `#006AF0`. El acento UI `#F8AE1A` (`OficiosColorAccent`) no pinta el logo. `ic_casco` del rol sigue blanco.

### Fix en #21 — Agenda Inicio: AHORA/HOY y sin turnos pasados

Causa raíz: `isNow = bookingStatus == IN_PROGRESS`. LLEGUE/EN_CURSO pintaba **AHORA** en todas las cards, sin mirar fecha ni franja. `loadUpcomingWorkerOrders` filtraba por estado abierto y hacía `take(20)` **sin recortar por fecha**, así que turnos de septiembre seguían en Inicio.

Reglas (`Clock` inyectable, zona `America/Argentina/Buenos_Aires`):
- **AHORA:** hoy y (ahora en la franja `inicio ≤ ahora < fin`, o LLEGUE/EN_CURSO). Nunca por estado solo. Sin fin → `inicio + duración` o 60 min de sesión.
- **HOY:** fecha de hoy que no cumple AHORA.
- **Sin etiqueta:** futuro (card normal Figma, no blanca).
- Lista del Inicio: solo hoy en adelante, orden ascendente. Pasados no cerrados quedan en Mis pedidos.
- Calendario: hoy blanco, días con turnos (de esa lista) azul.

“Ver agenda completa” es disponibilidad (`AgendaListScreen`, slots), no cards de turno. El mismo mapeo de etiqueta se aplicó a **Mis pedidos**, que sí usa `AgendaAppointmentRow`. Hoy de esa pantalla también usa zona Argentina.

### Fix en #21 — Perfil trabajador: una sola entrada Editar Perfil

Matías: las 3 filas de Ajustes (Presentación profesional / Servicios que ofrece / Galería y certificados) se sentían redundantes.

**Hallazgo:** sí había duplicación. “Presentación profesional” no editaba descripción/experiencia/cobro (el subtítulo mentía): abría `ProfessionalProfileScreen` (Figma `231:15`), que ya muestra oficios, Trabajos realizados, Certificado(s) y Darme De Baja. Servicios y galería también estaban como hermanos en Ajustes, además de anidados desde Editar Rubro / Añadir.

**Figma:** `231:15` es **una sola** pantalla Editar Perfil (oficios + especializado + CTA Editar Rubro o Especialidad + galería + certificados + Darme De Baja). Frames previos `224` / `230` también agrupan foto/descripción/oficios/galería/certs en un scroll; no hay 3 filas hermanas. Ajustes en Figma es el 5.º tab; en la app el tab Perfil = Ajustes.

**Cambio:** Ajustes del trabajador queda **una** fila “Editar Perfil” (subtítulo: oficios, trabajos realizados y certificados). Servicios y portfolio siguen como destinos anidados (Editar Rubro / Añadir), no como entradas de Ajustes. Descripción/experiencia siguen en onboarding; medios de cobro en el wizard de servicio. Cada dato se edita en un solo lugar.

### Fix en #21 — Editar servicio + padding TopAppBar

**Servicios que ofrece:** tocar la card abre el wizard precargado (especialidades, modalidad, días/horario). Rubro principal bloqueado. Persistencia = DELETE `/trabajador-oficio-rubro/{id}` + POST `/full` o `/full/secundario` (no hay PATCH atómico; BK-01). Toast de éxito/error. `DELETE` tipado `Unit` (body vacío).

**Padding:** `OficiosScaffold` suma `OficiosSpacing.afterTopBar` (16 dp) cuando hay `topBar`. Pantallas con barra: Servicios, Agregar/Editar servicio, Ajustes, Datos personales, Contraseña, Datos cliente, Portafolio, Favoritos, Notificaciones, Ayuda, Agenda lista, Calificar, Contrapropuesta, Términos, Denunciar trabajador.

### Fix en #21 — Badge verificado verde

`OficiosVerifiedBadge`: Lucide `badge-check` outline 24 dp, tint `OficiosColorSuccess` (`#4CAF7D`). Home/ficha/cards/solicitud del **trabajador**. Home **cliente** lo conserva porque Figma `25:95` / `221:990` lo dibuja y la API expone `usuario.emailVerificado`. No se agregó junto al nombre en Ajustes (no hay frame Figma de esa tarjeta).

Bottom nav: Inicio / Mis pedidos / Favoritos (oculto para trabajador) / Perfil (`home_tab_ajustes`). Ayuda vive en Ajustes.

### Fix en #21 — Estructura 1:1 pantallas importantes

Matías: no alcanza con tipografía/íconos si la composición sigue la app vieja. Rewrite por pantalla, side-by-side en `docs/qa-1a1/<pantalla>.png` (Figma | Roborazzi P9 | diff).

| Pantalla | Nodo | Diff pixel | Commit | Notas |
|---|---|---|---|---|
| Home trabajador | `191:11` + `259:534` | 15.6% incompleto / 33.5% completo | `ad703ed` | Incompleto: Beneficios + Completa Tu Perfil 80% + calendario. Completo: sin perfil; Beneficios queda (Figma `259:534` no lo tiene). Agenda completa solo por el link. |
| Home cliente | `25:95` | ~9.6% (nav acordada, placeholder usted, datos reales) | `6d3bea5` | Recientes sin "Ver todos"; 3 cards r16 |
| Detalle pedido | `198:697` | ~35.8% (mapa sin API key, D-DIN, P9 427 vs 440) | `64c68c1` | Mapa 248 dp → filas icon-box + Contacto/Estoy En Camino a la derecha; sin chips/avatar |
| Ficha trabajador | `40:559` | ~33.7% (sin foto, nav 4 vs 5) | `d491c96` | Título + card + oficios/especializado + Contactar/Calificar + Trabajos realizados |
| Solicitud/reserva | `613:1373` | ~18.3% (nav acordada, sin foto) | `608afdf` | Agendar Cita: mini-card, Fecha, calendario 5 semanas, Horario; `455:27` Completá tu Solicitud conserva ubicación |
| Agenda/calendario | `239:475` | ~28.5% (Guardar extra, nav, D-DIN) | `d7503bd` | Días pill, división 30/1h/2h, grilla, Calendario Por Oficio; sin form viejo duración/intervalo |
| Perfil/editar perfil | `231:15` | ~28.8% (sin fotos reales, nested sin 5-tab Figma) | `bd151d3` | Oficios + especializado + Editar Rubro + galería/certs dashed Añadir + Darme De Baja |
| Mis pedidos | — (sin frame) | ~6.4% vs placeholder navy | `0e34b36` | Lista agrupada por fecha + HOY; Activos/Historial; nav acordada |

## Bloqueos abiertos

| ID | Qué | Impacto | Doc |
|---|---|---|---|
| BK-01 | PATCH atómico especialidades/modalidad/`diasLaborales` | Edit servicio = DELETE+POST `/full` | [BK-01-editar-servicio.md](backend-requests/BK-01-editar-servicio.md) |
| BK-02 / BK-03 | PATCH `diasLaborales` / `sesion` | Calendario borra y recrea | [AGENDA_SESION_UI_VS_OPENAPI.md](backend/AGENDA_SESION_UI_VS_OPENAPI.md) |
| BK-04 | Bloqueos de agenda | No hay UI de bloqueo | — |
| BK-05 | Disponibilidad por subrubro / sin turnos | Horario por oficio incompleto | — |
| BK-06 / BK-07 | Plazo cancelación 1–24 vs 12–168 | InfoRow muestra lo que manda prod | — |
| BK-08 | — | Descartado (API nueva) | — |
| BK-09 | Firebase de prod | Push apagado; inbox + polling 12 s | [BK-09_FIREBASE_PRODUCTION.md](BK-09_FIREBASE_PRODUCTION.md) |
| BK-10 | Imagen por categoría/rubro | Assets locales | — |
| BK-11 | Estado certificado Validado/Pendiente | UI sin badge admin real | — |
| BK-12 | `categoriaId` / `q` documentado | Filtros parciales | — |
| BK-14 | Límite contactar 3/10 min | Aviso estático | — |
| BK-15 | `tipo` PRESUPUESTO/TRABAJO | Segmentado oculto | [backend-requests/BK-15-tipo-solicitud.md](backend-requests/BK-15-tipo-solicitud.md) |
| BK-16 | Recordatorios de turno | Baja | — |
| BK-17 | Puntaje único vs calidad+puntualidad | 2 filas de estrellas | — |
| BK-18 | Endpoint de beneficios | Copy estático | — |
| BK-19 | % numérico + flags galería/local/cert | Home trabajador calcula 0/33/66/100 | ver abajo |
| BK-20 | Auto-expirar PENDIENTE/CONFIRMADA al pasar el slot | App mueve a Historial por fecha; el estado del API no cambia | [BK-20-vencimiento-pedidos.md](backend-requests/BK-20-vencimiento-pedidos.md) |

### Decisión Home trabajador (Matías, 21/10/2026)

Figma tiene 3 frames inconsistentes: `191:11` (Beneficios + Completa Tu Perfil 80% + calendario, lista cortada), `259:254` (lista Agenda sin calendario), `259:534` (header + calendario + AHORA/HOY, sin Beneficios/perfil, 5 tabs). Quedó:

1. **Agenda vive en Inicio.** Header (avatar, nombre, shield-check verde, rubros, campana) → Beneficios → Completa Tu Perfil (solo si &lt;100%) → calendario mensual + lista por fecha (card AHORA blanca, HOY con etiqueta vertical, chevron) → `ver agenda completa`.
2. **No hay tab Agenda** en la bottom nav del trabajador. Nav acordada: Inicio · Mis pedidos · Perfil. Favoritos oculto. Ayuda en Ajustes/Perfil. `AgendaDestination.list` solo desde el link.
3. **Completa Tu Perfil** desaparece entera al 100%. Check si el hito está hecho, círculo punteado si falta. Cada tile abre el editor real (galería → portfolio; certificado → editor de certificado; local → formulario barrio/calle/mapa). **Nunca** `PROVIDER_CHECKLIST` / «¡Perfil confirmado!» desde Inicio. Al volver, Inicio re-fetcha el perfil en silencio.

### API: qué se puede calcular y qué no (BK-19)

`TrabajadorPerfilEstadoDto` expone flags (`perfilCompleto`, `configuracionCompleta`, `perfilOperativo`, `faltantes`) **sin % numérico** ni flags dedicados de galería / local comercial / certificado.

| Hito Figma | Fuente real | Limitación |
|---|---|---|
| Galería | `GET perfil` → `galeriaTrabajos` / `providerDetails.gallery.isNotEmpty()` | — |
| Certificado | `GET perfil` → `certificados` / `providerDetails.certificates.isNotEmpty()` | BK-11: no hay badge Validado/Pendiente admin |
| Local comercial | `lugarLocal` en subrubros **o** `GET perfil.domicilios` con `calle` | No hay entidad «local comercial». Write: `POST /trabajador-oficio-rubro/full` (`lugarLocal` + `domicilioLocal`) vía `replaceService` (BK-01 DELETE+POST). `ProviderDomicilioDto` no trae lat/lng: el mapa no se prefilla |
| % barra | `(hitos hechos × 100) / 3` → 0 / 33 / 66 / 100 | **66% = 2 de 3** (captura: galería + certificado; local sin tildar). Figma mockea **80%**; ese valor no sale de la API |
| Fallback | si `GET perfil` falla: `inferProviderHomeTiles(faltantes)` | Match por texto (`galer`/`foto`, `local`/`comercio`, `certific`) |
| `perfilCompleto == true` | solo en el fallback: los 3 hitos se marcan hechos | Si el perfil carga, mandan las listas reales |

Figma (Rediseño/Flujos): «Agrega tu local comercial» existe solo como tile de Inicio (`191:11`, `259:254`, `259:17`, Flujos `356:18`). **No hay frame de edición post-onboarding.** El formulario reutiliza el paso onboarding «Local comercial» (barrio, calle, mapa) alineado a OpenAPI `CreateDomicilioLocalRequestDto`.

AHORA = hoy + slot o `IN_PROGRESS` de hoy. HOY = fecha del turno = hoy (no AHORA). Beneficios siguen copy estático (BK-18).

## Reglas que se mantuvieron

- Sin merge de este stack. Sin force-push / rewrite / borrar ramas / amend de commits pusheados.
- Sin tocar `feat/login-fondo-oscuro`. Sin `.idea` / `local.properties` / secretos.
- Tests con fakes / MockWebServer. R8 on. Gate `assembleDebug testDebugUnitTest lintDebug` verde por PR.
- `OficiosScaffold` + `WindowInsets.safeDrawing`. Sin `imePadding()` extra ni insets ad hoc.
- No hay cambios de backend: solo docs de pedido.

## QA

- Roborazzi 440 + Pixel 9 en el buscador (#11).
- Unit: motivo de cancelación, polling, mapeo `WorkHoursSchedule`, `FIREBASE_ENABLED=false` con stub, mapeo `nombres`/`apellido` sin split.
- Push de dispositivo: bloqueado por BK-09. Matriz en `docs/qa/PUSH_NOTIFICATION_MATRIX.md`.
