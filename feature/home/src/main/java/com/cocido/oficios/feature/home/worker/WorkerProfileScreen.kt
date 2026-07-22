package com.cocido.oficios.feature.home.worker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
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
                title = stringResource(R.string.home_worker_stub_title),
                onBack = { onEvent(WorkerProfileEvent.Back) },
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = OficiosSpacing.lg, vertical = OficiosSpacing.md),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                OficiosButton(
                    text = stringResource(R.string.home_worker_hire),
                    onClick = { onEvent(WorkerProfileEvent.HireClicked) },
                    modifier = Modifier,
                    enabled = uiState is WorkerProfileUiState.Success,
                    variant = OficiosButtonVariant.Primary,
                )
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
                        .padding(paddingValues),
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
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
                    }
                }
            }
        }
    }
}

