# Backend — Falla en verificación de teléfono (OTP)

> **Estado: SIGUE FALLANDO.** El equipo de backend informó una corrección, pero se volvió a probar (con un contacto nuevo, sin historial de reintentos) y el error es idéntico. Ver "Caso 3" y "Cómo reproducir" abajo.

## Resumen para el equipo de backend

La integración del lado de la app **ya está hecha y funciona correctamente** contra `POST /contacto` y `POST /contacto/{id}/enviar-codigo-telefono`. El problema está confirmado del lado del backend/Twilio: **el envío del código OTP falla siempre**, tanto por WhatsApp como por SMS, para números argentinos reales. No es un tema de la app ni de datos mal enviados, y **no es un problema de token/autenticación** (ver evidencia abajo).

## Evidencia — request/response reales

### Caso 1: número de prueba, canal WhatsApp (vía la app)

```
POST /contacto  →  201 Created
{"nombre":"Cliente Prueba","telefonoWhatsapp":"3705123456","favorito":true}
→ {"id":6,...,"telefonoWhatsapp":"+543705123456","telefonoVerificado":false}

POST /contacto/6/enviar-codigo-telefono  →  400
Mensaje mostrado: "No se pudo entregar el código por el canal configurado.
Ingresá el código de 6 dígitos que enviamos a 3705123456 por WhatsApp."
```

### Caso 2: número real del usuario, canal SMS (llamado directo a la API, sin pasar por la app)

Contacto de prueba `id=8`, teléfono `+543705219109` (número argentino real, no de prueba).

```
POST /contacto/8/enviar-codigo-telefono
Authorization: Bearer <token válido>

→ 400 Bad Request
{
  "error": true,
  "errorDetails": {
    "code": "VF06",
    "message": "No se pudo enviar el código. Intentá más tarde.",
    "details": "{\"detail\":\"No se pudo entregar el código por el canal configurado\",\"canal\":\"sms\"}"
  }
}
```

Timestamp del servidor (header `date` de la respuesta): `Mon, 27 Jul 2026 14:00:14 GMT`.

### Caso 3: retest después del fix reportado — contacto nuevo, mismo resultado

Se creó un contacto **nuevo** (`id=10`, mismo número real `+543705219109`) específicamente para descartar cualquier cooldown o rate-limit acumulado de pruebas anteriores:

```
POST /contacto  →  201 Created
{"nombre":"Cliente Prueba","telefonoWhatsapp":"3705219109","favorito":false}
→ {"id":10,...,"telefonoWhatsapp":"+543705219109","telefonoVerificado":false}

POST /contacto/10/enviar-codigo-telefono  →  400
{
  "error": true,
  "errorDetails": {
    "code": "VF06",
    "message": "No se pudo enviar el código. Intentá más tarde.",
    "details": "{\"detail\":\"No se pudo entregar el código por el canal configurado\",\"canal\":\"whatsapp\"}"
  }
}
```

Mismo código `VF06`, mismo mensaje, contacto sin ningún historial previo. El fix reportado no cambió el resultado.

### Confirmamos que no es un problema de token

El `POST /contacto` que creó el contacto `id=10` usó el **mismo token**, en la **misma sesión**, y devolvió `201 Created`. Si el token estuviera vencido o mal formado, esa llamada también habría fallado con `401`. Como `POST /contacto` funcionó y `POST /contacto/10/enviar-codigo-telefono` con el mismo token dio `400 VF06` (no `401`), el backend está recibiendo la solicitud autenticada correctamente — el fallo es al despachar el código vía Twilio, no de autenticación.

## Cómo reproducir

```bash
# 1. Login (obtener token)
curl -X POST 'https://apioficios.pushsoftware.com.ar/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{"email":"<EMAIL_DE_PRUEBA>","contrasena":"<CONTRASENA_DE_PRUEBA>"}'
# → copiar "access_token" de la respuesta

# 2. Crear un contacto de prueba nuevo (sin historial de cooldown)
curl -X POST 'https://apioficios.pushsoftware.com.ar/contacto' \
  -H 'Authorization: Bearer <TOKEN>' \
  -H 'Content-Type: application/json' \
  -d '{"nombre":"Cliente Prueba","telefonoWhatsapp":"3705219109","favorito":false}'
# → copiar "id" de la respuesta

# 3. Intentar enviar el código (falla)
curl -X POST 'https://apioficios.pushsoftware.com.ar/contacto/<ID>/enviar-codigo-telefono' \
  -H 'Authorization: Bearer <TOKEN>'
```

## Qué confirma esto

- El endpoint **sí** recibe la solicitud correctamente (no es un 401/403/422 de datos mal formados) — el fallo ocurre al intentar despachar el mensaje a través de Twilio.
- Según la propia documentación del endpoint (`descripción` en el Swagger): "Genera un código de 6 dígitos y lo envía por WhatsApp (default) o SMS vía Twilio" — el backend decide internamente el canal, la app no tiene forma de elegirlo.
- **Falló primero por WhatsApp (caso 1) y también falló el fallback a SMS (caso 2)** — es decir, ningún canal está funcionando, no es un problema puntual de un canal.
- Probado con dos números distintos (uno de prueba, uno real del usuario) — descarta que sea un problema del número en sí.

## Qué necesitamos que revisen

1. **Credenciales de Twilio** configuradas en el backend (Account SID / Auth Token / API Key) — vigentes y con permisos correctos.
2. **Saldo de la cuenta de Twilio** — si es una cuenta trial, Twilio solo permite enviar a números verificados manualmente en su consola, lo cual explicaría el fallo con cualquier número real.
3. **WhatsApp Business API / Messaging Service**: si el sender de WhatsApp está aprobado por Meta y activo (el proceso de aprobación de WhatsApp Business es manual y puede quedar pendiente o vencer).
4. **Permisos geográficos** del número/Messaging Service de Twilio usado para SMS — Twilio requiere habilitar explícitamente el envío a Argentina (+54) en algunas cuentas.
5. **Logs de Twilio** (consola de Twilio → Monitor → Logs) alrededor del timestamp `2026-07-27 14:00:14 GMT` para ver el código de error específico que Twilio devolvió — eso va a decir exactamente cuál de los puntos anteriores es.

## Pedido adicional (para no bloquear QA mientras lo resuelven)

Sería muy útil un modo de prueba/staging: un código fijo (ej. `000000`) válido para números whitelisteados, para poder seguir probando el flujo de pedidos de punta a punta sin depender de que el envío real de OTP esté funcionando.

## Impacto actual

Mientras esto no se resuelva, **ninguna cuenta cliente nueva puede llegar a la Home** (la verificación de teléfono es requisito de `perfilCompleto` que calcula el propio backend). Esto bloquea el checkpoint final del módulo de Pedidos del lado de la app.
