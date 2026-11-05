import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

/**
 * Clave de Google Maps. local.properties no se versiona, así que un `git worktree`
 * nuevo no la tiene y el APK sale con la clave vacía: todos los mapas de la app
 * caen en "No pudimos cargar el mapa". Orden de búsqueda:
 * 1. local.properties de este checkout
 * 2. local.properties del checkout principal (si este es un worktree)
 * 3. -PMAPS_API_KEY o ~/.gradle/gradle.properties
 * 4. variable de entorno MAPS_API_KEY
 */
val mapsApiKey: String = run {
    fun Properties.mapsKey(): String? = getProperty("MAPS_API_KEY")?.trim()?.takeIf { it.isNotEmpty() }
    val mainCheckoutProperties = Properties().apply {
        val dotGit = rootProject.file(".git")
        if (!dotGit.isFile) return@apply
        // .git de un worktree: "gitdir: <checkout principal>/.git/worktrees/<nombre>"
        val gitDir = dotGit.readText().trim().removePrefix("gitdir:").trim()
        val mainRoot = File(gitDir).parentFile?.parentFile?.parentFile ?: return@apply
        val file = File(mainRoot, "local.properties")
        if (file.exists()) file.inputStream().use { load(it) }
    }
    localProperties.mapsKey()
        ?: mainCheckoutProperties.mapsKey()
        ?: providers.gradleProperty("MAPS_API_KEY").orNull?.trim()?.takeIf { it.isNotEmpty() }
        ?: providers.environmentVariable("MAPS_API_KEY").orNull?.trim()?.takeIf { it.isNotEmpty() }
        ?: ""
}

val firebaseEnabled: Boolean = run {
    val googleServicesFile = file("google-services.json")
    if (!googleServicesFile.exists()) return@run false
    try {
        val parsed = groovy.json.JsonSlurper().parseText(googleServicesFile.readText())
        val projectInfo = (parsed as? Map<*, *>)?.get("project_info") as? Map<*, *>
        val projectId = projectInfo?.get("project_id")?.toString() ?: ""
        !projectId.contains("stub", ignoreCase = true) && projectId.isNotBlank()
    } catch (_: Exception) {
        false
    }
}

android {
    namespace = "com.cocido.oficios"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.cocido.oficios"
        minSdk = 27
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Read from local.properties (never committed) — see MAPS_API_KEY below.
        manifestPlaceholders["mapsApiKey"] = mapsApiKey

        // Firebase es opcional: false cuando google-services.json es stub o falta.
        buildConfigField("boolean", "FIREBASE_ENABLED", firebaseEnabled.toString())
    }

    flavorDimensions += "environment"
    productFlavors {
        create("prod") {
            dimension = "environment"
            isDefault = true
            resValue("string", "app_name", "Oficios")
        }
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            resValue("string", "app_name", "Oficios DEV")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
        }
    }
}

// Un APK sin clave de Maps nunca debe salir: falla el empaquetado (no los tests unitarios).
androidComponents {
    onVariants { variant ->
        val packageTask = "package" + variant.name.replaceFirstChar { it.uppercase() }
        val keyMissing = mapsApiKey.isBlank()
        tasks.matching { it.name == packageTask }.configureEach {
            doFirst {
                if (keyMissing) {
                    throw GradleException(
                        "MAPS_API_KEY vacío: el APK saldría sin mapas. Definilo en local.properties " +
                            "(o en el del checkout principal si usás un worktree), -PMAPS_API_KEY o la variable de entorno MAPS_API_KEY.",
                    )
                }
            }
        }
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:ui"))
    implementation(project(":core:data"))
    implementation(project(":core:location"))
    implementation(project(":core:network"))
    implementation(project(":core:datastore"))
    implementation(project(":core:database"))
    implementation(project(":domain"))
    implementation(project(":feature:home"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:auth"))
    implementation(project(":feature:onboarding"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.kotlinx.coroutines.android)
    ksp(libs.hilt.compiler)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.androidx.activity.compose)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
