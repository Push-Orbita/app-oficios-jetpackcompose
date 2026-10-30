package com.cocido.oficios.feature.onboarding.provider

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.mapper.toRubroFullRequest
import com.cocido.oficios.domain.model.AddressLocationPhase
import com.cocido.oficios.domain.model.AddressLocationSnapshot
import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.domain.model.FormosaAddressGeocoding
import com.cocido.oficios.domain.model.GeoCoordinates
import com.cocido.oficios.domain.model.LocalAddressQuery
import com.cocido.oficios.domain.model.ProviderDniDocuments
import com.cocido.oficios.domain.model.ProviderProfessionalInfoRequest
import com.cocido.oficios.domain.model.ProviderPaymentMethods
import com.cocido.oficios.domain.model.ProviderLocalDomicilioRequest
import com.cocido.oficios.domain.model.ProviderRubroFullRequest
import com.cocido.oficios.domain.model.RegisterTradeTimeSlotDraft
import com.cocido.oficios.domain.repository.RegisterTradeDraftRepository
import com.cocido.oficios.domain.usecase.auth.GetOnboardingStatusUseCase
import com.cocido.oficios.domain.usecase.catalog.GetCategoriesUseCase
import com.cocido.oficios.domain.usecase.catalog.GetRubrosByCategoryUseCase
import com.cocido.oficios.domain.usecase.catalog.GetSubRubrosByRubroUseCase
import com.cocido.oficios.domain.usecase.catalog.SearchBarriosUseCase
import com.cocido.oficios.domain.usecase.catalog.launchDebouncedBarrioSearch
import com.cocido.oficios.domain.usecase.profile.FinalizeProviderProfileUseCase
import com.cocido.oficios.domain.usecase.profile.GetProviderDocumentoUseCase
import com.cocido.oficios.domain.usecase.profile.GetProviderDniDocumentsUseCase
import com.cocido.oficios.domain.usecase.profile.GetWorkCertificatesUseCase
import com.cocido.oficios.domain.usecase.profile.SubmitProviderRubroFullUseCase
import com.cocido.oficios.domain.usecase.profile.SubmitSecondaryProviderRubroUseCase
import com.cocido.oficios.domain.usecase.profile.SyncRegisterTradeDraftFromProfileUseCase
import com.cocido.oficios.domain.usecase.profile.DeleteProviderIdentityDocumentUseCase
import com.cocido.oficios.domain.usecase.profile.ReplaceProviderDniImagesUseCase
import com.cocido.oficios.domain.usecase.profile.UpdateProviderDniNumberUseCase
import com.cocido.oficios.domain.usecase.profile.UpdateProviderProfessionalInfoUseCase
import com.cocido.oficios.domain.usecase.location.GeocodeLocalAddressUseCase
import com.cocido.oficios.domain.usecase.location.launchDebouncedForwardGeocode
import com.cocido.oficios.domain.usecase.profile.UploadProviderDniImagesUseCase
import com.cocido.oficios.domain.usecase.profile.UploadWorkCertificateUseCase
import com.cocido.oficios.domain.validation.AuthValidators
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProviderDniUiState(
    val documento: String = "",
    /** Número que ya está guardado en el servidor; sirve para no re-enviar si no cambió. */
    val savedDocumento: String? = null,
    val frontImagePath: String? = null,
    val backImagePath: String? = null,
    val frontFileName: String? = null,
    val backFileName: String? = null,
    val frontRemoteLoaded: Boolean = false,
    val backRemoteLoaded: Boolean = false,
    val frontRemoteUrl: String? = null,
    val backRemoteUrl: String? = null,
    val existingDocumentId: Long? = null,
    val frontStatus: String? = null,
    val backStatus: String? = null,
    val certificateName: String = "",
    val certificatePath: String? = null,
    val existingCertificateName: String? = null,
    val isLoading: Boolean = false,
    val isPreloading: Boolean = true,
    val error: String? = null,
    /** Se muestra recién después del primer intento de envío, para no marcar errores mientras escribe. */
    val showValidation: Boolean = false,
) {
    val isApproved: Boolean
        get() = frontStatus.isDocumentStatus(DOCUMENT_STATUS_APPROVED) &&
            backStatus.isDocumentStatus(DOCUMENT_STATUS_APPROVED)
    val isRejected: Boolean
        get() = frontStatus.isDocumentStatus(DOCUMENT_STATUS_REJECTED) ||
            backStatus.isDocumentStatus(DOCUMENT_STATUS_REJECTED)
    val isPendingReview: Boolean
        get() = !isApproved && !isRejected && (
            frontStatus.isDocumentStatus(DOCUMENT_STATUS_UPLOADED) ||
                backStatus.isDocumentStatus(DOCUMENT_STATUS_UPLOADED)
            )

    val frontSlot: DocumentSlot
        get() = documentSlotOf(frontImagePath, frontRemoteLoaded, frontRemoteUrl, frontStatus)
    val backSlot: DocumentSlot
        get() = documentSlotOf(backImagePath, backRemoteLoaded, backRemoteUrl, backStatus)

    /** El formulario se puede tocar: no está cargando datos ni enviando. */
    val isEditable: Boolean get() = !isLoading && !isPreloading

    /** El número ya fue validado junto con las fotos; no se edita más desde el registro. */
    val isDocumentoLocked: Boolean get() = isApproved

    val documentoError: String?
        get() = AuthValidators.validateDocument(documento.trim())

    val hasLocalImages: Boolean get() = frontImagePath != null || backImagePath != null

    val hasPendingChanges: Boolean
        get() = hasLocalImages || documento.trim() != savedDocumento.orEmpty()

    /**
     * El backend solo reemplaza lados sueltos de un DNI rechazado. Si está en
     * revisión (SUBIDO) se borra y se vuelve a subir, así que hacen falta los dos.
     */
    val needsBothSidesToReplace: Boolean
        get() = existingDocumentId != null && isPendingReview &&
            (frontImagePath == null) != (backImagePath == null)

    /** Un lado rechazado tiene que reemplazarse antes de poder enviar. */
    val hasRejectedSideWithoutReplacement: Boolean
        get() = (frontStatus.isDocumentStatus(DOCUMENT_STATUS_REJECTED) && frontImagePath == null) ||
            (backStatus.isDocumentStatus(DOCUMENT_STATUS_REJECTED) && backImagePath == null)

    val canSubmit: Boolean
        get() = isEditable &&
            documentoError == null &&
            frontSlot.status.hasImage &&
            backSlot.status.hasImage &&
            !needsBothSidesToReplace &&
            !hasRejectedSideWithoutReplacement
}

sealed interface ProviderStepEffect {
    data object Saved : ProviderStepEffect
    data object Finalized : ProviderStepEffect
}

@HiltViewModel
class ProviderDniViewModel @Inject constructor(
    private val updateProviderDniNumberUseCase: UpdateProviderDniNumberUseCase,
    private val uploadProviderDniImagesUseCase: UploadProviderDniImagesUseCase,
    private val replaceProviderDniImagesUseCase: ReplaceProviderDniImagesUseCase,
    private val deleteProviderIdentityDocumentUseCase: DeleteProviderIdentityDocumentUseCase,
    private val getProviderDniDocumentsUseCase: GetProviderDniDocumentsUseCase,
    private val getProviderDocumentoUseCase: GetProviderDocumentoUseCase,
    private val uploadWorkCertificateUseCase: UploadWorkCertificateUseCase,
    private val getWorkCertificatesUseCase: GetWorkCertificatesUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProviderDniUiState())
    val uiState: StateFlow<ProviderDniUiState> = _uiState.asStateFlow()
    private val _effects = MutableSharedFlow<ProviderStepEffect>()
    val effects: SharedFlow<ProviderStepEffect> = _effects.asSharedFlow()

    init {
        preloadDocuments()
    }

    private fun preloadDocuments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isPreloading = true) }
            getProviderDocumentoUseCase()?.let { documento ->
                // No pisar lo que el usuario ya haya empezado a escribir.
                _uiState.update {
                    it.copy(
                        documento = it.documento.ifBlank { documento },
                        savedDocumento = documento,
                    )
                }
            }
            refreshRemoteDni()
            when (val certResult = getWorkCertificatesUseCase()) {
                is AppResult.Success -> certResult.data.firstOrNull()?.let { cert ->
                    _uiState.update { it.copy(existingCertificateName = cert.name) }
                }
                is AppResult.Error -> Unit
            }
            _uiState.update { it.copy(isPreloading = false) }
        }
    }

    private suspend fun refreshRemoteDni() {
        when (val dniResult = getProviderDniDocumentsUseCase()) {
            is AppResult.Success -> _uiState.update { it.withRemoteDni(dniResult.data) }
            is AppResult.Error -> Unit
        }
    }

    private fun ProviderDniUiState.withRemoteDni(docs: ProviderDniDocuments?): ProviderDniUiState = copy(
        existingDocumentId = docs?.id,
        frontStatus = docs?.frontStatus,
        backStatus = docs?.backStatus,
        frontRemoteLoaded = !docs?.frontUrl.isNullOrBlank(),
        backRemoteLoaded = !docs?.backUrl.isNullOrBlank(),
        frontRemoteUrl = docs?.frontUrl,
        backRemoteUrl = docs?.backUrl,
        frontFileName = frontImagePath?.substringAfterLast('/') ?: docs?.frontFileName,
        backFileName = backImagePath?.substringAfterLast('/') ?: docs?.backFileName,
    )

    fun onDocumentoChanged(value: String) {
        if (_uiState.value.isDocumentoLocked) return
        val digits = value.filter(Char::isDigit).take(DNI_MAX_LENGTH)
        _uiState.update { it.copy(documento = digits, error = null) }
    }

    /**
     * `null` llega cuando el usuario cancela la cámara, la galería o el
     * recorte: se conserva la foto anterior en vez de borrarla.
     */
    fun onFrontImage(path: String?) {
        if (path == null || !_uiState.value.frontSlot.status.canReplace) return
        _uiState.update {
            it.copy(frontImagePath = path, frontFileName = path.substringAfterLast('/'), error = null)
        }
    }

    fun onBackImage(path: String?) {
        if (path == null || !_uiState.value.backSlot.status.canReplace) return
        _uiState.update {
            it.copy(backImagePath = path, backFileName = path.substringAfterLast('/'), error = null)
        }
    }

    fun onCertificateNameChanged(value: String) = _uiState.update { it.copy(certificateName = value, error = null) }
    fun onCertificateSelected(path: String?) {
        if (path == null) return
        _uiState.update { it.copy(certificatePath = path, error = null) }
    }

    fun onPickError(message: String) = _uiState.update { it.copy(error = message) }

    fun submit() {
        val state = _uiState.value
        if (state.isLoading || state.isPreloading) return
        if (!state.canSubmit) {
            _uiState.update { it.copy(showValidation = true, error = it.blockingMessage()) }
            return
        }
        if (!state.hasPendingChanges && state.certificatePath == null) {
            // Nada cambió: no hay que pegarle al backend, solo avanzar.
            viewModelScope.launch { _effects.emit(ProviderStepEffect.Saved) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, showValidation = true) }
            val result = guardedSubmit { sendChanges(state) }
            when (result) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _effects.emit(ProviderStepEffect.Saved)
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.error.userMessage())
                }
            }
        }
    }

    private suspend fun sendChanges(state: ProviderDniUiState): AppResult<Unit> {
        val documento = state.documento.trim()
        if (documento != state.savedDocumento) {
            when (val numberResult = updateProviderDniNumberUseCase(documento)) {
                is AppResult.Error -> return numberResult
                is AppResult.Success -> _uiState.update { it.copy(savedDocumento = documento) }
            }
        }

        val front = state.frontImagePath
        val back = state.backImagePath
        if (front != null || back != null) {
            val imagesResult = uploadImages(state, front, back)
            if (imagesResult is AppResult.Error) return imagesResult
            // Las fotos ya están en el servidor: se limpian las locales y se
            // refresca el documento (id y estado nuevos) por si el usuario vuelve.
            _uiState.update {
                it.copy(
                    frontImagePath = null,
                    backImagePath = null,
                    frontRemoteLoaded = true,
                    backRemoteLoaded = true,
                    frontRemoteUrl = front ?: it.frontRemoteUrl,
                    backRemoteUrl = back ?: it.backRemoteUrl,
                    frontStatus = if (front != null) DOCUMENT_STATUS_UPLOADED else it.frontStatus,
                    backStatus = if (back != null) DOCUMENT_STATUS_UPLOADED else it.backStatus,
                )
            }
            refreshRemoteDni()
        }

        val certificatePath = state.certificatePath
        val certificateName = state.certificateName.trim()
        if (certificatePath != null && certificateName.isNotBlank()) {
            when (val certResult = uploadWorkCertificateUseCase(certificateName, certificatePath)) {
                is AppResult.Error -> return certResult
                is AppResult.Success -> _uiState.update { it.copy(certificatePath = null) }
            }
        }
        return AppResult.Success(Unit)
    }

    private suspend fun uploadImages(
        state: ProviderDniUiState,
        front: String?,
        back: String?,
    ): AppResult<Unit> {
        val existingId = state.existingDocumentId
        return when {
            // Rechazado: el backend acepta reemplazar solo el lado que cambió.
            existingId != null && state.isRejected ->
                replaceProviderDniImagesUseCase(existingId, front, back)
            // En revisión: no hay endpoint de reemplazo, se borra y se sube de nuevo.
            existingId != null && state.isPendingReview && front != null && back != null -> {
                when (val deleted = deleteProviderIdentityDocumentUseCase(existingId)) {
                    is AppResult.Error -> deleted
                    is AppResult.Success -> {
                        // Si la subida falla, el reintento tiene que ser un alta limpia.
                        _uiState.update { it.withRemoteDni(null) }
                        uploadProviderDniImagesUseCase(front, back)
                    }
                }
            }
            front != null && back != null -> uploadProviderDniImagesUseCase(front, back)
            else -> AppResult.Error(AppError.Validation(message = DNI_BOTH_SIDES_MESSAGE))
        }
    }

    private fun ProviderDniUiState.blockingMessage(): String = documentoError ?: when {
        !frontSlot.status.hasImage || !backSlot.status.hasImage -> "Cargue la foto del frente y del dorso del DNI."
        hasRejectedSideWithoutReplacement -> "Reemplace la foto rechazada antes de enviar."
        needsBothSidesToReplace -> DNI_BOTH_SIDES_MESSAGE
        else -> "Revise los datos antes de enviar."
    }

    private companion object {
        const val DNI_MAX_LENGTH = 11
        const val DNI_BOTH_SIDES_MESSAGE =
            "Para corregir un DNI en revisión, cargue de nuevo el frente y el dorso."
    }
}

data class ProviderProfessionalInfoUiState(
    val descripcion: String = "",
    val experiencia: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ProviderProfessionalInfoViewModel @Inject constructor(
    private val updateProviderProfessionalInfoUseCase: UpdateProviderProfessionalInfoUseCase,
    private val syncRegisterTradeDraftFromProfileUseCase: SyncRegisterTradeDraftFromProfileUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProviderProfessionalInfoUiState())
    val uiState: StateFlow<ProviderProfessionalInfoUiState> = _uiState.asStateFlow()
    private val _effects = MutableSharedFlow<ProviderStepEffect>()
    val effects: SharedFlow<ProviderStepEffect> = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            syncRegisterTradeDraftFromProfileUseCase()?.let { draft ->
                _uiState.update {
                    it.copy(
                        descripcion = draft.otherSpecialization.orEmpty(),
                        experiencia = draft.experienceRange,
                    )
                }
            }
        }
    }

    fun onDescripcionChanged(value: String) = _uiState.update { it.copy(descripcion = value, error = null) }
    fun onExperienciaChanged(value: String) = _uiState.update { it.copy(experiencia = value, error = null) }

    fun submit() {
        val state = _uiState.value
        if (state.descripcion.isBlank() || state.experiencia.isBlank()) {
            _uiState.update { it.copy(error = "Complete la descripción y la experiencia") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (
                val result = updateProviderProfessionalInfoUseCase(
                    ProviderProfessionalInfoRequest(
                        descripcion = state.descripcion.trim(),
                        experiencia = state.experiencia.trim(),
                    ),
                )
            ) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _effects.emit(ProviderStepEffect.Saved)
                }
                is AppResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.error.userMessage()) }
            }
        }
    }
}

data class ProviderRubrosUiState(
    val categoryOptions: List<CatalogOption> = emptyList(),
    val tradeOptions: List<CatalogOption> = emptyList(),
    val specializationOptions: List<CatalogOption> = emptyList(),
    val selectedCategoryId: Long? = null,
    val selectedTradeId: Long? = null,
    val selectedSubRubroIds: Set<Long> = emptySet(),
    val isSecondaryRubro: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val canSubmit: Boolean get() = selectedTradeId != null && selectedSubRubroIds.isNotEmpty()
}

@HiltViewModel
class ProviderRubrosViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getRubrosByCategoryUseCase: GetRubrosByCategoryUseCase,
    private val getSubRubrosByRubroUseCase: GetSubRubrosByRubroUseCase,
    private val registerTradeDraftRepository: RegisterTradeDraftRepository,
    private val syncRegisterTradeDraftFromProfileUseCase: SyncRegisterTradeDraftFromProfileUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProviderRubrosUiState())
    val uiState: StateFlow<ProviderRubrosUiState> = _uiState.asStateFlow()
    private val _effects = MutableSharedFlow<ProviderStepEffect>()
    val effects: SharedFlow<ProviderStepEffect> = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            when (val result = getCategoriesUseCase()) {
                is AppResult.Success -> _uiState.update { it.copy(categoryOptions = result.data) }
                is AppResult.Error -> _uiState.update { it.copy(error = result.error.userMessage()) }
            }
            syncRegisterTradeDraftFromProfileUseCase()?.let { draft ->
                _uiState.update {
                    it.copy(
                        selectedCategoryId = draft.categoryId,
                        selectedTradeId = draft.rubroId,
                        selectedSubRubroIds = draft.subRubroIds.toSet(),
                    )
                }
                loadRubros(draft.categoryId)
                loadSubRubros(draft.rubroId)
            }
        }
    }

    fun onCategorySelected(id: Long) {
        _uiState.update { it.copy(selectedCategoryId = id, selectedTradeId = null, tradeOptions = emptyList()) }
        loadRubros(id)
    }

    fun onTradeSelected(id: Long) {
        _uiState.update { it.copy(selectedTradeId = id, specializationOptions = emptyList()) }
        loadSubRubros(id)
    }

    fun onSecondaryRubroChanged(checked: Boolean) = _uiState.update { it.copy(isSecondaryRubro = checked) }

    fun onSubRubroToggled(id: Long, checked: Boolean) = _uiState.update {
        val updated = it.selectedSubRubroIds.toMutableSet()
        if (checked) updated.add(id) else updated.remove(id)
        it.copy(selectedSubRubroIds = updated)
    }

    private fun loadRubros(categoryId: Long) {
        viewModelScope.launch {
            when (val result = getRubrosByCategoryUseCase(categoryId)) {
                is AppResult.Success -> _uiState.update { it.copy(tradeOptions = result.data) }
                is AppResult.Error -> Unit
            }
        }
    }

    private fun loadSubRubros(rubroId: Long) {
        viewModelScope.launch {
            when (val result = getSubRubrosByRubroUseCase(rubroId)) {
                is AppResult.Success -> _uiState.update { it.copy(specializationOptions = result.data) }
                is AppResult.Error -> Unit
            }
        }
    }

    fun submit() {
        val state = _uiState.value
        val rubroId = state.selectedTradeId
        val categoryId = state.selectedCategoryId
        if (rubroId == null || categoryId == null || !state.canSubmit) {
            _uiState.update { it.copy(error = "Seleccione categoría, rubro y especialidades") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            // No POST /full aquí: el body OpenAPI exige modalidad, días y medios de cobro.
            // Solo persistimos el draft y avanzamos al checklist (disponibilidad / pagos).
            val draft = buildDraft(state, rubroId, categoryId) ?: run {
                _uiState.update { it.copy(isLoading = false, error = "Seleccione categoría, rubro y especialidades") }
                return@launch
            }
            registerTradeDraftRepository.save(draft)
            _uiState.update { it.copy(isLoading = false) }
            _effects.emit(ProviderStepEffect.Saved)
        }
    }

    private suspend fun buildDraft(
        state: ProviderRubrosUiState,
        rubroId: Long,
        categoryId: Long,
    ): com.cocido.oficios.domain.model.RegisterTradeDraft? {
        val existing = registerTradeDraftRepository.get()
        val categoryName = state.categoryOptions.find { it.id == categoryId }?.label.orEmpty()
        val rubroName = state.tradeOptions.find { it.id == rubroId }?.label.orEmpty()
        if (categoryName.isBlank() || rubroName.isBlank()) return null
        return (existing ?: com.cocido.oficios.domain.model.RegisterTradeDraft(
            categoryId = categoryId,
            categoryName = categoryName,
            rubroId = rubroId,
            rubroName = rubroName,
            subRubroIds = state.selectedSubRubroIds.toList(),
            otherSpecialization = null,
            experienceRange = "",
            neighborhoodQuery = "",
            timeSlots = listOf(RegisterTradeTimeSlotDraft("09:00", "18:00")),
        )).copy(
            categoryId = categoryId,
            categoryName = categoryName,
            rubroId = rubroId,
            rubroName = rubroName,
            subRubroIds = state.selectedSubRubroIds.toList(),
        )
    }
}

data class ProviderAvailabilityUiState(
    val workDayOptions: List<Pair<String, String>> = ProviderAvailabilityDefaults.workDays,
    val selectedWorkDays: Set<String> = ProviderAvailabilityDefaults.defaultSelectedWorkDays,
    val timeSlots: List<RegisterTradeTimeSlotDraft> = listOf(RegisterTradeTimeSlotDraft("09:00", "18:00")),
    val timeOptions: List<String> = ProviderAvailabilityDefaults.timeOptions,
    val servesAtClientHome: Boolean = true,
    val servesAtLocal: Boolean = false,
    val requiresTurns: Boolean = false,
    val turnDurationMinutes: Int? = null,
    val turnIntervalMinutes: Int? = null,
    val plazoCancelacionHorasInput: String = "",
    val hasRubroContext: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val canSubmit: Boolean
        get() = hasRubroContext &&
            selectedWorkDays.isNotEmpty() &&
            timeSlots.any { it.from.isNotBlank() && it.to.isNotBlank() } &&
            (!requiresTurns || turnDurationMinutes != null)

    val plazoCancelacionHorasError: String?
        get() {
            val trimmed = plazoCancelacionHorasInput.trim()
            if (trimmed.isEmpty()) return null
            val parsed = trimmed.toIntOrNull()
            return if (parsed == null || parsed !in 1..24) {
                "Ingrese un valor entre 1 y 24, o deje vacío"
            } else null
        }

    val plazoCancelacionHoras: Int?
        get() {
            val trimmed = plazoCancelacionHorasInput.trim()
            return trimmed.toIntOrNull()?.takeIf { it in 1..24 }
        }
}

private object ProviderAvailabilityDefaults {
    val defaultSelectedWorkDays = setOf("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES")

    val workDays = listOf(
        "LUNES" to "Lunes",
        "MARTES" to "Martes",
        "MIERCOLES" to "Miércoles",
        "JUEVES" to "Jueves",
        "VIERNES" to "Viernes",
        "SABADO" to "Sábado",
        "DOMINGO" to "Domingo",
    )

    val timeOptions: List<String> = buildList {
        for (hour in 0 until 24) {
            for (minute in listOf(0, 30)) {
                add("%02d:%02d".format(hour, minute))
            }
        }
    }
}

@HiltViewModel
class ProviderAvailabilityViewModel @Inject constructor(
    private val submitProviderRubroFullUseCase: SubmitProviderRubroFullUseCase,
    private val registerTradeDraftRepository: RegisterTradeDraftRepository,
    private val syncRegisterTradeDraftFromProfileUseCase: SyncRegisterTradeDraftFromProfileUseCase,
    private val getOnboardingStatusUseCase: GetOnboardingStatusUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProviderAvailabilityUiState())
    val uiState: StateFlow<ProviderAvailabilityUiState> = _uiState.asStateFlow()
    private val _effects = MutableSharedFlow<ProviderStepEffect>()
    val effects: SharedFlow<ProviderStepEffect> = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            val draft = syncRegisterTradeDraftFromProfileUseCase()
            if (draft != null) {
                _uiState.update {
                    it.copy(
                        selectedWorkDays = draft.workDays.toSet().ifEmpty { ProviderAvailabilityDefaults.defaultSelectedWorkDays },
                        timeSlots = draft.timeSlots.ifEmpty { listOf(RegisterTradeTimeSlotDraft("09:00", "18:00")) },
                        servesAtClientHome = draft.servesAtClientHome,
                        servesAtLocal = draft.servesAtLocal,
                        requiresTurns = draft.requiresTurns,
                        turnDurationMinutes = draft.turnDurationMinutes,
                        turnIntervalMinutes = draft.turnIntervalMinutes,
                        plazoCancelacionHorasInput = draft.plazoCancelacionHoras?.toString().orEmpty(),
                        hasRubroContext = draft.rubroId > 0 && draft.subRubroIds.isNotEmpty(),
                    )
                }
            } else {
                _uiState.update {
                    it.copy(error = "Complete primero su rubro y especialidades")
                }
            }
        }
    }

    fun onWorkDayToggled(day: String, checked: Boolean) = _uiState.update {
        val updated = it.selectedWorkDays.toMutableSet()
        if (checked) updated.add(day) else updated.remove(day)
        it.copy(selectedWorkDays = updated, error = null)
    }

    fun onTimeSlotFromChanged(index: Int, value: String) = _uiState.update { state ->
        val slots = state.timeSlots.toMutableList()
        if (index in slots.indices) {
            val current = slots[index]
            val newTo = if (current.to.isNotBlank() && current.to > value) current.to else ""
            slots[index] = current.copy(from = value, to = newTo)
        }
        state.copy(timeSlots = slots, error = null)
    }

    fun onTimeSlotToChanged(index: Int, value: String) = _uiState.update { state ->
        val slots = state.timeSlots.toMutableList()
        if (index in slots.indices) {
            slots[index] = slots[index].copy(to = value)
        }
        state.copy(timeSlots = slots, error = null)
    }

    fun onAddTimeSlot() = _uiState.update {
        it.copy(timeSlots = it.timeSlots + RegisterTradeTimeSlotDraft("", ""))
    }

    fun onServesAtClientHomeChanged(value: Boolean) = _uiState.update { it.copy(servesAtClientHome = value, error = null) }
    fun onServesAtLocalChanged(value: Boolean) = _uiState.update { it.copy(servesAtLocal = value, error = null) }
    fun onRequiresTurnsChanged(value: Boolean) = _uiState.update {
        it.copy(
            requiresTurns = value,
            turnDurationMinutes = if (value && it.turnDurationMinutes == null) 60 else it.turnDurationMinutes,
            error = null,
        )
    }
    fun onTurnDurationChanged(minutes: Int?) = _uiState.update { it.copy(turnDurationMinutes = minutes, error = null) }
    fun onTurnIntervalChanged(minutes: Int?) = _uiState.update { it.copy(turnIntervalMinutes = minutes, error = null) }
    fun onPlazoCancelacionHorasChanged(value: String) = _uiState.update {
        it.copy(plazoCancelacionHorasInput = value.filter { char -> char.isDigit() }, error = null)
    }

    fun submit() {
        val state = _uiState.value
        if (!state.canSubmit) {
            val message = when {
                state.requiresTurns && state.turnDurationMinutes == null -> "Ingrese la duración del turno"
                !state.selectedWorkDays.any() -> "Seleccione al menos un día laboral"
                !state.timeSlots.any { it.from.isNotBlank() && it.to.isNotBlank() } -> "Configure al menos un horario"
                else -> "Complete días, horarios y dónde atiende"
            }
            _uiState.update { it.copy(error = message) }
            return
        }
        state.plazoCancelacionHorasError?.let { errorMessage ->
            _uiState.update { it.copy(error = errorMessage) }
            return
        }
        if (!state.servesAtClientHome && !state.servesAtLocal) {
            _uiState.update { it.copy(error = "Elija al menos una modalidad de atención") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val draft = registerTradeDraftRepository.get()
            if (draft == null || draft.subRubroIds.isEmpty()) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        hasRubroContext = false,
                        error = "Complete primero su rubro y especialidades",
                    )
                }
                return@launch
            }
            val updatedDraft = draft.copy(
                workDays = state.selectedWorkDays.toList(),
                timeSlots = state.timeSlots.filter { it.from.isNotBlank() && it.to.isNotBlank() },
                servesAtClientHome = state.servesAtClientHome,
                servesAtLocal = state.servesAtLocal,
                requiresTurns = state.requiresTurns,
                turnDurationMinutes = state.turnDurationMinutes,
                turnIntervalMinutes = state.turnIntervalMinutes,
                plazoCancelacionHoras = state.plazoCancelacionHoras,
            )
            registerTradeDraftRepository.save(updatedDraft)
            // POST /full solo con body completo (incluye metodosPago). Si faltan pagos, solo draft.
            if (updatedDraft.paymentMethods.isEmpty()) {
                _uiState.update { it.copy(isLoading = false) }
                _effects.emit(ProviderStepEffect.Saved)
                return@launch
            }
            if (!isProviderIdentityVerified(getOnboardingStatusUseCase)) {
                _uiState.update { it.copy(isLoading = false) }
                _effects.emit(ProviderStepEffect.Saved)
                return@launch
            }
            when (val result = submitProviderRubroFullUseCase(updatedDraft.toRubroFullRequest())) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _effects.emit(ProviderStepEffect.Saved)
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.error.userMessage())
                }
            }
        }
    }
}

data class ProviderLocalUiState(
    val barrioQuery: String = "",
    val barrioOptions: List<CatalogOption> = emptyList(),
    val isSearchingBarrios: Boolean = false,
    val selectedBarrioId: Long? = null,
    val calle: String = "",
    val numero: String = "",
    val nombre: String = "Local comercial",
    val location: AddressLocationSnapshot = AddressLocationSnapshot(),
    val isGeocoding: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val latitud: Double? get() = location.latitude
    val longitud: Double? get() = location.longitude
    val canSubmit: Boolean get() = selectedBarrioId != null && calle.isNotBlank()
}

@HiltViewModel
class ProviderLocalViewModel @Inject constructor(
    private val searchBarriosUseCase: SearchBarriosUseCase,
    private val submitProviderRubroFullUseCase: SubmitProviderRubroFullUseCase,
    private val registerTradeDraftRepository: RegisterTradeDraftRepository,
    private val syncRegisterTradeDraftFromProfileUseCase: SyncRegisterTradeDraftFromProfileUseCase,
    private val getOnboardingStatusUseCase: GetOnboardingStatusUseCase,
    private val geocodeLocalAddress: GeocodeLocalAddressUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProviderLocalUiState())
    val uiState: StateFlow<ProviderLocalUiState> = _uiState.asStateFlow()
    private val _effects = MutableSharedFlow<ProviderStepEffect>()
    val effects: SharedFlow<ProviderStepEffect> = _effects.asSharedFlow()
    private var searchJob: Job? = null
    private var geocodeJob: Job? = null

    init {
        viewModelScope.launch {
            syncRegisterTradeDraftFromProfileUseCase()?.let { draft ->
                if (draft.barrioId != null || draft.neighborhoodQuery.isNotBlank()) {
                    _uiState.update {
                        it.copy(
                            barrioQuery = draft.neighborhoodQuery,
                            selectedBarrioId = draft.barrioId,
                        )
                    }
                }
            }
        }
    }

    fun onBarrioQueryChanged(query: String) = searchBarrio(query, preserveSelection = false)

    fun onBarrioFocused() = searchBarrio(_uiState.value.barrioQuery, preserveSelection = true)

    private fun searchBarrio(query: String, preserveSelection: Boolean) {
        _uiState.update {
            it.copy(
                barrioQuery = query,
                selectedBarrioId = if (preserveSelection) it.selectedBarrioId else null,
                isSearchingBarrios = true,
            )
        }
        if (!preserveSelection) scheduleLiveGeocode()
        searchJob = viewModelScope.launchDebouncedBarrioSearch(
            previous = searchJob,
            query = query,
            searchBarrios = searchBarriosUseCase,
            onResult = { options ->
                _uiState.update { it.copy(isSearchingBarrios = false, barrioOptions = options) }
            },
        )
    }

    fun onBarrioSelected(option: CatalogOption) {
        searchJob?.cancel()
        _uiState.update {
            it.copy(
                barrioQuery = option.label,
                selectedBarrioId = option.id,
                barrioOptions = emptyList(),
                isSearchingBarrios = false,
            )
        }
        scheduleLiveGeocode()
    }

    fun dismissBarrioSuggestions() {
        searchJob?.cancel()
        _uiState.update { it.copy(barrioOptions = emptyList(), isSearchingBarrios = false) }
    }

    fun onCalleChanged(value: String) {
        _uiState.update { it.copy(calle = value, error = null) }
        scheduleLiveGeocode()
    }
    fun onNumeroChanged(value: String) {
        _uiState.update { it.copy(numero = value, error = null) }
        scheduleLiveGeocode()
    }
    fun onNombreChanged(value: String) = _uiState.update { it.copy(nombre = value, error = null) }
    fun onMapLocationSelected(latitude: Double, longitude: Double) {
        cancelLiveGeocode()
        _uiState.update {
            it.copy(
                location = FormosaAddressGeocoding.pinMoved(it.location, latitude, longitude),
                error = null,
            )
        }
    }
    fun locateOnMap() {
        cancelLiveGeocode()
        _uiState.update {
            val next = FormosaAddressGeocoding.openMap(it.location)
            it.copy(location = next, error = next.message)
        }
    }
    fun confirmPin() {
        cancelLiveGeocode()
        val confirmed = FormosaAddressGeocoding.confirmPin(_uiState.value.location)
        _uiState.update { it.copy(location = confirmed, error = confirmed.message) }
        if (confirmed.canPersist) persist()
    }

    fun submit() {
        cancelLiveGeocode()
        val state = _uiState.value
        if (state.selectedBarrioId == null || !state.canSubmit) {
            _uiState.update { it.copy(error = "Complete barrio, calle y número de su local.") }
            return
        }
        if (!state.location.canPersist) {
            resolveFromAddress()
            return
        }
        persist()
    }

    private fun scheduleLiveGeocode() {
        val state = _uiState.value
        geocodeJob = viewModelScope.launchDebouncedForwardGeocode(
            previous = geocodeJob,
            query = LocalAddressQuery(
                street = state.calle,
                number = state.numero,
                neighborhood = state.barrioQuery,
            ),
            geocode = geocodeLocalAddress,
            onResult = { result ->
                val outcome = (result as? AppResult.Success)?.data ?: return@launchDebouncedForwardGeocode
                _uiState.update {
                    it.copy(location = FormosaAddressGeocoding.applyLiveOutcome(outcome, it.location))
                }
            },
        )
    }

    private fun cancelLiveGeocode() {
        geocodeJob?.cancel()
        geocodeJob = null
    }

    private fun resolveFromAddress() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isGeocoding = true,
                    error = null,
                    location = it.location.copy(phase = AddressLocationPhase.Geocoding),
                )
            }
            when (
                val result = geocodeLocalAddress(
                    LocalAddressQuery(
                        street = state.calle,
                        number = state.numero,
                        neighborhood = state.barrioQuery,
                    ),
                )
            ) {
                is AppResult.Error -> _uiState.update {
                    it.copy(
                        isGeocoding = false,
                        location = AddressLocationSnapshot(
                            phase = AddressLocationPhase.NeedsMap,
                            message = FormosaAddressGeocoding.GEOCODER_ERROR_MESSAGE,
                        ),
                        error = FormosaAddressGeocoding.GEOCODER_ERROR_MESSAGE,
                    )
                }
                is AppResult.Success -> {
                    val snapshot = FormosaAddressGeocoding.snapshotFor(result.data)
                    _uiState.update {
                        it.copy(isGeocoding = false, location = snapshot, error = snapshot.message)
                    }
                }
            }
        }
    }

    private fun persist() {
        val state = _uiState.value
        val barrioId = state.selectedBarrioId ?: return
        val location = state.location
        if (!location.canPersist) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            providerIdentityGateMessage(getOnboardingStatusUseCase)?.let { message ->
                _uiState.update { it.copy(isLoading = false, error = message) }
                return@launch
            }
            val draft = registerTradeDraftRepository.get()
            if (draft == null || draft.subRubroIds.isEmpty()) {
                _uiState.update { it.copy(isLoading = false, error = "Complete primero rubro y subrubros") }
                return@launch
            }
            val updatedDraft = draft.copy(
                barrioId = barrioId,
                servesAtLocal = true,
            )
            registerTradeDraftRepository.save(updatedDraft)
            val request = updatedDraft.toRubroFullRequest().copy(
                domicilioLocal = ProviderLocalDomicilioRequest(
                    barrioId = barrioId,
                    calle = state.calle.trim().ifBlank { null },
                    numero = state.numero.trim().ifBlank { null },
                    nombre = state.nombre.trim().ifBlank { "Local comercial" },
                    latitud = GeoCoordinates.roundForApi(location.latitude),
                    longitud = GeoCoordinates.roundForApi(location.longitude),
                ),
            )
            when (val result = submitProviderRubroFullUseCase(request)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _effects.emit(ProviderStepEffect.Saved)
                }
                is AppResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.error.userMessage()) }
            }
        }
    }
}

data class ProviderPaymentMethodsUiState(
    val selectedMethods: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val canSubmit: Boolean get() = selectedMethods.isNotEmpty()
}

@HiltViewModel
class ProviderPaymentMethodsViewModel @Inject constructor(
    private val updateProviderProfessionalInfoUseCase: UpdateProviderProfessionalInfoUseCase,
    private val submitProviderRubroFullUseCase: SubmitProviderRubroFullUseCase,
    private val registerTradeDraftRepository: RegisterTradeDraftRepository,
    private val syncRegisterTradeDraftFromProfileUseCase: SyncRegisterTradeDraftFromProfileUseCase,
    private val getOnboardingStatusUseCase: GetOnboardingStatusUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProviderPaymentMethodsUiState())
    val uiState: StateFlow<ProviderPaymentMethodsUiState> = _uiState.asStateFlow()
    private val _effects = MutableSharedFlow<ProviderStepEffect>()
    val effects: SharedFlow<ProviderStepEffect> = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            syncRegisterTradeDraftFromProfileUseCase()?.let { draft ->
                _uiState.update {
                    it.copy(selectedMethods = draft.paymentMethods.toSet())
                }
            }
        }
    }

    fun onMethodToggled(method: String, checked: Boolean) = _uiState.update {
        val updated = it.selectedMethods.toMutableSet()
        if (checked) updated.add(method) else updated.remove(method)
        it.copy(selectedMethods = updated, error = null)
    }

    fun submit() {
        val methods = _uiState.value.selectedMethods.toList()
        if (methods.isEmpty()) {
            _uiState.update { it.copy(error = "Elija al menos un medio de pago") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val draft = registerTradeDraftRepository.get()
            val updatedDraft = draft?.copy(paymentMethods = methods)
            if (updatedDraft != null) {
                registerTradeDraftRepository.save(updatedDraft)
            }
            val canSubmitFull = updatedDraft != null &&
                updatedDraft.rubroId > 0 &&
                updatedDraft.subRubroIds.isNotEmpty() &&
                updatedDraft.workDays.isNotEmpty() &&
                (updatedDraft.servesAtClientHome || updatedDraft.servesAtLocal) &&
                updatedDraft.paymentMethods.isNotEmpty()
            if (canSubmitFull) {
                val fullDraft = updatedDraft!!
                if (!isProviderIdentityVerified(getOnboardingStatusUseCase)) {
                    _uiState.update { it.copy(isLoading = false) }
                    _effects.emit(ProviderStepEffect.Saved)
                    return@launch
                }
                when (val fullResult = submitProviderRubroFullUseCase(fullDraft.toRubroFullRequest())) {
                    is AppResult.Success -> {
                        updateProviderProfessionalInfoUseCase(
                            ProviderProfessionalInfoRequest(paymentMethods = methods),
                        )
                        _uiState.update { it.copy(isLoading = false) }
                        _effects.emit(ProviderStepEffect.Saved)
                        return@launch
                    }
                    is AppResult.Error -> {
                        _uiState.update {
                            it.copy(isLoading = false, error = fullResult.error.userMessage())
                        }
                        return@launch
                    }
                }
            }
            // Perfil ya tiene rubro: solo PATCH de medios de cobro.
            val result = updateProviderProfessionalInfoUseCase(
                ProviderProfessionalInfoRequest(paymentMethods = methods),
            )
            when (result) {
                is AppResult.Success -> {
                    when (val status = getOnboardingStatusUseCase.getProviderStatus()) {
                        is AppResult.Success -> {
                            if ("metodos_pago" in status.data.missingSteps) {
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        error = "No pudimos confirmar que sus métodos de pago se hayan guardado. " +
                                            "Su selección sigue disponible; intente nuevamente en unos minutos.",
                                    )
                                }
                            } else {
                                _uiState.update { it.copy(isLoading = false) }
                                _effects.emit(ProviderStepEffect.Saved)
                            }
                        }
                        is AppResult.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = "Guardamos la solicitud, pero no pudimos verificar el resultado. " +
                                        "Revise su conexión e intente nuevamente.",
                                )
                            }
                        }
                    }
                }
                is AppResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.error.userMessage()) }
                }
            }
        }
    }
}

@HiltViewModel
class ProviderFinalizeViewModel @Inject constructor(
    private val finalizeProviderProfileUseCase: FinalizeProviderProfileUseCase,
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _effects = MutableSharedFlow<ProviderStepEffect>()
    val effects: SharedFlow<ProviderStepEffect> = _effects.asSharedFlow()

    fun finalizeProfile() {
        viewModelScope.launch {
            _isLoading.update { true }
            when (val result = finalizeProviderProfileUseCase()) {
                is AppResult.Success -> {
                    _isLoading.update { false }
                    _effects.emit(ProviderStepEffect.Finalized)
                }
                is AppResult.Error -> _isLoading.update { false }
            }
        }
    }
}
