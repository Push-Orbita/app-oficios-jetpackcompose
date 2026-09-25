package com.cocido.oficios.feature.home.worker.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.cards.WorkGalleryCard
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.domain.model.WorkGalleryItem
import com.cocido.oficios.feature.home.R

private const val GALLERY_PREVIEW_COUNT = 4

@Composable
fun WorkerGallerySection(
    gallery: List<WorkGalleryItem>,
    onPhotoClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val photos = gallery.filter { !it.imageUrl.isNullOrBlank() }
    if (photos.isEmpty()) return
    val preview = photos.take(GALLERY_PREVIEW_COUNT)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = OficiosSpacing.md),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
    ) {
        Text(
            text = stringResource(R.string.home_worker_gallery_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        preview.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
            ) {
                row.forEach { item ->
                    val index = photos.indexOf(item)
                    WorkGalleryCard(
                        imageUrl = item.imageUrl,
                        caption = null,
                        contentDescription = stringResource(R.string.home_worker_gallery_thumb_cd, index + 1),
                        onClick = { onPhotoClick(index) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (row.size == 1) {
                    androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
                }
            }
        }
        if (photos.size > preview.size) {
            OficiosButton(
                text = stringResource(R.string.home_worker_gallery_see_all, photos.size),
                onClick = { onPhotoClick(0) },
                modifier = Modifier.fillMaxWidth(),
                variant = OficiosButtonVariant.Outlined,
            )
        }
    }
}
