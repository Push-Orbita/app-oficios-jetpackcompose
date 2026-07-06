package com.cocido.oficios.feature.onboarding.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.ClientContactoRequest
import com.cocido.oficios.domain.usecase.profile.SaveClientContactoUseCase
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

data class ClientContactoUiState(
    val nombre: String = "",
    val telefono: String = "",
    val email: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val canSubmit: Boolean get() = nombre.isNotBlank() && telefono.isNotBlank()
}

sealed interface ClientContactoEffect {
    data object Saved : ClientContactoEffect
}

@HiltViewModel
class ClientContactoViewModel @Inject constructor(
    private val saveClientContactoUseCase: SaveClientContactoUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientContactoUiState())
    val uiState: StateFlow<ClientContactoUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<ClientContactoEffect>()
    val effects: SharedFlow<ClientContactoEffect> = _effects.asSharedFlow()

    fun onNombreChanged(value: String) = _uiState.update { it.copy(nombre = value, error = null) }
    fun onTelefonoChanged(value: String) = _uiState.update { it.copy(telefono = value, error = null) }
    fun onEmailChanged(value: String) = _uiState.update { it.copy(email = value, error = null) }

    fun submit() {
        val state = _uiState.value
        if (!state.canSubmit) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (
                val result = saveClientContactoUseCase(
                    ClientContactoRequest(
                        nombre = state.nombre.trim(),
                        telefonoWhatsapp = state.telefono.trim(),
                        email = state.email.trim().ifBlank { null },
                        favorito = true,
                    ),
                )
            ) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _effects.emit(ClientContactoEffect.Saved)
                }
                is AppResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.error.userMessage()) }
                }
            }
        }
    }
}
