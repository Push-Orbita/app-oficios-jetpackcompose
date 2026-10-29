package com.cocido.oficios.core.designsystem.component.categories

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import coil.compose.SubcomposeAsyncImage
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize

/**
 * Glifo de categoría / rubro usado por las grillas (registro, home, onboarding).
 *
 * Las ilustraciones WebP del design system son blancas (pensadas para fondo
 * oscuro). En cards claras se tiñen con [tint] para no desaparecer.
 */
@Composable
fun CategoryGlyph(
    id: Long,
    label: String,
    modifier: Modifier = Modifier,
    iconUrl: String? = null,
    contentDescription: String? = null,
    tint: Color = MaterialTheme.colorScheme.onBackground,
    illustrationModifier: Modifier = modifier,
) {
    val spec = remember(id, label, iconUrl) {
        CategoryIconResolver.resolve(id, label, iconUrl)
    }
    val remoteUrl = spec.remoteUrl
    if (remoteUrl != null) {
        SubcomposeAsyncImage(
            model = remoteUrl,
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = illustrationModifier,
            loading = {
                LocalCategoryGlyph(
                    spec = spec,
                    modifier = modifier,
                    illustrationModifier = illustrationModifier,
                    contentDescription = contentDescription,
                    tint = tint,
                )
            },
            error = {
                LocalCategoryGlyph(
                    spec = spec,
                    modifier = modifier,
                    illustrationModifier = illustrationModifier,
                    contentDescription = contentDescription,
                    tint = tint,
                )
            },
        )
        return
    }
    LocalCategoryGlyph(
        spec = spec,
        modifier = modifier,
        illustrationModifier = illustrationModifier,
        contentDescription = contentDescription,
        tint = tint,
    )
}

@Composable
private fun LocalCategoryGlyph(
    spec: CategoryIconSpec,
    modifier: Modifier,
    illustrationModifier: Modifier,
    contentDescription: String?,
    tint: Color,
) {
    val illustration = spec.illustrationRes
    if (illustration != null) {
        Image(
            painter = painterResource(illustration),
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(tint),
            modifier = illustrationModifier,
        )
        return
    }
    Icon(
        imageVector = spec.vector.asImageVector(),
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier,
    )
}

@Composable
private fun CategoryVectorIcon.asImageVector(): ImageVector = when (this) {
    is CategoryVectorIcon.Lucide -> ImageVector.vectorResource(resId)
    is CategoryVectorIcon.Material -> imageVector
}

const val CATEGORY_CARD_GLYPH_TEST_TAG = "oficios_category_card_glyph"

internal fun Modifier.categoryVectorSize(): Modifier =
    size(OficiosComponentSize.categoryCardGlyphSize)
        .testTag(CATEGORY_CARD_GLYPH_TEST_TAG)

internal fun Modifier.categoryIllustrationSize(): Modifier =
    size(
        width = OficiosComponentSize.categoryCardIllustrationWidth,
        height = OficiosComponentSize.categoryCardIllustrationHeight,
    ).testTag(CATEGORY_CARD_GLYPH_TEST_TAG)

