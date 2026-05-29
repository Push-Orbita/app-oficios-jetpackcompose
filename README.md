# Oficios

Base profesional reutilizable para proyectos Android nativos con **Kotlin**, **Jetpack Compose**, **Material 3** y arquitectura modular por capas.

No es una app de negocio: es un **starter** listo para crecer con features reales, manteniendo separación de responsabilidades, convenciones claras y código preparado para producción.

## Objetivo

Proveer un punto de partida sólido que incluya:

- Arquitectura por capas (UI → Domain → Data)
- Modularización progresiva
- Design System propio
- Navegación escalable
- Inyección de dependencias con Hilt
- Networking, persistencia y manejo de errores base
- Testing de referencia
- Demo mínima funcional (Home + Settings)

## Stack

| Área | Tecnología |
|------|------------|
| Lenguaje | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Navegación | Navigation Compose |
| DI | Hilt |
| Async | Coroutines + Flow / StateFlow |
| Networking | Retrofit + OkHttp + Kotlinx Serialization |
| Preferencias | DataStore |
| Base de datos | Room |
| Build | Gradle Kotlin DSL + Version Catalog |
| Tests | JUnit, Coroutines Test, Compose UI Test |

## Arquitectura

```
┌─────────────────────────────────────────┐
│  :app + :feature:*                      │
│  Compose UI, ViewModels, UiState        │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│  :domain                                │
│  Models, Repository interfaces, UseCases│
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│  :core:data                             │
│  Repository impl, mappers, data sources │
└───────┬─────────┬─────────┬─────────────┘
        │         │         │
   :network  :database  :datastore
```

### Flujo de datos

1. La **UI** envía eventos al **ViewModel**.
2. El **ViewModel** ejecuta **UseCases**.
3. Los **UseCases** delegan en **Repository interfaces** (`:domain`).
4. **Repository implementations** (`:core:data`) coordinan fuentes remotas/locales.
5. Los **DTOs** se mapean a **modelos de dominio** antes de llegar a la UI.
6. El resultado se expone como `AppResult<T>` o `StateFlow<UiState>`.

## Estructura de módulos

```
:app                      → Entry point, navegación raíz, Application Hilt
:core:common              → AppResult, AppError, dispatchers, logger
:core:model               → Constantes compartidas
:core:designsystem        → Tema, tokens, componentes visuales base
:core:ui                  → Componentes UI reutilizables (no tokens)
:core:network             → Retrofit, OkHttp, DTOs, NetworkModule
:core:database            → Room, entidades, DAOs
:core:datastore           → DataStore, preferencias de usuario
:core:data                → Repositorios, mappers, data sources
:domain                   → Contratos, use cases, modelos de dominio
:feature:home             → Demo Home (loading/success/error)
:feature:settings         → Demo Settings (tema claro/oscuro/sistema)
```

### Reglas de dependencia

- `:feature:*` → `:domain`, `:core:designsystem`, `:core:ui`
- `:core:data` → `:domain`, `:core:network`, `:core:database`, `:core:datastore`
- `:domain` → `:core:common` (sin Android Framework)
- `:app` conoce features y ensambla navegación
- **Sin dependencias circulares**

## Demo incluida

| Pantalla | Qué valida |
|----------|------------|
| **Home** | UiState (Loading/Success/Error), Design System, navegación, UseCase + Repository + API fake |
| **Settings** | DataStore, cambio de tema, UiState reactivo |

## Cómo correr el proyecto

**Requisitos:** Android Studio reciente, JDK 17+ (Android Studio JBR recomendado).

```bash
# Windows (usar JBR de Android Studio si el PATH apunta a Java 8)
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat assembleDebug
```

Abrir en Android Studio y ejecutar en emulador/dispositivo (minSdk 27).

## Cómo correr tests

```bash
# Unit tests
.\gradlew.bat test

# Instrumented tests (Compose UI)
.\gradlew.bat connectedDebugAndroidTest
```

### Tests de referencia incluidos

- `HomeViewModelTest` — ViewModel + fake repository
- `HomeMapperTest` — DTO → Domain
- `AppButtonTest` — Compose UI test
- `AppRoutesTest` — rutas estables

## Crear una nueva feature

Ver guía detallada: [docs/create-new-feature.md](docs/create-new-feature.md)

Resumen:

1. Crear módulo `:feature:nombre` (android library + compose + hilt).
2. Definir contrato en `:domain` (model + repository + use cases).
3. Implementar repositorio en `:core:data`.
4. Crear `UiState`, `Event`, `ViewModel`, `Screen`, `Route`.
5. Exponer `NavGraphBuilder.nombreScreen()` en el feature.
6. Registrar ruta en `AppNavHost` (`:app`).
7. Agregar `include(":feature:nombre")` en `settings.gradle.kts`.

## Agregar un endpoint

1. Definir DTO en `:core:network`.
2. Agregar método en el `ApiService`.
3. Crear/actualizar data source remoto en `:core:data`.
4. Mapear DTO → modelo de dominio.
5. Exponer vía repository + use case.

## Design System

Ver [docs/design-system.md](docs/design-system.md).

Usar siempre:

- `AppTheme` para theming
- Tokens: `AppSpacing`, `AppDimens`, `AppElevation`
- Componentes: `AppButton`, `AppScaffold`, `AppErrorState`, etc.

**No hardcodear colores ni tamaños en pantallas.**

## Estado UI

Patrón estándar por pantalla:

```kotlin
// UiState inmutable
sealed interface HomeUiState { ... }

// Eventos de usuario
sealed interface HomeEvent { ... }

// ViewModel
@HiltViewModel
class HomeViewModel @Inject constructor(...) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(...)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
}

// Route conecta VM ↔ Screen
@Composable
fun HomeRoute(viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(uiState = uiState, onEvent = viewModel::onEvent)
}
```

## Convenciones

- **Paquetes:** `com.cocido.oficios.[capa].[feature]`
- **Pantallas:** `*Screen` (stateless) + `*Route` (con ViewModel)
- **ViewModels:** `@HiltViewModel` + constructor injection
- **Errores:** `AppError` / `AppResult` — nunca stacktraces al usuario
- **Strings UI:** `strings.xml` por módulo feature
- **Rutas:** objeto `*Destination` por feature + `AppRoutes` central en `:app`

## Decisiones técnicas

| Decisión | Motivo |
|----------|--------|
| Rutas con objetos `*Destination` | Simple, sin overhead de type-safe navigation serializable |
| `:domain` como módulo JVM puro | Lógica de negocio testeable sin Android |
| Room con entidad mínima `CachedItem` | Base preparada sin inflar el proyecto |
| API demo JSONPlaceholder | Demuestra Retrofit real con fallback a caché local |
| Java 17 | Estándar moderno compatible con AGP 8.x |
| Sin dynamic color | Design System controlado y portable entre apps |

## Qué NO se incluyó (y por qué)

| Excluido | Razón |
|----------|-------|
| Type-safe Navigation (Kotlin Serialization routes) | Complejidad extra no justificada en starter |
| Convention plugins (`build-logic`) | Proyecto aún pequeño; se documenta escalabilidad |
| Firebase / Analytics / Crashlytics | Dependen del producto final |
| CI/CD | Configuración específica por equipo |
| Módulo `:core:testing` | Tests de ejemplo suficientes por ahora |
| Moshi | Kotlinx Serialization ya integrado con Retrofit |

## Documentación adicional

- [Arquitectura](docs/architecture.md)
- [Design System](docs/design-system.md)
- [Crear nueva feature](docs/create-new-feature.md)

## Licencia

Plantilla interna — adaptar según necesidad del proyecto.
