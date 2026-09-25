package com.cocido.oficios.feature.home.worker.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cocido.oficios.core.designsystem.component.chips.OficiosChip
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.domain.model.WorkerSpecialty
import com.cocido.oficios.feature.home.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkerSpecialtiesSection(
    specialties: List<WorkerSpecialty>,
    modifier: Modifier = Modifier,
) {
    if (specialties.isEmpty()) return
    val atHome = stringResource(R.string.home_worker_serves_home)
    val atLocal = stringResource(R.string.home_worker_serves_local)
    val chips = buildList {
        specialties.forEach { specialty ->
            if (specialty.subRubroNames.isEmpty()) {
                add(specialty.rubroName)
            } else {
                specialty.subRubroNames.forEach { add(it) }
            }
            if (specialty.servesAtHome) add(atHome)
            if (specialty.servesAtLocal) add(atLocal)
        }
    }.distinct()
    if (chips.isEmpty()) return

    WorkerProfileSection(
        title = stringResource(R.string.home_worker_specialties_title),
        modifier = modifier,
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
            verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
        ) {
            chips.forEach { label ->
                OficiosChip(label = label)
            }
        }
    }
}
