package com.cocido.oficios.feature.home.worker.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cocido.oficios.core.designsystem.component.chips.OficiosChip
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.feature.home.R
import com.cocido.oficios.domain.model.WorkerAvailability

@Composable
fun WorkerAvailabilitySection(
    availability: WorkerAvailability?,
    modifier: Modifier = Modifier,
) {
    if (availability == null) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = OficiosSpacing.lg),
    ) {
        Text(
            text = stringResource(R.string.home_worker_availability_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(vertical = OficiosSpacing.xs))

        OficiosChip(label = availability.summaryLabel)
    }
}

