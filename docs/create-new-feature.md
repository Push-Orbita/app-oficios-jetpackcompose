# Crear una nueva feature

Guía paso a paso para agregar `:feature:profile` como ejemplo.

## 1. Crear el módulo Gradle

`settings.gradle.kts`:

```kotlin
include(":feature:profile")
```

`feature/profile/build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.cocido.oficios.feature.profile"
    compileSdk { version = release(36) }
    defaultConfig { minSdk = 27 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:ui"))
    implementation(project(":domain"))
    // lifecycle, compose, hilt, navigation...
}
```

## 2. Domain layer

```kotlin
// domain/model/Profile.kt
data class Profile(val name: String, val email: String)

// domain/repository/ProfileRepository.kt
interface ProfileRepository {
    suspend fun getProfile(): AppResult<Profile>
}

// domain/usecase/GetProfileUseCase.kt
class GetProfileUseCase @Inject constructor(
    private val repository: ProfileRepository,
) {
    suspend operator fun invoke() = repository.getProfile()
}
```

## 3. Data layer

```kotlin
// core/data/repository/ProfileRepositoryImpl.kt
class ProfileRepositoryImpl @Inject constructor(...) : ProfileRepository { ... }

// core/data/di/DataModule.kt
@Binds abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository
```

## 4. UI layer en el feature

Estructura recomendada:

```
feature/profile/
├── ProfileUiState.kt
├── ProfileEvent.kt
├── ProfileViewModel.kt
├── ProfileScreen.kt      // stateless
├── ProfileRoute.kt       // hiltViewModel + collectAsStateWithLifecycle
└── navigation/
    ├── ProfileDestination.kt
    └── ProfileNavigation.kt
```

## 5. Navegación

`ProfileDestination.kt`:

```kotlin
object ProfileDestination {
    const val route = "profile"
}
```

`ProfileNavigation.kt`:

```kotlin
fun NavGraphBuilder.profileScreen(onNavigateBack: () -> Unit) {
    composable(route = ProfileDestination.route) {
        ProfileRoute(onNavigateBack = onNavigateBack)
    }
}
```

Registrar en `:app`:

```kotlin
// navigation/OficiosApp.kt
profileScreen(onNavigateBack = { navController.popBackStack() })
```

Agregar dependencia en `app/build.gradle.kts`:

```kotlin
implementation(project(":feature:profile"))
```

## 6. Strings y recursos

Colocar strings en `feature/profile/src/main/res/values/strings.xml`.

## 7. Tests mínimos

- `FakeProfileRepository` en `src/test`
- `ProfileViewModelTest` con `runTest` + `StandardTestDispatcher`
- (Opcional) Compose test de `ProfileScreen`

## Checklist

- [ ] UiState inmutable
- [ ] Screen sin lógica de negocio
- [ ] ViewModel no accede a Retrofit/Room directamente
- [ ] Repository devuelve modelos de dominio (no DTOs)
- [ ] Errores vía `AppResult` / `AppError`
- [ ] Colores y spacing del Design System
- [ ] Ruta centralizada en `*Destination`
- [ ] Proyecto compila (`./gradlew assembleDebug`)
