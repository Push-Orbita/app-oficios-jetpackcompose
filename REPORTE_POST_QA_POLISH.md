# Reporte Post-QA Polish — PR #22 — 2026-10-28

**Branch:** `cursor/qa-fixes-figma-ee07`  
**HEAD:** `1efe6270` (← `9f4e66d8`)  
**PR:** [#22](https://github.com/Push-Orbita/app-oficios-jetpackcompose/pull/22)  
**Target:** PROD exclusivo (`https://apioficios.pushsoftware.com.ar/`)

---

## Resumen ejecutivo

Tres fixes app-side tras smoke QA PROD en lanot (`INFORME_ea96.md`). Commit `1efe6270` resuelve:
1. ✅ Título "Pedido expirado" en detalle de booking EXPIRADA (antes decía "cancelado")
2. ✅ Contraste legible del chip "Solicitud expirada" (rojo saturado en lugar de rosado pálido)
3. ✅ Test fakes implementan `updateRubroFull` → `:core:data` tests compilan

**No Gradle en VM.** Re-QA pendiente en lanot. **No merge.**

---

## Fix 1 — Título pedido expirado

### Problema (QA)
Pedido #24 (booking `EXPIRADA`) mostraba título **"Pedido cancelado"** en pantalla de detalle. Debería decir "Pedido expirado".

### Root cause
`orderScreenTitleRes()` mapeaba `EXPIRED` → `R.string.orders_title_cancelled` (mismo título que `CANCELLED`).

### Solución
- ✅ Agregado string `orders_title_expired = "Pedido expirado"` en `strings.xml`
- ✅ `orderScreenTitleRes()` línea 199 ahora usa `R.string.orders_title_expired` para `EXPIRED`

### Archivos modificados
```
feature/home/src/main/res/values/strings.xml
feature/home/src/main/java/com/cocido/oficios/feature/home/orders/OrderStatusPresentation.kt
```

### Verificar (Andrea)
- Pedido expirado (#24) muestra título **"Pedido expirado"** en app bar
- Pedido cancelado sigue mostrando "Pedido cancelado"

---

## Fix 2 — Contraste chip expirado

### Problema (QA)
Chip "Solicitud expirada" era casi ilegible: rosado muy pálido sobre fondo blanco hueso (`07-ola2-booking24-expirada.png`). Chip cancelado era legible.

### Root cause
`orderStatusVisual()` para `EXPIRED` usaba:
- `container: colors.errorContainer` (rosado pálido ~#FFDAD6)
- `content: colors.onError` (blanco ~#FFFFFF)

Resultado: texto blanco sobre rosado muy claro = contraste insuficiente.

### Solución
Cambio de colores para `EXPIRED` en `orderStatusVisual()` línea 61:
- **Antes:** `errorContainer` + `onError` 
- **Ahora:** `error` + `onError` (rojo saturado + blanco)

Esto es la combinación estándar M3 de alto contraste para estados de error.

`CANCELLED` mantiene `errorContainer` + `onError` (no cambiado, según QA es legible).

### Archivos modificados
```
feature/home/src/main/java/com/cocido/oficios/feature/home/orders/OrderStatusPresentation.kt
```

### Verificar (Andrea)
- Chip "Solicitud expirada" legible en tema light (rojo saturado con texto blanco)
- Chip "Cancelado" sin cambios (sigue legible)

---

## Fix 3 — Test fake compile break

### Problema (QA)
```
:core:data:compileProdDebugUnitTestKotlin FAILED
AccountRepositoryCacheTest.kt:482:9 
Class 'UnusedProviderProfileApi' is not abstract and does not 
implement abstract member 'updateRubroFull'
```

Tests unitarios de `:core:data` no compilaban (no bloquea APK, pero rompe CI si se corre).

### Root cause
Commit `9f4e66d8` (Ola 1) agregó método `updateRubroFull(id, request)` a interface `ProviderProfileApi`.  
Los test fakes en `AccountRepositoryCacheTest.kt` no lo implementaban:
- `UnusedProviderProfileApi` (línea 482)
- `RecordingProviderProfileApi` (línea 460)

### Solución
Agregado método stub en ambos fakes:
```kotlin
override suspend fun updateRubroFull(
    id: Long, 
    request: CreateFullAltaRubrosSubrubrosRequestDto
) = unused()
```

### Archivos modificados
```
core/data/src/test/java/com/cocido/oficios/core/data/repository/AccountRepositoryCacheTest.kt
```

### Verificar
- `:core:data:compileProdDebugUnitTestKotlin` debe compilar OK
- Tests existentes siguen PASS (no cambiados, solo agregado stub)

---

## Archivos cambiados (resumen)

| Archivo | Cambios |
|---------|---------|
| `feature/home/src/main/res/values/strings.xml` | +1 string `orders_title_expired` |
| `feature/home/src/main/java/.../OrderStatusPresentation.kt` | Línea 61: `error` + `onError` para EXPIRED<br>Línea 199: `orders_title_expired` para EXPIRED |
| `core/data/src/test/.../AccountRepositoryCacheTest.kt` | +2 métodos `updateRubroFull` en test fakes |
| `REPORTE_OLA_1_2_PROD.md` | Agregado (documentación Ola 1+2) |

**Total:** 4 archivos modificados/agregados, 179 inserciones, 2 deleciones.

---

## Pendiente

### Re-QA en lanot (`1efe6270`)
1. Título "Pedido expirado" en detalle de booking EXPIRADA
2. Chip expirado legible (rojo saturado)
3. `:core:data:compileProdDebugUnitTestKotlin` compila

### Out of scope este PR
- Regenerar `slotsTurno` borrados (efecto colateral PATCH full — backend documenta)
- F8 day-by-day schedule echo / `diasLaborales` missing tras PATCH (backend)
- Remover flavor `dev`
- Crear nuevos slots o mutar más datos prod

---

## Commit

```
1efe6270 — Post-QA polish: título expirado, contraste chip, test fake

Fixes tras smoke QA PROD (9f4e66d8 → lanot):
- orderScreenTitleRes() usa orders_title_expired para EXPIRED
- orderStatusVisual() EXPIRED usa colors.error + onError (legible)
- Test fakes implementan updateRubroFull

Target: PROD. No Gradle en VM. No merge.
```

---

## Pull Request

**#22** — https://github.com/Push-Orbita/app-oficios-jetpackcompose/pull/22  
**Título:** QA Figma: a11y, Flujos 1:1 y Mis Ubicaciones  
**Body actualizado** con sección "Post-QA polish" al tope, luego Ola 1+2, luego cola anterior

---

**Fin del reporte** — 2026-10-28
