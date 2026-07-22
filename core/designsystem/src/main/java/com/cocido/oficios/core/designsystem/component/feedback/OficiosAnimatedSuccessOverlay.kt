package com.cocido.oficios.core.designsystem.component.feedback

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import com.cocido.oficios.core.designsystem.theme.OficiosColorMutedText
import com.cocido.oficios.core.designsystem.theme.OficiosColorSuccess
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OficiosAnimatedSuccessOverlay(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    val iconScale = remember { Animatable(0.2f) }
    val textAlpha = remember { Animatable(0f) }
    val ringScale = remember { Animatable(0.6f) }
    val ringAlpha = remember { Animatable(0.45f) }

    LaunchedEffect(Unit) {
        launch {
            iconScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            )
        }
        launch {
            ringScale.animateTo(
                targetValue = 1.9f,
                animationSpec = tween(durationMillis = 900),
            )
            ringAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 350),
            )
        }
        launch {
            delay(120)
            textAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 450),
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
                            color = OficiosColorSuccess.copy(alpha = 0.7f),
                            shape = CircleShape,
                        ),
                )
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = OficiosColorSuccess,
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
                color = OficiosColorMutedText,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer { alpha = textAlpha.value },
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun OficiosAnimatedSuccessOverlayPreview() {
    OficiosTheme {
        OficiosAnimatedSuccessOverlay(
            title = "¡Perfil confirmado!",
            description = "Ya podés empezar a recibir pedidos.",
        )
    }
}
