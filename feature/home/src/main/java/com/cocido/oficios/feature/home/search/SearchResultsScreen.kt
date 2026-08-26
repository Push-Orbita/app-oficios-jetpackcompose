package com.cocido.oficios.feature.home.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cocido.oficios.core.designsystem.component.cards.WorkerCard
import com.cocido.oficios.core.designsystem.component.feedback.OficiosEmptyState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.feature.home.R
import com.cocido.oficios.feature.home.components.HomeSearchEntryField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultsScreen(
    uiState: SearchResultsUiState,
    filterSheetState: FilterSheetUiState,
    searchQuery: String,
    onEvent: (SearchResultsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    OficiosScaffold(modifier = modifier) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            SearchResultsHeader(
                searchQuery = searchQuery,
                activeFilterCount = when (uiState) {
                    is SearchResultsUiState.Empty -> uiState.filterChips.size
                    is SearchResultsUiState.Success -> uiState.filterChips.size
                    else -> 0
                },
                onEvent = onEvent,
            )
            when (uiState) {
                SearchResultsUiState.Loading -> {
                    OficiosLoadingState(
                        message = stringResource(R.string.home_search_results_loading),
                    )
                }
                is SearchResultsUiState.Error -> {
                    OficiosErrorState(
                        title = stringResource(R.string.home_error_title),
                        description = uiState.message,
                        onRetry = { onEvent(SearchResultsEvent.Retry) },
                    )
                }
                is SearchResultsUiState.Empty -> {
                    ActiveFilterChips(
                        chips = uiState.filterChips,
                        onRemove = { onEvent(SearchResultsEvent.RemoveFilterChip(it)) },
                    )
                    val query = uiState.activeFilters.query?.takeIf { it.isNotBlank() }
                    OficiosEmptyState(
                        title = stringResource(R.string.home_search_results_empty_title),
                        description = if (query != null) {
                            stringResource(R.string.home_search_results_empty_query_description, query)
                        } else {
                            stringResource(R.string.home_search_results_empty_description)
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
                is SearchResultsUiState.Success -> {
                    ActiveFilterChips(
                        chips = uiState.filterChips,
                        onRemove = { onEvent(SearchResultsEvent.RemoveFilterChip(it)) },
                    )
                    WorkersList(
                        workers = uiState.workers,
                        isLoadingPage = uiState.isLoadingPage,
                        endReached = uiState.endReached,
                        onEvent = onEvent,
                        modifier = Modifier.weight(1f),
                    )
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
    searchQuery: String,
    activeFilterCount: Int,
    onEvent: (SearchResultsEvent) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = OficiosSpacing.lg, vertical = OficiosSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
    ) {
        IconButton(onClick = { onEvent(SearchResultsEvent.Back) }) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.home_back),
            )
        }
        HomeSearchEntryField(
            query = searchQuery,
            onQueryChange = { onEvent(SearchResultsEvent.QueryChanged(it)) },
            onSearch = { onEvent(SearchResultsEvent.QuerySubmitted) },
            placeholder = stringResource(R.string.home_search_hint),
            onMicClick = { onEvent(SearchResultsEvent.VoiceSearch) },
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = { onEvent(SearchResultsEvent.OpenFilters) }) {
            BadgedBox(
                badge = {
                    if (activeFilterCount > 0) {
                        Badge { Text(activeFilterCount.toString()) }
                    }
                },
            ) {
                Icon(
                    imageVector = Icons.Outlined.FilterList,
                    contentDescription = if (activeFilterCount > 0) {
                        stringResource(R.string.home_search_filters_with_count, activeFilterCount)
                    } else {
                        stringResource(R.string.home_search_filters)
                    },
                )
            }
        }
    }
}

@Composable
private fun ActiveFilterChips(
    chips: List<FilterChip>,
    onRemove: (FilterChip) -> Unit,
) {
    if (chips.isEmpty()) return
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = OficiosSpacing.lg),
        userScrollEnabled = false,
    ) {
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
                modifier = Modifier.padding(bottom = OficiosSpacing.sm),
            ) {
                chips.forEach { chip ->
                    FilterChip(
                        selected = true,
                        onClick = { onRemove(chip) },
                        label = { Text(chip.label) },
                    )
                }
            }
        }
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
            horizontal = OficiosSpacing.lg,
            vertical = OficiosSpacing.sm,
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
                contactLabel = stringResource(R.string.home_worker_contact),
                viewProfileLabel = stringResource(R.string.home_worker_view_profile),
                onContact = { onEvent(SearchResultsEvent.WorkerContact) },
                onViewProfile = { onEvent(SearchResultsEvent.WorkerViewProfile(worker)) },
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
                    CircularProgressIndicator()
                }
            }
        }
    }
}
