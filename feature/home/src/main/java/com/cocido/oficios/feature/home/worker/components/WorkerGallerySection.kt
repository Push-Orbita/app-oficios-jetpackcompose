package com.cocido.oficios.feature.home.worker.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.ui.gallery.GalleryMedia
import com.cocido.oficios.core.ui.gallery.OficiosPhotoMosaic
import com.cocido.oficios.domain.model.WorkGalleryItem
import com.cocido.oficios.feature.home.R

private const val FichaGalleryPreviewLimit = 5

@Composable
fun WorkerGallerySection(
    gallery: List<WorkGalleryItem>,
    onPhotoClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
    ) {
        Text(
            text = stringResource(R.string.home_worker_gallery_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        OficiosPhotoMosaic(
            items = gallery.map { GalleryMedia(id = it.id, url = it.imageUrl) },
            onItemClick = onPhotoClick,
            previewLimit = FichaGalleryPreviewLimit,
            emptyText = stringResource(R.string.home_worker_gallery_empty_title),
            seeAllLabel = { stringResource(R.string.home_worker_gallery_see_all, it) },
            onSeeAll = { onPhotoClick(0) },
        )
    }
}
