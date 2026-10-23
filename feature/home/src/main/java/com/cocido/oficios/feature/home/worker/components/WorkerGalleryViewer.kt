package com.cocido.oficios.feature.home.worker.components

import androidx.compose.runtime.Composable
import com.cocido.oficios.core.ui.gallery.GalleryMedia
import com.cocido.oficios.core.ui.gallery.OficiosGalleryViewer
import com.cocido.oficios.domain.model.WorkGalleryItem

@Composable
fun WorkerGalleryViewer(
    photos: List<WorkGalleryItem>,
    initialIndex: Int,
    onClose: () -> Unit,
    onOpenExternal: (String) -> Unit = {},
) {
    OficiosGalleryViewer(
        items = photos.map { GalleryMedia(id = it.id, url = it.imageUrl) },
        initialIndex = initialIndex,
        onClose = onClose,
        onOpenExternal = onOpenExternal,
    )
}
