package com.cocido.oficios.feature.home.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.component.avatar.OficiosAvatar
import com.cocido.oficios.core.designsystem.component.badges.OficiosVerifiedBadge
import com.cocido.oficios.core.designsystem.component.categories.CategoryIconResolver
import com.cocido.oficios.core.designsystem.component.chips.OficiosChip
import com.cocido.oficios.core.designsystem.component.chips.OficiosFilterChip
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.domain.model.RecentSearchEntry
import com.cocido.oficios.domain.model.WorkerSummary
import com.cocido.oficios.feature.home.R

@Composable
fun SearchIdleContent(
    state: SearchResultsUiState.Idle,
    onEvent: (SearchResultsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = OficiosSpacing.xl, vertical = OficiosSpacing.md),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
    ) {
        if (state.suggestions.isNotEmpty()) {
            IdleSectionTitle(stringResource(R.string.home_search_suggestions_title))
            state.suggestions.forEach { suggestion ->
                SuggestionRow(suggestion = suggestion, onClick = { onEvent(SearchResultsEvent.SuggestionSelected(suggestion)) })
            }
        }
        if (state.recentSearches.isNotEmpty()) {
            IdleSectionTitle(stringResource(R.string.home_search_recent_queries_title))
            state.recentSearches.forEach { entry ->
                RecentQueryRow(
                    entry = entry,
                    onClick = { onEvent(SearchResultsEvent.RecentSearchClicked(entry)) },
                    onRemove = { onEvent(SearchResultsEvent.RemoveRecentSearch(entry)) },
                )
            }
        }
        if (state.recentWorkers.isNotEmpty()) {
            IdleSectionTitle(stringResource(R.string.home_search_recent_workers_title))
            state.recentWorkers.forEach { worker ->
                RecentWorkerRow(
                    worker = worker,
                    onClick = { onEvent(SearchResultsEvent.RecentWorkerClicked(worker)) },
                )
            }
        }
        if (state.rubroChips.isNotEmpty()) {
            IdleSectionTitle(stringResource(R.string.home_search_rubros_title))
            RubroChipsRow(
                rubros = state.rubroChips,
                onRubroClick = { onEvent(SearchResultsEvent.RubroChipSelected(it)) },
            )
        }
        if (state.suggestions.isEmpty() &&
            state.recentSearches.isEmpty() &&
            state.recentWorkers.isEmpty() &&
            state.rubroChips.isEmpty()
        ) {
            Text(
                text = stringResource(R.string.home_search_idle_empty_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = stringResource(R.string.home_search_idle_empty_description),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun IdleSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RubroChipsRow(
    rubros: List<CatalogOption>,
    onRubroClick: (CatalogOption) -> Unit,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
    ) {
        rubros.forEach { rubro ->
            OficiosFilterChip(
                label = rubro.label,
                selected = false,
                iconUrl = rubro.iconUrl,
                leadingIcon = CategoryIconResolver.iconFor(rubro.id, rubro.label),
                onToggle = { onRubroClick(rubro) },
            )
        }
    }
}

@Composable
private fun SuggestionRow(
    suggestion: SearchSuggestion,
    onClick: () -> Unit,
) {
    val kindLabel = when (suggestion.kind) {
        SearchSuggestionKind.RUBRO -> stringResource(R.string.home_search_suggestion_rubro)
        SearchSuggestionKind.SUBRUBRO -> stringResource(R.string.home_search_suggestion_subrubro)
        SearchSuggestionKind.WORKER -> suggestion.subtitle.orEmpty()
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = OficiosSpacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
    ) {
        if (suggestion.worker != null) {
            OficiosAvatar(
                name = suggestion.label,
                imageUrl = suggestion.worker.avatarUrl,
                size = OficiosComponentSize.avatarSm,
                circular = true,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = suggestion.label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (kindLabel.isNotBlank()) {
                Text(
                    text = kindLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (suggestion.worker?.isVerified == true) {
            OficiosVerifiedBadge()
        }
    }
}

@Composable
private fun RecentQueryRow(
    entry: RecentSearchEntry,
    onClick: () -> Unit,
    onRemove: () -> Unit,
) {
    val removeLabel = stringResource(R.string.home_search_remove_recent, entry.label)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
    ) {
        Text(
            text = entry.label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onClick)
                .padding(vertical = OficiosSpacing.xxs),
        )
        Icon(
            imageVector = ImageVector.vectorResource(OficiosIcons.X),
            contentDescription = removeLabel,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .semantics { role = Role.Button; contentDescription = removeLabel }
                .clickable(onClick = onRemove),
        )
    }
}

@Composable
private fun RecentWorkerRow(
    worker: WorkerSummary,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = OficiosSpacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
    ) {
        OficiosAvatar(
            name = worker.displayName,
            imageUrl = worker.avatarUrl,
            size = OficiosComponentSize.avatarSm,
            circular = true,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = worker.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (worker.specialtyLabel.isNotBlank()) {
                Text(
                    text = worker.specialtyLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (worker.isVerified) {
            OficiosVerifiedBadge()
        }
    }
}

@Composable
fun ActiveSearchFiltersRow(
    chips: List<FilterChip>,
    resultCount: Int?,
    onEvent: (SearchResultsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (chips.isEmpty() && resultCount == null) return
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
    ) {
        if (chips.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                chips.forEach { chip ->
                    OficiosChip(
                        label = chip.label,
                        onClick = { onEvent(SearchResultsEvent.RemoveFilterChip(chip)) },
                        leadingIcon = ImageVector.vectorResource(OficiosIcons.X),
                    )
                }
                if (hasRemovableFilterChips(chips)) {
                    OficiosChip(
                        label = stringResource(R.string.home_search_clear_all),
                        onClick = { onEvent(SearchResultsEvent.ClearFilters) },
                    )
                }
            }
        }
        if (resultCount != null) {
            Text(
                text = stringResource(R.string.home_search_results_count, resultCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
