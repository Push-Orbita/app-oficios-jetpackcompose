package com.cocido.oficios.core.designsystem.component.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.theme.OficiosAuthGlyphBodyStyle
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

/**
 * Fila "texto + acción secundaria" que se usa al pie de los formularios de auth
 * (ej. «¿Ya tenés cuenta?» + Iniciar sesión).
 *
 * Con fuente estándar se muestra en una sola fila con el texto a la izquierda y
 * el botón a la derecha. A partir de fontScale grande (>= 1.3) degrada a columna
 * para que ni el texto se trunque ni el botón quede despegado del borde.
 */
@Composable
fun OficiosPromptActionRow(
    prompt: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    actionVariant: OficiosButtonVariant = OficiosButtonVariant.Secondary,
    figmaAuthStroke: Boolean = false,
) {
    val fontScale = LocalConfiguration.current.fontScale
    val stackVertically = fontScale >= 1.3f

    if (stackVertically) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
        ) {
            Text(
                text = prompt,
                style = OficiosAuthGlyphBodyStyle,
                color = MaterialTheme.colorScheme.onBackground,
            )
            OficiosButton(
                text = actionLabel,
                onClick = onAction,
                enabled = enabled,
                variant = actionVariant,
                modifier = Modifier.fillMaxWidth(),
                figmaAuthStroke = figmaAuthStroke,
            )
        }
    } else {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = prompt,
                style = OficiosAuthGlyphBodyStyle,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
            OficiosButton(
                text = actionLabel,
                onClick = onAction,
                enabled = enabled,
                variant = actionVariant,
                figmaAuthStroke = figmaAuthStroke,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun OficiosPromptActionRowPreview() {
    OficiosTheme {
        OficiosPromptActionRow(
            prompt = "¿Todavía no tenés cuenta?",
            actionLabel = "Registrate",
            onAction = {},
        )
    }
}
