package com.cocido.oficios.feature.home.worker.report

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.usecase.safety.GetReportReasonsUseCase
import com.cocido.oficios.domain.usecase.safety.SubmitReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class WorkerReportViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getReportReasonsUseCase: GetReportReasonsUseCase,
    private val submitReportUseCase: SubmitReportUseCase,
) : ViewModel() {

    private val workerId: Long = savedStateHandle.get<Long>("workerId") ?: 0L
    private val workerName: String = savedStateHandle.get<String>("workerName")
        .orEmpty()
        .ifBlank { "Trabajador" }

    private val _uiState = MutableStateFlow(
        WorkerReportUiState(workerName = workerName, isLoadingReasons = true),
    )
    val uiState: StateFlow<WorkerReportUiState> = _uiState.asStateFlow()

    private val _effects = Channel<WorkerReportEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var submitIdempotencyKey: String? = null

    init {
        loadReasons()
    }

    fun onEvent(event: WorkerReportEvent) {
        when (event) {
            WorkerReportEvent.Retry -> loadReasons()
            is WorkerReportEvent.ReasonSelected -> {
                submitIdempotencyKey = null
                val code = _uiState.value.reasons.firstOrNull { it.label == event.label }?.code
                _uiState.update { it.copy(selectedReasonCode = code, errorMessage = null) }
            }
            is WorkerReportEvent.DescriptionChanged -> {
                submitIdempotencyKey = null
                _uiState.update {
                    it.copy(
                        description = event.value.take(SubmitReportUseCase.MAX_DESCRIPTION_LENGTH),
                        errorMessage = null,
                    )
                }
            }
            WorkerReportEvent.Submit -> submit()
        }
    }

    private fun loadReasons() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingReasons = true, reasonsErrorMessage = null) }
            when (val result = getReportReasonsUseCase()) {
                is AppResult.Success -> _uiState.update {
                    it.copy(
                        isLoadingReasons = false,
                        reasons = result.data,
                        reasonsErrorMessage = null,
                    )
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(
                        isLoadingReasons = false,
                        reasonsErrorMessage = result.error.userMessage(),
                    )
                }
            }
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (!state.canSubmit) return
        val reasonCode = state.selectedReasonCode ?: return
        val key = submitIdempotencyKey ?: UUID.randomUUID().toString().also { submitIdempotencyKey = it }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            when (
                val result = submitReportUseCase(
                    workerId = workerId,
                    reasonCode = reasonCode,
                    description = state.description,
                    idempotencyKey = key,
                )
            ) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isSubmitting = false) }
                    emitEffect(WorkerReportEffect.ShowMessage("Recibimos tu denuncia. Vamos a revisarla."))
                    emitEffect(WorkerReportEffect.NavigateBack)
                }
                is AppResult.Error -> {
                    submitIdempotencyKey = if (shouldRotateIdempotencyKey(result.error)) null else key
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = reportErrorMessage(result.error),
                        )
                    }
                }
            }
        }
    }

    private fun shouldRotateIdempotencyKey(error: AppError): Boolean {
        val code = error.backendCode
        return code == "REPORT_REASON_INVALID" ||
            code == "REPORT_DESCRIPTION_INVALID" ||
            error is AppError.Validation
    }

    private fun reportErrorMessage(error: AppError): String = when {
        error is AppError.Forbidden -> "No podés denunciar este perfil."
        error.backendCode == "REPORT_ALREADY_OPEN" -> error.userMessage()
        else -> error.userMessage()
    }

    private fun emitEffect(effect: WorkerReportEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}
