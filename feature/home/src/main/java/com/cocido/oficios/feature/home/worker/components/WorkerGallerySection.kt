package com.cocido.oficios.feature.home.worker.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.component.cards.WorkGalleryCard
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.feature.home.R
import com.cocido.oficios.domain.model.WorkGalleryItem

@Composable
fun WorkerGallerySection(
    gallery: List<WorkGalleryItem>,
    modifier: Modifier = Modifier,
) {
    val photos = gallery.filter { !it.imageUrl.isNullOrBlank() }
    if (photos.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = OficiosSpacing.lg),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(OficiosSpacing.sm),
    ) {
        Text(
            text = stringResource(R.string.home_worker_gallery_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm)) {
            items(photos, key = { it.id }) { item ->
                WorkGalleryCard(
                    imageUrl = item.imageUrl,
                    caption = null,
                    modifier = Modifier
                        .size(160.dp),
                )
            }
        }
    }
}

