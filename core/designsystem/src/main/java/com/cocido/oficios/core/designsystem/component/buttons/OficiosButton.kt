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
import com.cocido.oficios.core.designsystem.theme.OficiosColorBackground
import com.cocido.oficios.core.designsystem.theme.OficiosColorError
import com.cocido.oficios.core.designsystem.theme.OficiosColorPrimaryBorder
import com.cocido.oficios.core.designsystem.theme.OficiosColorSuccess
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

@Composable
fun OficiosButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: OficiosButtonVariant = OficiosButtonVariant.Primary,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
) {
    val (containerColor, contentColor, border, shadowColor) = when (variant) {
        OficiosButtonVariant.Primary -> ButtonColors(
            container = MaterialTheme.colorScheme.primary,
            content = MaterialTheme.colorScheme.onPrimary,
            border = BorderStroke(1.dp, OficiosColorPrimaryBorder),
            shadow = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
        )
        OficiosButtonVariant.Secondary -> ButtonColors(
            container = MaterialTheme.colorScheme.secondary,
            content = MaterialTheme.colorScheme.onSecondary,
            border = BorderStroke(1.dp, Color(0xFFE2E2E2)),
            shadow = MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f),
        )
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
            shadow = OficiosColorError.copy(alpha = 0.15f),
        )
        OficiosButtonVariant.Success -> ButtonColors(
            container = OficiosColorSuccess,
            content = Color.White,
            border = BorderStroke(1.dp, OficiosColorSuccess.copy(alpha = 0.5f)),
            shadow = OficiosColorSuccess.copy(alpha = 0.15f),
        )
    }

    val disabledContainerColor = containerColor.copy(alpha = OficiosAlpha.disabled)
    val disabledContentColor = contentColor.copy(alpha = OficiosAlpha.disabled)

    Button(
        onClick = { if (!loading) onClick() },
        modifier = modifier
            .heightIn(min = OficiosComponentSize.buttonMinHeight)
            .then(
                if (shadowColor != Color.Transparent)
                    Modifier.shadow(elevation = 4.dp, shape = MaterialTheme.shapes.small, ambientColor = shadowColor, spotColor = shadowColor)
                else Modifier
            ),
        enabled = enabled && !loading,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = disabledContentColor,
        ),
        border = border,
        shape = MaterialTheme.shapes.small,
        contentPadding = PaddingValues(horizontal = OficiosSpacing.sm, vertical = OficiosSpacing.xs),
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
                    Icon(imageVector = leadingIcon, contentDescription = null, modifier = Modifier.size(OficiosComponentSize.iconMd))
                    Spacer(Modifier.width(OficiosSpacing.xxs))
                }
                Text(text = text, style = MaterialTheme.typography.labelLarge)
                if (trailingIcon != null) {
                    Spacer(Modifier.width(OficiosSpacing.xxs))
                    Icon(imageVector = trailingIcon, contentDescription = null, modifier = Modifier.size(OficiosComponentSize.iconMd))
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

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "Primary Button", showBackground = true, backgroundColor = 0xFF231D38)
@Composable
private fun PrimaryButtonPreview() {
    OficiosTheme {
        Box(Modifier.size(200.dp, 60.dp), contentAlignment = Alignment.Center) {
            OficiosButton(text = "Ingresar", onClick = {})
        }
    }
}

@Preview(name = "Secondary Button", showBackground = true, backgroundColor = 0xFF231D38)
@Composable
private fun SecondaryButtonPreview() {
    OficiosTheme {
        Box(Modifier.size(200.dp, 60.dp), contentAlignment = Alignment.Center) {
            OficiosButton(text = "Registrarse", onClick = {}, variant = OficiosButtonVariant.Secondary)
        }
    }
}

@Preview(name = "Loading Button", showBackground = true, backgroundColor = 0xFF231D38)
@Composable
private fun LoadingButtonPreview() {
    OficiosTheme {
        Box(Modifier.size(200.dp, 60.dp), contentAlignment = Alignment.Center) {
            OficiosButton(text = "Cargando…", onClick = {}, loading = true)
        }
    }
}

@Preview(name = "Disabled Button", showBackground = true, backgroundColor = 0xFF231D38)
@Composable
private fun DisabledButtonPreview() {
    OficiosTheme {
        Box(Modifier.size(200.dp, 60.dp), contentAlignment = Alignment.Center) {
            OficiosButton(text = "Deshabilitado", onClick = {}, enabled = false)
        }
    }
}

@Preview(name = "Ghost Button", showBackground = true, backgroundColor = 0xFF231D38)
@Composable
private fun GhostButtonPreview() {
    OficiosTheme {
        Box(Modifier.size(200.dp, 60.dp), contentAlignment = Alignment.Center) {
            OficiosButton(text = "Rechazar", onClick = {}, variant = OficiosButtonVariant.Ghost)
        }
    }
}

@Preview(name = "Danger Button", showBackground = true, backgroundColor = 0xFF231D38)
@Composable
private fun DangerButtonPreview() {
    OficiosTheme {
        Box(Modifier.size(200.dp, 60.dp), contentAlignment = Alignment.Center) {
            OficiosButton(text = "Eliminar", onClick = {}, variant = OficiosButtonVariant.Danger)
        }
    }
}
