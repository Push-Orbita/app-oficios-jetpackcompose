package com.cocido.oficios.feature.home.rubros

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.feature.home.R
import com.cocido.oficios.feature.home.components.HomeRubrosGrid
import com.cocido.oficios.feature.home.components.HomeSearchEntryField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RubrosScreen(
    uiState: RubrosUiState,
    onEvent: (RubrosEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    OficiosScaffold(modifier = modifier) { paddingValues ->
        when (uiState) {
            RubrosUiState.Loading -> {
                OficiosLoadingState(
                    modifier = Modifier.padding(paddingValues),
                    message = stringResource(R.string.home_rubros_loading),
                )
            }
            is RubrosUiState.Error -> {
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = { onEvent(RubrosEvent.Refresh) },
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                ) {
                    OficiosErrorState(
                        title = stringResource(R.string.home_error_title),
                        description = uiState.message,
                        onRetry = { onEvent(RubrosEvent.Retry) },
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    )
                }
            }
            is RubrosUiState.Success -> PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { onEvent(RubrosEvent.Refresh) },
                modifier = Modifier.fillMaxSize().padding(paddingValues),
            ) {
                var searchQuery by rememberSaveable { mutableStateOf("") }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = OficiosSpacing.lg, vertical = OficiosSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = { onEvent(RubrosEvent.Back) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = stringResource(R.string.home_back),
                                modifier = Modifier.size(OficiosSpacing.lg),
                            )
                        }
                        Text(
                            text = uiState.categoryName,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(start = OficiosSpacing.xs),
                        )
                    }
                    HomeSearchEntryField(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onSearch = { onEvent(RubrosEvent.SearchSubmitted(searchQuery)) },
                        placeholder = stringResource(R.string.home_search_rubros_hint),
                    )
                    HomeRubrosGrid(
                        rubros = uiState.rubros,
                        onRubroClick = { onEvent(RubrosEvent.RubroSelected(it)) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}
