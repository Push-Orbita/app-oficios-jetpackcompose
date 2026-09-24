package com.cocido.oficios.feature.home.worker.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cocido.oficios.core.designsystem.component.cards.WorkerReviewCard
import com.cocido.oficios.core.designsystem.component.feedback.OficiosEmptyState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosEmptyVariant
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.feature.home.R
import com.cocido.oficios.domain.model.WorkerReview

@Composable
fun WorkerReviewsSection(
    reviews: List<WorkerReview>,
    isLoadingReviews: Boolean,
    hasMoreReviews: Boolean,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = OficiosSpacing.lg),
    ) {
        Text(
            text = stringResource(R.string.home_worker_reviews_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = OficiosSpacing.sm),
        )

        when {
            isLoadingReviews && reviews.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                }
            }

            reviews.isEmpty() -> {
                OficiosEmptyState(
                    title = stringResource(R.string.home_worker_reviews_empty_title),
                    description = stringResource(R.string.home_worker_reviews_empty_description),
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Outlined.StarOutline,
                    ctaLabel = null,
                    onCta = null,
                    variant = OficiosEmptyVariant.Generic,
                )
            }

            else -> {
                reviews.forEach { review ->
                    WorkerReviewCard(
                        clientLabel = review.clientLabel,
                        qualityScore = review.qualityScore,
                        punctualityScore = review.punctualityScore,
                        comment = review.comment,
                        modifier = Modifier.padding(bottom = OficiosSpacing.sm),
                    )
                }
            }
        }

        if (hasMoreReviews) {
            if (isLoadingReviews && reviews.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                com.cocido.oficios.core.designsystem.component.buttons.OficiosButton(
                    text = stringResource(R.string.home_worker_load_more_reviews),
                    onClick = onLoadMore,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

