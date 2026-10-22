# Agenda config (UI) vs `POST /sesion` (OpenAPI)

Pedido para backend: la pantalla **Calendario general** (`AgendaConfigScreen`, Figma `526:494`) junta días y horas que **hoy no viajan** en `POST /sesion`. Este documento solo describe lo que la app manda y lo que el contrato publicado acepta. **No propone campos nuevos.**

Fuente del contrato: [OpenAPI 3.0](https://apioficios.pushsoftware.com.ar/api-json) — tag `Sesión (configuración de turnos)`. Leído el 2026-10-22.

Relacionado: BK-02 / BK-03 en `docs/ESTADO_PLAN.md`.

## Qué junta la UI

`AgendaConfigUiState.Success` + `AgendaConfigViewModel.save()`:

| Campo UI | Tipo en app | Obligatorio para Guardar | ¿Viaja a `POST /sesion`? |
|----------|-------------|--------------------------|--------------------------|
| Oficios / subrubros (`selectedSubRubroIds`) | `Set<Long>` = `trabajadorOficioSubRubroId` | sí (al menos uno) | sí: un POST por id |
| Duración (`durationInput`) | minutos, `Int` > 0 | sí | sí → `duracion` |
| Intervalo (`intervalInput`) | minutos, opcional | no | sí, si hay valor → `intervalo` string |
| Días (`selectedDays`) | `Set<String>` `LUNES`…`DOMINGO` | sí en `save()` | **no** |
| Horas (`selectedHours`) | `Set<Int>` 0–23 | sí en `save()` | **no** |

`save()` además borra las sesiones existentes del subrubro (`DELETE /sesion/{id}`) y vuelve a crear.

Body real que arma `AgendaRepositoryImpl.saveSession` → `CreateSesionRequestDto`:

```json
{
  "trabajadorOficioSubRubroId": 123,
  "duracion": 60,
  "medidaDuracion": "minutos",
  "intervalo": "15",
  "medidaIntervalo": "minutos"
}
```

Si no hay intervalo, `intervalo` y `medidaIntervalo` van `null`. `medidaDuracion` / `medidaIntervalo` los fija la app en `"minutos"`; no son input de la pantalla.

`selectedDays` y `selectedHours` se validan y se descartan. No hay otro endpoint de sesión que los reciba.

## Qué acepta el OpenAPI

### `POST /sesion` — `CreateSesionRequestDto`

Summary: *Crear configuración de sesión/turno para un subrubro*.

| Campo | Tipo OpenAPI | Required | Ejemplo |
|-------|--------------|----------|---------|
| `trabajadorOficioSubRubroId` | number | sí | — |
| `duracion` | number | sí | `60` |
| `medidaDuracion` | string | sí | `"minutos"` |
| `intervalo` | string | no | `"15"` |
| `medidaIntervalo` | string | no | `"minutos"` |

No hay propiedades de días ni de horas en este schema. No hay `PATCH /sesion/{id}`.

### `GET /sesion/por-subrubro/{trabajadorOficioSubRubroId}` — `SesionDTO`

| Campo | Tipo OpenAPI | Notas |
|-------|--------------|-------|
| `id` | number | |
| `duracion` | number | |
| `medidaDuracion` | string | ej. minutos, horas |
| `intervalo` | object, nullable | el DTO local de la app lo trata como `string?` |
| `medidaIntervalo` | object, nullable | idem |
| `trabajadorOficioSubRubroId` | number | |

Tampoco incluye días ni horas.

### `DELETE /sesion/{id}`

Sin body. Summary: *Eliminar sesión*.

## Días y horas en otro recurso (no es `sesion`)

`diasLaborales` + `horariosLaborales` existen en el alta/edición de rubro (`Create2DiaLaboralRequestDto` / `DiaLaboralDTO`), no en `POST /sesion`. Ese contrato es el de BK-01 (`PATCH` atómico de servicio). La pantalla de calendario no llama esos endpoints al guardar.

## Hueco a pedir

La UI de calendario necesita persistir **días + horas** junto con duración/intervalo por `trabajadorOficioSubRubroId`. El OpenAPI de `sesion` hoy no tiene dónde mandarlos. Hasta que backend publique el contrato, la app no inventa el body: guarda solo duración/intervalo y los días/horas quedan en estado local.
