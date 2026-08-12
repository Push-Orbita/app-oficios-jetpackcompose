# Backend — `/barrios/search` exige auth y rompe el registro de oficio

> **Estado: RESUELTO.** Confirmado por retest directo — `GET /barrios/search` sin header `Authorization` ahora devuelve `200` con resultados reales.

## Resumen para el equipo de backend

El campo **Barrio** en la pantalla **"Registro de oficio"** (paso previo a crear la cuenta del trabajador) no carga ninguna sugerencia, sin importar qué se escriba. No es un bug de la app: `GET /barrios/search` devuelve **401 Unauthorized** porque el endpoint exige autenticación (`security: authorization` en el propio Swagger), pero esta pantalla ocurre **antes** de que exista la cuenta — todavía no hay token para mandar.

## Evidencia — request real

```
GET /barrios/search?q=san&pageSize=20&pageNumber=1
(sin header Authorization — no hay sesión en este punto del flujo)

→ 401 Unauthorized
```

Confirmado además contra el propio Swagger (`https://apioficios.pushsoftware.com.ar/api-json`):

```json
"GET /barrios/search": {
  "security": [{ "authorization": [] }],
  "responses": { "401": { "description": "No autorizado" } }
}
```

## El problema es de inconsistencia, no solo de este endpoint

Revisando el resto del catálogo geográfico/rubros usado durante el registro (sin login todavía):

| Endpoint | Requiere auth |
|---|---|
| `GET /categorias/search` | No (público) |
| `GET /rubros/search` | No (público) |
| `GET /sub-rubros/search` | No (público) |
| `GET /barrios/search` | **Sí** |
| `GET /localidades/search` | **Sí** |
| `GET /provincias/search` | **Sí** |
| `GET /paises/search` | **Sí** |

Categorías, rubros y sub-rubros (también usados en el mismo formulario de registro, para elegir el oficio) están correctamente expuestos como públicos. El catálogo geográfico (barrios/localidades/provincias/países) quedó como la excepción que exige auth, y por eso es el único campo que falla en esta pantalla.

## Qué pedimos

Hacer público (sin requerir `Authorization`) al menos:

- `GET /barrios/search`

Y revisar si también corresponde para `GET /localidades/search`, `GET /provincias/search`, `GET /paises/search`, ya que son datos geográficos de referencia (no información sensible ni específica de un usuario), igual que ya lo es el catálogo de categorías/rubros.

## Impacto actual

Ningún trabajador nuevo puede completar el paso de "Registro de oficio" seleccionando su barrio — el campo nunca muestra resultados. Bloquea el alta de cuentas de trabajador desde cero.
