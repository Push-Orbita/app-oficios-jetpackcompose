package com.cocido.oficios.feature.auth.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.cocido.oficios.core.designsystem.R
import com.cocido.oficios.core.designsystem.component.cards.OficiosCard
import com.cocido.oficios.core.designsystem.theme.OficiosAuthGlyphBodyStyle
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

@Composable
fun RegisterRoleCard(
    title: String,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconHeight: Dp,
    iconWidth: Dp? = null,
    iconRotationDegrees: Float = 0f,
    iconTopInset: Dp = OficiosComponentSize.authRoleCardIconTopInsetClient,
) {
    OficiosCard(
        onClick = onClick,
        modifier = modifier.heightIn(min = OficiosComponentSize.authRoleCardMinHeight),
        contentPadding = PaddingValues(
            start = OficiosSpacing.md,
            top = OficiosComponentSize.authRoleCardTitleTopInset,
            end = OficiosSpacing.md,
            bottom = OficiosSpacing.md,
        ),
        expandContent = true,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Text(
                text = title,
                style = OficiosAuthGlyphBodyStyle,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = iconTopInset),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier
                        .then(
                            if (iconWidth != null) {
                                Modifier.size(width = iconWidth, height = iconHeight)
                            } else {
                                Modifier.height(iconHeight)
                            },
                        )
                        .graphicsLayer {
                            rotationZ = iconRotationDegrees
                            transformOrigin = TransformOrigin(0.5f, 0f)
                        },
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun RegisterRoleCardPreview() {
    OficiosTheme {
        RegisterRoleCard(
            title = "Trabajador de oficio",
            iconRes = R.drawable.ic_casco,
            onClick = {},
            iconWidth = OficiosComponentSize.authRoleCardIconWorkerWidth,
            iconHeight = OficiosComponentSize.authRoleCardIconWorkerHeight,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
