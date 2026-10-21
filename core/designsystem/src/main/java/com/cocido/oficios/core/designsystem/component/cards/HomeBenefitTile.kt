package com.cocido.oficios.core.designsystem.component.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.theme.OficiosColorOutlineFill
import com.cocido.oficios.core.designsystem.theme.OficiosColorWhite
import com.cocido.oficios.core.designsystem.theme.OficiosRadius
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

/**
 * Tile de beneficio — Figma home trabajador `259:294`.
 * Cuadrado r16, fill #004DA8, padding 16. Palabra clave en Bold.
 */
@Composable
fun HomeBenefitTile(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(OficiosRadius.lg)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(shape)
            .background(OficiosColorOutlineFill, shape)
            .border(1.dp, OficiosColorOutlineFill, shape)
            .padding(OficiosSpacing.md),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = OficiosColorWhite,
        )
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

@Preview(name = "Home Benefit Tile", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun HomeBenefitTilePreview() {
    OficiosTheme {
        HomeBenefitTile(
            text = emphasizedBody("Todos tus servicios en un mismo espacio", "Todos"),
            modifier = Modifier.padding(OficiosSpacing.md),
        )
    }
}
