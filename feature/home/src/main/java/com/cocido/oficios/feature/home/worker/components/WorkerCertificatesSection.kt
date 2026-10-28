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
import com.cocido.oficios.core.ui.gallery.GalleryMediaKind
import com.cocido.oficios.core.ui.gallery.OficiosPhotoMosaic
import com.cocido.oficios.core.ui.gallery.toGalleryMediaKind
import com.cocido.oficios.domain.model.WorkCertificateItem
import com.cocido.oficios.feature.home.R

@Composable
fun WorkerCertificatesSection(
    certificates: List<WorkCertificateItem>,
    onOpen: (WorkCertificateItem, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val visible = certificates.filter { it.name.isPresentableCertificate() }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
    ) {
        Text(
            text = stringResource(R.string.home_worker_certificates_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        OficiosPhotoMosaic(
            items = visible.map { cert ->
                GalleryMedia(
                    id = cert.id,
                    url = cert.fileUrl,
                    title = cert.name,
                    kind = cert.fileUrl.toGalleryMediaKind(),
                )
            },
            onItemClick = { index -> onOpen(visible[index], index) },
            emptyText = stringResource(R.string.home_worker_certificates_pending),
        )
    }
}

fun String.isPresentableCertificate(): Boolean {
    val normalized = trim().lowercase()
    if (normalized.length < 3) return false
    return normalized !in setOf("test", "prueba", "xxx", "certificado", "certificate")
}

fun WorkCertificateItem.toGalleryMedia(): GalleryMedia = GalleryMedia(
    id = id,
    url = fileUrl,
    title = name,
    kind = if (fileUrl.toGalleryMediaKind() == GalleryMediaKind.Pdf) {
        GalleryMediaKind.Pdf
    } else {
        GalleryMediaKind.Image
    },
)
