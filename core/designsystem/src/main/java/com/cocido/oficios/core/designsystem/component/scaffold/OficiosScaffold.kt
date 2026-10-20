package com.cocido.oficios.core.designsystem.component.scaffold

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.cocido.oficios.core.designsystem.component.feedback.LocalOficiosToastController
import com.cocido.oficios.core.designsystem.component.feedback.OficiosProgress
import com.cocido.oficios.core.designsystem.component.feedback.OficiosToastBar
import com.cocido.oficios.core.designsystem.component.feedback.OficiosToastController
import com.cocido.oficios.core.designsystem.theme.OficiosAlpha
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

// ---------------------------------------------------------------------------
// OficiosScaffold — wrapper centralizado sobre M3 Scaffold
//
// Con enableEdgeToEdge() en MainActivity:
// - contentWindowInsets = safeDrawing → respeta status bar y gesture bar en contenido
// - bottomBar opcional → OficiosBottomBarSlot reserva altura de navigationBars
// - topBar opcional → aplicar OficiosTopBarSlot si el top bar es custom
// - toastController opcional → muestra OficiosToastBar encima del bottomBar
//
// Todas las pantallas deben usar este scaffold (o AuthScreenLayout / layouts que lo usen).
// No aplicar insets manualmente en features salvo casos excepcionales (IME, sheets).
// ---------------------------------------------------------------------------

@Composable
fun OficiosScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: (@Composable () -> Unit)? = null,
    isLoading: Boolean = false,
    snackbarHostState: SnackbarHostState? = null,
    toastController: OficiosToastController? = null,
    contentWindowInsets: WindowInsets = WindowInsets.safeDrawing,
    content: @Composable (PaddingValues) -> Unit,
) {
    val snackbarState = snackbarHostState ?: remember { SnackbarHostState() }
    val resolvedToastController = toastController ?: LocalOficiosToastController.current

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = topBar,
            bottomBar = bottomBar?.let { bar ->
                { OficiosBottomBarSlot(content = bar) }
            } ?: {},
            snackbarHost = { SnackbarHost(hostState = snackbarState) },
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            contentWindowInsets = contentWindowInsets,
            content = content,
        )

        // Toast bar siempre visible
        // Si hay bottomBar, va arriba de ella; si no, va abajo respetando inset de navegación
        OficiosToastBar(
            controller = resolvedToastController,
            modifier = if (bottomBar != null) {
                Modifier.align(Alignment.BottomCenter)
            } else {
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom).asPaddingValues())
            }
        )

        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = OficiosAlpha.overlay)),
                contentAlignment = Alignment.Center,
            ) {
                OficiosProgress()
            }
        }
    }
}

@Composable
fun OficiosScreen(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: (@Composable () -> Unit)? = null,
    isLoading: Boolean = false,
    snackbarHostState: SnackbarHostState? = null,
    toastController: OficiosToastController? = null,
    content: @Composable () -> Unit,
) {
    OficiosScaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        isLoading = isLoading,
        snackbarHostState = snackbarHostState,
        toastController = toastController,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            content()
        }
    }
}

@Preview(name = "Scaffold Loading", showBackground = true)
@Composable
private fun ScaffoldLoadingPreview() {
    OficiosTheme {
        OficiosScaffold(isLoading = true) {}
    }
}
