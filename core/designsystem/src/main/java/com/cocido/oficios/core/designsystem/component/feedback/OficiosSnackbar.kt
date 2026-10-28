package com.cocido.oficios.core.designsystem.component.feedback

import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

enum class OficiosSnackbarVariant {
    Success,
    Error,
    Info,
}

data class OficiosSnackbarMessage(
    val text: String,
    val variant: OficiosSnackbarVariant = OficiosSnackbarVariant.Info,
)

class OficiosSnackbarController {
    private val _messages = MutableSharedFlow<OficiosSnackbarMessage>(extraBufferCapacity = 8)
    val messages: SharedFlow<OficiosSnackbarMessage> = _messages.asSharedFlow()

    fun show(message: String, variant: OficiosSnackbarVariant = OficiosSnackbarVariant.Info) {
        val text = message.trim()
        if (text.isEmpty()) return
        _messages.tryEmit(OficiosSnackbarMessage(text, variant))
    }

    fun showSuccess(message: String) = show(message, OficiosSnackbarVariant.Success)

    fun showError(message: String) = show(message, OficiosSnackbarVariant.Error)
}

val LocalOficiosSnackbarController = staticCompositionLocalOf { OficiosSnackbarController() }

@Composable
fun OficiosSnackbarHost(
    controller: OficiosSnackbarController,
    modifier: Modifier = Modifier,
) {
    val hostState = remember { SnackbarHostState() }
    var current by remember { mutableStateOf(OficiosSnackbarMessage("", OficiosSnackbarVariant.Info)) }

    LaunchedEffect(controller) {
        controller.messages.collect { message ->
            current = message
            hostState.currentSnackbarData?.dismiss()
            hostState.showSnackbar(message.text)
        }
    }

    val colors = snackbarColors(current.variant)
    SnackbarHost(hostState = hostState, modifier = modifier) { data ->
        Snackbar(
            containerColor = colors.first,
            contentColor = colors.second,
        ) {
            Text(text = data.visuals.message)
        }
    }
}

@Composable
private fun snackbarColors(variant: OficiosSnackbarVariant): Pair<Color, Color> {
    val scheme = androidx.compose.material3.MaterialTheme.colorScheme
    return when (variant) {
        OficiosSnackbarVariant.Success -> OficiosTheme.colors.successContainer to OficiosTheme.colors.success
        OficiosSnackbarVariant.Error -> scheme.error to MaterialTheme.colorScheme.onSurface
        OficiosSnackbarVariant.Info -> scheme.inverseSurface to scheme.inverseOnSurface
    }
}
