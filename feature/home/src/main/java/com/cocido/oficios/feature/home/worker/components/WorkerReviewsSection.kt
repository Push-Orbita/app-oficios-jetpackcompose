package com.cocido.oficios.feature.home.worker.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.cards.OficiosCard
import com.cocido.oficios.core.designsystem.component.feedback.OficiosProgress
import com.cocido.oficios.core.designsystem.theme.OficiosColorTextSecondaryAccessible
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.domain.model.WorkerReputation
import com.cocido.oficios.domain.model.WorkerReview
import com.cocido.oficios.feature.home.R
import kotlin.math.roundToInt

@Composable
fun WorkerReviewsSection(
    reviews: List<WorkerReview>,
    reputation: WorkerReputation?,
    isLoadingReviews: Boolean,
    hasMoreReviews: Boolean,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
    ) {
        Text(
            text = stringResource(R.string.home_worker_reviews_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        when {
            isLoadingReviews && reviews.isEmpty() -> {
                OficiosProgress(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            reviews.isEmpty() -> {
                Text(
                    text = stringResource(R.string.home_worker_reviews_empty_title),
                    style = MaterialTheme.typography.bodyLarge,
                    color = OficiosColorTextSecondaryAccessible,
                )
            }
            else -> {
                reviews.forEach { review ->
                    ReviewCard(review)
                }
            }
        }
        if (hasMoreReviews) {
            if (isLoadingReviews && reviews.isNotEmpty()) {
                OficiosProgress(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                OficiosButton(
                    text = stringResource(R.string.home_worker_load_more_reviews),
                    onClick = onLoadMore,
                    variant = OficiosButtonVariant.Outlined,
                )
            }
        }
    }
}

@Composable
private fun ReviewCard(review: WorkerReview) {
    val stars = reviewStars(review.qualityScore)
    OficiosCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md)) {
            if (stars != null) {
                WorkerFiveStarRow(rating = stars.toFloat())
            }
            review.comment?.takeIf { it.isNotBlank() }?.let { comment ->
                Text(
                    text = comment,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            review.reviewedAt?.takeIf { it.isNotBlank() }?.let { date ->
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodyLarge,
                    color = OficiosColorTextSecondaryAccessible,
                )
            }
        }
    }
}

internal fun reviewStars(qualityScore: Int?): Int? {
    val score = qualityScore?.takeIf { it > 0 } ?: return null
    return ((score.coerceIn(0, 10).toDouble() / 10.0) * 5.0).roundToInt().coerceIn(1, 5)
}
