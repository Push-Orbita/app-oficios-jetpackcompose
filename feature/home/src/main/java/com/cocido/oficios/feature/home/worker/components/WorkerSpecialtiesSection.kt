package com.cocido.oficios.feature.home.worker.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cocido.oficios.core.designsystem.component.chips.OficiosChip
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.feature.home.R
import com.cocido.oficios.domain.model.WorkerSpecialty

@Composable
fun WorkerSpecialtiesSection(
    specialties: List<WorkerSpecialty>,
    modifier: Modifier = Modifier,
) {
    if (specialties.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = OficiosSpacing.lg),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(OficiosSpacing.sm),
    ) {
        Text(
            text = stringResource(R.string.home_worker_specialties_title),
            style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
        ) {
            items(specialties, key = { it.rubroName }) { specialty ->
                OficiosChip(label = specialty.rubroName)
            }
        }
    }
}

