# BK-01 — Edición atómica de un servicio publicado

Pedido: PATCH (o PUT) de un `trabajador-oficio-rubro` ya creado, con especialidades, modalidad y disponibilidad.

## Por qué

`PATCH /trabajador-oficio-rubro/{id}` solo acepta `autoAceptacion`, `principal` y `plazoCancelacionHoras`. No hay endpoint de trabajador para:

- agregar/quitar subrubros (`trof_05`)
- cambiar `lugarDomicilio` / `lugarLocal` / `requiereTurnos`
- reemplazar `diasLaborales` + horarios

`PATCH /trabajador-oficio/admin/{id}/sub-rubros/{subRubroId}` actualiza modalidad, pero es **admin**.

`POST /sesion` + `DELETE /sesion/{id}` cubren la config de turnos (mismo criterio que PR-9). No hay `PATCH /sesion/{id}`.

## Workaround actual (app)

Editar un servicio hace **DELETE `/trabajador-oficio-rubro/{id}`** y después:

- principal → `POST /trabajador-oficio-rubro/full`
- secundario → `POST /trabajador-oficio-rubro/full/secundario`

Riesgo: si el POST falla después del DELETE, el trabajador queda sin ese rubro (y sin slots/días asociados). El domicilio local no viaja en `GET /trabajador-oficio/me` (`TrabajadorOficioRubroFullDTO` no lo incluye), así que hay que volver a cargarlo si atiende en local.

## Contrato pedido

`PATCH /trabajador-oficio-rubro/{id}/full` (o extensión del PATCH actual) con el mismo body que `CreateFullAltaRubrosSubrubrosRequestDto` **sin** cambiar `rubroId` si `principal=true`.

Debe actualizar subrubros, modalidad, `diasLaborales` y, si aplica, `domicilioLocal`, de forma atómica. El rubro principal no se elimina.
