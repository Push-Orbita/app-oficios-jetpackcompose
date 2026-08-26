package com.cocido.oficios.feature.home.worker.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
    if (certificates.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = OficiosSpacing.lg),
    ) {
        Text(
            text = stringResource(R.string.home_worker_certificates_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )

        certificates.forEach { cert ->
            CertificateCard(
                title = cert.name,
                modifier = Modifier.padding(top = OficiosSpacing.sm),
            )
        }
    }
}

