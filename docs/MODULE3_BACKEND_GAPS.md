# Módulo 3 — Gaps backend / producto

Consultado OpenAPI: https://apioficios.pushsoftware.com.ar/api-json

## Estado M3.1 (integración en curso)

Los ítems diferidos en el cierre M03 ahora tienen backend en staging. La integración en app se documenta en [MODULE3_1_INTEGRATION.md](MODULE3_1_INTEGRATION.md).

| Ítem | Backend staging | App |
|------|-----------------|-----|
| Selfie KYC | `POST /img-documento/subir-documento-selfie`, search, reemplazar | M3.1 |
| OTP teléfono | `POST /contacto/{id}/enviar-codigo-telefono`, verificar | M3.1 |
| Push KYC | `POST /notifications/device-token`, FCM `KYC_REVISION_DOCUMENTACION` | M3.1 (requiere `google-services.json`) |

## Implementado desde M03

- DNI frente/dorso: `POST /img-documento/subir-documento-dni`
- Certificado oficio opcional: `POST /certificado-trabajo`
- Domicilio piso/dpto: campos en `CreateDomicilioRequestDto`
- KYC feedback: polling al reanudar checklist + push FCM (M3.1)

## Infra manual

- Firebase + `google-services.json` para push en dispositivo real — ver [MODULE3_FCM_STAGING.md](MODULE3_FCM_STAGING.md)
- Cuentas staging cliente/proveedor + flujo admin DNI/selfie en staging
