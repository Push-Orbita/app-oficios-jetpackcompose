# Estado del plan 1:1 Figma → app

Fecha: 2026-10-21. Stack abierto vs `main` (`288befd`, PR #10 merged). **Ningún PR de este stack está mergeado.**

Fuente: plan 1:1 + mapa Figma. File Figma `hhKrcrusbr3h6qJxqYBiTd`. API solo prod (`apioficios.pushsoftware.com.ar`). Sin mutar datos reales.

## Orden de merge (lineal)

`#11 → #12 → #13 → #14 → #15 → #16 → #17 → #18 → #19 → #20 → #21`

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
| Cierre | este PR | `cursor/estado-plan-ee07` | draft | Este documento |

PR-0…PR-3 ya están en `main` (#7, #8, #9, #10).

## Diffs por pantalla y adaptaciones

| Pantalla | Nodo | PR | Diff / notas | Adaptación |
|---|---|---|---|---|
| Home cliente | `25:95` | #11 | Recientes caps 155, cards 199; QA r1 vs PNG | Recientes en DataStore (no hay endpoint) |
| Oficios | `25:258` | #11 | Grilla + ilustraciones locales | BK-10: assets locales |
| Rubros | `35:2` | #11 | Grilla | — |
| Resultados | `40:309` | #11 | Header, aviso 3/10 min, buscador | BK-12, BK-14 (aviso estático) |
| Ficha | `40:559` | #12 | Galería, certs, reseñas, CTA | — |
| Detalle / cancelar | `198:697`… | #13 | Motivo obligatorio + plazo InfoRow | Sin frame de cancelar; Danger CTA |
| Completá tu Solicitud | `455:27` | #14 | Form + radios de ubicación | Título Figma en voseo; Presupuesto/Trabajo oculto (BK-15) |
| Agendar Cita | `613:1373` | #14 | Calendario + grilla horas | Días off #918E9B |
| Resumen Solicitud | `455:423` | #14 | Toast `Solicitud enviada` → home | Sin shell intermedio |
| Home trabajador | `191:11` | #15 | 3 tiles de beneficio, % perfil | Beneficios estáticos (BK-18); % derivado de hitos |
| Calendario general | `239:475` | #16 | Do–Sá, 30/60/120, grilla máx. 12 h | Editar `sesion` = delete+recreate (BK-02/03) |
| Agenda completa | `210:106` | #16 | — | `diasLaborales` por día en alta |
| Editar Perfil | `231:15` | #17 | CTA Editar Rubro o Especialidad | PATCH limitado (BK-01) |
| Elegí Rubro | `526:903` | #17 | Grilla 2 col. | Título Figma en voseo |
| Elegí tu categoría | `526:1026` | #18 | Grilla 2 col. | Checkboxes (API multi), no radios; título Figma |
| Forgot / Settings / Pedidos / Notif / Favoritos / Ayuda | — | #19 | Lenguaje DS + usted + `OficiosScreenPadding` | Sin baseline Figma |
| Push | — | #20 | Código listo, **apagado** sin config | BK-09 stub |

Pixel-diff &lt;2 % (Roborazzi P9 vs PNG) salvo D-DIN, datos reales y adaptaciones acordadas. Layout ±3 dp.

## Copy

Decisión 20/10/2026: **usted** (como Figma). Auth ya estaba (#10). PR-13 pasa el resto.

Títulos Figma que se conservan en voseo (adaptación): **Completá tu Solicitud**, **Elegí Rubro**, **Elegí tu categoría**.

Bottom nav: Inicio / Mis pedidos / Favoritos (oculto para trabajador) / Perfil (`home_tab_ajustes`). Ayuda vive en Ajustes.

## Bloqueos abiertos

| ID | Qué | Impacto | Doc |
|---|---|---|---|
| BK-01 | Editar subrubros/modalidad | Editar Rubro parcial | backlog reunión |
| BK-02 / BK-03 | PATCH `diasLaborales` / `sesion` | Calendario borra y recrea | — |
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

## Reglas que se mantuvieron

- Sin merge de este stack. Sin force-push / rewrite / borrar ramas / amend de commits pusheados.
- Sin tocar `feat/login-fondo-oscuro`. Sin `.idea` / `local.properties` / secretos.
- Tests con fakes / MockWebServer. R8 on. Gate `assembleDebug testDebugUnitTest lintDebug` verde por PR.
- `OficiosScaffold` + `WindowInsets.safeDrawing`. Sin `imePadding()` extra ni insets ad hoc.
- No hay cambios de backend: solo docs de pedido.

## QA

- Roborazzi 440 + Pixel 9 en el buscador (#11).
- Unit: motivo de cancelación, polling, mapeo `WorkHoursSchedule`, `FIREBASE_ENABLED=false` con stub.
- Push de dispositivo: bloqueado por BK-09. Matriz en `docs/qa/PUSH_NOTIFICATION_MATRIX.md`.
