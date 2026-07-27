package com.cocido.oficios.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cocido.oficios.core.designsystem.component.cards.OficiosActionCard
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.domain.model.ClientHomeContent
import com.cocido.oficios.feature.home.components.ClientHomeBottomBar
import com.cocido.oficios.feature.home.components.ClientHomeHeader
import com.cocido.oficios.feature.home.components.ClientHomeTab
import com.cocido.oficios.feature.home.components.HomeFeaturedWorkersSection
import com.cocido.oficios.feature.home.components.HomeRecentSearchesSection
import com.cocido.oficios.feature.home.components.HomeSearchEntryField
import com.cocido.oficios.feature.home.components.HomeSupportSection

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    selectedTab: ClientHomeTab,
    onEvent: (HomeEvent) -> Unit,
    onTabSelected: (ClientHomeTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    OficiosScaffold(
        modifier = modifier,
        bottomBar = {
            ClientHomeBottomBar(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
            )
        },
    ) { paddingValues ->
        when (uiState) {
            HomeUiState.Loading -> {
                OficiosLoadingState(
                    modifier = Modifier.padding(paddingValues),
                    message = stringResource(R.string.home_loading),
                )
            }
            is HomeUiState.Error -> {
                OficiosErrorState(
                    title = stringResource(R.string.home_error_title),
                    description = uiState.message,
                    onRetry = { onEvent(HomeEvent.Retry) },
                    modifier = Modifier.padding(paddingValues),
                )
            }
            is HomeUiState.Success -> {
                HomeSuccessContent(
                    content = uiState.content,
                    onEvent = onEvent,
                    modifier = Modifier.padding(paddingValues),
                )
            }
        }
    }
}

@Composable
private fun HomeSuccessContent(
    content: ClientHomeContent,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = OficiosSpacing.lg, vertical = OficiosSpacing.md),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
    ) {
        ClientHomeHeader(
            userName = content.userDisplayName,
            emailVerified = content.emailVerified,
            profilePhotoUrl = content.profilePhotoUrl,
            onNotificationsClick = { onEvent(HomeEvent.OpenNotifications) },
        )
        HomeSearchEntryField(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onSearch = { onEvent(HomeEvent.SearchSubmitted(searchQuery)) },
            placeholder = stringResource(R.string.home_search_hint),
            onMicClick = { onEvent(HomeEvent.VoiceSearch) },
        )
        OficiosActionCard(
            title = stringResource(R.string.home_my_orders_title),
            description = stringResource(R.string.home_my_orders_description),
            primaryActionLabel = stringResource(R.string.home_my_orders_cta),
            onPrimaryAction = { onEvent(HomeEvent.OpenMyOrders) },
        )
        if (content.featuredWorkers.isNotEmpty()) {
            HomeFeaturedWorkersSection(
                workers = content.featuredWorkers,
                onViewProfile = { onEvent(HomeEvent.WorkerViewProfile(it.id)) },
                onContact = { onEvent(HomeEvent.WorkerContact) },
                onSeeAll = { onEvent(HomeEvent.SeeAllWorkers) },
            )
        }
        if (content.recentSearches.isNotEmpty()) {
            HomeRecentSearchesSection(
                searches = content.recentSearches,
                onSearchClick = { onEvent(HomeEvent.RecentSearchSelected(it)) },
            )
        }
        HomeSupportSection(
            onContactClick = { onEvent(HomeEvent.ContactSupport) },
            modifier = Modifier.padding(top = OficiosSpacing.md),
        )
    }
}
