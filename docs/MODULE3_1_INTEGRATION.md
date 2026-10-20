# Módulo 3.1 — Integración Selfie, OTP teléfono y Push KYC

Extensión post-cierre M03. Contratos validados contra OpenAPI staging (`apioficios.pushsoftware.com.ar`).

## 1. Selfie KYC (proveedor)

| Operación | Endpoint |
|-----------|----------|
| Subir | `POST /img-documento/subir-documento-selfie` (multipart `selfieArchivo`) |
| Buscar | `GET /img-documento/search?tipoDocumento=selfie` |
| Reemplazar | `PATCH /img-documento/{id}/reemplazar-selfie` |

**Faltantes checklist:** `selfie_imagen`, `selfie_pendiente_revision`, `selfie_rechazada`.

**Ruta app:** `onboarding/provider/selfie`

`identidadVerificada` requiere DNI + selfie aprobados.

## 2. OTP teléfono (cliente)

| Operación | Endpoint |
|-----------|----------|
| Enviar código | `POST /contacto/{id}/enviar-codigo-telefono` |
| Verificar | `POST /contacto/{id}/verificar-codigo-telefono` body `{ codigo }` |

Cooldown servidor: 60 s (`proximoReenvioEnSegundos` en respuesta/error VF05).

**Flujo:** guardar contacto → verificar teléfono → guardar domicilio.

**Ruta app:** `onboarding/verify-phone/{contactId}`

## 3. Push KYC (proveedor)

| Operación | Endpoint |
|-----------|----------|
| Registrar token | `POST /notifications/device-token` |
| Marcar leída | `PATCH /notifications/{id}/read` |
| Historial | `GET /notifications` |

**Payload FCM:** `data.type === "KYC_REVISION_DOCUMENTACION"`, campos `evento`, `deep_link`, `notificacionId`.

**Deep links → rutas internas:**

| Backend | App |
|---------|-----|
| `onboarding/identidad/dni` | `onboarding/provider/dni` |
| `onboarding/identidad/selfie` | `onboarding/provider/selfie` |
| `onboarding/identidad` | `onboarding/provider-checklist` |
| `onboarding` | `onboarding/provider-checklist` |

**Fallback:** polling `ON_RESUME` en checklist proveedor.

## QA manual (staging)

- [ ] Subir selfie → pendiente → aprobada
- [ ] Selfie rechazada + reemplazo con motivo
- [ ] DNI + selfie → `identidadVerificada`
- [ ] Contacto → OTP WhatsApp → domicilio
- [ ] Reenvío OTP respeta cooldown
- [ ] Push DNI/selfie aprobado/rechazado → navegación correcta
- [ ] Sin permiso notificaciones → polling funciona
