package com.cocido.oficios.core.designsystem.component.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.theme.OficiosColorOutlineFill
import com.cocido.oficios.core.designsystem.theme.OficiosColorWhite
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosRadius
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

internal const val HOME_BENEFIT_TILE_TEST_TAG_PREFIX = "home_benefit_tile_"

/**
 * Tile de beneficio — Figma home trabajador `259:294`.
 * Fill #004DA8, padding 16, radio 16, 16sp D-DIN, palabra clave en Bold.
 * Alto según el texto (sin recorte); no parte palabras.
 */
@Composable
fun HomeBenefitTile(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(OficiosRadius.lg)
    Box(
        modifier = modifier
            .clip(shape)
            .background(OficiosColorOutlineFill, shape)
            .border(1.dp, OficiosColorOutlineFill, shape)
            .padding(OficiosSpacing.md),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                lineBreak = LineBreak.Paragraph,
                hyphens = Hyphens.None,
            ),
            color = OficiosColorWhite,
            overflow = TextOverflow.Clip,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * Fila de beneficios. Si las 3 cards entran con ancho ≥ [OficiosComponentSize.homeBenefitTileMinWidth],
 * van en una fila a la misma altura. Si no, carrusel horizontal con peek de la siguiente.
 */
@Composable
fun HomeBenefitsRow(
    tiles: List<AnnotatedString>,
    modifier: Modifier = Modifier,
) {
    if (tiles.isEmpty()) return
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val gap = OficiosSpacing.s10
        val minCard = OficiosComponentSize.homeBenefitTileMinWidth
        val threeFit = minCard * tiles.size + gap * (tiles.size - 1) <= maxWidth
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (threeFit) Modifier else Modifier.horizontalScroll(rememberScrollState()),
                )
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(gap),
        ) {
            tiles.forEachIndexed { index, text ->
                val tileModifier = if (threeFit) {
                    Modifier.weight(1f)
                } else {
                    Modifier.width(minCard)
                }
                HomeBenefitTile(
                    text = text,
                    modifier = tileModifier
                        .fillMaxHeight()
                        .testTag("$HOME_BENEFIT_TILE_TEST_TAG_PREFIX$index"),
                )
            }
        }
    }
}

fun emphasizedBody(full: String, emphasis: String): AnnotatedString {
    if (emphasis.isEmpty()) return AnnotatedString(full)
    val start = full.indexOf(emphasis)
    if (start < 0) return AnnotatedString(full)
    return buildAnnotatedString {
        append(full.substring(0, start))
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append(emphasis)
        }
        append(full.substring(start + emphasis.length))
    }
}

@Preview(name = "Home Benefit Tiles", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun HomeBenefitTilePreview() {
    OficiosTheme {
        Column(modifier = Modifier.padding(OficiosSpacing.md)) {
            HomeBenefitsRow(
                tiles = listOf(
                    emphasizedBody("Todos sus servicios en un mismo espacio", "Todos"),
                    emphasizedBody("Mayor alcance a potenciales clientes", "alcance"),
                    emphasizedBody("Organícese de manera más eficiente", "eficiente"),
                ),
            )
        }
    }
}
