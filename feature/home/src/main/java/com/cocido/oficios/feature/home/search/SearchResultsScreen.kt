package com.cocido.oficios.feature.home.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.component.brand.OficiosLogo
import com.cocido.oficios.core.designsystem.component.brand.OficiosLogoVariant
import com.cocido.oficios.core.designsystem.component.cards.WorkerCard
import com.cocido.oficios.core.designsystem.component.feedback.OficiosEmptyState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosEmptyVariant
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosProgress
import com.cocido.oficios.core.designsystem.component.feedback.OficiosSkeletonList
import com.cocido.oficios.core.designsystem.component.feedback.OficiosStateContent
import com.cocido.oficios.core.designsystem.component.motion.oficiosAnimateItem
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosColorSecondaryText
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.domain.model.WorkerSearchFilters
import com.cocido.oficios.feature.home.R
import com.cocido.oficios.feature.home.components.ClientHomeBottomBar
import com.cocido.oficios.feature.home.components.ClientHomeTab
import com.cocido.oficios.feature.home.components.HomeSearchEntryField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultsScreen(
    uiState: SearchResultsUiState,
    filterSheetState: FilterSheetUiState,
    searchQuery: String,
    onEvent: (SearchResultsEvent) -> Unit,
    modifier: Modifier = Modifier,
    selectedTab: ClientHomeTab = ClientHomeTab.INICIO,
    onTabSelected: (ClientHomeTab) -> Unit = {},
    showFavorites: Boolean? = null,
) {
    val searchFocusRequester = remember { FocusRequester() }
    val filters = when (uiState) {
        is SearchResultsUiState.Empty -> uiState.activeFilters
        is SearchResultsUiState.Success -> uiState.activeFilters
        else -> WorkerSearchFilters()
    }
    val activeFilterCount = when (uiState) {
        is SearchResultsUiState.Empty -> uiState.filterChips.count { it.type != FilterChipType.QUERY }
        is SearchResultsUiState.Success -> uiState.filterChips.count { it.type != FilterChipType.QUERY }
        else -> 0
    }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            SearchResultsHeader(
                filters = filters,
                searchQuery = searchQuery,
                searchFocusRequester = searchFocusRequester,
                activeFilterCount = activeFilterCount,
                onEvent = onEvent,
            )
            val isRefreshing = when (uiState) {
                is SearchResultsUiState.Success -> uiState.isRefreshing
                is SearchResultsUiState.Empty -> uiState.isRefreshing
                is SearchResultsUiState.Error -> uiState.isRefreshing
                SearchResultsUiState.Loading -> false
            }
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { onEvent(SearchResultsEvent.Refresh) },
                modifier = Modifier.weight(1f).fillMaxWidth(),
            ) {
                OficiosStateContent(targetState = uiState) { state ->
                    when (state) {
                        SearchResultsUiState.Loading -> {
                            OficiosSkeletonList(
                                contentDescription = stringResource(R.string.home_search_results_loading),
                                contentPadding = PaddingValues(
                                    horizontal = OficiosSpacing.xl,
                                    vertical = OficiosSpacing.sm,
                                ),
                            )
                        }
                        is SearchResultsUiState.Error -> {
                            OficiosErrorState(
                                title = stringResource(R.string.home_error_title),
                                description = state.message,
                                onRetry = { onEvent(SearchResultsEvent.Retry) },
                                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                            )
                        }
                        is SearchResultsUiState.Empty -> {
                            val query = state.activeFilters.query?.takeIf { it.isNotBlank() }
                            val hasRemovableFilters = hasRemovableFilterChips(state.filterChips)
                            val hasQueryOnly = query != null && !hasRemovableFilters
                            OficiosEmptyState(
                                title = stringResource(R.string.home_search_results_empty_title),
                                description = if (query != null) {
                                    stringResource(R.string.home_search_results_empty_query_description, query)
                                } else {
                                    stringResource(R.string.home_search_results_empty_description)
                                },
                                variant = OficiosEmptyVariant.NoResults,
                                ctaLabel = when {
                                    hasRemovableFilters -> stringResource(R.string.home_search_clear_filters)
                                    hasQueryOnly -> stringResource(R.string.home_search_change_query)
                                    else -> null
                                },
                                onCta = when {
                                    hasRemovableFilters -> ({ onEvent(SearchResultsEvent.ClearFilters) })
                                    hasQueryOnly -> ({ searchFocusRequester.requestFocus() })
                                    else -> null
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        is SearchResultsUiState.Success -> {
                            WorkersList(
                                workers = state.workers,
                                isLoadingPage = state.isLoadingPage,
                                endReached = state.endReached,
                                onEvent = onEvent,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                }
            }
        }
    }

    if (filterSheetState.visible) {
        SearchFilterBottomSheet(
            state = filterSheetState,
            onEvent = onEvent,
        )
    }
}

@Composable
private fun SearchResultsHeader(
    filters: WorkerSearchFilters,
    searchQuery: String,
    searchFocusRequester: FocusRequester,
    activeFilterCount: Int,
    onEvent: (SearchResultsEvent) -> Unit,
) {
    val rubroLabel = filters.rubroLabel ?: filters.subRubroLabel ?: filters.categoryLabel
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = OficiosSpacing.xl,
                top = OficiosSpacing.md,
                end = OficiosSpacing.xl,
            ),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.screenGap),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = OficiosComponentSize.searchResultsHeaderMinHeight)
                .semantics { role = Role.Button }
                .clickable(onClick = { onEvent(SearchResultsEvent.Back) }),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.lg),
        ) {
            OficiosLogo(
                variant = OficiosLogoVariant.Icon,
                height = 101.dp,
                contentDescription = stringResource(R.string.home_back),
                modifier = Modifier
                    .width(61.dp)
                    .height(101.dp)
                    .offset(
                        x = OficiosComponentSize.searchResultsLogoOffsetX,
                        y = OficiosComponentSize.searchResultsLogoOffsetY,
                    ),
            )
            Text(
                text = RubroDisplayNames.pluralTitle(rubroLabel),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
        }
        Text(
            text = stringResource(R.string.home_search_contact_limit),
            style = MaterialTheme.typography.bodyLarge,
            color = OficiosColorSecondaryText,
            modifier = Modifier.fillMaxWidth(),
        )
        HomeSearchEntryField(
            query = searchQuery,
            onQueryChange = { onEvent(SearchResultsEvent.QueryChanged(it)) },
            onSearch = { onEvent(SearchResultsEvent.QuerySubmitted) },
            onMicClick = { onEvent(SearchResultsEvent.VoiceSearch) },
            onFilterClick = { onEvent(SearchResultsEvent.OpenFilters) },
            filterContentDescription = if (activeFilterCount > 0) {
                stringResource(R.string.home_search_filters_with_count, activeFilterCount)
            } else {
                stringResource(R.string.home_search_filters)
            },
            activeFilterCount = activeFilterCount,
            placeholder = RubroDisplayNames.searchHint(rubroLabel),
            placeholderMaxLines = 2,
            modifier = Modifier.focusRequester(searchFocusRequester),
        )
    }
}

@Composable
private fun WorkersList(
    workers: List<com.cocido.oficios.domain.model.WorkerSummary>,
    isLoadingPage: Boolean,
    endReached: Boolean,
    onEvent: (SearchResultsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= workers.size - 3 && workers.isNotEmpty() && !endReached
        }
    }

    LaunchedEffect(shouldLoadMore, isLoadingPage) {
        if (shouldLoadMore && !isLoadingPage) {
            onEvent(SearchResultsEvent.LoadNextPage)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = OficiosSpacing.xl,
            end = OficiosSpacing.xl,
            top = OficiosComponentSize.searchResultsListTop,
            bottom = OficiosSpacing.xl,
        ),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
    ) {
        items(workers, key = { it.id }) { worker ->
            WorkerCard(
                name = worker.displayName,
                specialty = worker.specialtyLabel,
                rating = worker.rating,
                avatarUrl = worker.avatarUrl,
                isVerified = worker.isVerified,
                contactLabel = stringResource(R.string.home_search_worker_contact),
                viewProfileLabel = stringResource(R.string.home_search_worker_view_ficha),
                onContact = { onEvent(SearchResultsEvent.WorkerContact(worker)) },
                onViewProfile = { onEvent(SearchResultsEvent.WorkerViewProfile(worker)) },
                modifier = oficiosAnimateItem(),
            )
        }
        if (isLoadingPage) {
            item(key = "loading_page") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(OficiosSpacing.md),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    OficiosProgress()
                }
            }
        }
    }
}
