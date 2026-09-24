package com.cocido.oficios.feature.home.worker.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cocido.oficios.core.designsystem.component.cards.CertificateCard
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.feature.home.R
import com.cocido.oficios.domain.model.WorkCertificateItem

@Composable
fun WorkerCertificatesSection(
    certificates: List<WorkCertificateItem>,
    modifier: Modifier = Modifier,
) {
    val visible = certificates.filter { it.name.isPresentableCertificate() }
    if (visible.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = OficiosSpacing.lg),
    ) {
        Text(
            text = stringResource(R.string.home_worker_certificates_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )

        visible.forEach { cert ->
            CertificateCard(
                title = cert.name,
                modifier = Modifier.padding(top = OficiosSpacing.sm),
            )
        }
    }
}

private fun String.isPresentableCertificate(): Boolean {
    val normalized = trim().lowercase()
    if (normalized.length < 3) return false
    return normalized !in setOf("test", "prueba", "xxx", "certificado", "certificate")
}

