package com.cocido.oficios.feature.home.worker

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.cocido.oficios.core.designsystem.component.OficiosDialog
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosSkeleton
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.domain.model.WorkGalleryItem
import com.cocido.oficios.domain.model.WorkerProfile
import com.cocido.oficios.domain.model.WorkerReputation
import com.cocido.oficios.domain.model.WorkerReview
import com.cocido.oficios.domain.model.WorkerSpecialty
import com.cocido.oficios.domain.model.WorkerTimeRange
import com.cocido.oficios.domain.model.WorkerAvailability
import com.cocido.oficios.domain.model.WorkerWorkDay
import com.cocido.oficios.feature.home.R
import com.cocido.oficios.feature.home.worker.components.WorkerAvailabilitySection
import com.cocido.oficios.feature.home.worker.components.WorkerCertificatesSection
import com.cocido.oficios.feature.home.worker.components.WorkerGallerySection
import com.cocido.oficios.feature.home.worker.components.WorkerGalleryViewer
import com.cocido.oficios.feature.home.worker.components.WorkerProfileHeader
import com.cocido.oficios.feature.home.worker.components.WorkerProfileSection
import com.cocido.oficios.feature.home.worker.components.WorkerReviewsSection
import com.cocido.oficios.feature.home.worker.components.WorkerSpecialtiesSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerProfileScreen(
    uiState: WorkerProfileUiState,
    onEvent: (WorkerProfileEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuOpen by rememberSaveable { mutableStateOf(false) }
    var confirmReport by rememberSaveable { mutableStateOf(false) }
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
                            modifier = Modifier.size(OficiosComponentSize.buttonHeight),
                        ) {
                            Icon(
                                imageVector = if (success.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
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
                    if (success != null) {
                        Box {
                            IconButton(
                                onClick = { menuOpen = true },
                                modifier = Modifier.size(OficiosComponentSize.buttonHeight),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = stringResource(R.string.home_worker_more),
                                )
                            }
                            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.home_worker_report_profile)) },
                                    onClick = {
                                        menuOpen = false
                                        confirmReport = true
                                    },
                                )
                            }
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (uiState is WorkerProfileUiState.Success) {
                Surface(color = MaterialTheme.colorScheme.surface) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = OficiosSpacing.md, vertical = OficiosSpacing.sm),
                        horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
                    ) {
                        if (uiState.rateBookingId != null) {
                            OficiosButton(
                                text = stringResource(R.string.home_worker_rate),
                                onClick = { onEvent(WorkerProfileEvent.RateClicked) },
                                modifier = Modifier.weight(1f),
                                variant = OficiosButtonVariant.Outlined,
                            )
                        }
                        OficiosButton(
                            text = stringResource(R.string.home_worker_hire),
                            onClick = { onEvent(WorkerProfileEvent.HireClicked) },
                            modifier = Modifier.weight(2f),
                            variant = OficiosButtonVariant.Secondary,
                            size = OficiosButtonSize.Large,
                        )
                    }
                }
            }
        },
    ) { paddingValues ->
        when (uiState) {
            WorkerProfileUiState.Loading -> WorkerProfileSkeleton(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )
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
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = { onEvent(WorkerProfileEvent.Refresh) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                ) {
                    WorkerProfileContent(uiState = uiState, onEvent = onEvent)
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
) {
    val context = LocalContext.current
    var viewerIndex by rememberSaveable { mutableStateOf(-1) }
    val photos = uiState.profile.gallery.filter { !it.imageUrl.isNullOrBlank() }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = OficiosSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.lg),
    ) {
        WorkerProfileHeader(profile = uiState.profile)
        WorkerSpecialtiesSection(specialties = uiState.profile.specialties)
        WorkerAvailabilitySection(availability = uiState.profile.availability)
        uiState.profile.about?.let { about ->
            AboutSection(about = about)
        }
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
    }
    if (viewerIndex >= 0) {
        WorkerGalleryViewer(
            photos = photos,
            initialIndex = viewerIndex,
            onClose = { viewerIndex = -1 },
        )
    }
}

@Composable
private fun AboutSection(about: String) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val longText = about.length > 140
    WorkerProfileSection(title = stringResource(R.string.home_worker_about_title)) {
        Text(
            text = about,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = if (expanded || !longText) Int.MAX_VALUE else 3,
            overflow = TextOverflow.Ellipsis,
        )
        if (longText) {
            OficiosButton(
                text = stringResource(
                    if (expanded) R.string.home_worker_about_collapse else R.string.home_worker_about_expand,
                ),
                onClick = { expanded = !expanded },
                variant = OficiosButtonVariant.Ghost,
                size = OficiosButtonSize.Small,
            )
        }
    }
}

@Composable
private fun WorkerProfileSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(OficiosSpacing.md),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.lg),
    ) {
        OficiosSkeleton(
            modifier = Modifier
                .fillMaxWidth()
                .height(OficiosSpacing.xxxl),
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

@Preview(name = "Perfil con galería", showBackground = true)
@Composable
private fun WorkerProfilePreview() {
    OficiosTheme {
        WorkerProfileScreen(uiState = previewState(withGallery = true, withReviews = true), onEvent = {})
    }
}

@Preview(name = "Perfil vacío", showBackground = true)
@Composable
private fun WorkerProfileEmptyPreview() {
    OficiosTheme {
        WorkerProfileScreen(uiState = previewState(withGallery = false, withReviews = false), onEvent = {})
    }
}

@Preview(name = "Perfil oscuro", showBackground = true)
@Composable
private fun WorkerProfileDarkPreview() {
    OficiosTheme(darkTheme = true) {
        WorkerProfileScreen(uiState = previewState(withGallery = true, withReviews = false), onEvent = {})
    }
}

private fun previewState(withGallery: Boolean, withReviews: Boolean): WorkerProfileUiState.Success {
    return WorkerProfileUiState.Success(
        profile = WorkerProfile(
            id = 1L,
            displayName = "Ana Gómez",
            avatarUrl = null,
            isVerified = true,
            zoneLabel = "San Francisco, Formosa",
            about = "Arreglo cañerías y destapes en domicilio.",
            specialties = listOf(
                WorkerSpecialty(
                    rubroName = "Plomería",
                    subRubroNames = listOf("Destapes", "Cañerías"),
                    servesAtHome = true,
                    servesAtLocal = false,
                    isPrimary = true,
                ),
            ),
            availability = WorkerAvailability(
                summaryLabel = "Lun - Vie",
                workDays = listOf(
                    WorkerWorkDay(
                        dayStart = "LUNES",
                        dayEnd = "VIERNES",
                        hours = listOf(WorkerTimeRange("09:00", "13:00"), WorkerTimeRange("16:00", "19:00")),
                    ),
                ),
            ),
            reputation = if (withReviews) WorkerReputation(rating = 4.8f, reviewCount = 12) else WorkerReputation(rating = null, reviewCount = 0),
            gallery = if (withGallery) {
                listOf(WorkGalleryItem(1L, "https://example.com/a.jpg"), WorkGalleryItem(2L, "https://example.com/b.jpg"))
            } else {
                emptyList()
            },
        ),
        reviews = if (withReviews) {
            listOf(
                WorkerReview(
                    id = 1L,
                    qualityScore = 9,
                    punctualityScore = 5,
                    comment = "Quedó impecable.",
                    clientLabel = "María",
                    reviewedAt = "2026-03-01T12:00:00Z",
                ),
            )
        } else {
            emptyList()
        },
        canFavorite = true,
    )
}
