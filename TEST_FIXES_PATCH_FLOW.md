# Test Fixes — AccountRepositoryCacheTest — PATCH full flow

**Branch:** `cursor/qa-fixes-figma-ee07`  
**Commit:** Siguiente tras `1efe6270`

---

## Problema

Después del commit Ola 1 (`9f4e66d8`) que introdujo `updateRubroFull` (PATCH), 3 de 8 tests en `AccountRepositoryCacheTest` fallaban:

1. `replace of cached service posts sesion and never deletes` ❌
2. `destildar does not delete sesion nor invent a subrubro patch` ❌  
3. `extra specialty ids on edit do not invent a subrubro post` ❌

**Root cause:** 
- `RecordingProviderProfileApi.updateRubroFull` tiraba `unused()` error
- Assertions verificaban el flujo viejo (DELETE+POST) en lugar del nuevo (PATCH)

---

## Solución

### 1. Fake actualizado: `RecordingProviderProfileApi`

**Antes:**
```kotlin
override suspend fun updateRubroFull(id: Long, request: ...) = unused()
```

**Ahora:**
```kotlin
var updateRubroFullCalls = 0

override suspend fun updateRubroFull(id: Long, request: ...) {
    updateRubroFullCalls += 1
}
```

El fake ahora registra llamadas a `updateRubroFull` y retorna exitosamente.

---

## Tests reescritos

### Test 1: `edit cached service uses PATCH and creates sesion when needed`

**Antes:** `replace of cached service posts sesion and never deletes`

**Cambios:**
- **Nombre:** Refleja el flujo PATCH actual
- **Assertions:**
  - ✅ `assertEquals(1, api.updateRubroFullCalls)` — verifica PATCH llamado
  - ✅ `assertEquals(0, api.createFullCalls)` — no POST
  - ✅ `assertEquals(emptyList<Long>(), api.deletedIds)` — no DELETE
  - ✅ `assertEquals(1, agenda.createSesionCalls)` — sesión creada cuando `requiresTurns=true`

**Qué verifica:** Editar servicio cacheado usa PATCH `/trabajador-oficio-rubro/{id}/full` y crea sesión si se requieren turnos.

---

### Test 2: `disabling turns uses PATCH and does not delete sesion`

**Antes:** `destildar does not delete sesion nor invent a subrubro patch`

**Cambios:**
- **Nombre:** Actualizado a semántica PATCH
- **Assertions:**
  - ✅ `assertEquals(1, api.updateRubroFullCalls)` — PATCH llamado
  - ✅ `assertEquals(0, api.createFullCalls)` — no POST
  - ✅ `assertEquals(0, agenda.createSesionCalls)` — no crea sesión nueva
  - ✅ `assertEquals(emptyList<Long>(), agenda.deletedSesionIds)` — **no borra** sesión existente

**Qué verifica:** Deshabilitar turnos (`requiresTurns: false`) usa PATCH y **preserva** sesión existente (backend mantiene histórico).

---

### Test 3: `editing specialty ids uses PATCH and does not recreate service`

**Antes:** `extra specialty ids on edit do not invent a subrubro post`

**Cambios:**
- **Nombre:** Clarifica que edita specialty IDs con PATCH, no recrea
- **Assertions:**
  - ✅ `assertEquals(1, api.updateRubroFullCalls)` — PATCH llamado
  - ✅ `assertEquals(0, api.createFullCalls)` — no POST
  - ✅ `assertEquals(emptyList<Long>(), api.deletedIds)` — no DELETE

**Qué verifica:** Editar lista de `subRubroIds` usa PATCH atómico, no borra y recrea el servicio.

---

## Tests sin cambios (2/2 PASS)

Estos tests NO se tocaron porque verifican flujos que no usan el servicio cacheado:

1. ✅ `replace does not delete when create returns 400` — falla antes de editar
2. ✅ `replace deletes old rubro only after create 2xx` — DELETE+POST para servicios NO cacheados

El flujo DELETE+POST sigue existiendo como fallback cuando el servicio no está en caché (`updateExistingProviderService` vs `createThenDeleteOld`).

---

## Resumen de cambios

| Test | Cambio | Razón |
|------|--------|-------|
| `edit cached service uses PATCH...` | Renombrado + assertions PATCH | Flujo ahora usa `updateRubroFull` |
| `disabling turns uses PATCH...` | Renombrado + assertions PATCH | Verificar PATCH + no-delete sesión |
| `editing specialty ids uses PATCH...` | Renombrado + assertions PATCH | PATCH en lugar de POST |
| `RecordingProviderProfileApi` | +`updateRubroFullCalls` counter | Fake registra llamadas PATCH |

**Total:** 3 tests reescritos, 1 fake actualizado, 5 tests sin cambios.

---

## Verificación (lanot)

`:core:data:testProdDebugUnitTest` debe ejecutar **8/8 PASS**:

```
AccountRepositoryCacheTest > upload and delete gallery emit the new provider details PASSED
AccountRepositoryCacheTest > update profile emits the new cached name PASSED
AccountRepositoryCacheTest > logout coordinator clears gallery cache PASSED
AccountRepositoryCacheTest > replace does not delete when create returns 400 PASSED
AccountRepositoryCacheTest > replace deletes old rubro only after create 2xx PASSED
AccountRepositoryCacheTest > edit cached service uses PATCH and creates sesion when needed PASSED
AccountRepositoryCacheTest > disabling turns uses PATCH and does not delete sesion PASSED
AccountRepositoryCacheTest > editing specialty ids uses PATCH and does not recreate service PASSED
```

---

**Archivo:** `/workspace/core/data/src/test/java/com/cocido/oficios/core/data/repository/AccountRepositoryCacheTest.kt`
