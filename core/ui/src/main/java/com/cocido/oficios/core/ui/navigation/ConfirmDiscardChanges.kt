package com.cocido.oficios.core.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.cocido.oficios.core.designsystem.component.OficiosDialog

internal const val DISCARD_CHANGES_TITLE = "¿Descartar cambios?"
internal const val DISCARD_CHANGES_MESSAGE = "Los datos que ingresó se van a perder."
internal const val DISCARD_CHANGES_CONFIRM = "Descartar"
internal const val DISCARD_CHANGES_DISMISS = "Seguir editando"

@Composable
private fun DiscardChangesDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    OficiosDialog(
        title = DISCARD_CHANGES_TITLE,
        message = DISCARD_CHANGES_MESSAGE,
        confirmLabel = DISCARD_CHANGES_CONFIRM,
        dismissLabel = DISCARD_CHANGES_DISMISS,
        isDangerous = true,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}

/**
 * Para pantallas que ya reciben un callback explícito de "volver" (botón en el top bar).
 * Intercepta tanto el gesto de sistema como el botón, y muestra un diálogo de confirmación
 * antes de invocar [onConfirmExit] si [hasUnsavedChanges] es true.
 *
 * Devuelve el handler a usar en el botón de "volver" del top bar.
 */
@Composable
fun rememberConfirmDiscardBackHandler(
    hasUnsavedChanges: Boolean,
    onConfirmExit: () -> Unit,
    enabled: Boolean = hasUnsavedChanges,
): () -> Unit {
    var showDialog by remember { mutableStateOf(false) }

    val requestExit: () -> Unit = {
        if (hasUnsavedChanges) showDialog = true else onConfirmExit()
    }

    BackHandler(enabled = enabled) { requestExit() }

    if (showDialog) {
        DiscardChangesDialog(
            onConfirm = {
                showDialog = false
                onConfirmExit()
            },
            onDismiss = { showDialog = false },
        )
    }

    return requestExit
}

/**
 * Para pantallas sin callback explícito de "volver" (dependen del pop por default del
 * NavHost ante el gesto de sistema, sin botón propio en la pantalla). Intercepta el gesto,
 * muestra el diálogo, y si se confirma vuelve a disparar el back del sistema ya sin
 * intercepción para que el NavHost haga el pop normal.
 */
@Composable
fun ConfirmDiscardOnSystemBack(hasUnsavedChanges: Boolean) {
    var showDialog by remember { mutableStateOf(false) }
    var bypassGuard by remember { mutableStateOf(false) }
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    BackHandler(enabled = hasUnsavedChanges && !bypassGuard) { showDialog = true }

    if (showDialog) {
        DiscardChangesDialog(
            onConfirm = {
                showDialog = false
                bypassGuard = true
                backDispatcher?.onBackPressed()
            },
            onDismiss = { showDialog = false },
        )
    }
}
