package com.cocido.oficios.feature.onboarding.provider

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.usecase.profile.GetProviderDniDocumentsUseCase
import com.cocido.oficios.domain.usecase.profile.GetProviderDocumentoUseCase
import com.cocido.oficios.domain.usecase.profile.GetProviderSelfieUseCase
import com.cocido.oficios.domain.usecase.profile.GetWorkCertificatesUseCase
import com.cocido.oficios.domain.usecase.profile.ReplaceProviderSelfieUseCase
import com.cocido.oficios.domain.usecase.profile.UpdateProviderDniNumberUseCase
import com.cocido.oficios.domain.usecase.profile.UploadProviderDniImagesUseCase
import com.cocido.oficios.domain.usecase.profile.UploadProviderSelfieUseCase
import com.cocido.oficios.domain.usecase.profile.UploadWorkCertificateUseCase
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

data class ProviderIdentityUiState(
    val documento: String = "",
    val frontImagePath: String? = null,
    val backImagePath: String? = null,
    val selfieImagePath: String? = null,
    val certificateName: String = "",
    val certificatePath: String? = null,
    val frontRemoteLoaded: Boolean = false,
    val backRemoteLoaded: Boolean = false,
    val selfieRemoteLoaded: Boolean = false,
    val existingSelfieDocumentId: Long? = null,
    val existingCertificateName: String? = null,
    val isLoading: Boolean = false,
    val isPreloading: Boolean = true,
    val uploadError: String? = null,
) {
    val canSubmit: Boolean
        get() = (frontImagePath != null || frontRemoteLoaded) &&
            (backImagePath != null || backRemoteLoaded) &&
            (selfieImagePath != null || selfieRemoteLoaded) &&
            documento.isNotBlank()
}

@HiltViewModel
class ProviderIdentityViewModel @Inject constructor(
    private val updateProviderDniNumberUseCase: UpdateProviderDniNumberUseCase,
    private val uploadProviderDniImagesUseCase: UploadProviderDniImagesUseCase,
    private val getProviderDniDocumentsUseCase: GetProviderDniDocumentsUseCase,
    private val getProviderDocumentoUseCase: GetProviderDocumentoUseCase,
    private val uploadProviderSelfieUseCase: UploadProviderSelfieUseCase,
    private val replaceProviderSelfieUseCase: ReplaceProviderSelfieUseCase,
    private val getProviderSelfieUseCase: GetProviderSelfieUseCase,
    private val uploadWorkCertificateUseCase: UploadWorkCertificateUseCase,
    private val getWorkCertificatesUseCase: GetWorkCertificatesUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProviderIdentityUiState())
    val uiState: StateFlow<ProviderIdentityUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<ProviderStepEffect>()
    val effects: SharedFlow<ProviderStepEffect> = _effects.asSharedFlow()

    init {
        preloadDocuments()
    }

    private fun preloadDocuments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isPreloading = true) }
            
            getProviderDocumentoUseCase()?.let { documento ->
                _uiState.update { it.copy(documento = documento) }
            }

            when (val dniResult = getProviderDniDocumentsUseCase()) {
                is AppResult.Success -> dniResult.data?.let { docs ->
                    _uiState.update {
                        it.copy(
                            frontRemoteLoaded = !docs.frontUrl.isNullOrBlank(),
                            backRemoteLoaded = !docs.backUrl.isNullOrBlank(),
                        )
                    }
                }
                is AppResult.Error -> Unit
            }

            when (val selfieResult = getProviderSelfieUseCase()) {
                is AppResult.Success -> selfieResult.data?.let { selfie ->
                    _uiState.update {
                        it.copy(
                            existingSelfieDocumentId = selfie.id,
                            selfieRemoteLoaded = !selfie.url.isNullOrBlank(),
                        )
                    }
                }
                is AppResult.Error -> Unit
            }

            when (val certResult = getWorkCertificatesUseCase()) {
                is AppResult.Success -> certResult.data.firstOrNull()?.let { cert ->
                    _uiState.update { it.copy(existingCertificateName = cert.name) }
                }
                is AppResult.Error -> Unit
            }

            _uiState.update { it.copy(isPreloading = false) }
        }
    }

    fun onDocumentoChanged(value: String) = _uiState.update { 
        it.copy(documento = value, uploadError = null) 
    }

    fun onFrontImageSelected(path: String?) = _uiState.update {
        it.copy(frontImagePath = path, uploadError = null)
    }

    fun onBackImageSelected(path: String?) = _uiState.update {
        it.copy(backImagePath = path, uploadError = null)
    }

    fun onSelfieImageSelected(path: String?) = _uiState.update {
        it.copy(selfieImagePath = path, uploadError = null)
    }

    fun onCertificateNameChanged(value: String) = _uiState.update {
        it.copy(certificateName = value, uploadError = null)
    }

    fun onCertificateSelected(path: String?) = _uiState.update {
        it.copy(certificatePath = path, uploadError = null)
    }

    fun submit() {
        val state = _uiState.value
        val documento = state.documento.trim()
        val front = state.frontImagePath
        val back = state.backImagePath
        val selfie = state.selfieImagePath
        val hasRemoteDniImages = state.frontRemoteLoaded && state.backRemoteLoaded
        val hasRemoteSelfie = state.selfieRemoteLoaded

        if (documento.isBlank() || 
            (!hasRemoteDniImages && (front == null || back == null)) ||
            (!hasRemoteSelfie && selfie == null)) {
            _uiState.update { 
                it.copy(uploadError = "Completá el número de DNI y todas las fotos obligatorias") 
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, uploadError = null) }

            // 1. Actualizar número de DNI
            when (val numberResult = updateProviderDniNumberUseCase(documento)) {
                is AppResult.Error -> {
                    _uiState.update { 
                        it.copy(isLoading = false, uploadError = numberResult.error.userMessage()) 
                    }
                    return@launch
                }
                is AppResult.Success -> Unit
            }

            // 2. Subir fotos de DNI si son nuevas
            if (front != null && back != null) {
                when (val imagesResult = uploadProviderDniImagesUseCase(front, back)) {
                    is AppResult.Error -> {
                        _uiState.update { 
                            it.copy(isLoading = false, uploadError = imagesResult.error.userMessage()) 
                        }
                        return@launch
                    }
                    is AppResult.Success -> Unit
                }
            }

            // 3. Subir selfie si es nueva
            if (selfie != null) {
                val selfieResult = state.existingSelfieDocumentId?.let { documentId ->
                    replaceProviderSelfieUseCase(documentId, selfie)
                } ?: uploadProviderSelfieUseCase(selfie)

                when (selfieResult) {
                    is AppResult.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                uploadError = "DNI enviado, pero la selfie falló: ${selfieResult.error.userMessage()}"
                            ) 
                        }
                        return@launch
                    }
                    is AppResult.Success -> Unit
                }
            }

            // 4. Subir certificado opcional si fue seleccionado
            val certificatePath = state.certificatePath
            val certificateName = state.certificateName.trim()
            if (certificatePath != null && certificateName.isNotBlank()) {
                when (val certResult = uploadWorkCertificateUseCase(certificateName, certificatePath)) {
                    is AppResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                uploadError = "DNI y selfie enviados, pero el certificado falló: ${certResult.error.userMessage()}",
                            )
                        }
                        return@launch
                    }
                    is AppResult.Success -> Unit
                }
            }

            _uiState.update { it.copy(isLoading = false) }
            _effects.emit(ProviderStepEffect.Saved)
        }
    }
}
