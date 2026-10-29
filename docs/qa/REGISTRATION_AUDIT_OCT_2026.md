# Auditoría de Flujo de Registro - Octubre 2026

## Alcance
Auditoría completa de flujos de registro cliente y trabajador en la aplicación Android Oficios, enfocada en:
1. **Coherencia domicilio ↔ mapa**: Barrio, calle, número vs. pin confirmado
2. **Upload de fotos**: Selfie, DNI frente/dorso, certificados — validación multipart contra PROD API

## Ambiente
- **API**: `https://apioficios.pushsoftware.com.ar/` (PROD)
- **OpenAPI**: `/docs/swagger/swagger.yml`
- **Figma**: Diseño actualizado de onboarding
- **Branch**: `cursor/fix-registration-coherence-photo-upload-a88e`

---

## Problema 1: Coherencia Domicilio ↔ Mapa

### Evidencia
Screenshot proporcionado por Matías muestra:
- Pin colocado en **Farmacia Galeno** (coordenadas visibles en mapa)
- Campos de dirección: Barrio, Calle, Número **vacíos**
- Usuario pudo **guardar** esta inconsistencia

### Root Cause
**Diseño original del flujo cliente/trabajador domicilio permitía**:
1. Usuario coloca pin en mapa o usa GPS → coordenadas guardadas en `latitud`/`longitud`
2. Campos `barrio`/`calle`/`número` **independientes** del pin
3. Submit valida solo `barrioId != null && calle.isNotBlank()`
4. **NO valida** coherencia pin ↔ dirección escrita

**El repo YA tenía** una arquitectura robusta en `FormosaAddressGeocoding` (usada por `CommercialLocalViewModel` y provider onboarding) que **previene** este problema:
- `AddressLocationSnapshot`: state machine (None, Preview, Picking, Confirmed, NeedsMap)
- **Forward geocoding**: dirección escrita → coordenadas (via `GeocodeLocalAddressUseCase`)
- **Confirmación de pin**: usuario debe confirmar explícitamente
- **Validación `canPersist`**: solo guarda cuando address + location confirmado

**Cliente onboarding NO usaba este diseño** — permitía guardar pin sin dirección o dirección sin pin confirmado.

### Solución Implementada (PR #29)
Integrar `AddressLocationSnapshot` + `FormosaAddressGeocoding` en flujos cliente:

#### Cambios en ViewModels
**`ClientDomicilioViewModel` y `ClientContactDomicilioViewModel`**:
1. **Agregar** `location: AddressLocationSnapshot` al state (reemplaza `latitud`/`longitud` sueltos)
2. **Inyectar** `GeocodeLocalAddressUseCase`
3. **Flujo submit**:
   ```kotlin
   fun submit() {
       if (barrioId == null || calle.isBlank()) {
           error = "Complete barrio y calle"
           return
       }
       if (!location.canPersist) {
           resolveFromAddress()  // Forward geocode
           return
       }
       persist()  // Solo si location.confirmed
   }
   ```
4. **Métodos nuevos**:
   - `locateOnMap()` → `FormosaAddressGeocoding.openMap()` (abre picker sin autocompletar dirección)
   - `confirmPin()` → `FormosaAddressGeocoding.confirmPin()` (marca como confirmado)
   - `resolveFromAddress()` → forward geocode con `GeocodeLocalAddressUseCase`
5. **Invalidar location** cuando usuario cambia barrio/calle/número → fuerza re-geocode

#### Comportamiento Correcto
- **Usuario escribe** barrio + calle + número → Submit intenta **forward geocode** → muestra pin preview → usuario confirma
- **Geocoding ambiguo/fallido** → mensaje `FormosaAddressGeocoding.GEOCODER_ERROR_MESSAGE` → usuario debe ubicar manualmente en mapa
- **«Usar mi ubicación»** → coloca pin en GPS coords → **NO autollena dirección** (respeta regla "La calle escrita no se modifica")
- **Guardar** bloqueado hasta que `location.canPersist` (dirección válida + pin confirmado)

#### UI
Pantallas ya usan `OficiosLocationPicker`/`OficiosAddressLocationPicker` que respetan `AddressLocationSnapshot.mapMode` (Preview vs Picking).

---

## Problema 2: Upload de Fotos

### Contexto
PR #28 ya corrigió:
- `IdentityDocumentUploadDataSource`: usa `ImageUploadContract.requireAcceptedMime()` (magic bytes JPEG/PNG/WEBP/GIF)
- Part names correctos: `frenteArchivo`, `dorsoArchivo`, `selfieArchivo`
- Endpoints alineados con OpenAPI:
  - `/img-documento/subir-documento-dni` (frente + dorso)
  - `/img-documento/subir-documento-selfie` (selfie)
  - `/img-documento/{id}/reemplazar-selfie` (reemplazo)

### Error Reportado
Log `facundo-selfie-400_c667.log`:
```
POST /api/v1/img-documento/subir-documento-selfie → VA01 400
Body: {}
```

### Análisis
- **VA01** = "Datos de entrada no válidos" (error de validación del backend)
- Body vacío indica:
  1. Multipart parts no enviados, O
  2. Backend rechaza MIME type / nombre de archivo, O  
  3. Archivo 0 bytes o corrupto

**Posibles causas**:
1. **Picker de cámara** (vs gallery) escribe archivo temporal que:
   - Compresión diferente
   - MIME incorrecto en headers (aunque magic bytes deberían pasar)
   - Archivo movido/eliminado antes de upload
2. **Permisos Android 13+** (MANAGE_EXTERNAL_STORAGE / READ_MEDIA_IMAGES)
3. **Backend validation** más estricta que OpenAPI docs (ej. max size no documentado, EXIF requirements)

### Acciones
1. ✅ **PR #28**: MIME magic bytes, part names, endpoints correctos
2. ⚠️ **Verificación pendiente**:
   - Reproducir con APK debug en dispositivo físico / emulador API 33+
   - Capturar foto desde cámara → verificar archivo existe y tiene >0 bytes antes de upload
   - Logs de `ImageUploadContract.requireAcceptedMime()` output
   - Comparar headers multipart cámara vs gallery
3. **Mejora recomendada**: mapear códigos VA01/VA02/etc. a mensajes user-friendly:
   ```kotlin
   when (errorCode) {
       "VA01" -> "La imagen no es válida. Intente con otra foto."
       "VA02" -> "La imagen es demasiado grande. Capture otra con menor resolución."
       // ...
   }
   ```

---

## Resumen de Cambios (PR #29)

### Archivos Modificados
- `feature/onboarding/.../ClientDomicilioViewModel.kt`
- `feature/onboarding/.../ClientContactDomicilioViewModel.kt`
- `docs/qa/REGISTRATION_AUDIT_OCT_2026.md` (este archivo)

### Patrón Implementado
✅ **Forward geocoding** (dirección → coords) con confirmación explícita de pin  
❌ ~~Reverse geocoding~~ (coords → dirección autofill) — **removido**

### Estado de Compilación
- [x] Kotlin compila sin errores
- [ ] Unit tests pendientes (fuera del scope de esta auditoría)
- [x] Alineación con `FormosaAddressGeocoding` design existente

### Referencias Internas
- `domain/.../model/FormosaAddressGeocoding.kt`
- `domain/.../usecase/location/GeocodeLocalAddressUseCase.kt`
- `feature/settings/.../CommercialLocalViewModel.kt` (patrón canónico)

---

## Estado Final
| Issue | Status | Notas |
|-------|--------|-------|
| Pin sin dirección (screenshot) | ✅ FIXED | Submit bloqueado sin `location.canPersist` |
| Dirección sin pin | ✅ FIXED | Forward geocode forzado antes de persist |
| GPS autollena dirección | ✅ FIXED | GPS solo coloca pin, no modifica calle |
| Upload selfie VA01 | ⚠️ NEEDS TEST | PR #28 aplicado, requiere verificación en device |
| Códigos error user-facing | 🔜 FUTURE | Mapear VA01/VA02 a mensajes claros |

**Next Steps**:
1. Merge PR #29 (coherencia domicilio ↔ mapa)
2. QA manual: capturar selfie desde cámara en API 33+ device → verificar upload exitoso
3. Si VA01 persiste: agregar logs detallados en `IdentityDocumentUploadDataSource` y reproducir con APK signed
