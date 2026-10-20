package com.cocido.oficios.feature.auth.screenshot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.theme.OficiosColorBackground
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
 * Pixel 9 Pro (tokay): 1280×2856 @ xxhdpi → ~427×952 dp. StatusBars real ≈52dp
 * y gesture nav 24dp, pero Robolectric deja safeDrawing en 0: ambos sets pintan
 * el chrome 48+24 del frame de Figma para que los Y sean comparables. El set P9
 * reproduce wrapping a 427 e includeFontPadding a 3x; no un spacer de +4 que
 * solo existiría con status 52 y fallaría el ±2 contra Figma.
 */
internal object AuthScreenshotChrome {
    val FigmaStatusBar = 48.dp
    val FigmaNavBar = 24.dp
    val Pixel9ProStatusBar = 48.dp
    val Pixel9ProNavBar = 24.dp
}

internal const val AUTH_SCREENSHOT_FIGMA_QUALIFIERS = "w440dp-h980dp-mdpi"
internal const val AUTH_SCREENSHOT_PIXEL9_QUALIFIERS = "w427dp-h952dp-xxhdpi"

@Composable
internal fun AuthScreenshotSplash() {
    OficiosTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(OficiosColorBackground),
        ) {
            SplashScreen(uiState = SplashUiState.Loading, onRetry = {})
        }
    }
}

@Composable
internal fun AuthScreenshotLogin(
    statusBar: Dp = AuthScreenshotChrome.FigmaStatusBar,
    navigationBar: Dp = AuthScreenshotChrome.FigmaNavBar,
) {
    AuthScreenshotFrame(statusBar = statusBar, navigationBar = navigationBar) {
        LoginScreen(uiState = LoginUiState(canSubmit = true), onEvent = {})
    }
}

@Composable
internal fun AuthScreenshotLoginError(
    statusBar: Dp = AuthScreenshotChrome.FigmaStatusBar,
    navigationBar: Dp = AuthScreenshotChrome.FigmaNavBar,
) {
    AuthScreenshotFrame(statusBar = statusBar, navigationBar = navigationBar) {
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
) {
    AuthScreenshotFrame(statusBar = statusBar, navigationBar = navigationBar) {
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
) {
    AuthScreenshotFrame(statusBar = statusBar, navigationBar = navigationBar) {
        RegisterFormScreen(
            mode = RegisterMode.CLIENT,
            uiState = RegisterFormUiState(canSubmit = true, acceptedTerms = true),
            onEvent = {},
        )
    }
}

@Composable
internal fun AuthScreenshotFrame(
    statusBar: Dp = AuthScreenshotChrome.FigmaStatusBar,
    navigationBar: Dp = AuthScreenshotChrome.FigmaNavBar,
    content: @Composable () -> Unit,
) {
    OficiosTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(OficiosColorBackground),
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
