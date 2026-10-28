# BK-23 — Filtros de búsqueda no expuestos por el backend

**Pantalla Figma:** Buscador / resultados (Flujos, home `454:467`, resultados `453:206`).

## Qué hay en la API de producción

`GET /trabajador-oficio/search` acepta:

- `rubroId`, `subRubroId`
- `modalidadAtencion`
- `localidadId`, `barrioId`
- `fechaDeseada`
- `q`
- `pageNumber`, `pageSize`

## Qué no existe y la app no inventa

La hoja de filtros del cliente **no** pinta estos controles porque el backend no los recibe:

1. **Verificado** (solo trabajadores validados)
2. **Orden** (rating, distancia, recencia)
3. **Disponibilidad** (hoy / ahora)
4. **Calificación mínima**
5. **Zona como texto libre** (solo `barrioId` / `localidadId`)

El DTO de resultados tampoco trae barrio/zona del trabajador: la lista muestra foto, nombre, rubro y verificado, sin zona inventada.

## Pedido

1. Documentar o agregar query params reales para verificado, orden y disponibilidad si producto los necesita.
2. Si la ficha de resultado debe mostrar zona, exponer `barrio`/`localidad` en `WorkerSearchItemDto`.
3. Si `localidadId` es el filtro de ciudad, exponer un endpoint de localidades para el autocomplete (hoy solo hay barrios).
