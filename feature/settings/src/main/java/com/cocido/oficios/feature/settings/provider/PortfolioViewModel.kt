package com.cocido.oficios.feature.settings.provider

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.usecase.account.AccountUseCases
import com.cocido.oficios.feature.settings.navigation.SettingsDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PortfolioViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val accountUseCases: AccountUseCases,
) : ViewModel() {
    private val openCertificateOnStart =
        savedStateHandle.get<String>("focus") == SettingsDestination.FOCUS_CERTIFICATE
    private var didOpenCertificateOnStart = false
    private val _uiState = MutableStateFlow(PortfolioUiState())
    val uiState = _uiState.asStateFlow()
    private val _effects = Channel<PortfolioEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()
    private var skipNextResume = true

    init {
        viewModelScope.launch {
            accountUseCases.providerDetails.collect { details ->
                if (details != null) {
                    update {
                        it.copy(
                            isLoading = false,
                            gallery = details.gallery,
                            certificates = details.certificates,
                        )
                    }
                }
            }
        }
        load()
    }

    fun onResume() {
        if (skipNextResume) {
            skipNextResume = false
            return
        }
        load()
    }

    fun onEvent(event: PortfolioEvent) {
        when (event) {
            PortfolioEvent.Retry -> load()
            PortfolioEvent.Back -> emit(PortfolioEffect.NavigateBack)
            is PortfolioEvent.GallerySelected -> uploadGallery(event.path)
            PortfolioEvent.AddCertificate -> editCertificate()
            is PortfolioEvent.EditCertificate -> editCertificate(event.item)
            PortfolioEvent.CloseCertificateEditor -> closeEditor()
            is PortfolioEvent.CertificateNameChanged -> update { it.copy(certificateName = event.value, errorMessage = null) }
            is PortfolioEvent.CertificateFileSelected -> update {
                it.copy(
                    certificateFilePath = event.path,
                    certificateFileName = event.displayName,
                    errorMessage = null,
                )
            }
            PortfolioEvent.CertificateFileCleared -> update {
                it.copy(certificateFilePath = null, certificateFileName = null, errorMessage = null)
            }
            PortfolioEvent.SaveCertificate -> saveCertificate()
            is PortfolioEvent.RequestDelete -> update { it.copy(pendingDelete = event.target) }
            PortfolioEvent.ConfirmDelete -> deletePending()
            PortfolioEvent.DismissDelete -> update { it.copy(pendingDelete = null) }
        }
    }

    private fun load() = viewModelScope.launch {
        update {
            it.copy(
                isLoading = it.gallery.isEmpty() && it.certificates.isEmpty(),
                errorMessage = null,
            )
        }
        val (certificateResult, galleryResult) = coroutineScope {
            val certificates = async { accountUseCases.getCertificates() }
            val gallery = async { accountUseCases.getGallery() }
            certificates.await() to gallery.await()
        }
        if (certificateResult is AppResult.Error) {
            update { it.copy(isLoading = false, isSaving = false, errorMessage = certificateResult.error.userMessage()) }
            return@launch
        }
        if (galleryResult is AppResult.Error) {
            update { it.copy(isLoading = false, isSaving = false, errorMessage = galleryResult.error.userMessage()) }
            return@launch
        }
        update {
            it.copy(
                isLoading = false,
                isSaving = false,
                certificates = (certificateResult as AppResult.Success).data,
                gallery = (galleryResult as AppResult.Success).data,
                errorMessage = null,
            )
        }
        if (openCertificateOnStart && !didOpenCertificateOnStart) {
            didOpenCertificateOnStart = true
            editCertificate()
        }
    }

    private fun uploadGallery(path: String) = viewModelScope.launch {
        update { it.copy(isSaving = true, errorMessage = null) }
        when (val result = accountUseCases.uploadGallery(listOf(path))) {
            is AppResult.Success -> {
                update { it.copy(isSaving = false) }
                emit(PortfolioEffect.ShowMessage("Foto agregada a su galería."))
            }
            is AppResult.Error -> update { it.copy(isSaving = false, errorMessage = result.error.userMessage()) }
        }
    }

    private fun editCertificate(item: com.cocido.oficios.domain.model.WorkCertificateItem? = null) = update {
        it.copy(
            certificateEditorVisible = true,
            editingCertificateId = item?.id,
            certificateName = item?.name.orEmpty(),
            certificateFilePath = null,
            certificateFileName = null,
            currentCertificateUrl = item?.fileUrl,
            errorMessage = null,
        )
    }

    private fun closeEditor() = update {
        it.copy(
            certificateEditorVisible = false,
            editingCertificateId = null,
            certificateName = "",
            certificateFilePath = null,
            certificateFileName = null,
            currentCertificateUrl = null,
            errorMessage = null,
        )
    }

    private fun saveCertificate() {
        val state = _uiState.value
        val name = state.certificateName.trim()
        val error = when {
            name.length < 3 -> "Ingrese un nombre claro para el certificado."
            state.editingCertificateId == null && state.certificateFilePath == null -> "Seleccione el archivo del certificado."
            else -> null
        }
        if (error != null) return update { it.copy(errorMessage = error) }
        viewModelScope.launch {
            update { it.copy(isSaving = true, errorMessage = null) }
            val result: AppResult<*> = if (state.editingCertificateId == null) {
                accountUseCases.uploadCertificate(name, state.certificateFilePath!!)
            } else {
                accountUseCases.updateCertificate(state.editingCertificateId, name, state.currentCertificateUrl, state.certificateFilePath)
            }
            when (result) {
                is AppResult.Success -> {
                    closeEditor()
                    load()
                    emit(PortfolioEffect.ShowMessage("Certificado guardado."))
                }
                is AppResult.Error -> update { it.copy(isSaving = false, errorMessage = result.error.userMessage()) }
            }
        }
    }

    private fun deletePending() {
        val target = _uiState.value.pendingDelete ?: return
        viewModelScope.launch {
            update { it.copy(isSaving = true, pendingDelete = null, errorMessage = null) }
            val result = when (target) {
                is PortfolioDeleteTarget.Gallery -> accountUseCases.deleteGalleryItem(target.id)
                is PortfolioDeleteTarget.Certificate -> accountUseCases.deleteCertificate(target.id)
            }
            when (result) {
                is AppResult.Success -> {
                    load()
                    emit(PortfolioEffect.ShowMessage("Elemento eliminado."))
                }
                is AppResult.Error -> update { it.copy(isSaving = false, errorMessage = result.error.userMessage()) }
            }
        }
    }

    private fun update(block: (PortfolioUiState) -> PortfolioUiState) = _uiState.update(block)
    private fun emit(effect: PortfolioEffect) = viewModelScope.launch { _effects.send(effect) }
}
