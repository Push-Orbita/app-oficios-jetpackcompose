package com.cocido.oficios.feature.home.worker.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import com.cocido.oficios.core.designsystem.component.cards.CertificateCard
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.domain.model.WorkCertificateItem
import com.cocido.oficios.feature.home.R

@Composable
fun WorkerCertificatesSection(
    certificates: List<WorkCertificateItem>,
    onOpen: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val visible = certificates.filter { it.name.isPresentableCertificate() }
    if (visible.isEmpty()) return

    WorkerProfileSection(
        title = stringResource(R.string.home_worker_certificates_title),
        modifier = modifier,
    ) {
        Column {
            visible.forEach { cert ->
                val url = cert.fileUrl
                CertificateCard(
                    title = cert.name,
                    modifier = Modifier
                        .sizeIn(minHeight = OficiosComponentSize.buttonMinHeight)
                        .then(
                            if (!url.isNullOrBlank()) {
                                Modifier
                                    .semantics(mergeDescendants = true) { contentDescription = cert.name }
                                    .clickable(
                                        onClickLabel = stringResource(R.string.home_worker_certificate_open),
                                    ) { onOpen(url) }
                            } else {
                                Modifier
                            },
                        )
                        .then(Modifier),
                )
                if (!url.isNullOrBlank()) {
                    Text(
                        text = stringResource(R.string.home_worker_certificate_open),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = OficiosSpacing.xs),
                    )
                }
            }
        }
    }
}

private fun String.isPresentableCertificate(): Boolean {
    val normalized = trim().lowercase()
    if (normalized.length < 3) return false
    return normalized !in setOf("test", "prueba", "xxx", "certificado", "certificate")
}
