package com.cocido.oficios.feature.onboarding.provider

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.InitialDestination
import com.cocido.oficios.domain.model.OnboardingStep
import com.cocido.oficios.domain.repository.RegisterTradeDraftRepository
import com.cocido.oficios.domain.usecase.auth.GetOnboardingStatusUseCase
import com.cocido.oficios.domain.usecase.auth.ResolveInitialDestinationUseCase
import com.cocido.oficios.domain.usecase.profile.FinalizeProviderProfileUseCase
import com.cocido.oficios.domain.usecase.profile.RetryPendingTradeDraftUseCase
import com.cocido.oficios.domain.usecase.profile.SyncRegisterTradeDraftFromProfileUseCase
import com.cocido.oficios.feature.onboarding.common.OnboardingChecklistEffect
import com.cocido.oficios.feature.onboarding.common.OnboardingChecklistUiState
import com.cocido.oficios.feature.onboarding.common.OnboardingCompletionNavigator
import com.cocido.oficios.feature.onboarding.common.OnboardingCompletionPhase
import com.cocido.oficios.feature.onboarding.common.shouldAutoFinalizeProviderProfile
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

sealed interface ProviderStatusUiState {
    data object Loading : ProviderStatusUiState
    data class Pending(val steps: List<OnboardingStep>) : ProviderStatusUiState
    data class Rejected(val reason: String?, val steps: List<OnboardingStep>) : ProviderStatusUiState
    data class Error(val message: String) : ProviderStatusUiState
}

@HiltViewModel
class ProviderOnboardingViewModel @Inject constructor(
    private val getOnboardingStatusUseCase: GetOnboardingStatusUseCase,
    private val registerTradeDraftRepository: RegisterTradeDraftRepository,
    private val syncRegisterTradeDraftFromProfileUseCase: SyncRegisterTradeDraftFromProfileUseCase,
    private val retryPendingTradeDraftUseCase: RetryPendingTradeDraftUseCase,
    private val finalizeProviderProfileUseCase: FinalizeProviderProfileUseCase,
    private val resolveInitialDestinationUseCase: ResolveInitialDestinationUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<OnboardingChecklistUiState>(OnboardingChecklistUiState.Loading)
    val uiState: StateFlow<OnboardingChecklistUiState> = _uiState.asStateFlow()

    private val _completionPhase = MutableStateFlow<OnboardingCompletionPhase>(OnboardingCompletionPhase.Idle)
    val completionPhase: StateFlow<OnboardingCompletionPhase> = _completionPhase.asStateFlow()

    private val _completionError = MutableStateFlow<String?>(null)
    val completionError: StateFlow<String?> = _completionError.asStateFlow()

    private val _effects = MutableSharedFlow<OnboardingChecklistEffect>(replay = 1)
    val effects: SharedFlow<OnboardingChecklistEffect> = _effects.asSharedFlow()

    private val _kycBannerMessage = MutableStateFlow<String?>(null)
    val kycBannerMessage: StateFlow<String?> = _kycBannerMessage.asStateFlow()

    private var lastIdentityVerified: Boolean? = null

    init {
        load()
    }

    fun dismissKycBanner() {
        _kycBannerMessage.update { null }
    }

    fun load() {
        if (_completionPhase.value != OnboardingCompletionPhase.Idle) return

        viewModelScope.launch {
            _uiState.update { OnboardingChecklistUiState.Loading }
            when (val result = getOnboardingStatusUseCase.getProviderStatus()) {
                is AppResult.Success -> {
                    val status = syncTradeDraftIfNeeded(result.data)
                    if (lastIdentityVerified == false && status.isIdentityVerified) {
                        _kycBannerMessage.update {
                            "Su documentación fue actualizada. Revise el estado en el checklist."
                        }
                    }
                    lastIdentityVerified = status.isIdentityVerified
                    val steps = getOnboardingStatusUseCase.getProviderSteps(status)
                    when {
                        steps.shouldAutoFinalizeProviderProfile() -> finalizeProfile()
                        steps.isEmpty() -> completeAndNavigate()
                        else -> _uiState.update { OnboardingChecklistUiState.Success(steps) }
                    }
                }
                is AppResult.Error -> {
                    _uiState.update { OnboardingChecklistUiState.Error("No se pudo cargar el estado del perfil") }
                }
            }
        }
    }

    private suspend fun syncTradeDraftIfNeeded(
        status: com.cocido.oficios.domain.model.ProviderProfileStatus,
    ): com.cocido.oficios.domain.model.ProviderProfileStatus {
        syncRegisterTradeDraftFromProfileUseCase()

        if (status.missingSteps.none { it in TRADE_DRAFT_SYNC_TRIGGER_STEPS }) return status
        if (!status.isIdentityVerified) return status

        val draft = registerTradeDraftRepository.get()
        if (draft == null || draft.subRubroIds.isEmpty()) return status

        retryPendingTradeDraftUseCase()

        return when (val refreshed = getOnboardingStatusUseCase.getProviderStatus()) {
            is AppResult.Success -> refreshed.data
            is AppResult.Error -> status
        }
    }

    fun finalizeProfile() {
        if (_completionPhase.value != OnboardingCompletionPhase.Idle) return

        viewModelScope.launch {
            _completionPhase.update { OnboardingCompletionPhase.Submitting }
            _completionError.update { null }
            when (val finalizeResult = finalizeProviderProfileUseCase()) {
                is AppResult.Success -> {
                    when (val destination = resolveInitialDestinationUseCase()) {
                        is AppResult.Success -> {
                            OnboardingCompletionNavigator.navigateAfterCompletion(
                                destination = destination.data,
                                phase = _completionPhase,
                                effects = _effects,
                            )
                        }
                        is AppResult.Error -> {
                            _completionPhase.update { OnboardingCompletionPhase.Idle }
                            _completionError.update { destination.error.userMessage() }
                            reloadChecklist()
                        }
                    }
                }
                is AppResult.Error -> {
                    _completionPhase.update { OnboardingCompletionPhase.Idle }
                    _completionError.update {
                        finalizeResult.error.userMessage().ifBlank {
                            "No se pudo finalizar el perfil. Revise los pasos pendientes."
                        }
                    }
                    reloadChecklist()
                }
            }
        }
    }

    private suspend fun completeAndNavigate() {
        OnboardingCompletionNavigator.resolveAndNavigate(
            resolveInitialDestinationUseCase = resolveInitialDestinationUseCase,
            stuckDestination = InitialDestination.ProviderOnboarding,
            fallbackDestination = null,
            phase = _completionPhase,
            effects = _effects,
            onStuck = {
                _uiState.update {
                    OnboardingChecklistUiState.Error(
                        "No encontramos pasos pendientes, pero su perfil aún no está listo para operar.",
                    )
                }
            },
            onResolveError = { message ->
                _uiState.update { OnboardingChecklistUiState.Error(message) }
            },
        )
    }

    private suspend fun reloadChecklist() {
        when (val result = getOnboardingStatusUseCase.getProviderStatus()) {
            is AppResult.Success -> {
                val steps = getOnboardingStatusUseCase.getProviderSteps(result.data)
                if (steps.isEmpty()) {
                    completeAndNavigate()
                } else {
                    _uiState.update { OnboardingChecklistUiState.Success(steps) }
                }
            }
            is AppResult.Error -> {
                _uiState.update { OnboardingChecklistUiState.Error("No se pudo cargar el estado del perfil") }
            }
        }
    }

    private companion object {
        // POST /trabajador-oficio-rubro/full sólo corresponde cuando todavía no existe
        // el servicio principal. Usarlo para disponibilidad, pagos o descripción sobre
        // un perfil existente intenta duplicar el rubro principal.
        val TRADE_DRAFT_SYNC_TRIGGER_STEPS = setOf(
            "rubro_principal",
            "subrubros",
        )
    }
}

@HiltViewModel
class ProviderPendingViewModel @Inject constructor(
    private val getOnboardingStatusUseCase: GetOnboardingStatusUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProviderStatusUiState>(ProviderStatusUiState.Loading)
    val uiState: StateFlow<ProviderStatusUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            when (val result = getOnboardingStatusUseCase.getProviderStatus()) {
                is AppResult.Success -> {
                    val steps = getOnboardingStatusUseCase.getProviderSteps(result.data)
                    _uiState.update { ProviderStatusUiState.Pending(steps) }
                }
                is AppResult.Error -> {
                    _uiState.update { ProviderStatusUiState.Error("No se pudo cargar el estado") }
                }
            }
        }
    }
}

@HiltViewModel
class ProviderRejectedViewModel @Inject constructor(
    private val getOnboardingStatusUseCase: GetOnboardingStatusUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProviderStatusUiState>(ProviderStatusUiState.Loading)
    val uiState: StateFlow<ProviderStatusUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            when (val result = getOnboardingStatusUseCase.getProviderStatus()) {
                is AppResult.Success -> {
                    val steps = getOnboardingStatusUseCase.getProviderSteps(result.data)
                    _uiState.update {
                        ProviderStatusUiState.Rejected(
                            reason = result.data.rejectionReason,
                            steps = steps,
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update { ProviderStatusUiState.Error("No se pudo cargar el estado") }
                }
            }
        }
    }
}
