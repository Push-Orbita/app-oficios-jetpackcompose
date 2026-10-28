# BK-01 — Edición atómica de un servicio publicado

Pedido: PATCH (o PUT) de un `trabajador-oficio-rubro` ya creado, con especialidades, modalidad y disponibilidad.

## Por qué

`PATCH /trabajador-oficio-rubro/{id}` solo acepta `autoAceptacion`, `principal` y `plazoCancelacionHoras`. No hay endpoint de trabajador para:

- agregar/quitar subrubros (`trof_05`)
- cambiar `lugarDomicilio` / `lugarLocal` / `requiereTurnos`
- reemplazar `diasLaborales` + horarios

`PATCH /trabajador-oficio/admin/{id}/sub-rubros/{subRubroId}` actualiza modalidad, pero es **admin**.

`POST /sesion` + `DELETE /sesion/{id}` cubren la config de turnos (mismo criterio que PR-9). No hay `PATCH /sesion/{id}`.

## Workaround actual (app) — QA r8

El trabajador **no** tiene `POST`/`PATCH`/`DELETE /trabajador-oficio-sub-rubro` (404 DB03). La app ya no los llama.

En edición de un rubro cacheado:

- no se borra el rubro ni se hace `POST /full`
- no se destilda `requiereTurnos` (toggle solo lectura) y **no** se hace `DELETE /sesion`
- no se agregan/quitan especialidades (lista solo lectura); el alta sigue siendo `POST /full` en un servicio nuevo
- `POST /sesion` solo si el servicio pasa a requerir turnos
- horarios: `PUT …/disponibilidad` por especialidad

Sigue faltando el PATCH atómico de este ticket.

## Contrato pedido

`PATCH /trabajador-oficio-rubro/{id}/full` (o extensión del PATCH actual) con el mismo body que `CreateFullAltaRubrosSubrubrosRequestDto` **sin** cambiar `rubroId` si `principal=true`.

Debe actualizar subrubros, modalidad, `diasLaborales` y, si aplica, `domicilioLocal`, de forma atómica. El rubro principal no se elimina.
