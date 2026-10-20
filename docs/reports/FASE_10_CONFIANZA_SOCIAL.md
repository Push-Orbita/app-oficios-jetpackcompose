# Fase 10 — Confianza y social

**Estado:** Placeholders Android listos; APIs backend pendientes

## Favoritos (BACK-SOCIAL-001)

- Tab **Favoritos** visible con `OficiosEmptyState` (decisión de producto)
- Sin llamadas API hasta CRUD backend
- Pantalla: `feature/home/favorites/FavoritesRoute.kt`

## Denuncias (BACK-SAFETY-001)

- Perfil trabajador → **Denunciar Perfil** navega a pantalla dedicada
- Formulario deshabilitado con copy explicando gate backend
- Pantalla: `feature/home/worker/report/WorkerReportRoute.kt`

## Fuera de v1

- Búsqueda por voz: sin contrato backend — ícono mic sin acción engañosa

## Próximo paso

Cuando backend cierre gates, reemplazar empty state favoritos por lista real y habilitar `POST /denuncias`.
