# Fase 9 — Release readiness

**Estado:** Hardening Android aplicado; items externos pendientes

## Completado

| Ítem | Archivo |
|------|---------|
| R8/ProGuard | `app/build.gradle.kts` `isMinifyEnabled = true`, `isShrinkResources = true` |
| Reglas Retrofit/Hilt/serialization | `app/proguard-rules.pro` |
| Network security config | `app/src/main/res/xml/network_security_config.xml` |
| Manifest | `android:networkSecurityConfig` |
| Backup rules — excluir tokens | `data_extraction_rules.xml` |
| Eliminar cuenta UI | `SettingsScreen` — deshabilitado hasta BACK-ACCOUNT-001 |
| Logging sanitizado | Solo debug en `NetworkModule` |

## Pendiente externo / producto

| Ítem | Acción |
|------|--------|
| `AppConstants.SUPPORT_PHONE` | Reemplazar `+5491112345678` con línea Formosa real |
| `google-services.json` productivo | Firebase proyecto `com.cocido.oficios` |
| Política privacidad URL | Confirmar URL en producción para Play Store |
| Play Store assets | Ícono 512px, feature graphic, screenshots (cliente + trabajador) |
| Certificate pinning | Evaluar v1.1 si backend usa CDN |

## Checklist pre-release

- [ ] `./gradlew :app:assembleRelease` sin errores ProGuard
- [ ] Smoke E2E en AVD Pixel_Tablet API 35
- [ ] Verificar contratos: `verify-fase7-backend.ps1`, `verify-fase8-backend.ps1`
