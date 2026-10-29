package com.cocido.oficios.feature.onboarding.provider

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
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
    val canSubmit: Boolean get() = imagePath != null || remoteLoaded
    val isApproved: Boolean get() = remoteStatus?.equals("APROBADO", ignoreCase = true) == true
    val isRejected: Boolean get() = remoteStatus?.equals("RECHAZADO", ignoreCase = true) == true
    val isUploaded: Boolean get() = remoteStatus?.equals("SUBIDO", ignoreCase = true) == true
}

@HiltViewModel
class ProviderSelfieViewModel @Inject constructor(
    private val uploadProviderSelfieUseCase: UploadProviderSelfieUseCase,
    private val replaceProviderSelfieUseCase: ReplaceProviderSelfieUseCase,
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
            when (val result = getProviderSelfieUseCase()) {
                is AppResult.Success -> result.data?.let { selfie ->
                    _uiState.update {
                        it.copy(
                            existingDocumentId = selfie.id,
                            remoteUrl = selfie.url,
                            remoteStatus = selfie.status,
                            remoteLoaded = !selfie.url.isNullOrBlank(),
                            rejectionReason = selfie.rejectionReason,
                        )
                    }
                }
                is AppResult.Error -> Unit
            }
            _uiState.update { it.copy(isPreloading = false) }
        }
    }

    fun onImageSelected(path: String?) = _uiState.update {
        it.copy(imagePath = path, error = null)
    }

    fun onPickError(message: String) = _uiState.update { it.copy(error = message) }

    fun submit() {
        val state = _uiState.value
        val imagePath = state.imagePath
        
        if (imagePath.isNullOrBlank()) {
            if (state.remoteLoaded) {
                viewModelScope.launch {
                    _effects.emit(ProviderStepEffect.Saved)
                }
                return
            }
            _uiState.update { it.copy(error = "Suba una selfie para continuar") }
            return
        }

        if (state.isApproved) {
            _uiState.update { it.copy(error = "La selfie ya fue aprobada y no puede ser reemplazada") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = if (state.existingDocumentId != null && (state.isRejected || state.isUploaded)) {
                replaceProviderSelfieUseCase(state.existingDocumentId, imagePath)
            } else {
                uploadProviderSelfieUseCase(imagePath)
            }

            when (result) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _effects.emit(ProviderStepEffect.Saved)
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = result.error.userMessage())
                    }
                }
            }
        }
    }
}
