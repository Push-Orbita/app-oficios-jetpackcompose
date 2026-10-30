package com.cocido.oficios.feature.home.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
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

/** Máximo de recientes que se muestran en el home. */
private const val RECENT_MAX = 3

const val HOME_RECENT_CARD_TAG = "home_recent_card"

/**
 * Recientes del home cliente. Cada card tiene ancho propio
 * ([OficiosComponentSize.homeRecentCardWidth]) y no se reparte 1/3 del ancho:
 * con 1 o 2 recientes quedan alineadas a la izquierda con tamaño normal, y con 3
 * la fila scrollea en horizontal en pantallas angostas en vez de achicar las cards
 * hasta cortar el nombre.
 */
@Composable
fun HomeRecentWorkersSection(
    workers: List<WorkerSummary>,
    onWorkerClick: (WorkerSummary) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (workers.isEmpty()) return
    val visible = workers.take(RECENT_MAX)
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
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
            verticalAlignment = Alignment.Top,
        ) {
            visible.forEach { worker ->
                HomeRecentWorkerCard(
                    worker = worker,
                    onClick = { onWorkerClick(worker) },
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
        contentPadding = PaddingValues(
            horizontal = OficiosSpacing.sm,
            vertical = OficiosSpacing.md,
        ),
        modifier = modifier
            .width(OficiosComponentSize.homeRecentCardWidth)
            .heightIn(min = OficiosComponentSize.homeRecentCardMinHeight)
            .testTag(HOME_RECENT_CARD_TAG)
            .semantics(mergeDescendants = true) {
                role = Role.Button
                contentDescription = description
            },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xxs),
        ) {
            // El badge va sobre el avatar: así el nombre usa todo el ancho de la card.
            Box(modifier = Modifier.padding(bottom = OficiosSpacing.xxs)) {
                OficiosAvatar(
                    name = worker.displayName,
                    imageUrl = worker.avatarUrl,
                    size = OficiosComponentSize.avatarMd,
                    showBorder = true,
                    circular = true,
                )
                if (worker.isVerified) {
                    OficiosVerifiedBadge(
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = OficiosSpacing.xxs, y = OficiosSpacing.xxs)
                            .background(MaterialTheme.colorScheme.surface, CircleShape),
                    )
                }
            }
            Text(
                text = worker.displayName,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            val rating = worker.rating
            if (rating != null) {
                Text(
                    text = formatStarRatingLabel(rating, worker.reviewCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            }
            if (worker.specialtyLabel.isNotBlank()) {
                Text(
                    text = worker.specialtyLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
