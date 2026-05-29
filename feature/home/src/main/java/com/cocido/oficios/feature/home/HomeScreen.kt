package com.cocido.oficios.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cocido.oficios.core.designsystem.component.AppButton
import com.cocido.oficios.core.designsystem.component.AppCard
import com.cocido.oficios.core.designsystem.component.AppErrorState
import com.cocido.oficios.core.designsystem.component.AppLoading
import com.cocido.oficios.core.designsystem.component.AppOutlinedButton
import com.cocido.oficios.core.designsystem.component.AppScaffold
import com.cocido.oficios.core.designsystem.component.AppTopBar
import com.cocido.oficios.core.designsystem.theme.AppDimens
import com.cocido.oficios.core.designsystem.theme.AppSpacing
import com.cocido.oficios.feature.home.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppScaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.home_title),
                actionIcon = Icons.Outlined.Settings,
                onActionClick = { onEvent(HomeEvent.NavigateToSettings) },
                actionContentDescription = stringResource(R.string.home_open_settings),
            )
        },
    ) { paddingValues ->
        when (uiState) {
            HomeUiState.Loading -> {
                AppLoading(
                    modifier = Modifier.padding(paddingValues),
                    message = stringResource(R.string.home_loading),
                )
            }
            is HomeUiState.Error -> {
                AppErrorState(
                    message = uiState.message,
                    onRetry = { onEvent(HomeEvent.Retry) },
                    modifier = Modifier.padding(paddingValues),
                    retryLabel = stringResource(R.string.home_retry),
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
            .padding(AppSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        AppCard(
            modifier = Modifier.widthIn(max = AppDimens.contentMaxWidth),
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
                modifier = Modifier.padding(top = AppSpacing.sm),
            )
        }

        AppButton(
            text = stringResource(R.string.home_refresh),
            onClick = { onEvent(HomeEvent.LoadContent) },
            modifier = Modifier.widthIn(max = AppDimens.contentMaxWidth),
        )

        AppOutlinedButton(
            text = stringResource(R.string.home_simulate_error),
            onClick = { onEvent(HomeEvent.SimulateError) },
            modifier = Modifier.widthIn(max = AppDimens.contentMaxWidth),
        )
    }
}
