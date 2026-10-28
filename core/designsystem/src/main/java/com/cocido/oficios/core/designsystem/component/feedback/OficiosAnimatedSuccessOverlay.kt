package com.cocido.oficios.core.designsystem.component.feedback

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.theme.LocalReducedMotion
import com.cocido.oficios.core.designsystem.theme.OficiosMotion
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Confirmación de éxito de una acción importante (onboarding, finalizar, calificar).
 * Acompaña la animación con el háptico [OficiosHaptics.confirm]; con reduced motion se muestra
 * directamente el estado final.
 */
@Composable
fun OficiosAnimatedSuccessOverlay(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    val reducedMotion = LocalReducedMotion.current
    val haptics = rememberOficiosHaptics()
    val iconScale = remember { Animatable(if (reducedMotion) 1f else 0.6f) }
    val textAlpha = remember { Animatable(if (reducedMotion) 1f else 0f) }
    val ringScale = remember { Animatable(1f) }
    val ringAlpha = remember { Animatable(if (reducedMotion) 0f else 0.45f) }

    LaunchedEffect(Unit) {
        haptics.confirm()
        if (reducedMotion) return@LaunchedEffect
        launch {
            iconScale.animateTo(
                targetValue = 1f,
                animationSpec = OficiosMotion.tween(OficiosMotion.DurationLong, OficiosMotion.EmphasizedDecelerate),
            )
        }
        launch {
            ringScale.animateTo(
                targetValue = 1.6f,
                animationSpec = OficiosMotion.tween(OficiosMotion.DurationLong, OficiosMotion.EmphasizedDecelerate),
            )
        }
        launch {
            ringAlpha.animateTo(
                targetValue = 0f,
                animationSpec = OficiosMotion.tween(OficiosMotion.DurationLong, OficiosMotion.EmphasizedAccelerate),
            )
        }
        launch {
            delay(OficiosMotion.DurationShort.toLong())
            textAlpha.animateTo(
                targetValue = 1f,
                animationSpec = OficiosMotion.tween(OficiosMotion.DurationMedium, OficiosMotion.EmphasizedDecelerate),
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = OficiosSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .graphicsLayer {
                            scaleX = ringScale.value
                            scaleY = ringScale.value
                            alpha = ringAlpha.value
                        }
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            color = OficiosTheme.colors.success.copy(alpha = 0.7f),
                            shape = CircleShape,
                        ),
                )
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = OficiosTheme.colors.success,
                    modifier = Modifier
                        .size(88.dp)
                        .graphicsLayer {
                            scaleX = iconScale.value
                            scaleY = iconScale.value
                        },
                )
            }
            Spacer(Modifier.height(OficiosSpacing.lg))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer { alpha = textAlpha.value },
            )
            Spacer(Modifier.height(OficiosSpacing.xs))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer { alpha = textAlpha.value },
            )
            if (action != null) {
                Spacer(Modifier.height(OficiosSpacing.lg))
                Box(modifier = Modifier.graphicsLayer { alpha = textAlpha.value }) {
                    action()
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun OficiosAnimatedSuccessOverlayPreview() {
    OficiosTheme {
        OficiosAnimatedSuccessOverlay(
            title = "¡Perfil confirmado!",
            description = "Ya puede empezar a recibir pedidos.",
        )
    }
}
