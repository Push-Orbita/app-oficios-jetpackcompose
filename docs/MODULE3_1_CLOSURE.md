# Módulo 3.1 — Cierre integración Selfie, OTP y Push KYC

## Entregado

### Selfie KYC (proveedor)
- API: upload, search con `tipoDocumento=selfie`, reemplazar selfie
- Capa data/domain: `ProviderSelfieDocument`, repositorio y use cases
- UI: `ProviderSelfieRoute` / pantalla dedicada
- Checklist: faltantes `selfie_imagen`, `selfie_pendiente_revision`, `selfie_rechazada`
- Corrección DNI: `canSubmit` acepta imágenes remotas precargadas; preload de número de documento

### OTP teléfono (cliente)
- API: `enviar-codigo-telefono`, `verificar-codigo-telefono`
- `saveContacto` retorna `id` del contacto
- Flujo contacto+domicilio → verificación teléfono → guardado de domicilio pendiente
- Pantalla `VerifyPhoneRoute` (patrón VerifyEmail, cooldown 60 s servidor)
- Checklist: paso `telefono_verificado`

### Push KYC (FCM)
- Firebase Messaging + `google-services.json` stub (reemplazar con proyecto real para staging/prod)
- `NotificationsApi`: device-token, mark-read, list
- `OficiosFirebaseMessagingService` con handler `KYC_REVISION_DOCUMENTACION`
- `OnboardingDeepLinkMapper` backend → rutas Compose
- Registro de token post-login
- Polling `ON_RESUME` mantenido como fallback

## Tests
- `IdentityDocumentApiIntegrationTest` (selfie multipart)
- `OnboardingMapperTest` (selfie + teléfono)
- `OnboardingStepNavigatorTest`, `OnboardingDeepLinkMapperTest`

## QA manual pendiente (staging)
Ver checklist en [MODULE3_1_INTEGRATION.md](MODULE3_1_INTEGRATION.md).

## Infra
Reemplazar `app/google-services.json` stub por configuración Firebase real antes de probar push en dispositivo.
