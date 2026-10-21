package com.cocido.oficios.feature.home.worker.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.theme.OficiosColorOutlineFill
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosRadius
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.domain.model.WorkGalleryItem
import com.cocido.oficios.feature.home.R

@Composable
fun WorkerGallerySection(
    gallery: List<WorkGalleryItem>,
    onPhotoClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val photos = gallery.filter { !it.imageUrl.isNullOrBlank() }.ifEmpty { gallery }
    if (photos.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
    ) {
        Text(
            text = stringResource(R.string.home_worker_gallery_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xl),
            contentPadding = PaddingValues(),
        ) {
            itemsIndexed(photos, key = { _, item -> item.id }) { index, item ->
                GallerySlide(
                    imageUrl = item.imageUrl,
                    contentDescription = stringResource(R.string.home_worker_gallery_thumb_cd, index + 1),
                    showChevrons = photos.size > 1 && index == 0,
                    onClick = { onPhotoClick(index) },
                    onPrevious = if (index > 0) {
                        { onPhotoClick(index - 1) }
                    } else {
                        null
                    },
                    onNext = if (index < photos.lastIndex) {
                        { onPhotoClick(index + 1) }
                    } else {
                        null
                    },
                )
            }
        }
    }
}

@Composable
private fun GallerySlide(
    imageUrl: String?,
    contentDescription: String,
    showChevrons: Boolean,
    onClick: () -> Unit,
    onPrevious: (() -> Unit)?,
    onNext: (() -> Unit)?,
) {
    val shape = RoundedCornerShape(OficiosRadius.lg)
    Box(
        modifier = Modifier
            .size(OficiosComponentSize.workerGallerySize)
            .clip(shape)
            .background(MaterialTheme.colorScheme.background)
            .border(1.dp, OficiosColorOutlineFill, shape)
            .clickable(onClick = onClick),
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        if (showChevrons) {
            if (onPrevious != null) {
                Icon(
                    imageVector = ImageVector.vectorResource(OficiosIcons.ChevronLeft),
                    contentDescription = stringResource(R.string.home_worker_gallery_previous),
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(OficiosComponentSize.iconMd)
                        .clickable(onClick = onPrevious),
                )
            }
            if (onNext != null) {
                Icon(
                    imageVector = ImageVector.vectorResource(OficiosIcons.ChevronRight),
                    contentDescription = stringResource(R.string.home_worker_gallery_next),
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(OficiosComponentSize.iconMd)
                        .clickable(onClick = onNext),
                )
            }
        }
    }
}
