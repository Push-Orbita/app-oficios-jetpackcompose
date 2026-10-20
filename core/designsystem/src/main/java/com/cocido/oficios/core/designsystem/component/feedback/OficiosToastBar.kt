package com.cocido.oficios.core.designsystem.component.feedback

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.theme.OficiosColorBackground
import com.cocido.oficios.core.designsystem.theme.OficiosColorOutlineFill
import com.cocido.oficios.core.designsystem.theme.OficiosColorSuccess
import com.cocido.oficios.core.designsystem.theme.OficiosColorError
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Toast/Banner component según Figma 475:65
 * 
 * Banda fija arriba de la bottom bar con border-top, p16, icon-box 40 + texto + `x`.
 * Variantes: success (circle-check), error (circle-alert), info
 * 
 * Uso en app:
 * ```
 * val toastController = remember { OficiosToastController() }
 * 
 * // En el scaffold:
 * OficiosToastBar(
 *     controller = toastController,
 *     modifier = Modifier.align(Alignment.BottomCenter)
 * )
 * 
 * // Para mostrar:
 * toastController.showSuccess("Solicitud enviada")
 * ```
 */
enum class OficiosToastVariant {
    Success,
    Error,
    Info,
}

data class OficiosToastMessage(
    val text: String,
    val variant: OficiosToastVariant = OficiosToastVariant.Info,
    val durationMs: Long = 3000L,
)

class OficiosToastController {
    private val _messages = MutableSharedFlow<OficiosToastMessage>(extraBufferCapacity = 8)
    val messages: SharedFlow<OficiosToastMessage> = _messages.asSharedFlow()

    fun show(message: String, variant: OficiosToastVariant = OficiosToastVariant.Info, durationMs: Long = 3000L) {
        val text = message.trim()
        if (text.isEmpty()) return
        _messages.tryEmit(OficiosToastMessage(text, variant, durationMs))
    }

    fun showSuccess(message: String, durationMs: Long = 3000L) = 
        show(message, OficiosToastVariant.Success, durationMs)

    fun showError(message: String, durationMs: Long = 4000L) = 
        show(message, OficiosToastVariant.Error, durationMs)
}

@Composable
fun OficiosToastBar(
    controller: OficiosToastController,
    modifier: Modifier = Modifier,
) {
    var currentMessage by remember { mutableStateOf<OficiosToastMessage?>(null) }

    LaunchedEffect(controller) {
        controller.messages.collect { message ->
            currentMessage = message
            delay(message.durationMs)
            currentMessage = null
        }
    }

    AnimatedVisibility(
        visible = currentMessage != null,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier,
    ) {
        currentMessage?.let { msg ->
            OficiosToastContent(
                message = msg.text,
                variant = msg.variant,
                onDismiss = { currentMessage = null }
            )
        }
    }
}

@Composable
private fun OficiosToastContent(
    message: String,
    variant: OficiosToastVariant,
    onDismiss: () -> Unit,
) {
    val icon = when (variant) {
        OficiosToastVariant.Success -> OficiosIcons.CircleCheck
        OficiosToastVariant.Error -> OficiosIcons.CircleAlert
        OficiosToastVariant.Info -> OficiosIcons.CircleQuestionMark
    }

    val iconBoxColor = when (variant) {
        OficiosToastVariant.Success -> OficiosColorSuccess
        OficiosToastVariant.Error -> OficiosColorError
        OficiosToastVariant.Info -> OficiosColorOutlineFill
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = OficiosColorOutlineFill,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .background(
                color = OficiosColorBackground,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .padding(OficiosSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Icon box 40 (24 + p8)
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(iconBoxColor, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(icon),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color.White,
            )
        }

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )

        Icon(
            imageVector = ImageVector.vectorResource(OficiosIcons.X),
            contentDescription = "Cerrar",
            modifier = Modifier
                .size(24.dp)
                .clickable(onClick = onDismiss),
            tint = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Preview(name = "Toast Success", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun PreviewToastSuccess() {
    OficiosTheme {
        OficiosToastContent(
            message = "Solicitud enviada",
            variant = OficiosToastVariant.Success,
            onDismiss = {}
        )
    }
}

@Preview(name = "Toast Error", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun PreviewToastError() {
    OficiosTheme {
        OficiosToastContent(
            message = "Los datos de sesión no coinciden",
            variant = OficiosToastVariant.Error,
            onDismiss = {}
        )
    }
}

@Preview(name = "Toast Info", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun PreviewToastInfo() {
    OficiosTheme {
        OficiosToastContent(
            message = "Complete los datos para ingresar",
            variant = OficiosToastVariant.Info,
            onDismiss = {}
        )
    }
}
