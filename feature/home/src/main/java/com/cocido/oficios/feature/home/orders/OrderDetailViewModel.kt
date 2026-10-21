package com.cocido.oficios.feature.home.orders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.UserRole
import com.cocido.oficios.domain.repository.AuthRepository
import com.cocido.oficios.domain.model.ServiceOrder
import com.cocido.oficios.domain.usecase.OrderUseCases
import com.cocido.oficios.domain.usecase.location.GetCurrentLocationUseCase
import com.cocido.oficios.domain.usecase.worker.GetWorkerReviewsUseCase
import com.cocido.oficios.domain.model.BookingStatus
import com.cocido.oficios.domain.model.ServiceRequestStatus
import com.cocido.oficios.domain.model.withArrivedTracking
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val orderUseCases: OrderUseCases,
    private val authRepository: AuthRepository,
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase,
    private val getWorkerReviewsUseCase: GetWorkerReviewsUseCase,
) : ViewModel() {

    private val orderId: Long = savedStateHandle.get<Long>("orderId") ?: 0L
    private var viewerRole: UserRole = UserRole.UNKNOWN
    private var trackingRefreshJob: Job? = null

    /** Último código fresco obtenido de GET /booking/{id}, para no pisarlo con el del detalle de la solicitud. */
    private var latestCompletionCode: Pair<Long, String>? = null

    private val _uiState = MutableStateFlow<OrderDetailUiState>(OrderDetailUiState.Loading)
    val uiState: StateFlow<OrderDetailUiState> = _uiState.asStateFlow()

    private val _effects = Channel<OrderDetailEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        load()
    }

    fun onEvent(event: OrderDetailEvent) {
        when (event) {
            OrderDetailEvent.Retry -> load(fromRefresh = false)
            OrderDetailEvent.Refresh -> load(fromRefresh = true)
            OrderDetailEvent.Accept -> runAction { orderUseCases.acceptOrder(orderId) }
            is OrderDetailEvent.Reject -> runAction { orderUseCases.rejectOrder(orderId, event.reason) }
            OrderDetailEvent.OpenCounterOffer -> emitEffect(OrderDetailEffect.NavigateToCounterOffer(orderId))
            OrderDetailEvent.AcceptCounterOffer -> {
                val counterOfferId = (uiState.value as? OrderDetailUiState.Success)?.order?.pendingCounterOffer?.id
                if (counterOfferId != null) runAction { orderUseCases.acceptCounterOffer(counterOfferId) }
            }
            OrderDetailEvent.RejectCounterOffer -> {
                val counterOfferId = (uiState.value as? OrderDetailUiState.Success)?.order?.pendingCounterOffer?.id
                if (counterOfferId != null) runAction { orderUseCases.rejectCounterOffer(counterOfferId) }
            }
            OrderDetailEvent.CancelBeforeAcceptance -> runAction { orderUseCases.cancelOrderBeforeAcceptance(orderId) }
            is OrderDetailEvent.CancelConfirmed -> {
                val motivo = event.reason?.trim().orEmpty()
                if (motivo.isEmpty()) {
                    emitEffect(OrderDetailEffect.ShowMessage("Ingrese el motivo de cancelación."))
                    return
                }
                runAction { orderUseCases.cancelConfirmedOrder(orderId, motivo) }
            }
            OrderDetailEvent.OpenWorkerProfile -> {
                val workerId = (uiState.value as? OrderDetailUiState.Success)?.order?.workerId ?: return
                if (workerId != 0L) emitEffect(OrderDetailEffect.NavigateToWorker(workerId))
            }
            OrderDetailEvent.MarkEnRoute -> emitEffect(
                OrderDetailEffect.RequestPreciseLocation(TrackingLocationAction.START_ROUTE),
            )
            OrderDetailEvent.MarkArrived -> emitEffect(
                OrderDetailEffect.RequestPreciseLocation(TrackingLocationAction.ARRIVED),
            )
            is OrderDetailEvent.LocationPermissionGranted -> performLocationAction(event.action)
            is OrderDetailEvent.LocationPermissionDenied -> emitEffect(
                OrderDetailEffect.ShowMessage(
                    "Para compartir el recorrido necesitamos ubicación precisa. Podés habilitarla desde los ajustes del teléfono.",
                ),
            )
            OrderDetailEvent.Finalize -> Unit
            is OrderDetailEvent.FinalizeWithCode -> finalizeWithCode(event.code)
            OrderDetailEvent.ClearCompletionError -> {
                _uiState.update { state ->
                    (state as? OrderDetailUiState.Success)?.copy(completionCodeError = null) ?: state
                }
            }
            OrderDetailEvent.GoToRate -> {
                val state = uiState.value as? OrderDetailUiState.Success ?: return
                val bookingId = state.order.bookingId ?: return
                emitEffect(
                    OrderDetailEffect.NavigateToRate(
                        bookingId = bookingId,
                        workerId = state.order.workerId,
                        workerName = state.order.workerName,
                    ),
                )
            }
        }
    }

    private fun load(fromRefresh: Boolean = false, silent: Boolean = false) {
        viewModelScope.launch {
            val previous = _uiState.value
            try {
                when {
                    silent -> Unit
                    fromRefresh && previous is OrderDetailUiState.Success ->
                        _uiState.update { previous.copy(isRefreshing = true) }
                    fromRefresh && previous is OrderDetailUiState.Error ->
                        _uiState.update { previous.copy(isRefreshing = true) }
                    else -> _uiState.update { OrderDetailUiState.Loading }
                }
                val userResult = authRepository.getCurrentUser()
                viewerRole = (userResult as? AppResult.Success)?.data?.role ?: UserRole.UNKNOWN

                when (val result = orderUseCases.getOrderDetail(orderId)) {
                    is AppResult.Success -> {
                        val loaded = withClientRating(result.data)
                        val tracking = loaded.bookingId?.takeIf {
                            loaded.bookingStatus == BookingStatus.EN_ROUTE || loaded.bookingStatus == BookingStatus.IN_PROGRESS
                        }?.let { bookingId ->
                            (orderUseCases.getBookingTracking(bookingId) as? AppResult.Success)?.data
                        }
                        val order = loaded.withArrivedTracking(tracking?.status).withFreshCompletionCode()
                        _uiState.update { OrderDetailUiState.Success(order, viewerRole, tracking) }
                        startTrackingRefreshIfNeeded(loaded.bookingId, loaded.bookingStatus)
                    }
                    is AppResult.Error -> {
                        val message = result.error.userMessage().ifBlank { "No pudimos cargar el pedido" }
                        if (silent) {
                            // Recarga silenciosa (refresco del código): no molestamos al cliente.
                        } else if (previous is OrderDetailUiState.Success) {
                            _uiState.update { previous.copy(isRefreshing = false) }
                            if (fromRefresh) emitEffect(OrderDetailEffect.ShowMessage(message))
                        } else {
                            _uiState.update { OrderDetailUiState.Error(message) }
                            if (fromRefresh) emitEffect(OrderDetailEffect.ShowMessage(message))
                        }
                    }
                }
            } finally {
                _uiState.update { state ->
                    when (state) {
                        is OrderDetailUiState.Success -> state.copy(isRefreshing = false)
                        is OrderDetailUiState.Error -> state.copy(isRefreshing = false)
                        OrderDetailUiState.Loading -> state
                    }
                }
            }
        }
    }

    /**
     * Mantiene vigente el código de finalización que ve el cliente mientras la pantalla está visible.
     * La Route la llama dentro de `repeatOnLifecycle(STARTED)`: se cancela al salir de la pantalla o
     * pasar a segundo plano, y al volver arranca de nuevo (regenera + GET).
     *
     * - Al entrar/volver: POST completion-code/resend (silencioso) y luego GET /booking/{id}.
     * - Cada [COMPLETION_CODE_FETCH_MILLIS]: GET para mostrar el código vigente.
     * - Cada [COMPLETION_CODE_REGENERATE_MILLIS]: regenera, por debajo de cualquier vencimiento razonable.
     * - 429 (COMPLETION_CODE_RATE_LIMITED) u otros errores del resend se ignoran: el GET trae el vigente.
     */
    suspend fun keepCompletionCodeFresh() {
        uiState
            .filterIsInstance<OrderDetailUiState.Success>()
            .map { it.completionCodeBookingId() }
            .distinctUntilChanged()
            .collectLatest { bookingId ->
                if (bookingId != null) refreshCompletionCodeWhileVisible(bookingId)
            }
    }

    private fun OrderDetailUiState.Success.completionCodeBookingId(): Long? =
        order.bookingId?.takeIf {
            viewerRole == UserRole.CLIENT && order.bookingStatus == BookingStatus.IN_PROGRESS
        }

    private suspend fun refreshCompletionCodeWhileVisible(bookingId: Long) {
        regenerateCompletionCode(bookingId)
        var ticks = 0
        while (true) {
            delay(COMPLETION_CODE_FETCH_MILLIS)
            ticks++
            if (ticks % REGENERATE_EVERY_TICKS == 0) {
                regenerateCompletionCode(bookingId)
            } else {
                fetchCompletionCode(bookingId)
            }
        }
    }

    private suspend fun regenerateCompletionCode(bookingId: Long) {
        // Resultado ignorado a propósito: si hay 429 o falla, el código anterior sigue vigente y el GET lo trae.
        orderUseCases.resendCompletionCode(bookingId)
        fetchCompletionCode(bookingId)
    }

    private suspend fun fetchCompletionCode(bookingId: Long) {
        when (val result = orderUseCases.getCompletionCode(bookingId)) {
            is AppResult.Success -> {
                val code = result.data
                if (code.isNullOrBlank()) {
                    // El backend ya no expone código: el booking cambió de estado (p. ej. finalizado).
                    latestCompletionCode = null
                    load(silent = true)
                } else {
                    latestCompletionCode = bookingId to code
                    _uiState.update { state ->
                        if (state is OrderDetailUiState.Success && state.order.bookingId == bookingId) {
                            state.copy(
                                order = state.order.copy(completionCode = code),
                                completionCodeUnavailable = false,
                            )
                        } else {
                            state
                        }
                    }
                }
            }
            is AppResult.Error -> _uiState.update { state ->
                // Solo avisamos si no hay ningún código para mostrar; si hay uno, lo dejamos.
                if (state is OrderDetailUiState.Success && state.order.completionCode.isNullOrBlank()) {
                    state.copy(completionCodeUnavailable = true)
                } else {
                    state
                }
            }
        }
    }

    private fun ServiceOrder.withFreshCompletionCode(): ServiceOrder {
        val fresh = latestCompletionCode?.takeIf { it.first == bookingId }?.second ?: return this
        if (viewerRole != UserRole.CLIENT || bookingStatus != BookingStatus.IN_PROGRESS) return this
        return copy(completionCode = fresh)
    }

    private fun performLocationAction(action: TrackingLocationAction) {
        val current = uiState.value as? OrderDetailUiState.Success ?: return
        val bookingId = current.order.bookingId ?: return
        viewModelScope.launch {
            _uiState.update { current.copy(isActionInProgress = true) }
            when (val location = getCurrentLocationUseCase(requirePrecise = true)) {
                is AppResult.Error -> {
                    _uiState.update { current.copy(isActionInProgress = false) }
                    emitEffect(OrderDetailEffect.ShowMessage(location.error.userMessage()))
                }
                is AppResult.Success -> {
                    val result = when (action) {
                        TrackingLocationAction.START_ROUTE -> orderUseCases.markEnRoute(bookingId, location.data)
                        TrackingLocationAction.ARRIVED -> orderUseCases.markArrived(bookingId, location.data)
                    }
                    when (result) {
                        is AppResult.Success -> {
                            when (action) {
                                TrackingLocationAction.START_ROUTE -> emitEffect(
                                    OrderDetailEffect.StartForegroundTracking(bookingId),
                                )
                                TrackingLocationAction.ARRIVED -> emitEffect(OrderDetailEffect.StopForegroundTracking)
                            }
                            load()
                        }
                        is AppResult.Error -> {
                            _uiState.update { current.copy(isActionInProgress = false) }
                            emitEffect(OrderDetailEffect.ShowMessage(result.error.userMessage()))
                        }
                    }
                }
            }
        }
    }

    private suspend fun withClientRating(order: ServiceOrder): ServiceOrder {
        if (viewerRole != UserRole.CLIENT || order.bookingStatus != BookingStatus.FINISHED) return order
        val bookingId = order.bookingId ?: return order
        if (order.workerId == 0L) return order
        val reviews = (getWorkerReviewsUseCase(order.workerId, pageNumber = 1, pageSize = 20) as? AppResult.Success)
            ?.data
            ?.items
            ?.firstOrNull { it.bookingId == bookingId }
            ?: return order
        return order.copy(
            alreadyRated = true,
            qualityScore = reviews.qualityScore,
            punctualityScore = reviews.punctualityScore,
        )
    }

    /**
     * Cliente/trabajador: mientras el pedido está pendiente o en contrapropuesta,
     * recarga el detalle cada [STATUS_REFRESH_MILLIS] para ver la confirmación
     * sin push. Se llama desde la Route con `repeatOnLifecycle(STARTED)`.
     */
    suspend fun keepOrderStatusFresh() {
        uiState
            .filterIsInstance<OrderDetailUiState.Success>()
            .map { it.shouldPollStatus() }
            .distinctUntilChanged()
            .collectLatest { shouldPoll ->
                if (!shouldPoll) return@collectLatest
                while (true) {
                    delay(STATUS_REFRESH_MILLIS)
                    load(silent = true)
                }
            }
    }

    private fun OrderDetailUiState.Success.shouldPollStatus(): Boolean =
        order.status == ServiceRequestStatus.PENDING || order.status == ServiceRequestStatus.COUNTER_OFFER

    private fun startTrackingRefreshIfNeeded(bookingId: Long?, status: BookingStatus?) {
        trackingRefreshJob?.cancel()
        if (bookingId == null || status != BookingStatus.EN_ROUTE || viewerRole != UserRole.CLIENT) return
        trackingRefreshJob = viewModelScope.launch {
            while (true) {
                delay(TRACKING_REFRESH_MILLIS)
                when (val result = orderUseCases.getBookingTracking(bookingId)) {
                    is AppResult.Success -> _uiState.update { state ->
                        (state as? OrderDetailUiState.Success)?.copy(tracking = result.data) ?: state
                    }
                    is AppResult.Error -> Unit
                }
            }
        }
    }

    private fun finalizeWithCode(rawCode: String) {
        val current = uiState.value as? OrderDetailUiState.Success ?: return
        val code = rawCode.trim()
        if (code.length != 6) {
            _uiState.update {
                current.copy(completionCodeError = "Ingresá el código de 6 dígitos que ve el cliente en el pedido.")
            }
            return
        }
        val bookingId = current.order.bookingId ?: return
        viewModelScope.launch {
            _uiState.update { current.copy(isActionInProgress = true, completionCodeError = null) }
            when (val result = orderUseCases.finalizeBooking(bookingId, code)) {
                is AppResult.Success -> {
                    emitEffect(OrderDetailEffect.CompletionConfirmed)
                    load()
                }
                is AppResult.Error -> _uiState.update {
                    current.copy(
                        isActionInProgress = false,
                        completionCodeError = result.error.userMessage(),
                    )
                }
            }
        }
    }

    private fun runAction(block: suspend () -> AppResult<Unit>) {
        val current = uiState.value
        if (current !is OrderDetailUiState.Success) return

        viewModelScope.launch {
            _uiState.update { current.copy(isActionInProgress = true) }
            when (val result = block()) {
                is AppResult.Success -> load()
                is AppResult.Error -> {
                    _uiState.update { current.copy(isActionInProgress = false) }
                    emitEffect(OrderDetailEffect.ShowMessage(result.error.userMessage()))
                }
            }
        }
    }

    private fun emitEffect(effect: OrderDetailEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }

    override fun onCleared() {
        trackingRefreshJob?.cancel()
        super.onCleared()
    }

    internal companion object {
        const val STATUS_REFRESH_MILLIS = 12_000L
        const val TRACKING_REFRESH_MILLIS = 15_000L
        const val COMPLETION_CODE_FETCH_MILLIS = 30_000L
        const val COMPLETION_CODE_REGENERATE_MILLIS = 4 * 60_000L
        const val REGENERATE_EVERY_TICKS = (COMPLETION_CODE_REGENERATE_MILLIS / COMPLETION_CODE_FETCH_MILLIS).toInt()
    }
}
