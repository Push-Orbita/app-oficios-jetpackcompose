package com.cocido.oficios.feature.auth.wizard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.designsystem.component.inputs.PhoneCountryOption
import com.cocido.oficios.core.designsystem.component.inputs.PhoneCountryOptions
import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.domain.model.RegistrationClientDraft
import com.cocido.oficios.domain.model.RegistrationCommitPhase
import com.cocido.oficios.domain.model.RegistrationDraft
import com.cocido.oficios.domain.model.RegisterTradeDraft
import com.cocido.oficios.domain.model.RegistrationPersonalDraft
import com.cocido.oficios.domain.model.RegistrationReviewSummary
import com.cocido.oficios.domain.model.RegistrationRole
import com.cocido.oficios.domain.repository.RegistrationDraftRepository
import com.cocido.oficios.domain.usecase.auth.RegistrationCommitResult
import com.cocido.oficios.domain.usecase.auth.RegistrationCommitter
import com.cocido.oficios.domain.usecase.catalog.SearchBarriosUseCase
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.usecase.catalog.DebouncedBarrioSearcher
import com.cocido.oficios.domain.validation.AuthValidators
import com.cocido.oficios.feature.auth.register.RegisterFormEvent
import com.cocido.oficios.feature.auth.register.RegisterFormUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegistrationWizardUiState(
    val role: RegistrationRole = RegistrationRole.CLIENT,
    val personal: RegisterFormUiState = RegisterFormUiState(),
    val contactName: String = "",
    val phone: String = "",
    val selectedCountry: PhoneCountryOption = PhoneCountryOptions.ARGENTINA,
    val barrioQuery: String = "",
    val barrioOptions: List<CatalogOption> = emptyList(),
    val isSearchingBarrios: Boolean = false,
    val barrioSearchError: String? = null,
    val selectedBarrioId: Long? = null,
    val calle: String = "",
    val numero: String = "",
    val piso: String = "",
    val departamento: String = "",
    val providerNeighborhoodQuery: String = "",
    val providerLocalCalle: String = "",
    val providerLocalNumero: String = "",
    val providerDraft: RegisterTradeDraft? = null,
    val profileError: String? = null,
    val commitError: String? = null,
    val isCommitting: Boolean = false,
    val commitPhase: RegistrationCommitPhase = RegistrationCommitPhase.NOT_STARTED,
    val createdUserEmail: String? = null,
) {
    val review: RegistrationReviewSummary
        get() = RegistrationReviewSummary.fromParts(
            role = role,
            firstName = personal.firstName,
            lastName = personal.lastName,
            email = personal.email,
            phone = phone,
            clientBarrio = barrioQuery,
            clientCalle = calle,
            clientNumero = numero,
            providerBarrio = providerNeighborhoodQuery,
            providerCalle = providerLocalCalle,
            providerNumero = providerLocalNumero,
            document = personal.document,
            contactName = contactName,
            piso = piso,
            departamento = departamento,
            provider = providerDraft,
        )

    val canContinueProfile: Boolean
        get() = contactName.isNotBlank() &&
            AuthValidators.validateArgentineMobile(phone) == null &&
            selectedBarrioId != null &&
            calle.isNotBlank()
}

sealed interface RegistrationWizardEffect {
    data object GoToProfile : RegistrationWizardEffect
    data object GoToOficio : RegistrationWizardEffect
    data object GoToZona : RegistrationWizardEffect
    data object GoToAgenda : RegistrationWizardEffect
    data object GoToCobro : RegistrationWizardEffect
    data object GoToIdentidad : RegistrationWizardEffect
    data object GoToReview : RegistrationWizardEffect
    data object GoToPersonal : RegistrationWizardEffect
    data object GoBack : RegistrationWizardEffect
    data object PopToProfile : RegistrationWizardEffect
    data object PopToOficio : RegistrationWizardEffect
    data object PopToZona : RegistrationWizardEffect
    data object PopToAgenda : RegistrationWizardEffect
    data object PopToCobro : RegistrationWizardEffect
    data object AccountCreated : RegistrationWizardEffect
    data object NavigateToLogin : RegistrationWizardEffect
    data object NavigateToTerms : RegistrationWizardEffect
}

@HiltViewModel
class RegistrationWizardViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val draftRepository: RegistrationDraftRepository,
    private val committer: RegistrationCommitter,
    private val searchBarriosUseCase: SearchBarriosUseCase,
) : ViewModel() {

    private val role: RegistrationRole = savedStateHandle.get<String>("role")
        ?.let { runCatching { RegistrationRole.valueOf(it) }.getOrNull() }
        ?: RegistrationRole.CLIENT

    private val _uiState = MutableStateFlow(RegistrationWizardUiState(role = role))
    val uiState: StateFlow<RegistrationWizardUiState> = _uiState.asStateFlow()

    private val _effects = Channel<RegistrationWizardEffect>(Channel.BUFFERED)
    val effects: Flow<RegistrationWizardEffect> = _effects.receiveAsFlow()

    /** ContraseÃ±a solo en memoria. Nunca se escribe en el draft. */
    private var passwordInMemory: String = ""
    private var confirmPasswordInMemory: String = ""
    private var providerDraftInMemory: RegisterTradeDraft? = null
    private var barrioSearcher: DebouncedBarrioSearcher? = null

    private fun barrioSearcher(): DebouncedBarrioSearcher {
        return barrioSearcher ?: DebouncedBarrioSearcher(
            scope = viewModelScope,
            searchBarrios = searchBarriosUseCase,
            onLoading = { _uiState.update { it.copy(isSearchingBarrios = true, barrioSearchError = null) } },
            onResult = { options ->
                _uiState.update { it.copy(isSearchingBarrios = false, barrioOptions = options, barrioSearchError = null) }
            },
            onError = { error ->
                _uiState.update {
                    it.copy(
                        isSearchingBarrios = false,
                        barrioOptions = emptyList(),
                        barrioSearchError = error.userMessage(),
                    )
                }
            },
        ).also { barrioSearcher = it }
    }

    init {
        viewModelScope.launch { restoreDraft() }
    }

    fun onPersonalEvent(event: RegisterFormEvent) {
        when (event) {
            is RegisterFormEvent.FirstNameChanged -> updatePersonal { it.copy(firstName = event.value) }
            is RegisterFormEvent.LastNameChanged -> updatePersonal { it.copy(lastName = event.value) }
            is RegisterFormEvent.EmailChanged -> updatePersonal { it.copy(email = event.value) }
            is RegisterFormEvent.PasswordChanged -> {
                passwordInMemory = event.value
                updatePersonal { it.copy(password = event.value) }
            }
            is RegisterFormEvent.ConfirmPasswordChanged -> {
                confirmPasswordInMemory = event.value
                updatePersonal { it.copy(confirmPassword = event.value) }
            }
            is RegisterFormEvent.DocumentChanged -> updatePersonal { it.copy(document = event.value) }
            is RegisterFormEvent.TermsChanged -> updatePersonal { it.copy(acceptedTerms = event.value) }
            RegisterFormEvent.Submit -> continueFromPersonal()
            RegisterFormEvent.LoginClicked -> emit(RegistrationWizardEffect.NavigateToLogin)
            RegisterFormEvent.TermsClicked -> emit(RegistrationWizardEffect.NavigateToTerms)
        }
    }

    fun onContactNameChanged(value: String) = updateProfile { it.copy(contactName = value, profileError = null) }
    fun onPhoneChanged(value: String) = updateProfile { it.copy(phone = value, profileError = null) }
    fun onCalleChanged(value: String) = updateProfile { it.copy(calle = value, profileError = null) }
    fun onNumeroChanged(value: String) = updateProfile { it.copy(numero = value, profileError = null) }
    fun onPisoChanged(value: String) = updateProfile { it.copy(piso = value) }
    fun onDepartamentoChanged(value: String) = updateProfile { it.copy(departamento = value) }

    fun onBarrioQueryChanged(query: String) = searchBarrio(query, preserveSelection = false)
    fun onBarrioFocused() = searchBarrio(_uiState.value.barrioQuery, preserveSelection = true)
    fun onBarrioSelected(option: CatalogOption) {
        barrioSearcher?.clearPending()
        // Do not submit the selected label as a new query â€” that used to re-open the loop.
        updateProfile {
            it.copy(
                barrioQuery = option.label,
                selectedBarrioId = option.id,
                barrioOptions = emptyList(),
                isSearchingBarrios = false,
                barrioSearchError = null,
                profileError = null,
            )
        }
    }
    fun retryBarrioSearch() {
        barrioSearcher()?.retry()
    }

    fun dismissBarrioSuggestions() {
        _uiState.update { it.copy(barrioOptions = emptyList(), isSearchingBarrios = false, barrioSearchError = null) }
    }

    fun continueFromProfile() {
        val state = _uiState.value
        if (!state.canContinueProfile) {
            _uiState.update { it.copy(profileError = "Complete nombre, WhatsApp, barrio y calle") }
            return
        }
        persistDraft()
        emit(RegistrationWizardEffect.GoToReview)
    }

    fun continueFromOficio(trade: RegisterTradeDraft?) {
        persistProvider(trade)
        emit(RegistrationWizardEffect.GoToZona)
    }

    fun continueFromZona(trade: RegisterTradeDraft?) {
        persistProvider(trade)
        emit(RegistrationWizardEffect.GoToAgenda)
    }

    fun continueFromAgenda(trade: RegisterTradeDraft?) {
        persistProvider(trade)
        emit(RegistrationWizardEffect.GoToCobro)
    }

    fun continueFromCobro(trade: RegisterTradeDraft?, servesAtLocal: Boolean) {
        persistProvider(trade)
        emit(if (servesAtLocal) RegistrationWizardEffect.GoToIdentidad else RegistrationWizardEffect.GoToReview)
    }

    fun providerDraft(): RegisterTradeDraft? = providerDraftInMemory

    fun continueFromIdentidad(trade: RegisterTradeDraft?) {
        persistProvider(trade)
        emit(RegistrationWizardEffect.GoToReview)
    }

    fun onReviewBack() = emit(RegistrationWizardEffect.GoBack)
    fun editPersonal() = emit(RegistrationWizardEffect.GoToPersonal)
    fun editProfile() = emit(RegistrationWizardEffect.PopToProfile)
    fun editOficio() = emit(RegistrationWizardEffect.PopToOficio)
    fun editZona() = emit(RegistrationWizardEffect.PopToZona)
    fun editAgenda() = emit(RegistrationWizardEffect.PopToAgenda)
    fun editCobro() = emit(RegistrationWizardEffect.PopToCobro)

    fun createAccount(providerDraft: RegisterTradeDraft? = null) {
        val state = _uiState.value
        if (state.isCommitting) return
        if (passwordInMemory.isBlank()) {
            _uiState.update { it.copy(commitError = "Volvé al primer paso e ingresá tu contraseña") }
            emit(RegistrationWizardEffect.GoToPersonal)
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isCommitting = true, commitError = null) }
            val snapshot = currentDraft(providerDraft)
            draftRepository.save(snapshot)
            when (val result = committer.commit(snapshot, passwordInMemory)) {
                RegistrationCommitResult.Completed -> {
                    passwordInMemory = ""
                    confirmPasswordInMemory = ""
                    _uiState.update { it.copy(isCommitting = false, personal = it.personal.copy(password = "", confirmPassword = "")) }
                    _effects.send(RegistrationWizardEffect.AccountCreated)
                }
                is RegistrationCommitResult.EmailConflict -> {
                    _uiState.update {
                        it.copy(
                            isCommitting = false,
                            personal = it.personal.copy(
                                emailError = result.message,
                                hasAttemptedSubmit = true,
                            ),
                        )
                    }
                    _effects.send(RegistrationWizardEffect.GoToPersonal)
                }
                is RegistrationCommitResult.Failed -> {
                    _uiState.update { it.copy(isCommitting = false, commitError = result.message) }
                }
            }
        }
    }

    private fun continueFromPersonal() {
        val personal = _uiState.value.personal.copy(hasAttemptedSubmit = true).validatedPersonal()
        _uiState.update { it.copy(personal = personal) }
        if (!personal.canSubmit) return
        persistDraft()
        if (_uiState.value.contactName.isBlank()) {
            val fullName = listOf(personal.firstName, personal.lastName).joinToString(" ").trim()
            _uiState.update { it.copy(contactName = fullName) }
        }
        emit(
            if (role == RegistrationRole.PROVIDER) {
                RegistrationWizardEffect.GoToOficio
            } else {
                RegistrationWizardEffect.GoToProfile
            },
        )
    }

    private suspend fun restoreDraft() {
        val draft = draftRepository.get() ?: return
        if (draft.role != role) return
        _uiState.update {
            it.copy(
                personal = RegisterFormUiState(
                    firstName = draft.personal.firstName,
                    lastName = draft.personal.lastName,
                    email = draft.personal.email,
                    document = draft.personal.document,
                    acceptedTerms = draft.personal.acceptedTerms,
                    password = passwordInMemory,
                    confirmPassword = confirmPasswordInMemory,
                ).validatedPersonal(),
                contactName = draft.client.contactName,
                phone = draft.client.phone,
                barrioQuery = draft.client.barrioQuery,
                selectedBarrioId = draft.client.barrioId,
                calle = draft.client.calle,
                numero = draft.client.numero,
                piso = draft.client.piso,
                departamento = draft.client.departamento,
                providerNeighborhoodQuery = draft.provider?.neighborhoodQuery.orEmpty(),
                providerLocalCalle = draft.provider?.localCalle.orEmpty(),
                providerLocalNumero = draft.provider?.localNumero.orEmpty(),
                providerDraft = draft.provider,
                commitPhase = draft.commitPhase,
                createdUserEmail = draft.createdUserEmail,
            )
        }
        providerDraftInMemory = draft.provider
    }

    private fun persistDraft() {
        val snapshot = currentDraft()
        viewModelScope.launch { draftRepository.save(snapshot) }
    }

    private fun persistProvider(trade: RegisterTradeDraft?) {
        if (trade != null) {
            providerDraftInMemory = trade
            _uiState.update { it.withProvider(trade) }
        }
        viewModelScope.launch { draftRepository.save(currentDraft(trade)) }
    }

    private fun RegistrationWizardUiState.withProvider(trade: RegisterTradeDraft) = copy(
        providerNeighborhoodQuery = trade.neighborhoodQuery,
        providerLocalCalle = trade.localCalle.orEmpty(),
        providerLocalNumero = trade.localNumero.orEmpty(),
        providerDraft = trade,
    )

    private fun currentDraft(providerDraft: RegisterTradeDraft? = null): RegistrationDraft {
        if (providerDraft != null) providerDraftInMemory = providerDraft
        val state = _uiState.value
        return RegistrationDraft(
            role = role,
            personal = RegistrationPersonalDraft(
                firstName = state.personal.firstName,
                lastName = state.personal.lastName,
                email = state.personal.email,
                document = state.personal.document,
                acceptedTerms = state.personal.acceptedTerms,
            ),
            client = RegistrationClientDraft(
                contactName = state.contactName,
                phone = state.phone,
                barrioQuery = state.barrioQuery,
                barrioId = state.selectedBarrioId,
                calle = state.calle,
                numero = state.numero,
                piso = state.piso,
                departamento = state.departamento,
            ),
            provider = providerDraftInMemory,
            commitPhase = state.commitPhase,
            createdUserEmail = state.createdUserEmail ?: state.personal.email.trim().ifBlank { null },
        )
    }

    private fun searchBarrio(query: String, preserveSelection: Boolean) {
        _uiState.update {
            it.copy(
                barrioQuery = query,
                selectedBarrioId = if (preserveSelection) it.selectedBarrioId else null,
                barrioSearchError = null,
            )
        }
        // DebouncedBarrioSearcher.distinctUntilChanged drops duplicate submits
        // (e.g. repeated onFocused from autocomplete expand). Loading flag is
        // set only when a real search starts.
        barrioSearcher().submit(query)
    }

    private fun updatePersonal(block: (RegisterFormUiState) -> RegisterFormUiState) {
        _uiState.update { state ->
            state.copy(personal = block(state.personal).validatedPersonal())
        }
        persistDraft()
    }

    private fun updateProfile(block: (RegistrationWizardUiState) -> RegistrationWizardUiState) {
        _uiState.update(block)
        persistDraft()
    }

    private fun emit(effect: RegistrationWizardEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }

    private fun RegisterFormUiState.validatedPersonal(): RegisterFormUiState {
        val firstNameError = AuthValidators.validateName(firstName, "nombres")
        val lastNameError = AuthValidators.validateName(lastName, "apellido")
        val emailError = AuthValidators.validateEmail(email)
        val passwordError = AuthValidators.validatePassword(password)
        val confirmPasswordError = AuthValidators.validatePasswordConfirmation(password, confirmPassword)
        val documentError = AuthValidators.validateDocument(document)
        val termsError = if (!acceptedTerms) "Debe aceptar los tÃ©rminos" else null
        val canSubmit = listOf(
            firstNameError, lastNameError, emailError, passwordError,
            confirmPasswordError, documentError, termsError,
        ).all { it == null }
        return copy(
            firstNameError = firstNameError,
            lastNameError = lastNameError,
            emailError = emailError,
            passwordError = passwordError,
            confirmPasswordError = confirmPasswordError,
            documentError = documentError,
            termsError = termsError,
            canSubmit = canSubmit,
        )
    }
}
