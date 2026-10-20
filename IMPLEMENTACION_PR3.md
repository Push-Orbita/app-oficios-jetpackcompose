# Notas de Implementación - PR #3: Auth y Onboarding

## Decisiones de Adaptación

### 1. Pantalla de Contacto (Figma 359:789)
**Decisión:** La API de backend requiere los campos `calle` y `numero` por separado, mientras que Figma muestra un único campo "Dirección".

**Implementación:**
- Se mantuvieron los dos campos separados (`calle` y `numero`) para cumplir con la API
- Se agregaron labels claros "Calle" y "Número" para mejorar la UX
- El campo `barrio` usa un autocomplete que consulta la API de barrios
- Campos opcionales de `piso` y `departamento` según Figma

**Archivos:**
- `feature/onboarding/src/main/java/com/cocido/oficios/feature/onboarding/steps/OnboardingStepScreens.kt` (ClientContactDomicilioScreen)
- `core/data/src/main/java/com/cocido/oficios/core/data/model/request/UpdateClientContactRequest.kt`

### 2. Sistema de Toast (Ítem 2e)
**Implementación:**
- Migración completa de `LocalOficiosSnackbarController` a `LocalOficiosToastController`
- `OficiosApp.kt` provee el controller explícitamente vía `CompositionLocalProvider`
- `OficiosScaffold.kt` muestra el toast siempre (no solo con bottomBar)
- Uso de `showSuccess()` en acciones exitosas:
  - Calificación enviada (RateWorkerRoute)
  - Contraoferta enviada (CounterOfferRoute)
  - Código validado (VerifyEmailRoute)

**Archivos modificados:**
- `app/src/main/java/com/cocido/oficios/navigation/OficiosApp.kt`
- `core/designsystem/src/main/java/com/cocido/oficios/core/designsystem/component/scaffold/OficiosScaffold.kt`
- `feature/home/src/main/java/com/cocido/oficios/feature/home/orders/RateWorkerRoute.kt`
- `feature/home/src/main/java/com/cocido/oficios/feature/home/orders/CounterOfferRoute.kt`
- `feature/onboarding/src/main/java/com/cocido/oficios/feature/onboarding/verify/VerifyEmailRoute.kt`

### 3. OficiosCodeInput Parametrizable (Ítem 4)
**Implementación:**
- Parámetro `digitCount: Int = 6` (default para email)
- Uso en VerifyPhoneStepScreen con la longitud requerida por API
- Tests unitarios en `OficiosCodeInputTest.kt` (8 casos)

**Archivos:**
- `core/designsystem/src/main/java/com/cocido/oficios/core/designsystem/component/inputs/OficiosCodeInput.kt`
- `core/designsystem/src/test/java/com/cocido/oficios/core/designsystem/component/inputs/OficiosCodeInputTest.kt`

### 4. Iconos Lucide (Ítem 5)
**Implementación:**
- Agregado `ic_lucide_camera.xml` en `core/designsystem/res/drawable/`
- Registrado en `OficiosIcons.Camera`
- Migrado `CheckCircle` → `OficiosIcons.CircleCheck` en `OnboardingStepScreens.kt`
- Migrado `PhotoCamera` → `OficiosIcons.Camera` en `OnboardingStepScreens.kt`

**Archivos:**
- `core/designsystem/src/main/res/drawable/ic_lucide_camera.xml`
- `core/designsystem/src/main/java/com/cocido/oficios/core/designsystem/component/OficiosIcons.kt`
- `feature/onboarding/src/main/java/com/cocido/oficios/feature/onboarding/steps/OnboardingStepScreens.kt`

### 5. Copy en Formal 'Usted' (Ítem 2)
**Implementación:**
- Actualización completa de ~45 strings entre `feature/auth/strings.xml` y `feature/onboarding/strings.xml`
- Eliminación de voseo ("vos", "tenés", "elegí", "buscá")
- Eliminación de "tú/tu/te" informal → "usted/su/le" formal
- Ejemplos:
  - "¿Olvidaste tu contraseña?" → "¿Olvidó su contraseña?"
  - "Te enviamos un código" → "Le enviamos un código"
  - "Elegí cómo registrarte" → "Elija cómo registrarse"
  - "Ya podés iniciar sesión" → "Ya puede iniciar sesión"

**Archivos:**
- `feature/auth/src/main/res/values/strings.xml`
- `feature/onboarding/src/main/res/values/strings.xml`

## Alcance Pendiente (Ítem 6)

### A. Unified Identity Screen (Figma 526:1118)
**Pendiente:** Consolidar los pasos de DNI frente, dorso, selfie y certificado opcional en una única pantalla.

**Consideraciones:**
- Mantener las llamadas de API existentes (individual por imagen)
- Usar el ViewModel de DNI actual como base
- Agregar manejo de selfie y certificado en el mismo flujo
- UI: 4 cards verticales con indicador de "Cargado"/"Pendiente"

### B. Splash Screen 1:1 (Figma 16:2)
**Pendiente:** Verificar animación y logo contra diseño de Figma.

**Estado actual:** SplashScreen implementado en `feature/auth/splash/SplashScreen.kt`

**Validación necesaria:**
- Comparar logo y tamaño con Figma
- Verificar duración de animación (fade-in/out)

### C. Role Selection 1:1 (Figma 356:482)
**Pendiente:** Verificar cards de selección de rol contra Figma.

**Estado actual:** RegisterRoleScreen implementado en `feature/auth/register/RegisterRoleScreen.kt`

**Validación necesaria:**
- Comparar títulos, descripciones e iconos con Figma
- Verificar spacing y tamaños de cards

## Commits Principales

1. `chore: remover artefactos de diseño commiteados por error`
   - Eliminó ctx/, screens/, PLAN_COMPLETO_1a1.md, etc. (~34 MB, 244 archivos)
   - Agregó rutas a .gitignore

2. `feat(strings): actualizar TODOS los strings a formal 'usted'`
   - 26 strings en auth/strings.xml
   - 20 strings en onboarding/strings.xml

3. `feat(toast): completar migración de snackbar a toast + proveer controller explícitamente`
   - OficiosApp con CompositionLocalProvider
   - OficiosScaffold arreglado para mostrar toast sin bottomBar
   - OficiosCodeInput parametrizable + tests
   - OnboardingStepScreens con Lucide icons

4. `feat(toast): agregar showSuccess en acciones exitosas`
   - RateWorkerViewModel: RatingSubmittedSuccess
   - CounterOfferViewModel: CounterOfferSentSuccess
   - VerifyEmailRoute: código validado

## Validación Pre-Push

```bash
./gradlew assembleDebug testDebugUnitTest lintDebug
```

**Resultado:** ✅ BUILD SUCCESSFUL

- assembleDebug: OK
- testDebugUnitTest: OK (todos los tests pasan)
- lintDebug: OK (sin warnings críticos)

## Próximos Pasos Sugeridos

1. Implementar Unified Identity Screen consolidando steps de DNI/selfie/certificado
2. Validar Splash y Role Selection 1:1 contra Figma
3. Agregar más casos de showSuccess:
   - Pedido aceptado/rechazado (OrderDetailViewModel)
   - Favorito agregado/quitado (WorkerProfileViewModel)
   - Perfil guardado (SettingsViewModel)
