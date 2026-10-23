# Agenda config (UI) vs `POST /sesion` (OpenAPI)

**Decisión de producto (Matías, 2026-10-22):** la app se adapta al contrato publicado. Cada formulario pide y muestra como editable **solo** lo que el backend guarda. El pedido de un PATCH de días/horas en `sesion` queda **opcional**.

Fuente del contrato: [OpenAPI 3.0](https://apioficios.pushsoftware.com.ar/api-json) — tag `Sesión (configuración de turnos)`. Leído el 2026-10-22.

Relacionado: BK-02 / BK-03 en `docs/ESTADO_PLAN.md`.

## Qué junta la UI ahora

`AgendaConfigScreen` / `AgendaConfigViewModel.save()` → `POST /sesion` (`CreateSesionRequestDto`):

| Campo UI | Campo OpenAPI | ¿Viaja? |
|----------|---------------|---------|
| Oficio (`selectedSubRubroIds`) | `trabajadorOficioSubRubroId` | sí, un POST por id |
| Duración (`durationInput`) | `duracion` | sí |
| Unidad de duración (`durationUnit`) | `medidaDuracion` | sí (`minutos` o `horas`) |
| Intervalo (`intervalInput`, opcional) | `intervalo` | sí, si hay valor (string) |
| Unidad de intervalo (`intervalUnit`) | `medidaIntervalo` | sí, si hay intervalo |

`save()` borra las sesiones existentes del subrubro (`DELETE /sesion/{id}`) y vuelve a crear.

Body:

```json
{
  "trabajadorOficioSubRubroId": 123,
  "duracion": 60,
  "medidaDuracion": "minutos",
  "intervalo": "15",
  "medidaIntervalo": "minutos"
}
```

No hay selectores de días ni de horas en esta pantalla.

## Qué acepta el OpenAPI

### `POST /sesion` — `CreateSesionRequestDto`

| Campo | Tipo OpenAPI | Required | Ejemplo |
|-------|--------------|----------|---------|
| `trabajadorOficioSubRubroId` | number | sí | — |
| `duracion` | number | sí | `60` |
| `medidaDuracion` | string | sí | `"minutos"` |
| `intervalo` | string | no | `"15"` |
| `medidaIntervalo` | string | no | `"minutos"` |

No hay propiedades de días ni de horas. No hay `PATCH /sesion/{id}`.

### `GET /sesion/por-subrubro/{trabajadorOficioSubRubroId}` — `SesionDTO`

Mismos campos + `id`. La lista de configuraciones guardadas es **solo lectura** (con borrar).

### `DELETE /sesion/{id}`

Sin body.

## Días y horarios laborales (otro recurso)

`DiaLaboral` (`diaInicio`, `diaFin`, `horariosLaborales[horaInicio, horaFin]`) **no** pertenece a `sesion`. Se crea en:

- `POST /trabajador-oficio-rubro/full`
- `POST /trabajador-oficio-rubro/full/secundario`

La app los manda en el **alta y reemplazo de rubro/servicio** (`CreateFullAltaRubrosSubrubrosRequestDto.diasLaborales`). No hay PATCH atómico de `diasLaborales`; editar un servicio hace DELETE + POST `/full`.

Los turnos concretos se cargan con `POST /agenda/slot` (`inicio`, `fin`, `autoAceptacion`).

## Pedido opcional al backend

Un PATCH de `diasLaborales` / `sesion` con días y horas permitiría editar disponibilidad sin recrear el rubro. **No bloquea la app:** calendario general guarda duración/intervalo; días/horas se editan en el alta/reemplazo de servicio.
