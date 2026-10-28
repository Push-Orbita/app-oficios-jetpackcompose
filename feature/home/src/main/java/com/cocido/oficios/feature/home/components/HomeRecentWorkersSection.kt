package com.cocido.oficios.feature.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.cocido.oficios.core.designsystem.component.avatar.OficiosAvatar
import com.cocido.oficios.core.designsystem.component.badges.OficiosVerifiedBadge
import com.cocido.oficios.core.designsystem.component.cards.OficiosCard
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.domain.model.WorkerSummary
import com.cocido.oficios.domain.model.formatStarRatingLabel
import com.cocido.oficios.feature.home.R

@Composable
fun HomeRecentWorkersSection(
    workers: List<WorkerSummary>,
    onWorkerClick: (WorkerSummary) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (workers.isEmpty()) return
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = OficiosComponentSize.homeRecentsAfterSearchExtra)
            .heightIn(min = OficiosComponentSize.homeRecentsSectionMinHeight),
        verticalArrangement = Arrangement.spacedBy(OficiosComponentSize.homeRecentsTitleToCards),
    ) {
        Text(
            text = stringResource(R.string.home_recent_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.s10),
        ) {
            workers.take(3).forEach { worker ->
                HomeRecentWorkerCard(
                    worker = worker,
                    onClick = { onWorkerClick(worker) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun HomeRecentWorkerCard(
    worker: WorkerSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val verifiedLabel = stringResource(R.string.home_verified_user)
    val description = buildString {
        append(worker.displayName)
        if (worker.specialtyLabel.isNotBlank()) {
            append(", ")
            append(worker.specialtyLabel)
        }
        if (worker.isVerified) {
            append(", ")
            append(verifiedLabel)
        }
    }
    OficiosCard(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = OficiosComponentSize.categoryCardSize)
            .semantics(mergeDescendants = true) {
                role = Role.Button
                contentDescription = description
            },
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
        ) {
        OficiosAvatar(
            name = worker.displayName,
            imageUrl = worker.avatarUrl,
            size = OficiosComponentSize.avatarSm,
            showBorder = true,
            circular = true,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xxs),
        ) {
            Text(
                text = worker.displayName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f, fill = false),
            )
            if (worker.isVerified) {
                OficiosVerifiedBadge(contentDescription = null)
            }
        }
        val rating = worker.rating
        if (rating != null) {
            Text(
                text = formatStarRatingLabel(rating, worker.reviewCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                textAlign = TextAlign.Center,
            )
        }
        if (worker.specialtyLabel.isNotBlank()) {
            Text(
                text = worker.specialtyLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
        }
    }
}
