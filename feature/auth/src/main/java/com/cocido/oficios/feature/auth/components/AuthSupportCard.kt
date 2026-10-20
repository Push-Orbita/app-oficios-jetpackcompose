package com.cocido.oficios.feature.auth.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.cards.OficiosCard
import com.cocido.oficios.core.designsystem.theme.OficiosAuthGlyphBodyStyle
import com.cocido.oficios.core.designsystem.theme.OficiosColorBackground
import com.cocido.oficios.core.designsystem.theme.OficiosColorOutlineFill
import com.cocido.oficios.core.designsystem.theme.OficiosColorWhite
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosRadius
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.core.ui.intent.isSupportPhoneConfigured
import com.cocido.oficios.feature.auth.R

@Composable
fun AuthSupportCard(
    onContactClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    minHeight: Dp = OficiosComponentSize.authLoginSupportCardMinHeight,
    message: String = stringResource(R.string.auth_support_message),
) {
    val supportAvailable = isSupportPhoneConfigured()
    val resolvedMessage = if (supportAvailable) message else stringResource(R.string.auth_support_unavailable)
    val badgeShape = RoundedCornerShape(OficiosRadius.sm)
    Box(modifier = modifier.fillMaxWidth()) {
        OficiosCard(
            modifier = Modifier
                .fillMaxWidth()
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
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
            ) {
                Text(
                    text = resolvedMessage,
                    style = OficiosAuthGlyphBodyStyle,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f),
                )
                OficiosButton(
                    text = stringResource(R.string.auth_support_contact),
                    onClick = onContactClick,
                    enabled = enabled && supportAvailable,
                    variant = OficiosButtonVariant.Secondary,
                    size = OficiosButtonSize.Small,
                    figmaAuthStroke = true,
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(
                    x = OficiosComponentSize.authSupportBadgeOffsetX,
                    y = -OficiosComponentSize.authSupportBadgeOffsetY,
                )
                .size(
                    width = OficiosComponentSize.authSupportBadgeWidth,
                    height = OficiosComponentSize.authSupportBadgeHeight,
                )
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
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun AuthSupportCardPreview() {
    OficiosTheme {
        AuthSupportCard(onContactClick = {}, modifier = Modifier.padding(OficiosSpacing.xl))
    }
}
