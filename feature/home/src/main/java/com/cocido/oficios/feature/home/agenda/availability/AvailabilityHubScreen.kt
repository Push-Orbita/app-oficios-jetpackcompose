package com.cocido.oficios.feature.home.agenda.availability

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.cards.OficiosCard
import com.cocido.oficios.core.designsystem.component.feedback.OficiosEmptyState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosInlineAlert
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosStateContent
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.ui.lifecycle.RefreshOnResume
import com.cocido.oficios.domain.model.ProviderSubRubroOption
import com.cocido.oficios.domain.usecase.GetMySubRubrosUseCase
import com.cocido.oficios.feature.home.R
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AvailabilityHubUiState(
    val isLoading: Boolean = true,
    /** Pull-to-refresh en curso; la lista sigue visible. */
    val isRefreshing: Boolean = false,
    /** Error de carga inicial (pantalla de error con reintento). */
    val errorMessage: String? = null,
    /** Error al refrescar con contenido ya visible: aviso inline, no tapa la lista. */
    val refreshErrorMessage: String? = null,
    val specialties: List<ProviderSubRubroOption> = emptyList(),
)

@HiltViewModel
class AvailabilityHubViewModel @Inject constructor(
    private val getMySubRubrosUseCase: GetMySubRubrosUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AvailabilityHubUiState())
    val uiState = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var skipNextResume = true

    init { load() }

    /** ON_RESUME: el primero coincide con el init, así que se saltea. */
    fun onResume() {
        if (skipNextResume) {
            skipNextResume = false
            return
        }
        load()
    }

    fun refresh() = load(userInitiated = true)

    /**
     * Stale-while-revalidate: si ya hay especialidades en pantalla, recarga sin spinner de
     * pantalla completa (antes cada vuelta a la pantalla la tapaba con "Cargando").
     */
    fun load(userInitiated: Boolean = false) {
        if (loadJob?.isActive == true && !userInitiated) return
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val hasContent = !_uiState.value.isLoading && _uiState.value.errorMessage == null
            _uiState.update {
                if (hasContent) {
                    it.copy(isRefreshing = userInitiated, refreshErrorMessage = null)
                } else {
                    it.copy(isLoading = true, errorMessage = null)
                }
            }
            when (val result = getMySubRubrosUseCase()) {
                is AppResult.Success -> _uiState.value = AvailabilityHubUiState(
                    isLoading = false,
                    specialties = result.data,
                )
                is AppResult.Error -> _uiState.update {
                    if (hasContent) {
                        it.copy(isRefreshing = false, refreshErrorMessage = result.error.userMessage())
                    } else {
                        AvailabilityHubUiState(isLoading = false, errorMessage = result.error.userMessage())
                    }
                }
            }
        }
    }
}

@Composable
fun AvailabilityHubRoute(
    onNavigateBack: () -> Unit,
    onOpenWeek: (Long) -> Unit,
    onOpenBlocks: (Long) -> Unit,
    viewModel: AvailabilityHubViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    RefreshOnResume(viewModel::onResume)
    AvailabilityHubScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onRetry = { viewModel.load() },
        onRefresh = viewModel::refresh,
        onOpenWeek = onOpenWeek,
        onOpenBlocks = onOpenBlocks,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailabilityHubScreen(
    uiState: AvailabilityHubUiState,
    onNavigateBack: () -> Unit,
    onRetry: () -> Unit,
    onOpenWeek: (Long) -> Unit,
    onOpenBlocks: (Long) -> Unit,
    onRefresh: () -> Unit = {},
) {
    OficiosScaffold(
        topBar = {
            OficiosBackTopBar(
                title = stringResource(R.string.agenda_availability_hub_title),
                onBack = onNavigateBack,
            )
        },
    ) { padding ->
        OficiosStateContent(
            targetState = uiState,
            modifier = Modifier.padding(padding),
            contentKey = { if (it.isLoading) 0 else if (it.errorMessage != null) 1 else 2 },
        ) { state ->
            when {
                state.isLoading -> OficiosLoadingState(
                    message = stringResource(R.string.agenda_availability_loading),
                    modifier = Modifier.fillMaxSize(),
                )
                state.errorMessage != null -> OficiosErrorState(
                    title = stringResource(R.string.agenda_availability_error),
                    description = state.errorMessage,
                    onRetry = onRetry,
                    modifier = Modifier.fillMaxSize(),
                )
                state.specialties.isEmpty() -> OficiosEmptyState(
                    title = stringResource(R.string.agenda_config_empty_title),
                    description = stringResource(R.string.agenda_config_empty_description),
                    icon = Icons.Outlined.EventBusy,
                    modifier = Modifier.fillMaxSize(),
                )
                else -> PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize(),
                ) { LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = OficiosSpacing.OficiosScreenPaddingAfterTopBar,
                    verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
                ) {
                    state.refreshErrorMessage?.let { message -> item { OficiosInlineAlert(message) } }
                    item {
                        Text(
                            text = stringResource(R.string.agenda_availability_hub_intro),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    items(state.specialties, key = { it.id }) { specialty ->
                        OficiosCard(modifier = Modifier.fillMaxWidth()) {
                            Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm)) {
                                Text(
                                    text = specialty.subRubroLabel,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                if (specialty.rubroLabel.isNotBlank()) {
                                    Text(
                                        text = specialty.rubroLabel,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                OficiosButton(
                                    text = stringResource(R.string.agenda_availability_open_week),
                                    onClick = { onOpenWeek(specialty.id) },
                                    modifier = Modifier.fillMaxWidth(),
                                    variant = OficiosButtonVariant.Primary,
                                    size = OficiosButtonSize.Form,
                                )
                                OficiosButton(
                                    text = stringResource(R.string.agenda_availability_open_blocks),
                                    onClick = { onOpenBlocks(specialty.id) },
                                    modifier = Modifier.fillMaxWidth(),
                                    variant = OficiosButtonVariant.Outlined,
                                    size = OficiosButtonSize.Form,
                                )
                            }
                        }
                    }
                } }
            }
        }
    }
}
