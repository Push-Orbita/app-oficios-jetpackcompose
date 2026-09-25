package com.cocido.oficios.feature.home.worker.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cocido.oficios.core.designsystem.component.avatar.OficiosAvatar
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.cards.WorkerReviewCard
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.domain.model.WorkerReputation
import com.cocido.oficios.domain.model.WorkerReview
import com.cocido.oficios.feature.home.R
import java.util.Locale

@Composable
fun WorkerReviewsSection(
    reviews: List<WorkerReview>,
    reputation: WorkerReputation?,
    isLoadingReviews: Boolean,
    hasMoreReviews: Boolean,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WorkerProfileSection(
        title = stringResource(R.string.home_worker_reviews_title),
        modifier = modifier,
    ) {
        val count = reputation?.reviewCount
        val rating = reputation?.rating
        if (rating != null && (count ?: 0) > 0) {
            Text(
                text = stringResource(
                    R.string.home_worker_reviews_summary,
                    String.format(Locale.US, "%.1f", rating),
                    count ?: reviews.size,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = OficiosSpacing.sm),
            )
        }
        when {
            isLoadingReviews && reviews.isEmpty() -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            reviews.isEmpty() -> {
                Text(
                    text = stringResource(R.string.home_worker_reviews_empty_title),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.home_worker_reviews_empty_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = OficiosSpacing.xxs),
                )
            }
            else -> {
                Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm)) {
                    reviews.forEach { review ->
                        ReviewRow(review)
                    }
                }
            }
        }
        if (hasMoreReviews) {
            if (isLoadingReviews && reviews.isNotEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(top = OficiosSpacing.sm)
                        .align(Alignment.CenterHorizontally),
                )
            } else {
                OficiosButton(
                    text = stringResource(R.string.home_worker_load_more_reviews),
                    onClick = onLoadMore,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = OficiosSpacing.sm),
                    variant = OficiosButtonVariant.Outlined,
                )
            }
        }
    }
}

@Composable
private fun ReviewRow(review: WorkerReview) {
    val label = review.clientLabel?.takeIf { it.isNotBlank() }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
    ) {
        OficiosAvatar(
            name = label ?: stringResource(R.string.home_worker_review_anonymous),
            size = OficiosComponentSize.avatarSm,
            circular = true,
        )
        Column(modifier = Modifier.weight(1f)) {
            review.reviewedAt?.let { date ->
                Text(
                    text = date,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            WorkerReviewCard(
                clientLabel = label,
                qualityScore = review.qualityScore,
                punctualityScore = review.punctualityScore,
                comment = review.comment,
            )
        }
    }
}
