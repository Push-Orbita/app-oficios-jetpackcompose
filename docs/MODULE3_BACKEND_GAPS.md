# Módulo 3 — Gaps backend / producto (decisión de cierre)

Consultado OpenAPI: https://apioficios.pushsoftware.com.ar/api-json

## Decisión adoptada (opción A)

| Ítem Figma | Estado API | Acción app |
|------------|------------|------------|
| Selfie / foto de rostro | No hay endpoint | **Diferido** — no implementar hasta que backend exponga upload |
| OTP teléfono en registro/onboarding | No hay endpoint | **Diferido** — pantalla unificada contacto+domicilio sin OTP; copy sin promesa de código SMS |

## Implementado en su lugar

- DNI frente/dorso: `POST /img-documento/subir-documento-dni`
- Certificado oficio opcional: `POST /certificado-trabajo`
- Domicilio piso/dpto: campos en `CreateDomicilioRequestDto`
- KYC feedback: polling al reanudar checklist (sin FCM hasta `google-services.json`)

## Pendiente de vos (infra)

- Firebase + `google-services.json` para push de aprobación DNI
- Cuentas staging cliente/proveedor + flujo admin DNI en staging
