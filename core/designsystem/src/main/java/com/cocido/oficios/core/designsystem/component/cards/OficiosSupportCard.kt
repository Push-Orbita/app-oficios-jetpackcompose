package com.cocido.oficios.core.designsystem.component.cards

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.cocido.oficios.core.designsystem.R
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.theme.OficiosAuthGlyphBodyStyle
import com.cocido.oficios.core.designsystem.theme.OficiosColorBackground
import com.cocido.oficios.core.designsystem.theme.OficiosColorOutlineFill
import com.cocido.oficios.core.designsystem.theme.OficiosColorWhite
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosRadius
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

/**
 * Card de asistencia con pestaña "?" — Figma 25:141 / auth 356:95.
 *
 * La pestaña (33×36) asoma 18 dp por encima y 16 dp a la derecha, como en
 * Figma; el padding superior reserva ese aire para que el padre no recorte
 * ni deje el "?" encimado sobre el rincón. El hit de 48 dp usa
 * [requiredSize] centrado (mismo patrón que el ojo del password y el
 * chevron del título: no infla el visual 33×36).
 */
@Composable
fun OficiosSupportCard(
    message: String,
    contactLabel: String,
    onContactClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    minHeight: Dp = OficiosComponentSize.authLoginSupportCardMinHeight,
) {
    val badgeShape = RoundedCornerShape(OficiosRadius.sm)
    val badgeWidth = OficiosComponentSize.authSupportBadgeWidth
    val badgeHeight = OficiosComponentSize.authSupportBadgeHeight
    val overlapY = OficiosComponentSize.authSupportBadgeOffsetY
    val overlapX = OficiosComponentSize.authSupportBadgeOffsetX
    val helpDescription = stringResource(R.string.ds_support_help)
    Box(modifier = modifier.fillMaxWidth()) {
        OficiosCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = overlapY)
                .heightIn(min = minHeight),
            contentPadding = PaddingValues(
                start = OficiosSpacing.md,
                top = OficiosComponentSize.authSupportCardPaddingTop,
                end = OficiosSpacing.md,
                bottom = OficiosComponentSize.authSupportCardPaddingBottom,
            ),
            expandContent = true,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(
                        min = minHeight -
                            OficiosComponentSize.authSupportCardPaddingTop -
                            OficiosComponentSize.authSupportCardPaddingBottom,
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
            ) {
                Text(
                    text = message,
                    style = OficiosAuthGlyphBodyStyle,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f),
                )
                OficiosButton(
                    text = contactLabel,
                    onClick = onContactClick,
                    enabled = enabled,
                    variant = OficiosButtonVariant.Secondary,
                    size = OficiosButtonSize.Small,
                    figmaAuthStroke = true,
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = overlapX)
                .size(width = badgeWidth, height = badgeHeight),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(badgeShape)
                    .drawBehind { drawRect(OficiosColorBackground) }
                    .border(1.dp, OficiosColorOutlineFill, badgeShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "?",
                    style = OficiosAuthGlyphBodyStyle.copy(fontWeight = FontWeight.Bold),
                    color = OficiosColorWhite,
                )
            }
            Box(
                modifier = Modifier
                    .requiredSize(OficiosComponentSize.minTouchTarget)
                    .clickable(
                        enabled = enabled,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onContactClick,
                    )
                    .semantics {
                        role = Role.Button
                        contentDescription = helpDescription
                    },
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun OficiosSupportCardPreview() {
    OficiosTheme {
        OficiosSupportCard(
            message = "Para cualquier consulta o duda de asistencia, contáctenos a nuestra línea de atención",
            contactLabel = "Contactar",
            onContactClick = {},
            modifier = Modifier.padding(OficiosSpacing.xl),
        )
    }
}
