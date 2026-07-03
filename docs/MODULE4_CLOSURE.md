# Cierre Módulo 4 — Home / Búsqueda (cliente)

## Criterio de cierre

- Home cliente con feed de trabajadores, recientes y buscador editable operativo contra API staging.
- Flujo categoría → rubro → resultados con filtros, paginación y búsqueda por texto (`q`).
- Paridad Figma ≥ 90% en home/búsqueda (voz diferida; ficha completa en M05).
- Suite de tests del plan en verde.

## Dependencias backend documentadas

Ver [MODULE3_BACKEND_GAPS.md](MODULE3_BACKEND_GAPS.md): selfie y OTP teléfono siguen diferidos (opción A, M3).

Endpoints M04 usados:

- `GET /trabajador-oficio/search` — feed, resultados, query `q`
- `GET /categorias`, `/rubros`, `/sub-rubros` — navegación catálogo
- `GET /barrios/search` — filtro zona

## QA manual — Cliente

1. Login cliente completo → home con nombre, badge verificado y feed de trabajadores.
2. Búsqueda por texto desde home, categorías o resultados (parámetro `q` en API).
3. Tap categoría → rubros → resultados paginados.
4. Filtros: rubro, subrubro, barrio, modalidad, fecha → chips removibles.
5. Recientes: categoría, rubro y query persisten tras reinicio.
6. Ver perfil desde card → stub (placeholder M05).
7. Tabs Perfil / Favoritos / Ayuda / Notificaciones → "Próximamente".
8. Rotación / back stack: filtros y query se conservan en nav args.

## Tests automatizados

```bash
./gradlew :feature:home:testDebugUnitTest \
  :core:network:testDebugUnitTest \
  :core:data:testDebugUnitTest \
  :domain:test
```

## Fuera de alcance M4

- Ficha pública del trabajador (`GET /trabajador-oficio/{id}`) — M05.
- Contactar / contratar trabajador — M06 Pedidos.
- Selfie / OTP teléfono — M3 opción A.
- Búsqueda por voz — diferida (toast Próximamente).
- Tabs Perfil / Favoritos / Ayuda / Notificaciones push — M07 / M10.
- Agenda (`/sesion`, `/agenda/slot`) — M08 Calendario.
- Home proveedor dedicado.
