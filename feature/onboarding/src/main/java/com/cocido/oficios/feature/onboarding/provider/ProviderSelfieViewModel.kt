package com.cocido.oficios.feature.onboarding.provider

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.ProviderSelfieDocument
import com.cocido.oficios.domain.usecase.profile.DeleteProviderIdentityDocumentUseCase
import com.cocido.oficios.domain.usecase.profile.GetProviderSelfieUseCase
import com.cocido.oficios.domain.usecase.profile.ReplaceProviderSelfieUseCase
import com.cocido.oficios.domain.usecase.profile.UploadProviderSelfieUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProviderSelfieUiState(
    val imagePath: String? = null,
    val remoteUrl: String? = null,
    val remoteStatus: String? = null,
    val remoteLoaded: Boolean = false,
    val existingDocumentId: Long? = null,
    val rejectionReason: String? = null,
    val isLoading: Boolean = false,
    val isPreloading: Boolean = true,
    val error: String? = null,
) {
    val isApproved: Boolean get() = remoteStatus.isDocumentStatus(DOCUMENT_STATUS_APPROVED)
    val isRejected: Boolean get() = remoteStatus.isDocumentStatus(DOCUMENT_STATUS_REJECTED)
    val isUploaded: Boolean get() = remoteStatus.isDocumentStatus(DOCUMENT_STATUS_UPLOADED)

    val slot: DocumentSlot get() = documentSlotOf(imagePath, remoteLoaded, remoteUrl, remoteStatus)

    val isEditable: Boolean get() = !isLoading && !isPreloading

    /** Hay una selfie nueva elegida que todavía no se envió. */
    val hasPendingChanges: Boolean get() = imagePath != null

    /** Una selfie rechazada no deja avanzar hasta que se elija otra. */
    val canSubmit: Boolean
        get() = isEditable && slot.status.hasImage && slot.status != DocumentSlotStatus.Rejected
}

@HiltViewModel
class ProviderSelfieViewModel @Inject constructor(
    private val uploadProviderSelfieUseCase: UploadProviderSelfieUseCase,
    private val replaceProviderSelfieUseCase: ReplaceProviderSelfieUseCase,
    private val deleteProviderIdentityDocumentUseCase: DeleteProviderIdentityDocumentUseCase,
    private val getProviderSelfieUseCase: GetProviderSelfieUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProviderSelfieUiState())
    val uiState: StateFlow<ProviderSelfieUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<ProviderStepEffect>()
    val effects: SharedFlow<ProviderStepEffect> = _effects.asSharedFlow()

    init {
        preloadSelfie()
    }

    private fun preloadSelfie() {
        viewModelScope.launch {
            _uiState.update { it.copy(isPreloading = true) }
            refreshRemoteSelfie()
            _uiState.update { it.copy(isPreloading = false) }
        }
    }

    private suspend fun refreshRemoteSelfie() {
        when (val result = getProviderSelfieUseCase()) {
            is AppResult.Success -> _uiState.update { it.withRemoteSelfie(result.data) }
            is AppResult.Error -> Unit
        }
    }

    private fun ProviderSelfieUiState.withRemoteSelfie(selfie: ProviderSelfieDocument?) = copy(
        existingDocumentId = selfie?.id,
        remoteUrl = selfie?.url,
        remoteStatus = selfie?.status,
        remoteLoaded = !selfie?.url.isNullOrBlank(),
        rejectionReason = selfie?.rejectionReason,
    )

    /**
     * `null` llega cuando el usuario cancela la cámara o la galería: se
     * conserva la selfie anterior en vez de borrarla.
     */
    fun onImageSelected(path: String?) {
        if (path == null || !_uiState.value.slot.status.canReplace) return
        _uiState.update { it.copy(imagePath = path, error = null) }
    }

    fun onPickError(message: String) = _uiState.update { it.copy(error = message) }

    fun submit() {
        val state = _uiState.value
        if (state.isLoading || state.isPreloading) return
        val imagePath = state.imagePath

        if (imagePath == null) {
            when (state.slot.status) {
                DocumentSlotStatus.InReview,
                DocumentSlotStatus.Approved,
                -> viewModelScope.launch { _effects.emit(ProviderStepEffect.Saved) }
                DocumentSlotStatus.Rejected ->
                    _uiState.update { it.copy(error = "Su selfie fue rechazada. Tome o elija otra foto para continuar.") }
                else -> _uiState.update { it.copy(error = "Suba una selfie para continuar.") }
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = guardedSubmit { sendSelfie(state, imagePath) }) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            imagePath = null,
                            remoteUrl = imagePath,
                            remoteLoaded = true,
                            remoteStatus = DOCUMENT_STATUS_UPLOADED,
                            rejectionReason = null,
                        )
                    }
                    refreshRemoteSelfie()
                    _effects.emit(ProviderStepEffect.Saved)
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.error.userMessage())
                }
            }
        }
    }

    private suspend fun sendSelfie(state: ProviderSelfieUiState, imagePath: String): AppResult<Unit> {
        val existingId = state.existingDocumentId
        return when {
            existingId != null && state.isRejected ->
                replaceProviderSelfieUseCase(existingId, imagePath)
            // En revisión: el backend solo reemplaza rechazadas, así que se borra y se sube de nuevo.
            existingId != null && state.isUploaded -> {
                when (val deleted = deleteProviderIdentityDocumentUseCase(existingId)) {
                    is AppResult.Error -> deleted
                    is AppResult.Success -> {
                        // Si la subida falla, el reintento tiene que ser un alta limpia.
                        _uiState.update { it.withRemoteSelfie(null) }
                        uploadProviderSelfieUseCase(imagePath)
                    }
                }
            }
            else -> uploadProviderSelfieUseCase(imagePath)
        }
    }
}