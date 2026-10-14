package com.cocido.oficios.feature.home.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import com.cocido.oficios.core.designsystem.component.navigation.OficiosTopAppBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.domain.model.FavoriteWorker
import com.cocido.oficios.feature.home.R
import com.cocido.oficios.feature.home.common.ClientHomeTabNavigator
import com.cocido.oficios.feature.home.components.ClientHomeBottomBar
import com.cocido.oficios.feature.home.components.ClientHomeTab
import com.cocido.oficios.feature.home.navigation.SharedHomeDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    uiState: FavoritesUiState,
    onEvent: (FavoritesEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    showBackNavigation: Boolean = true,
    onTabSelected: (ClientHomeTab) -> Unit = {},
) {
    OficiosScaffold(
        modifier = modifier,
        topBar = {
            if (showBackNavigation) {
                OficiosBackTopBar(
                    title = stringResource(R.string.home_nav_favoritos),
                    onBack = onNavigateBack,
                )
            } else {
                OficiosTopAppBar(title = stringResource(R.string.home_nav_favoritos))
            }
        },
        bottomBar = {
            if (!showBackNavigation) {
                ClientHomeBottomBar(
                    selectedTab = ClientHomeTabNavigator.tabForDestination(SharedHomeDestination.favorites),
                    onTabSelected = onTabSelected,
                )
            }
        },
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { onEvent(FavoritesEvent.Refresh) },
            modifier = Modifier.fillMaxSize().padding(paddingValues),
        ) {
        when {
            uiState.isLoading -> OficiosLoadingState(
                modifier = Modifier.fillMaxSize(),
            )
            uiState.errorMessage != null && uiState.items.isEmpty() -> OficiosErrorState(
                title = stringResource(R.string.favorites_load_error),
                description = uiState.errorMessage,
                onRetry = { onEvent(FavoritesEvent.Retry) },
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            )
            uiState.items.isEmpty() -> OficiosEmptyState(
                title = stringResource(R.string.favorites_empty_title),
                description = stringResource(R.string.favorites_empty_description),
                ctaLabel = stringResource(R.string.favorites_empty_cta),
                onCta = { onEvent(FavoritesEvent.SearchClicked) },
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            )
            else -> FavoritesList(
                items = uiState.items,
                hasMore = uiState.hasMore,
                isLoadingMore = uiState.isLoadingMore,
                onHire = { onEvent(FavoritesEvent.HireClicked(it)) },
                onViewProfile = { onEvent(FavoritesEvent.ProfileClicked(it)) },
                onLoadMore = { onEvent(FavoritesEvent.LoadMore) },
                modifier = Modifier.fillMaxSize(),
            )
        }
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
        contentPadding = PaddingValues(horizontal = OficiosSpacing.md, vertical = OficiosSpacing.lg),
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
