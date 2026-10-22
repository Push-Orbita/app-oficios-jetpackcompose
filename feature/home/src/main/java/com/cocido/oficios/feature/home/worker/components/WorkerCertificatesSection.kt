package com.cocido.oficios.feature.home.worker.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
    ) {
        Text(
            text = stringResource(R.string.home_worker_certificates_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        if (visible.isEmpty()) {
            Text(
                text = stringResource(R.string.home_worker_certificates_pending),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        } else {
            visible.forEach { cert ->
                val url = cert.fileUrl
                Text(
                    text = cert.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
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
                        ),
                )
            }
        }
    }
}

private fun String.isPresentableCertificate(): Boolean {
    val normalized = trim().lowercase()
    if (normalized.length < 3) return false
    return normalized !in setOf("test", "prueba", "xxx", "certificado", "certificate")
}
