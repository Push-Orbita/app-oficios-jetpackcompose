package com.cocido.oficios.feature.home.worker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.theme.OficiosColorAlert
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.feature.home.R
import com.cocido.oficios.feature.home.worker.components.WorkerAvailabilitySection
import com.cocido.oficios.feature.home.worker.components.WorkerCertificatesSection
import com.cocido.oficios.feature.home.worker.components.WorkerGallerySection
import com.cocido.oficios.feature.home.worker.components.WorkerProfileHeader
import com.cocido.oficios.feature.home.worker.components.WorkerReviewsSection
import com.cocido.oficios.feature.home.worker.components.WorkerSpecialtiesSection

@Composable
fun WorkerProfileScreen(
    uiState: WorkerProfileUiState,
    onEvent: (WorkerProfileEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    OficiosScaffold(
        modifier = modifier,
        topBar = {
            OficiosBackTopBar(
                title = stringResource(R.string.home_worker_profile_title),
                onBack = { onEvent(WorkerProfileEvent.Back) },
                actions = {
                    val success = uiState as? WorkerProfileUiState.Success
                    if (success?.canFavorite == true) {
                        IconButton(
                            onClick = { onEvent(WorkerProfileEvent.FavoriteClicked) },
                            enabled = !success.isTogglingFavorite,
                        ) {
                            Icon(
                                imageVector = if (success.isFavorite) {
                                    Icons.Filled.Favorite
                                } else {
                                    Icons.Filled.FavoriteBorder
                                },
                                contentDescription = stringResource(
                                    if (success.isFavorite) {
                                        R.string.home_worker_favorite_remove
                                    } else {
                                        R.string.home_worker_favorite_add
                                    },
                                ),
                                tint = if (success.isFavorite) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = OficiosSpacing.lg, vertical = OficiosSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
            ) {
                OficiosButton(
                    text = stringResource(R.string.home_worker_rate),
                    onClick = { onEvent(WorkerProfileEvent.RateClicked) },
                    modifier = Modifier.weight(1f),
                    enabled = uiState is WorkerProfileUiState.Success,
                    variant = OficiosButtonVariant.Secondary,
                )
                OficiosButton(
                    text = stringResource(R.string.home_worker_hire),
                    onClick = { onEvent(WorkerProfileEvent.HireClicked) },
                    modifier = Modifier.weight(1f),
                    enabled = uiState is WorkerProfileUiState.Success,
                    variant = OficiosButtonVariant.Primary,
                )
            }
            }
        },
    ) { paddingValues ->
        when (uiState) {
            WorkerProfileUiState.Loading -> {
                OficiosLoadingState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    message = stringResource(R.string.home_worker_profile_loading),
                )
            }

            is WorkerProfileUiState.Error -> {
                OficiosErrorState(
                    title = stringResource(R.string.home_worker_profile_error_title),
                    description = uiState.message,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    onRetry = { onEvent(WorkerProfileEvent.Retry) },
                )
            }

            is WorkerProfileUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = OficiosSpacing.lg),
                    verticalArrangement = Arrangement.spacedBy(OficiosSpacing.lg),
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        WorkerProfileHeader(profile = uiState.profile)
                        WorkerSpecialtiesSection(specialties = uiState.profile.specialties)
                        WorkerAvailabilitySection(availability = uiState.profile.availability)
                        WorkerGallerySection(gallery = uiState.profile.gallery)
                        WorkerCertificatesSection(certificates = uiState.profile.certificates)
                        WorkerReviewsSection(
                            reviews = uiState.reviews,
                            isLoadingReviews = uiState.isLoadingReviews,
                            hasMoreReviews = uiState.hasMoreReviews,
                            onLoadMore = { onEvent(WorkerProfileEvent.LoadNextReviews) },
                        )
                        Text(
                            text = stringResource(R.string.home_worker_report_profile),
                            style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.Underline),
                            color = OficiosColorAlert,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEvent(WorkerProfileEvent.ReportProfileClicked) }
                                .padding(OficiosSpacing.lg),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

