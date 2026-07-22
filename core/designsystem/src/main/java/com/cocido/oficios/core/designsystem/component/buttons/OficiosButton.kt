package com.cocido.oficios.core.designsystem.component.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.theme.OficiosAlpha
import com.cocido.oficios.core.designsystem.theme.OficiosColorAction
import com.cocido.oficios.core.designsystem.theme.OficiosColorBackground
import com.cocido.oficios.core.designsystem.theme.OficiosColorError
import com.cocido.oficios.core.designsystem.theme.OficiosColorSuccess
import com.cocido.oficios.core.designsystem.theme.OficiosColorWhite
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

enum class OficiosButtonVariant {
    Primary,
    Secondary,
    Outlined,
    Ghost,
    Danger,
    Success,
}

enum class OficiosButtonSize {
    Compact,
    Prominent,
}

@Composable
fun OficiosButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: OficiosButtonVariant = OficiosButtonVariant.Primary,
    size: OficiosButtonSize = OficiosButtonSize.Compact,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
) {
    val minHeight = when (size) {
        OficiosButtonSize.Compact -> OficiosComponentSize.buttonMinHeight
        OficiosButtonSize.Prominent -> OficiosComponentSize.buttonHeight
    }

    val (containerColor, contentColor, border, shadowColor) = when (variant) {
        // Botón primario — Figma: fondo rgb(0,109,239), sombra 0px 4px 10px rgba(0,0,0,.15)
        OficiosButtonVariant.Primary -> ButtonColors(
            container = OficiosColorAction,
            content = OficiosColorWhite,
            border = BorderStroke(1.dp, Color.Transparent),
            shadow = Color.Black.copy(alpha = 0.15f),
        )
        // Botón secundario — Figma: fondo blanco, texto rgb(0,53,139)
        OficiosButtonVariant.Secondary -> ButtonColors(
            container = OficiosColorWhite,
            content = OficiosColorBackground,
            border = BorderStroke(1.dp, Color(0xFFE2E2E2)),
            shadow = Color.Black.copy(alpha = 0.15f),
        )
        // Botón outline — Figma: transparente + box-shadow inset 1px rgb(0,77,168)
        OficiosButtonVariant.Outlined -> ButtonColors(
            container = Color.Transparent,
            content = MaterialTheme.colorScheme.onSurface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shadow = Color.Transparent,
        )
        OficiosButtonVariant.Ghost -> ButtonColors(
            container = OficiosColorBackground,
            content = MaterialTheme.colorScheme.onSurface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shadow = Color.Transparent,
        )
        OficiosButtonVariant.Danger -> ButtonColors(
            container = OficiosColorError,
            content = MaterialTheme.colorScheme.onError,
            border = BorderStroke(1.dp, OficiosColorError.copy(alpha = 0.5f)),
            shadow = Color.Black.copy(alpha = 0.15f),
        )
        OficiosButtonVariant.Success -> ButtonColors(
            container = OficiosColorSuccess,
            content = Color.White,
            border = BorderStroke(1.dp, OficiosColorSuccess.copy(alpha = 0.5f)),
            shadow = Color.Black.copy(alpha = 0.15f),
        )
    }

    val isInteractive = enabled && !loading
    val (disabledContainerColor, disabledContentColor) = when (variant) {
        OficiosButtonVariant.Primary -> OficiosColorAction.copy(alpha = OficiosAlpha.disabled) to
            OficiosColorWhite.copy(alpha = OficiosAlpha.high)
        else -> containerColor.copy(alpha = OficiosAlpha.disabled) to
            contentColor.copy(alpha = OficiosAlpha.disabled)
    }
    val activeBorder = when {
        isInteractive -> border
        variant == OficiosButtonVariant.Secondary ->
            BorderStroke(1.dp, Color(0xFFE2E2E2).copy(alpha = OficiosAlpha.disabled))
        else -> border
    }

    Button(
        onClick = { if (!loading) onClick() },
        modifier = modifier
            .heightIn(min = minHeight)
            .then(
                if (shadowColor != Color.Transparent && isInteractive) {
                    Modifier.shadow(
                        elevation = 4.dp,
                        shape = MaterialTheme.shapes.small,
                        ambientColor = shadowColor,
                        spotColor = shadowColor,
                    )
                } else {
                    Modifier
                },
            ),
        enabled = isInteractive,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = disabledContentColor,
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp,
            disabledElevation = 0.dp,
        ),
        border = activeBorder,
        shape = MaterialTheme.shapes.small,
        contentPadding = PaddingValues(
            horizontal = OficiosComponentSize.buttonPaddingHorizontal,
            vertical = OficiosComponentSize.buttonPaddingVertical,
        ),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(OficiosComponentSize.iconMd),
                color = contentColor,
                strokeWidth = 2.dp,
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(OficiosComponentSize.iconMd),
                    )
                    Spacer(Modifier.width(OficiosSpacing.xxs))
                }
                Text(text = text, style = MaterialTheme.typography.labelLarge)
                if (trailingIcon != null) {
                    Spacer(Modifier.width(OficiosSpacing.xxs))
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(OficiosComponentSize.iconMd),
                    )
                }
            }
        }
    }
}

private data class ButtonColors(
    val container: Color,
    val content: Color,
    val border: BorderStroke,
    val shadow: Color,
)

@Preview(name = "Primary Button", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun PrimaryButtonPreview() {
    OficiosTheme {
        Box(Modifier.size(200.dp, 60.dp), contentAlignment = Alignment.Center) {
            OficiosButton(text = "Ingresar", onClick = {})
        }
    }
}

@Preview(name = "Secondary Prominent", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun SecondaryButtonPreview() {
    OficiosTheme {
        Box(Modifier.size(260.dp, 60.dp), contentAlignment = Alignment.Center) {
            OficiosButton(
                text = "Añadir otro horario",
                onClick = {},
                variant = OficiosButtonVariant.Secondary,
                size = OficiosButtonSize.Prominent,
            )
        }
    }
}
