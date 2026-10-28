package com.cocido.oficios.core.designsystem.component.cards

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.R
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.theme.OficiosAuthGlyphBodyStyle
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

internal const val OFICIOS_SUPPORT_CARD_TEST_TAG = "oficios_support_card"
internal const val OFICIOS_SUPPORT_HELP_ICON_TEST_TAG = "oficios_support_help_icon"
internal const val OFICIOS_SUPPORT_CONTACT_TEST_TAG = "oficios_support_contact"

/**
 * Card de asistencia. El "?" vive **dentro** del borde (círculo ~32 dp,
 * mismo outline que la card): ícono a la izquierda, texto, botón Contactar.
 * Si no entran en una fila, el botón baja alineado a la derecha.
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
    val helpDescription = stringResource(R.string.ds_support_help)
    OficiosCard(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .testTag(OFICIOS_SUPPORT_CARD_TEST_TAG),
        contentPadding = PaddingValues(
            start = OficiosSpacing.md,
            top = OficiosComponentSize.authSupportCardPaddingTop,
            end = OficiosSpacing.md,
            bottom = OficiosComponentSize.authSupportCardPaddingBottom,
        ),
        expandContent = true,
    ) {
        SupportCardBody(
            message = message,
            contactLabel = contactLabel,
            onContactClick = onContactClick,
            enabled = enabled,
            helpDescription = helpDescription,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun SupportCardBody(
    message: String,
    contactLabel: String,
    onContactClick: () -> Unit,
    enabled: Boolean,
    helpDescription: String,
    modifier: Modifier = Modifier,
) {
    val gap = OficiosSpacing.sm
    val iconSize = OficiosComponentSize.authSupportHelpIconSize
    val minTextWidth = OficiosComponentSize.authSupportMinTextWidth
    SubcomposeLayout(modifier) { constraints ->
        val width = constraints.maxWidth
        val gapPx = gap.roundToPx()
        val iconPlaceable = subcompose("icon") {
            SupportHelpIcon(description = helpDescription)
        }.first().measure(Constraints.fixed(iconSize.roundToPx(), iconSize.roundToPx()))

        val buttonPlaceable = subcompose("button") {
            OficiosButton(
                text = contactLabel,
                onClick = onContactClick,
                enabled = enabled,
                variant = OficiosButtonVariant.Secondary,
                size = OficiosButtonSize.Small,
                figmaAuthStroke = true,
                modifier = Modifier.testTag(OFICIOS_SUPPORT_CONTACT_TEST_TAG),
            )
        }.first().measure(Constraints())

        val textMeasurable = subcompose("text") {
            SupportMessage(message)
        }.first()
        val minTextPx = textMeasurable.minIntrinsicWidth(Constraints.Infinity)
        val remainingAfterIcon = (width - iconPlaceable.width - gapPx).coerceAtLeast(0)
        val singleRow = remainingAfterIcon - buttonPlaceable.width - gapPx >=
            maxOf(minTextPx, minTextWidth.roundToPx())

        val textMaxWidth = if (singleRow) {
            (remainingAfterIcon - buttonPlaceable.width - gapPx).coerceAtLeast(0)
        } else {
            remainingAfterIcon
        }
        val textPlaceable = textMeasurable.measure(
            Constraints(minWidth = 0, maxWidth = textMaxWidth),
        )

        val textRowHeight = maxOf(iconPlaceable.height, textPlaceable.height)
        val contentHeight = if (singleRow) {
            maxOf(textRowHeight, buttonPlaceable.height)
        } else {
            textRowHeight + gapPx + buttonPlaceable.height
        }
        val height = maxOf(contentHeight, constraints.minHeight)
        val y0 = (height - contentHeight) / 2

        layout(width, height) {
            val iconY = y0 + (textRowHeight - iconPlaceable.height) / 2
            val textY = y0 + (textRowHeight - textPlaceable.height) / 2
            iconPlaceable.placeRelative(0, iconY)
            textPlaceable.placeRelative(iconPlaceable.width + gapPx, textY)
            if (singleRow) {
                val buttonY = y0 + (contentHeight - buttonPlaceable.height) / 2
                buttonPlaceable.placeRelative(width - buttonPlaceable.width, buttonY)
            } else {
                buttonPlaceable.placeRelative(
                    width - buttonPlaceable.width,
                    y0 + textRowHeight + gapPx,
                )
            }
        }
    }
}

@Composable
private fun SupportHelpIcon(description: String) {
    Box(
        modifier = Modifier
            .size(OficiosComponentSize.authSupportHelpIconSize)
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            .testTag(OFICIOS_SUPPORT_HELP_ICON_TEST_TAG)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "?",
            style = OficiosAuthGlyphBodyStyle.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun SupportMessage(message: String) {
    Text(
        text = message,
        style = OficiosAuthGlyphBodyStyle,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF00358B, widthDp = 360)
@Preview(showBackground = true, backgroundColor = 0xFF00358B, widthDp = 412)
@Composable
private fun OficiosSupportCardPreview() {
    OficiosTheme {
        OficiosSupportCard(
            message = "Para cualquier consulta o duda de asistencia, contáctenos a nuestra línea de atención.",
            contactLabel = "Contactar",
            onContactClick = {},
            modifier = Modifier.padding(OficiosSpacing.md),
        )
    }
}
