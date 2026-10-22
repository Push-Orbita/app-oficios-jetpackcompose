package com.cocido.oficios.feature.auth.screenshot

import androidx.compose.material3.MaterialTheme
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.feature.auth.login.LOGIN_INVALID_CREDENTIALS_MESSAGE
import com.cocido.oficios.feature.auth.login.LoginScreen
import com.cocido.oficios.feature.auth.login.LoginUiState
import com.cocido.oficios.feature.auth.register.RegisterFormScreen
import com.cocido.oficios.feature.auth.register.RegisterFormUiState
import com.cocido.oficios.feature.auth.register.RegisterMode
import com.cocido.oficios.feature.auth.register.RegisterRoleSelectionScreen
import com.cocido.oficios.feature.auth.splash.SplashScreen
import com.cocido.oficios.feature.auth.splash.SplashUiState
import java.io.File
import org.junit.Assume

/**
 * Chrome de las capturas. Robolectric deja `WindowInsets.safeDrawing` en 0, así que
 * el frame pinta status+nav. No uses un spacer que solo cierre a 440: el set P9
 * reproduce ancho 427 / xxhdpi (wrapping + métricas de fuente).
 *
 * Pixel 9 Pro (tokay): 1280×2856 @ xxhdpi → ~427×952 dp. StatusBars real 52dp,
 * gesture nav 24dp. El +4 vs el frame Figma (48) es adaptación de dispositivo:
 * no se compensa con spacers. Los targets se miden relativos al bottom del
 * status bar (y − statusBar).
 */
internal object AuthScreenshotChrome {
    val FigmaStatusBar = 48.dp
    val FigmaNavBar = 24.dp
    val Pixel9ProStatusBar = 52.dp
    val Pixel9ProNavBar = 24.dp
}

internal const val AUTH_SCREENSHOT_FIGMA_QUALIFIERS = "w440dp-h980dp-mdpi"
internal const val AUTH_SCREENSHOT_PIXEL9_QUALIFIERS = "w427dp-h952dp-xxhdpi"

@Composable
internal fun AuthScreenshotSplash(darkTheme: Boolean = false) {
    OficiosTheme(darkTheme = darkTheme) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            SplashScreen(uiState = SplashUiState.Loading, onRetry = {})
        }
    }
}

@Composable
internal fun AuthScreenshotLogin(
    statusBar: Dp = AuthScreenshotChrome.FigmaStatusBar,
    navigationBar: Dp = AuthScreenshotChrome.FigmaNavBar,
    darkTheme: Boolean = false,
) {
    AuthScreenshotFrame(statusBar = statusBar, navigationBar = navigationBar, darkTheme = darkTheme) {
        LoginScreen(uiState = LoginUiState(canSubmit = true), onEvent = {})
    }
}

@Composable
internal fun AuthScreenshotLoginError(
    statusBar: Dp = AuthScreenshotChrome.FigmaStatusBar,
    navigationBar: Dp = AuthScreenshotChrome.FigmaNavBar,
    darkTheme: Boolean = false,
) {
    AuthScreenshotFrame(statusBar = statusBar, navigationBar = navigationBar, darkTheme = darkTheme) {
        LoginScreen(
            uiState = LoginUiState(
                email = "correoescritoeninputselected@mail.com",
                password = "12345678",
                bannerError = LOGIN_INVALID_CREDENTIALS_MESSAGE,
                canSubmit = true,
                hasAttemptedSubmit = true,
            ),
            onEvent = {},
        )
    }
}

@Composable
internal fun AuthScreenshotRole(
    statusBar: Dp = AuthScreenshotChrome.FigmaStatusBar,
    navigationBar: Dp = AuthScreenshotChrome.FigmaNavBar,
    darkTheme: Boolean = false,
) {
    AuthScreenshotFrame(statusBar = statusBar, navigationBar = navigationBar, darkTheme = darkTheme) {
        RegisterRoleSelectionScreen(
            onSelectClient = {},
            onSelectProvider = {},
            onNavigateToLogin = {},
        )
    }
}

@Composable
internal fun AuthScreenshotRegister(
    statusBar: Dp = AuthScreenshotChrome.FigmaStatusBar,
    navigationBar: Dp = AuthScreenshotChrome.FigmaNavBar,
    darkTheme: Boolean = false,
) {
    AuthScreenshotFrame(statusBar = statusBar, navigationBar = navigationBar, darkTheme = darkTheme) {
        RegisterFormScreen(
            mode = RegisterMode.CLIENT,
            uiState = RegisterFormUiState(canSubmit = true, acceptedTerms = true),
            onEvent = {},
        )
    }
}

@Composable
internal fun AuthFontScaleOverride(
    fontScale: Float,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val configuration = Configuration(LocalConfiguration.current).apply {
        this.fontScale = fontScale
    }
    CompositionLocalProvider(
        LocalConfiguration provides configuration,
        LocalDensity provides Density(density = density.density, fontScale = fontScale),
        content = content,
    )
}

@Composable
internal fun AuthScreenshotFrame(
    statusBar: Dp = AuthScreenshotChrome.FigmaStatusBar,
    navigationBar: Dp = AuthScreenshotChrome.FigmaNavBar,
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    OficiosTheme(darkTheme = darkTheme) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            Spacer(Modifier.fillMaxWidth().height(statusBar))
            Box(modifier = Modifier.weight(1f)) {
                content()
            }
            Spacer(Modifier.fillMaxWidth().height(navigationBar))
        }
    }
}

internal fun assumeGoldenOrRecording(testClass: Class<*>, methodName: String) {
    val record = System.getProperty("roborazzi.test.record") == "true"
    val className = testClass.name
    val simple = testClass.simpleName
    val pkgPath = testClass.name.replace('.', '/')
    val candidates = listOf(
        File("src/test/screenshots/$className.$methodName.png"),
        File("src/test/screenshots/$pkgPath.$methodName.png"),
        File("src/test/screenshots/$pkgPath/$methodName.png"),
        File("src/test/screenshots/com/cocido/oficios/feature/auth/screenshot/$simple.$methodName.png"),
        File("src/screenshots/$className.$methodName.png"),
    )
    Assume.assumeTrue(
        "Sin golden ni -Proborazzi.test.record=true; se omite para no romper testDebugUnitTest",
        record || candidates.any(File::exists),
    )
}
