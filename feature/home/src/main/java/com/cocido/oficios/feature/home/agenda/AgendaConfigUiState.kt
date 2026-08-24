package com.cocido.oficios.feature.home.agenda

import com.cocido.oficios.domain.model.AgendaSessionConfig
import com.cocido.oficios.domain.model.ProviderSubRubroOption

sealed interface AgendaConfigUiState {
    data object Loading : AgendaConfigUiState

    data class Error(val message: String) : AgendaConfigUiState

    data class Success(
        val subRubroOptions: List<ProviderSubRubroOption> = emptyList(),
        val selectedSubRubroId: Long? = null,
        val sessions: List<AgendaSessionConfig> = emptyList(),
        val isLoadingSessions: Boolean = false,
        val durationInput: String = "60",
        val intervalInput: String = "",
        val isSaving: Boolean = false,
        val hasAttemptedSubmit: Boolean = false,
        val error: String? = null,
    ) : AgendaConfigUiState {
        val selectedSubRubro: ProviderSubRubroOption?
            get() = subRubroOptions.firstOrNull { it.id == selectedSubRubroId }

        val durationError: String?
            get() = if (hasAttemptedSubmit && durationInput.toIntOrNull().let { it == null || it <= 0 }) {
                "Ingresá una duración válida en minutos"
            } else {
                null
            }

        val canSubmit: Boolean
            get() = durationInput.toIntOrNull()?.let { it > 0 } == true
    }
}

sealed interface AgendaConfigEvent {
    data object Retry : AgendaConfigEvent
    data class SubRubroSelected(val subRubroId: Long) : AgendaConfigEvent
    data class DurationChanged(val value: String) : AgendaConfigEvent
    data class IntervalChanged(val value: String) : AgendaConfigEvent
    data object Save : AgendaConfigEvent
    data class DeleteSession(val sessionId: Long) : AgendaConfigEvent
}

sealed interface AgendaConfigEffect {
    data class ShowMessage(val message: String) : AgendaConfigEffect
}
