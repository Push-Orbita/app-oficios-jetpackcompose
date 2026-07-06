# Módulo 3 — FCM y staging (acción manual)

No hay `google-services.json` en el repo. La app usa **polling al reanudar** el checklist proveedor para detectar cambios de `identidadVerificada`.

## Para habilitar push KYC

1. Crear proyecto Firebase y agregar app Android `com.cocido.oficios`.
2. Colocar `google-services.json` en `app/`.
3. Agregar plugin Google Services y dependencia FCM.
4. Implementar registro de token: `POST /notifications/device-token`.

## Cuentas staging recomendadas

- Cliente de prueba en `apioficios.pushsoftware.com.ar`
- Proveedor de prueba con flujo DNI pendiente → aprobado (admin)
