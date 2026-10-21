package com.cocido.oficios.feature.home.worker.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cocido.oficios.core.designsystem.theme.OficiosColorOutlineFill
import com.cocido.oficios.core.designsystem.theme.OficiosColorTextSecondaryAccessible
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosRadius
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.domain.model.WorkGalleryItem
import com.cocido.oficios.feature.home.R

/**
 * Miniaturas 118 dp (mismo tile que Editar Perfil), no el carrusel 376×376 de
 * Figma `613:460`: esa slide se recortaba contra el bottom nav.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkerGallerySection(
    gallery: List<WorkGalleryItem>,
    onPhotoClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val photos = gallery.filter { !it.imageUrl.isNullOrBlank() }
    val tiles = photos.ifEmpty { gallery }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
    ) {
        Text(
            text = stringResource(R.string.home_worker_gallery_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        if (tiles.isEmpty()) {
            Text(
                text = stringResource(R.string.home_worker_gallery_empty_title),
                style = MaterialTheme.typography.bodyLarge,
                color = OficiosColorTextSecondaryAccessible,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        } else {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
                verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
            ) {
                tiles.forEachIndexed { index, item ->
                    val url = item.imageUrl
                    GalleryThumb(
                        imageUrl = url,
                        contentDescription = stringResource(
                            R.string.home_worker_gallery_thumb_cd,
                            index + 1,
                        ),
                        onClick = if (!url.isNullOrBlank()) {
                            { onPhotoClick(index) }
                        } else {
                            null
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun GalleryThumb(
    imageUrl: String?,
    contentDescription: String,
    onClick: (() -> Unit)?,
) {
    val shape = RoundedCornerShape(OficiosRadius.lg)
    Box(
        modifier = Modifier
            .size(OficiosComponentSize.profileMediaTile)
            .clip(shape)
            .background(MaterialTheme.colorScheme.background)
            .border(1.dp, OficiosColorOutlineFill, shape)
            .semantics { this.contentDescription = contentDescription }
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
