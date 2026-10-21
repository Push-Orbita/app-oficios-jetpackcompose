package com.cocido.oficios.core.designsystem.component.buttons

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.component.interaction.OficiosTouchTarget
import com.cocido.oficios.core.designsystem.component.feedback.OficiosProgress
import com.cocido.oficios.core.designsystem.theme.OficiosAlpha
import com.cocido.oficios.core.designsystem.theme.OficiosColorAction
import com.cocido.oficios.core.designsystem.theme.OficiosColorAlertText
import com.cocido.oficios.core.designsystem.theme.OficiosColorBackground
import com.cocido.oficios.core.designsystem.theme.OficiosColorError
import com.cocido.oficios.core.designsystem.theme.OficiosColorSuccess
import com.cocido.oficios.core.designsystem.theme.OficiosColorWhite
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosMotion
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.core.designsystem.theme.oficiosTween

enum class OficiosButtonVariant {
    Primary,
    Secondary,
    Outlined,
    OutlinedDanger,
    Ghost,
    Danger,
    Success,
}

enum class OficiosButtonSize {
    Small,
    Medium,
    Large,
}

@Composable
fun OficiosButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: OficiosButtonVariant = OficiosButtonVariant.Primary,
    size: OficiosButtonSize = OficiosButtonSize.Small,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    /**
     * Stroke/sombra del frame de auth (356:49 / 356:422). Apagado por defecto
     * para no cambiar Home ni el resto de pantallas fuera de este PR.
     */
    figmaAuthStroke: Boolean = false,
) {
    val minHeight = when (size) {
        OficiosButtonSize.Small -> OficiosComponentSize.buttonHeightSmall
        OficiosButtonSize.Medium -> OficiosComponentSize.buttonHeightMedium
        OficiosButtonSize.Large -> OficiosComponentSize.buttonHeightLarge
    }

    val (containerColor, contentColor, border, shadowColor) = when (variant) {
        // Botón primario — Figma home: fondo rgb(0,109,239), sombra 0px 4px 10px rgba(0,0,0,.15)
        OficiosButtonVariant.Primary -> ButtonColors(
            container = OficiosColorAction,
            content = OficiosColorWhite,
            border = if (figmaAuthStroke) {
                BorderStroke(1.dp, OficiosColorAlertText.copy(alpha = 0.5f))
            } else {
                BorderStroke(1.dp, Color.Transparent)
            },
            shadow = if (figmaAuthStroke) {
                Color(0xFFA32F56).copy(alpha = 0.15f)
            } else {
                Color.Black.copy(alpha = 0.15f)
            },
        )
        // Botón secundario — Figma: fondo blanco, borde #E2E2E2
        OficiosButtonVariant.Secondary -> ButtonColors(
            container = OficiosColorWhite,
            content = OficiosColorBackground,
            border = BorderStroke(1.dp, Color(0xFFE2E2E2)),
            shadow = if (figmaAuthStroke) {
                Color.White.copy(alpha = 0.1f)
            } else {
                Color.Black.copy(alpha = 0.15f)
            },
        )
        // Botón outline — Figma: transparente + box-shadow inset 1px rgb(0,77,168)
        OficiosButtonVariant.Outlined -> ButtonColors(
            container = Color.Transparent,
            content = MaterialTheme.colorScheme.onSurface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shadow = Color.Transparent,
        )
        // Cancelar: borde y texto en el rosa de alerta. El fill de error (#BA6380) sobre
        // el azul de fondo no llega a AA (2.75); el texto de alerta sí (4.64).
        OficiosButtonVariant.OutlinedDanger -> ButtonColors(
            container = Color.Transparent,
            content = OficiosColorAlertText,
            border = BorderStroke(1.dp, OficiosColorAlertText),
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

    val visualHeight = if (LocalDensity.current.fontScale <= 1.15f) {
        Modifier.height(minHeight)
    } else {
        Modifier.heightIn(min = minHeight)
    }
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed && isInteractive) PressedScale else 1f,
        animationSpec = oficiosTween(OficiosMotion.DurationShort),
        label = "OficiosButtonPressScale",
    )
    val shape = MaterialTheme.shapes.small
    val paintedColor = if (isInteractive) containerColor else disabledContainerColor
    val paintedContent = if (isInteractive) contentColor else disabledContentColor
    OficiosTouchTarget(
        onClick = { if (!loading) onClick() },
        modifier = modifier,
        enabled = isInteractive,
        accessibilityText = text,
        interactionSource = interactionSource,
        indication = null,
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = pressScale
                    scaleY = pressScale
                }
                .then(visualHeight)
                .then(
                    if (shadowColor != Color.Transparent && isInteractive) {
                        Modifier.shadow(
                            elevation = 4.dp,
                            shape = shape,
                            ambientColor = shadowColor,
                            spotColor = shadowColor,
                        )
                    } else {
                        Modifier
                    },
                )
                .clip(shape)
                .background(paintedColor)
                .border(activeBorder, shape)
                .padding(
                    horizontal = OficiosComponentSize.buttonPaddingHorizontal,
                    vertical = OficiosComponentSize.buttonPaddingVertical,
                ),
            contentAlignment = Alignment.Center,
        ) {
            CompositionLocalProvider(LocalContentColor provides paintedContent) {
                val labelFade = oficiosTween<Float>(OficiosMotion.DurationMedium)
                AnimatedContent(
                    targetState = loading,
                    transitionSpec = {
                        (fadeIn(labelFade) togetherWith fadeOut(labelFade)).using(null)
                    },
                    contentAlignment = Alignment.Center,
                    label = "OficiosButtonContent",
                ) { isLoading ->
                    if (isLoading) {
                        Box(contentAlignment = Alignment.Center) {
                            OficiosButtonLabel(
                                text = text,
                                leadingIcon = leadingIcon,
                                trailingIcon = trailingIcon,
                                modifier = Modifier.alpha(0f),
                            )
                            OficiosProgress(
                                modifier = Modifier.size(OficiosComponentSize.iconMd),
                                color = paintedContent,
                            )
                        }
                    } else {
                        OficiosButtonLabel(
                            text = text,
                            leadingIcon = leadingIcon,
                            trailingIcon = trailingIcon,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OficiosButtonLabel(
    text: String,
    leadingIcon: ImageVector?,
    trailingIcon: ImageVector?,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(OficiosComponentSize.iconMd),
            )
            Spacer(Modifier.width(OficiosSpacing.xxs))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
        )
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

private const val PressedScale = 0.97f

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

@Preview(name = "Primary Loading", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun PrimaryLoadingButtonPreview() {
    OficiosTheme {
        Box(Modifier.size(200.dp, 60.dp), contentAlignment = Alignment.Center) {
            OficiosButton(text = "Ingresar", onClick = {}, loading = true)
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
                size = OficiosButtonSize.Large,
            )
        }
    }
}

@Preview(name = "Danger Button", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun DangerButtonPreview() {
    OficiosTheme {
        Box(Modifier.size(220.dp, 60.dp), contentAlignment = Alignment.Center) {
            OficiosButton(
                text = "Cancelar pedido",
                onClick = {},
                variant = OficiosButtonVariant.Danger,
            )
        }
    }
}

@Preview(name = "Outlined Danger Button", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun OutlinedDangerButtonPreview() {
    OficiosTheme {
        Box(Modifier.size(180.dp, 60.dp), contentAlignment = Alignment.Center) {
            OficiosButton(
                text = "Rechazar",
                onClick = {},
                variant = OficiosButtonVariant.OutlinedDanger,
            )
        }
    }
}
