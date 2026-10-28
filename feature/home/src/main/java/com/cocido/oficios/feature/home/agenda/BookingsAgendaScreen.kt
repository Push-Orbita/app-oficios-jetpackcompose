package com.cocido.oficios.feature.home.agenda

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosSkeletonList
import com.cocido.oficios.core.designsystem.component.feedback.OficiosStateContent
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.feature.home.R
import com.cocido.oficios.feature.home.providerhome.components.ProviderAgendaSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingsAgendaScreen(
    uiState: BookingsAgendaUiState,
    onEvent: (BookingsAgendaEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OficiosScaffold(
        modifier = modifier,
        topBar = {
            OficiosBackTopBar(
                title = stringResource(R.string.agenda_title),
                onBack = onNavigateBack,
                actions = {
                    IconButton(onClick = { onEvent(BookingsAgendaEvent.OpenSlots) }) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.agenda_configure),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        OficiosStateContent(targetState = uiState, modifier = Modifier.padding(paddingValues)) { state ->
            when (state) {
                BookingsAgendaUiState.Loading -> OficiosSkeletonList(
                    contentDescription = stringResource(R.string.agenda_loading),
                    modifier = Modifier.fillMaxSize(),
                )
                is BookingsAgendaUiState.Error -> PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = { onEvent(BookingsAgendaEvent.Refresh) },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    OficiosErrorState(
                        title = stringResource(R.string.agenda_load_error),
                        description = state.message,
                        onRetry = { onEvent(BookingsAgendaEvent.Retry) },
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    )
                }
                is BookingsAgendaUiState.Success -> PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = { onEvent(BookingsAgendaEvent.Refresh) },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    ProviderAgendaSection(
                        upcomingOrders = state.orders,
                        now = state.now,
                        onViewFullAgenda = {},
                        onOrderClick = { onEvent(BookingsAgendaEvent.OrderClicked(it)) },
                        showTitle = false,
                        showViewFullAgenda = false,
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(OficiosSpacing.OficiosScreenPadding),
                    )
                }
            }
        }
    }
}
