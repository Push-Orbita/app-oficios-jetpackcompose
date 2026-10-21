package com.cocido.oficios.feature.home.worker

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import com.cocido.oficios.core.designsystem.component.OficiosDialog
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosSkeleton
import com.cocido.oficios.core.designsystem.component.feedback.OficiosStateContent
import com.cocido.oficios.core.designsystem.component.feedback.rememberOficiosHaptics
import com.cocido.oficios.core.designsystem.component.navigation.OficiosScreenTitle
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.domain.model.WorkGalleryItem
import com.cocido.oficios.domain.model.WorkerProfile
import com.cocido.oficios.domain.model.WorkerReputation
import com.cocido.oficios.domain.model.WorkerReview
import com.cocido.oficios.domain.model.WorkerSpecialty
import com.cocido.oficios.feature.home.R
import com.cocido.oficios.feature.home.components.ClientHomeBottomBar
import com.cocido.oficios.feature.home.components.ClientHomeTab
import com.cocido.oficios.feature.home.worker.components.WorkerCertificatesSection
import com.cocido.oficios.feature.home.worker.components.WorkerGallerySection
import com.cocido.oficios.feature.home.worker.components.WorkerGalleryViewer
import com.cocido.oficios.feature.home.worker.components.WorkerProfileHeader
import com.cocido.oficios.feature.home.worker.components.WorkerReviewsSection

/** Ficha trabajador — fuente de verdad Flujos `613:460` / `453:343`. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerProfileScreen(
    uiState: WorkerProfileUiState,
    onEvent: (WorkerProfileEvent) -> Unit,
    modifier: Modifier = Modifier,
    selectedTab: ClientHomeTab = ClientHomeTab.INICIO,
    onTabSelected: (ClientHomeTab) -> Unit = {},
    showFavorites: Boolean? = null,
) {
    var confirmReport by rememberSaveable { mutableStateOf(false) }
    val haptics = rememberOficiosHaptics()
    OficiosScaffold(
        modifier = modifier,
        bottomBar = {
            ClientHomeBottomBar(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                showFavorites = showFavorites,
            )
        },
    ) { paddingValues ->
        OficiosStateContent(targetState = uiState, modifier = Modifier.padding(paddingValues)) { state ->
            when (state) {
                WorkerProfileUiState.Loading -> WorkerProfileSkeleton(
                    modifier = Modifier.fillMaxSize(),
                )
                is WorkerProfileUiState.Error -> {
                    OficiosErrorState(
                        title = stringResource(R.string.home_worker_profile_error_title),
                        description = state.message,
                        modifier = Modifier.fillMaxSize(),
                        onRetry = { onEvent(WorkerProfileEvent.Retry) },
                    )
                }
                is WorkerProfileUiState.Success -> {
                    PullToRefreshBox(
                        isRefreshing = state.isRefreshing,
                        onRefresh = { onEvent(WorkerProfileEvent.Refresh) },
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        WorkerProfileContent(
                            uiState = state,
                            onEvent = onEvent,
                            onReport = {
                                haptics.tick()
                                confirmReport = true
                            },
                        )
                    }
                }
            }
        }
    }
    if (confirmReport) {
        OficiosDialog(
            title = stringResource(R.string.home_worker_report_confirm_title),
            message = stringResource(R.string.home_worker_report_confirm_body),
            confirmLabel = stringResource(R.string.home_worker_report_confirm_action),
            dismissLabel = stringResource(R.string.home_worker_report_cancel),
            onConfirm = {
                confirmReport = false
                onEvent(WorkerProfileEvent.ReportProfileClicked)
            },
            onDismiss = { confirmReport = false },
        )
    }
}

@Composable
private fun WorkerProfileContent(
    uiState: WorkerProfileUiState.Success,
    onEvent: (WorkerProfileEvent) -> Unit,
    onReport: () -> Unit,
) {
    val context = LocalContext.current
    var viewerIndex by rememberSaveable { mutableStateOf(-1) }
    val photos = uiState.profile.gallery.filter { !it.imageUrl.isNullOrBlank() }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(OficiosSpacing.OficiosScreenPadding),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.screenGap),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        OficiosScreenTitle(
            title = stringResource(R.string.home_worker_profile_title),
            onNavigateBack = { onEvent(WorkerProfileEvent.Back) },
        )
        WorkerProfileHeader(
            profile = uiState.profile,
            onFavoriteClick = { onEvent(WorkerProfileEvent.FavoriteClicked) },
            isFavorite = uiState.isFavorite,
            canFavorite = uiState.canFavorite,
            showRate = true,
            onHireClick = { onEvent(WorkerProfileEvent.HireClicked) },
            onRateClick = { onEvent(WorkerProfileEvent.RateClicked) },
        )
        WorkerGallerySection(
            gallery = uiState.profile.gallery,
            onPhotoClick = { viewerIndex = it },
        )
        WorkerCertificatesSection(
            certificates = uiState.profile.certificates,
            onOpen = { url ->
                runCatching {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            },
        )
        WorkerReviewsSection(
            reviews = uiState.reviews,
            reputation = uiState.profile.reputation,
            isLoadingReviews = uiState.isLoadingReviews,
            hasMoreReviews = uiState.hasMoreReviews,
            onLoadMore = { onEvent(WorkerProfileEvent.LoadNextReviews) },
        )
        OficiosButton(
            text = stringResource(R.string.home_worker_report_profile),
            onClick = onReport,
            variant = OficiosButtonVariant.Primary,
            leadingIcon = ImageVector.vectorResource(OficiosIcons.CircleAlert),
        )
    }
    if (viewerIndex >= 0 && photos.isNotEmpty()) {
        WorkerGalleryViewer(
            photos = photos,
            initialIndex = viewerIndex,
            onClose = { viewerIndex = -1 },
        )
    }
}

@Composable
private fun WorkerProfileSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(OficiosSpacing.OficiosScreenPadding),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.screenGap),
    ) {
        OficiosSkeleton(
            modifier = Modifier
                .fillMaxWidth()
                .height(OficiosSpacing.xxl),
            shape = MaterialTheme.shapes.large,
        )
        repeat(3) {
            OficiosSkeleton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(OficiosSpacing.xxl),
                shape = MaterialTheme.shapes.large,
            )
        }
    }
}

@Preview(name = "Ficha Completa", showBackground = true)
@Composable
private fun WorkerProfilePreview() {
    OficiosTheme {
        WorkerProfileScreen(
            uiState = previewState(withGallery = true, withReviews = true),
            onEvent = {},
            showFavorites = true,
        )
    }
}

@Preview(name = "Ficha vacía", showBackground = true)
@Composable
private fun WorkerProfileEmptyPreview() {
    OficiosTheme {
        WorkerProfileScreen(
            uiState = previewState(withGallery = false, withReviews = false),
            onEvent = {},
            showFavorites = true,
        )
    }
}

internal fun previewState(withGallery: Boolean, withReviews: Boolean): WorkerProfileUiState.Success {
    return WorkerProfileUiState.Success(
        profile = WorkerProfile(
            id = 1L,
            displayName = "Emanuel Molina",
            avatarUrl = null,
            isVerified = true,
            zoneLabel = "Formosa",
            about = "Soy un profesional en el área de construcción, con experiencia como albañil y 8 años de trayectoria. Me especializo en trabajo en general de albañilería construcción en seco, plomería en general, pinturas latex sintético texturados, electricidad. Buen precio y trabajos comprobables .\n\"Siempre dispuesto a ayudar en lo que necesite.\"",
            specialties = listOf(
                WorkerSpecialty(
                    rubroName = "Albañil",
                    subRubroNames = listOf(
                        "Albañilería general",
                        "Reparación de humedad",
                        "Revestimiento de Cerámicos y azulejos",
                    ),
                    servesAtHome = true,
                    servesAtLocal = false,
                    isPrimary = true,
                ),
                WorkerSpecialty(
                    rubroName = "Electricista",
                    subRubroNames = listOf("Iluminación"),
                    servesAtHome = true,
                    servesAtLocal = false,
                    isPrimary = false,
                ),
            ),
            reputation = WorkerReputation(rating = 5f, reviewCount = 1),
            gallery = if (withGallery) {
                listOf(WorkGalleryItem(1L, null), WorkGalleryItem(2L, null))
            } else {
                emptyList()
            },
        ),
        reviews = if (withReviews) {
            listOf(
                WorkerReview(
                    id = 1L,
                    qualityScore = 8,
                    punctualityScore = 5,
                    comment = "Excelente profesional",
                    clientLabel = "María",
                    reviewedAt = "25/02/2025, 08:27:46",
                ),
            )
        } else {
            emptyList()
        },
        canFavorite = true,
        rateBookingId = 99L,
    )
}
