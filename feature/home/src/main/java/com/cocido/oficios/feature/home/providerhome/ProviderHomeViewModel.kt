package com.cocido.oficios.feature.home.providerhome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.ProviderHomeTiles
import com.cocido.oficios.domain.model.ProviderProfileCompletionTarget
import com.cocido.oficios.domain.model.nowInArgentina
import com.cocido.oficios.domain.model.upcomingAgendaAppointments
import com.cocido.oficios.domain.usecase.GetProviderHomeUseCase
import com.cocido.oficios.domain.usecase.OrderUseCases
import com.cocido.oficios.domain.usecase.notifications.GetUnreadNotificationCountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProviderHomeViewModel @Inject constructor(
    private val getProviderHomeUseCase: GetProviderHomeUseCase,
    private val getUnreadNotificationCountUseCase: GetUnreadNotificationCountUseCase,
    private val orderUseCases: OrderUseCases,
    private val clock: Clock,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProviderHomeUiState>(ProviderHomeUiState.Loading)
    val uiState: StateFlow<ProviderHomeUiState> = _uiState.asStateFlow()

    private val _unreadNotificationCount = MutableStateFlow(0)
    val unreadNotificationCount: StateFlow<Int> = _unreadNotificationCount.asStateFlow()

    private val _effects = Channel<ProviderHomeEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            getUnreadNotificationCountUseCase.unreadCount.collect { count ->
                if (count != null) _unreadNotificationCount.value = count
            }
        }
        viewModelScope.launch {
            orderUseCases.workerOrders.collect { list ->
                if (list == null) return@collect
                val now = nowInArgentina(clock)
                _uiState.update { state ->
                    val success = state as? ProviderHomeUiState.Success ?: return@update state
                    success.copy(
                        content = success.content.copy(
                            upcomingOrders = upcomingAgendaAppointments(list, now),
                        ),
                        now = now,
                    )
                }
            }
        }
        loadHome()
        refreshUnreadCount()
    }

    fun onVisible() {
        refreshUnreadCount()
        if (_uiState.value is ProviderHomeUiState.Loading) return
        loadHome(refreshing = true, silent = true)
    }

    fun onEvent(event: ProviderHomeEvent) {
        when (event) {
            ProviderHomeEvent.Retry -> loadHome()
            ProviderHomeEvent.Refresh -> loadHome(refreshing = true)
            ProviderHomeEvent.OpenNotifications -> emitEffect(ProviderHomeEffect.NavigateToNotifications)
            ProviderHomeEvent.GalleryTileClicked -> emitEffect(ProviderHomeEffect.NavigateToGallery)
            ProviderHomeEvent.CommercialLocalTileClicked -> emitEffect(ProviderHomeEffect.NavigateToCommercialLocal)
            ProviderHomeEvent.CertificateTileClicked -> emitEffect(ProviderHomeEffect.NavigateToCertificate)
            ProviderHomeEvent.CompleteProfileLinkClicked -> navigateToFirstIncomplete()
            ProviderHomeEvent.ViewFullAgendaClicked -> emitEffect(ProviderHomeEffect.NavigateToAgenda)
            ProviderHomeEvent.ConfigureAgendaClicked -> emitEffect(ProviderHomeEffect.NavigateToAgendaConfig)
            is ProviderHomeEvent.OrderClicked -> emitEffect(ProviderHomeEffect.NavigateToOrderDetail(event.orderId))
            ProviderHomeEvent.NavigateToSettings -> emitEffect(ProviderHomeEffect.NavigateToSettings)
            ProviderHomeEvent.NavigateToFavorites -> emitEffect(ProviderHomeEffect.NavigateToFavorites)
            ProviderHomeEvent.NavigateToHelp -> emitEffect(ProviderHomeEffect.NavigateToHelp)
        }
    }

    private fun navigateToFirstIncomplete() {
        val content = (_uiState.value as? ProviderHomeUiState.Success)?.content ?: return
        val target = ProviderHomeTiles(
            gallery = content.hasGalleryPhotos,
            local = content.hasCommercialLocal,
            certificate = content.hasCertificate,
        ).firstIncompleteTarget()
        when (target) {
            ProviderProfileCompletionTarget.GALLERY -> emitEffect(ProviderHomeEffect.NavigateToGallery)
            ProviderProfileCompletionTarget.COMMERCIAL_LOCAL -> emitEffect(ProviderHomeEffect.NavigateToCommercialLocal)
            ProviderProfileCompletionTarget.CERTIFICATE -> emitEffect(ProviderHomeEffect.NavigateToCertificate)
            null -> Unit
        }
    }

    fun refreshUnreadCount() {
        viewModelScope.launch {
            when (val result = getUnreadNotificationCountUseCase()) {
                is AppResult.Success -> _unreadNotificationCount.update { result.data }
                is AppResult.Error -> Unit
            }
        }
    }

    private fun loadHome(refreshing: Boolean = false, silent: Boolean = false) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val previous = _uiState.value
            try {
                if (refreshing && previous is ProviderHomeUiState.Success) {
                    if (!silent) _uiState.update { previous.copy(isRefreshing = true) }
                } else if (refreshing && previous is ProviderHomeUiState.Error) {
                    if (!silent) _uiState.update { previous.copy(isRefreshing = true) }
                } else if (!silent) {
                    _uiState.update { ProviderHomeUiState.Loading }
                }
                when (val result = getProviderHomeUseCase()) {
                    is AppResult.Success -> {
                        val now = nowInArgentina(clock)
                        val agenda = upcomingAgendaAppointments(result.data.upcomingOrders, now)
                        _uiState.update {
                            ProviderHomeUiState.Success(
                                content = result.data.copy(upcomingOrders = agenda),
                                now = now,
                            )
                        }
                    }
                    is AppResult.Error -> {
                        val message = result.error.userMessage()
                        if (previous is ProviderHomeUiState.Success) {
                            _uiState.update { previous.copy(isRefreshing = false) }
                            if (!silent) emitEffect(ProviderHomeEffect.ShowMessage(message))
                        } else {
                            _uiState.update { ProviderHomeUiState.Error(message) }
                            if (refreshing && !silent) emitEffect(ProviderHomeEffect.ShowMessage(message))
                        }
                    }
                }
            } finally {
                _uiState.update { state ->
                    when (state) {
                        is ProviderHomeUiState.Success -> state.copy(isRefreshing = false)
                        is ProviderHomeUiState.Error -> state.copy(isRefreshing = false)
                        ProviderHomeUiState.Loading -> state
                    }
                }
            }
        }
    }

    private fun emitEffect(effect: ProviderHomeEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}
