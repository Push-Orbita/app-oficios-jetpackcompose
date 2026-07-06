# Cierre Módulo 3 — Auth / Onboarding + Identidad

## Criterio de cierre

- Flujos auth + onboarding cliente/proveedor operativos contra API staging.
- Paridad Figma ≥ 90% en auth + onboarding (sin selfie ni OTP teléfono).
- Suite de tests del plan en verde.

## Dependencias backend documentadas

Ver [MODULE3_BACKEND_GAPS.md](MODULE3_BACKEND_GAPS.md): selfie y OTP teléfono diferidos (opción A).

## FCM / staging manual

Ver [MODULE3_FCM_STAGING.md](MODULE3_FCM_STAGING.md). KYC usa polling al reanudar checklist (sin FCM).

## QA manual — Cliente

1. Splash nativo → login → registro cliente → checklist.
2. Paso unificado **Contacto y domicilio**: WhatsApp, calle, número, piso, dpto, barrio.
3. Verificar persistencia vía API (`GET /domicilios/mis`, `GET /contacto/mis`).
4. Verificar email opcional.

## QA manual — Proveedor

1. Registro con categorías → datos personales → checklist.
2. DNI frente/dorso + certificado opcional → estado en revisión.
3. Tras aprobación admin: rubros, disponibilidad, métodos de pago, local.
4. Rubro secundario vía toggle en paso rubros (`POST /full/secundario`).
5. Finalizar → pending approval → home proveedor.

## Tests automatizados

```bash
./gradlew :feature:auth:testDebugUnitTest \
  :feature:onboarding:testDebugUnitTest \
  :domain:test \
  :core:network:testDebugUnitTest \
  :core:data:testDebugUnitTest
```

## Fuera de alcance M3

- Selfie / OTP teléfono (sin endpoint).
- Push FCM DNI aprobado (requiere Firebase).
- Agenda rica (`/sesion`, `/agenda/slot`) — backlog M4.
