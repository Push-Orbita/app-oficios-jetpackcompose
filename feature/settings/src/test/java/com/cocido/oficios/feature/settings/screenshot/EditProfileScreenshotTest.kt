package com.cocido.oficios.feature.settings.screenshot

import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.feature.settings.provider.ProfessionalProfileScreen
import com.cocido.oficios.feature.settings.provider.ProfessionalProfileUiState
import com.cocido.oficios.feature.settings.provider.ProfileCertificateUi
import com.github.takahirom.roborazzi.captureRoboImage
import java.io.File
import org.junit.Assume
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val PIXEL9 = "w427dp-h952dp-xxhdpi"

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = PIXEL9)
class EditProfileScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun editProfile() {
        assumeGoldenOrRecording(javaClass, "editProfile")
        composeRule.setContent {
            ScreenshotFrame {
                ProfessionalProfileScreen(
                    uiState = ProfessionalProfileUiState(
                        isLoading = false,
                        oficiosLabel = "Albañil, Electricista",
                        specializedLabel = "Albañilería general, Reparación de humedad, Revestimiento de Cerámicos y azulejos, Iluminación.",
                        gallery = emptyList(),
                        certificates = listOf(
                            ProfileCertificateUi(1, "Curso", null, validated = true),
                            ProfileCertificateUi(2, "Matrícula", null, validated = false),
                        ),
                    ),
                    onEvent = {},
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun editProfileDark() {
        assumeGoldenOrRecording(javaClass, "editProfileDark")
        composeRule.setContent {
            ScreenshotFrame(darkTheme = true) {
                ProfessionalProfileScreen(
                    uiState = ProfessionalProfileUiState(
                        isLoading = false,
                        oficiosLabel = "Albañil, Electricista",
                        specializedLabel = "Albañilería general, Reparación de humedad, Revestimiento de Cerámicos y azulejos, Iluminación.",
                        gallery = emptyList(),
                        certificates = listOf(
                            ProfileCertificateUi(1, "Curso", null, validated = true),
                            ProfileCertificateUi(2, "Matrícula", null, validated = false),
                        ),
                    ),
                    onEvent = {},
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}

@Composable
private fun ScreenshotFrame(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    OficiosTheme(darkTheme = darkTheme) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            Spacer(Modifier.fillMaxWidth().height(52.dp))
            Box(modifier = Modifier.weight(1f)) { content() }
            Spacer(Modifier.fillMaxWidth().height(24.dp))
        }
    }
}

private fun assumeGoldenOrRecording(testClass: Class<*>, methodName: String) {
    val record = System.getProperty("roborazzi.test.record") == "true"
    val className = testClass.name
    val simple = testClass.simpleName
    val pkgPath = testClass.name.replace('.', '/')
    val candidates = listOf(
        File("src/test/screenshots/$className.$methodName.png"),
        File("src/test/screenshots/$pkgPath.$methodName.png"),
        File("src/test/screenshots/$pkgPath/$methodName.png"),
        File("src/test/screenshots/com/cocido/oficios/feature/settings/screenshot/$simple.$methodName.png"),
    )
    Assume.assumeTrue(
        "Sin golden ni -Proborazzi.test.record=true; se omite para no romper testDebugUnitTest",
        record || candidates.any(File::exists),
    )
}