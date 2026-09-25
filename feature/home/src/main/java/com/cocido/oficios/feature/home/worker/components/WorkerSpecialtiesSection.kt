package com.cocido.oficios.feature.home.worker.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
    WorkerProfileSection(
        title = stringResource(R.string.home_worker_specialties_title),
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md)) {
            specialties.forEach { specialty ->
                SpecialtyGroup(specialty)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SpecialtyGroup(specialty: WorkerSpecialty) {
    val chipContainer = MaterialTheme.colorScheme.background
    val chipContent = MaterialTheme.colorScheme.onSurface
    val chipBorder = MaterialTheme.colorScheme.primary
    Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xs)) {
        if (specialty.rubroName.isNotBlank()) {
            Text(
                text = specialty.rubroName,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
            verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
        ) {
            specialty.subRubroNames.forEach { name ->
                OficiosChip(
                    label = name,
                    containerColor = chipContainer,
                    contentColor = chipContent,
                    borderColor = chipBorder,
                )
            }
            if (specialty.servesAtHome) {
                ModalityChip(
                    label = stringResource(R.string.home_worker_serves_home),
                    icon = Icons.Outlined.Home,
                )
            }
            if (specialty.servesAtLocal) {
                ModalityChip(
                    label = stringResource(R.string.home_worker_serves_local),
                    icon = Icons.Outlined.Storefront,
                )
            }
        }
    }
}

@Composable
private fun ModalityChip(label: String, icon: ImageVector) {
    OficiosChip(
        label = label,
        leadingIcon = icon,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onSurface,
        borderColor = MaterialTheme.colorScheme.primary,
    )
}
