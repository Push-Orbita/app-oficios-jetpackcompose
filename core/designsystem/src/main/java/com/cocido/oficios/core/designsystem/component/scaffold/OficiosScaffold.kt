package com.cocido.oficios.core.designsystem.component.scaffold

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.cocido.oficios.core.designsystem.theme.OficiosAlpha
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

// ---------------------------------------------------------------------------
// OficiosScaffold — wrapper centralizado sobre M3 Scaffold
//
// Aplica WindowInsets.systemBars automáticamente a través de Scaffold M3.
// Loading overlay bloquea toda interacción cuando isLoading = true.
// ---------------------------------------------------------------------------

@Composable
fun OficiosScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    isLoading: Boolean = false,
    snackbarHostState: SnackbarHostState? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    val snackbarState = snackbarHostState ?: remember { SnackbarHostState() }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = topBar,
            bottomBar = bottomBar,
            snackbarHost = { SnackbarHost(hostState = snackbarState) },
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            content = content,
        )

        // Loading overlay: semitransparente, bloquea toda interacción
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
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// OficiosScreen — atajo que combina OficiosScaffold + content sin padding manual
// ---------------------------------------------------------------------------

@Composable
fun OficiosScreen(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    isLoading: Boolean = false,
    snackbarHostState: SnackbarHostState? = null,
    content: @Composable () -> Unit,
) {
    OficiosScaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        isLoading = isLoading,
        snackbarHostState = snackbarHostState,
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
