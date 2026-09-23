package com.cocido.oficios.feature.home.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.cards.WorkerCard
import com.cocido.oficios.core.designsystem.component.feedback.OficiosEmptyState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.domain.model.FavoriteWorker
import com.cocido.oficios.feature.home.R

@Composable
fun FavoritesScreen(
    uiState: FavoritesUiState,
    onEvent: (FavoritesEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OficiosScaffold(
        modifier = modifier,
        topBar = {
            OficiosBackTopBar(
                title = stringResource(R.string.home_nav_favoritos),
                onBack = onNavigateBack,
            )
        },
    ) { paddingValues ->
        when {
            uiState.isLoading -> OficiosLoadingState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )
            uiState.errorMessage != null && uiState.items.isEmpty() -> OficiosErrorState(
                title = stringResource(R.string.favorites_load_error),
                description = uiState.errorMessage,
                onRetry = { onEvent(FavoritesEvent.Retry) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )
            uiState.items.isEmpty() -> OficiosEmptyState(
                title = stringResource(R.string.favorites_empty_title),
                description = stringResource(R.string.favorites_empty_description),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )
            else -> FavoritesList(
                items = uiState.items,
                hasMore = uiState.hasMore,
                isLoadingMore = uiState.isLoadingMore,
                onHire = { onEvent(FavoritesEvent.HireClicked(it)) },
                onViewProfile = { onEvent(FavoritesEvent.ProfileClicked(it)) },
                onLoadMore = { onEvent(FavoritesEvent.LoadMore) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )
        }
    }
}

@Composable
private fun FavoritesList(
    items: List<FavoriteWorker>,
    hasMore: Boolean,
    isLoadingMore: Boolean,
    onHire: (Long) -> Unit,
    onViewProfile: (Long) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(OficiosSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
    ) {
        items(items, key = { it.favoriteId }) { worker ->
            WorkerCard(
                name = worker.displayName,
                specialty = worker.specialtyLabel.ifBlank { stringResource(R.string.favorites_specialty_fallback) },
                rating = worker.rating,
                avatarUrl = worker.avatarUrl,
                isVerified = worker.isVerified,
                contactLabel = stringResource(R.string.home_worker_hire),
                viewProfileLabel = stringResource(R.string.home_worker_view_profile),
                onContact = { onHire(worker.workerId) },
                onViewProfile = { onViewProfile(worker.workerId) },
            )
        }
        if (hasMore) {
            item(key = "load_more") {
                OficiosButton(
                    text = stringResource(R.string.favorites_load_more),
                    onClick = onLoadMore,
                    variant = OficiosButtonVariant.Ghost,
                    loading = isLoadingMore,
                    enabled = !isLoadingMore,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
