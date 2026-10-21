package com.cocido.oficios.feature.settings.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.UpdateAccountProfileRequest
import com.cocido.oficios.domain.usecase.account.AccountUseCases
import com.cocido.oficios.domain.validation.AuthValidators
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PersonalDataViewModel @Inject constructor(
    private val accountUseCases: AccountUseCases,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PersonalDataUiState())
    val uiState: StateFlow<PersonalDataUiState> = _uiState.asStateFlow()

    private val _effects = Channel<PersonalDataEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        load()
    }

    fun onEvent(event: PersonalDataEvent) {
        when (event) {
            PersonalDataEvent.Retry -> load()
            is PersonalDataEvent.FirstNameChanged -> _uiState.update {
                it.copy(firstName = event.value, firstNameError = null)
            }
            is PersonalDataEvent.LastNameChanged -> _uiState.update {
                it.copy(lastName = event.value, lastNameError = null)
            }
            is PersonalDataEvent.EmailChanged -> _uiState.update {
                it.copy(email = event.value, emailError = null)
            }
            is PersonalDataEvent.PhotoSelected -> _uiState.update {
                it.copy(selectedPhotoPath = event.path, removePhoto = false)
            }
            PersonalDataEvent.RemovePhoto -> _uiState.update {
                it.copy(selectedPhotoPath = null, removePhoto = true)
            }
            PersonalDataEvent.Save -> save()
            PersonalDataEvent.Back -> emitEffect(PersonalDataEffect.NavigateBack)
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = accountUseCases.getProfile()) {
                is AppResult.Success -> {
                    val user = result.data.user
                    _uiState.update {
                        PersonalDataUiState(
                            isLoading = false,
                            userId = user.id,
                            firstName = user.firstName,
                            lastName = user.lastName,
                            email = user.email,
                            currentPhotoUrl = user.profilePhotoUrl,
                        )
                    }
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.error.userMessage())
                }
            }
        }
    }

    private fun save() {
        val state = _uiState.value
        val firstNameError = AuthValidators.validateName(state.firstName, "nombres")
        val lastNameError = AuthValidators.validateName(state.lastName, "apellido")
        val emailError = AuthValidators.validateEmail(state.email.trim())
        if (firstNameError != null || lastNameError != null || emailError != null) {
            _uiState.update {
                it.copy(
                    firstNameError = firstNameError,
                    lastNameError = lastNameError,
                    emailError = emailError,
                )
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val request = UpdateAccountProfileRequest(
                userId = state.userId,
                firstName = state.firstName.trim(),
                lastName = state.lastName.trim(),
                email = state.email.trim(),
                currentPhotoUrl = state.currentPhotoUrl,
                photoFilePath = state.selectedPhotoPath,
                removePhoto = state.removePhoto,
            )
            when (val result = accountUseCases.updateProfile(request)) {
                is AppResult.Success -> {
                    val user = result.data.user
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            firstName = user.firstName,
                            lastName = user.lastName,
                            email = user.email,
                            currentPhotoUrl = user.profilePhotoUrl,
                            selectedPhotoPath = null,
                            removePhoto = false,
                        )
                    }
                    emitEffect(PersonalDataEffect.Saved("Tus datos se actualizaron correctamente."))
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isSaving = false, errorMessage = result.error.userMessage())
                }
            }
        }
    }

    private fun emitEffect(effect: PersonalDataEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}
