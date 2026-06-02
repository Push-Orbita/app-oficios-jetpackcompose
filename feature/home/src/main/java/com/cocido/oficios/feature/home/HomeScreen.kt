package com.cocido.oficios.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.cards.OficiosCard
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.navigation.OficiosTopAppBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.feature.home.R
import androidx.compose.material3.Icon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    OficiosScaffold(
        modifier = modifier,
        topBar = {
            OficiosTopAppBar(
                title = stringResource(R.string.home_title),
                actions = {
                    IconButton(onClick = { onEvent(HomeEvent.NavigateToSettings) }) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.home_open_settings),
                        )
                    }
                },
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
                HomeContent(
                    uiState = uiState,
                    onEvent = onEvent,
                    modifier = Modifier.padding(paddingValues),
                )
            }
        }
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState.Success,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(OficiosSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.lg),
    ) {
        OficiosCard(
            modifier = Modifier.widthIn(max = OficiosComponentSize.contentMaxWidth),
        ) {
            Text(
                text = uiState.content.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = uiState.content.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = OficiosSpacing.sm),
            )
        }

        OficiosButton(
            text = stringResource(R.string.home_refresh),
            onClick = { onEvent(HomeEvent.LoadContent) },
            modifier = Modifier.widthIn(max = OficiosComponentSize.contentMaxWidth),
        )

        OficiosButton(
            text = stringResource(R.string.home_simulate_error),
            onClick = { onEvent(HomeEvent.SimulateError) },
            modifier = Modifier.widthIn(max = OficiosComponentSize.contentMaxWidth),
            variant = OficiosButtonVariant.Outlined,
        )
    }
}
