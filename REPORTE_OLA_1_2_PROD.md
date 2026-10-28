# Reporte Ola 1+2 — Target PROD — 2026-10-28

**Branch:** `cursor/qa-fixes-figma-ee07`  
**HEAD:** `9f4e66d8`  
**PR:** #22 (https://github.com/Push-Orbita/app-oficios-jetpackcompose/pull/22)  
**Target:** PROD exclusivo (`https://apioficios.pushsoftware.com.ar/`)

## Resumen ejecutivo

Implementadas **Ola 1** (edición atómica de servicio con PATCH) y **Ola 2** (booking EXPIRADA) según plan de incorporación PROD. No se corrió Gradle/tests en VM (Andrea QA en lanot). No merge. Backend `api-oficios` sin tocar.

---

## Ola 1 — Edición atómica del servicio con PATCH

### ✅ Completado

| Item | Estado | Detalle |
|------|--------|---------|
| Endpoint Retrofit | ✅ | `PATCH /trabajador-oficio-rubro/{id}/full` agregado a `ProviderProfileApi` |
| Remote data source | ✅ | `updateRubroFull(id, request)` en `ProviderProfileRemoteDataSource` |
| Repositorio | ✅ | `updateExistingProviderService()` ahora llama `updateRubroFull` (PATCH) en lugar de DELETE+POST |
| DTO response | ✅ | `TrabajadorOficioRubroFullDto.domicilioLocal?: DomicilioEnrichedDto` agregado |
| Mapper | ✅ | `toManagedService()` mapea `domicilioLocal` a `ManagedProviderService.localDomicilio` |
| Domain | ✅ | `ManagedProviderService.localDomicilio?: ProviderLocalDomicilioRequest` |
| Precarga | ✅ | `toRubroFullRequest()` usa `this.localDomicilio` como default si existe |
| Rubro principal UI | ✅ | `AddServiceViewModel` línea 199: `if (!isEditing)` bloquea cambio de rubro en edición |

### Archivos modificados (Ola 1)

```
core/network/src/main/java/com/cocido/oficios/core/network/api/profile/ProviderProfileApi.kt
core/network/src/main/java/com/cocido/oficios/core/network/api/profile/dto/ProviderProfileResponseDtos.kt
core/data/src/main/java/com/cocido/oficios/core/data/remote/ProviderProfileRemoteDataSource.kt
core/data/src/main/java/com/cocido/oficios/core/data/mapper/AccountMapper.kt
core/data/src/main/java/com/cocido/oficios/core/data/repository/AccountRepositoryImpl.kt
domain/src/main/java/com/cocido/oficios/domain/model/AccountProfile.kt
```

### Cómo funciona ahora

**Antes (DELETE+POST inseguro):**
1. Cliente edita especialidades/horarios/turnos de un servicio
2. Repo llama `DELETE /trabajador-oficio-rubro/{id}`
3. Repo llama `POST /trabajador-oficio-rubro/full` con nuevo cuerpo
4. Si POST falla → servicio perdido (id viejo borrado, nuevo no creado)

**Ahora (PATCH atómico):**
1. Cliente edita especialidades/horarios/turnos de un servicio
2. Repo llama `PATCH /trabajador-oficio-rubro/{id}/full` con cuerpo completo
3. Backend actualiza en transacción, conserva el `id`
4. Si falla → servicio original intacto

**domicilioLocal:**
- Backend PROD devuelve `domicilioLocal` (nullable) en `GET /trabajador-oficio/me` → `TrabajadorOficioRubroFullDto`
- Mapper lo convierte a `ProviderLocalDomicilioRequest` y lo almacena en `ManagedProviderService`
- Al editar, se precarga automáticamente en el request de PATCH

**Rubro principal no cambia rubroId:**
- `AddServiceViewModel` línea 199: cuando `isEditing=true`, los eventos `RubroSelected` y `SubRubroToggled` se ignoran
- El rubro NO se puede cambiar en edición (ni principal ni secundario) — más restrictivo que el requisito

---

## Ola 2 — Booking EXPIRADA

### ✅ Completado

| Item | Estado | Detalle |
|------|--------|---------|
| Enum dominio | ✅ | `BookingStatus.EXPIRED` agregado |
| Mapper | ✅ | `toBookingStatus()` mapea `"EXPIRADA"` → `EXPIRED` (línea 96) |
| Historial | ✅ | `isHistoryOrder()` trata `EXPIRED` como terminal (junto con `FINISHED`/`CANCELLED`) |
| Timeline | ✅ | `orderTimeline()` retorna lista vacía para `EXPIRED` (sin pasos de progreso) |
| Visual | ✅ | `orderStatusVisual()` renderiza `EXPIRED` con `errorContainer` (rojo) y `Cancel` icon |
| Título | ✅ | `orderScreenTitleRes()` devuelve "Cancelado" para `EXPIRED` |
| Acciones | ✅ | Bookings expirados sin botón cancelar (defensivo: línea 504 `OrderDetailScreen`) |
| Tracking | ✅ | `withArrivedTracking()` no cambia estado si `bookingStatus == EXPIRED` |
| Tests | ✅ | `OrderStatusMappingTest` con caso EXPIRADA en historial |

### Archivos modificados (Ola 2)

```
domain/src/main/java/com/cocido/oficios/domain/model/ServiceOrder.kt
core/data/src/main/java/com/cocido/oficios/core/data/mapper/OrderMapper.kt
core/data/src/test/java/com/cocido/oficios/core/data/mapper/OrderStatusMappingTest.kt
feature/home/src/main/java/com/cocido/oficios/feature/home/orders/OrderStatusPresentation.kt
feature/home/src/main/java/com/cocido/oficios/feature/home/orders/OrderDetailScreen.kt
```

### Comportamiento en UI

| Estado | Dónde aparece | Color | Acciones |
|--------|---------------|-------|----------|
| `EXPIRADA` | Mis pedidos → Historial | Rojo (`errorContainer`) | Ninguna |
| `EXPIRADA` | Detalle del pedido | Rojo + ícono Cancel | Sin botón cancelar |
| `EXPIRADA` | Timeline | Vacío (sin pasos) | — |

**Antes:** Bookings con estado `"EXPIRADA"` caían a `PENDING` (else branch) y aparecían como "Pendiente" amarillo en Activos.  
**Ahora:** Booking `EXPIRADA` se mapea a `BookingStatus.EXPIRED`, aparece en Historial con chip rojo "Expirado", sin acciones disponibles.

---

## Verificación pendiente (Andrea QA en lanot)

### Ola 1
1. **Editar servicio QA en prod:**
   - Login con cuenta QA trabajador
   - Editar especialidades/horarios/turnos de un servicio existente
   - Verificar: mismo `id` del rubro conservado después de guardar
   - Verificar: `GET /trabajador-oficio/me` devuelve cambios + `domicilioLocal` si existe
   - Verificar: no se puede cambiar el rubro (ni principal ni secundario) en edición

2. **domicilioLocal:**
   - Si el servicio editado tiene domicilioLocal en PROD, debe precargarse en campos de dirección

### Ola 2
1. **Booking expirado:**
   - Login con cuenta cliente/trabajador QA
   - Mis pedidos → Historial
   - Verificar: pedidos con estado `EXPIRADA` aparecen con chip rojo "Expirado"
   - Verificar: detalle del pedido expirado sin botones de acción (ni cancelar)
   - Verificar: pedidos expirados NO aparecen en Activos

---

## Contrato PROD (OpenAPI)

- `PATCH /trabajador-oficio-rubro/{id}/full` existe en PROD (401 AU02 sin auth) y 404 en DEV
- Body del PATCH = mismo que `POST /full` (`CreateFullAltaRubrosSubrubrosRequestDto`)
- `TrabajadorOficioRubroFullDTO.domicilioLocal?: DomicilioDTO` en PROD
- Booking enum en PROD: `PENDIENTE, CONFIRMADA, EN_CAMINO, EN_CURSO, FINALIZADA, CANCELADA, EXPIRADA`

---

## Fuera de scope este PR

- Ola 0 (badge entorno DEV/PROD)
- Ola 3 (disponibilidad UI + bloqueos)
- Ola 4 (smoke QA mutando datos prod)
- Gaps backend (PATCH sesion, plazo 1–24, F7–F9)
- Redesign fuera de Figma

---

## Notas técnicas

- **No se corrió Gradle build ni tests en el VM** (política de QA en máquina lanot)
- **No merge** hasta QA PASS
- **Backend `api-oficios` sin tocar** (todos los cambios son cliente Android)
- **Tests unitarios actualizados:** `OrderStatusMappingTest` con caso EXPIRADA
- **Tests de integración/repo:** pendientes (no se corrieron en VM)

---

## Commit

```
9f4e66d8 — Ola 1+2: PATCH full endpoint + BookingStatus.EXPIRED

Target: solo PROD (https://apioficios.pushsoftware.com.ar/)
```

---

## Pull Request

**#22** — https://github.com/Push-Orbita/app-oficios-jetpackcompose/pull/22  
**Título:** QA Figma: a11y, Flujos 1:1 y Mis Ubicaciones  
**Body actualizado** con sección "Ola 1+2: PATCH full + EXPIRADA" al tope

---

**Fin del reporte** — 2026-10-28
