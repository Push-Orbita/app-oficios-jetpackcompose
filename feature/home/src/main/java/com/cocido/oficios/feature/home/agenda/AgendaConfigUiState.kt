package com.cocido.oficios.feature.home.agenda

import com.cocido.oficios.domain.model.AgendaSessionConfig
import com.cocido.oficios.domain.model.ProviderSubRubroOption

internal const val AgendaDurationUnitMinutes = "minutos"
internal const val AgendaDurationUnitHours = "horas"
internal val AgendaDurationUnits = listOf(AgendaDurationUnitMinutes, AgendaDurationUnitHours)

sealed interface AgendaConfigUiState {
    data object Loading : AgendaConfigUiState

    data class Error(val message: String) : AgendaConfigUiState

    data class Success(
        val subRubroOptions: List<ProviderSubRubroOption> = emptyList(),
        val selectedSubRubroId: Long? = null,
        val selectedSubRubroIds: Set<Long> = emptySet(),
        val sessions: List<AgendaSessionConfig> = emptyList(),
        val isLoadingSessions: Boolean = false,
        val durationInput: String = "60",
        val durationUnit: String = AgendaDurationUnitMinutes,
        val intervalInput: String = "",
        val intervalUnit: String = AgendaDurationUnitMinutes,
        val isSaving: Boolean = false,
        val sessionPendingDeleteId: Long? = null,
        val isDeleting: Boolean = false,
        val hasAttemptedSubmit: Boolean = false,
        val error: String? = null,
    ) : AgendaConfigUiState {
        val selectedSubRubro: ProviderSubRubroOption?
            get() = subRubroOptions.firstOrNull { it.id == selectedSubRubroId }

        val durationError: String?
            get() = if (hasAttemptedSubmit && durationInput.toIntOrNull().let { it == null || it <= 0 }) {
                "Ingrese una duración válida"
            } else {
                null
            }

        val intervalError: String?
            get() {
                if (intervalInput.isBlank()) return null
                return if (hasAttemptedSubmit && intervalInput.toIntOrNull().let { it == null || it <= 0 }) {
                    "Ingrese un intervalo válido"
                } else {
                    null
                }
            }

        val oficioError: String?
            get() = if (hasAttemptedSubmit && selectedSubRubroIds.isEmpty() && selectedSubRubroId == null) {
                "Elija al menos un oficio"
            } else {
                null
            }

        val canSubmit: Boolean
            get() = durationInput.toIntOrNull()?.let { it > 0 } == true &&
                (selectedSubRubroIds.isNotEmpty() || selectedSubRubroId != null) &&
                (intervalInput.isBlank() || intervalInput.toIntOrNull()?.let { it > 0 } == true)
    }
}

sealed interface AgendaConfigEvent {
    data object Retry : AgendaConfigEvent
    data class SubRubroSelected(val subRubroId: Long) : AgendaConfigEvent
    data class DurationChanged(val value: String) : AgendaConfigEvent
    data class DurationUnitChanged(val unit: String) : AgendaConfigEvent
    data class IntervalChanged(val value: String) : AgendaConfigEvent
    data class IntervalUnitChanged(val unit: String) : AgendaConfigEvent
    data class OficioToggled(val subRubroId: Long) : AgendaConfigEvent
    data object Save : AgendaConfigEvent
    data class RequestDeleteSession(val sessionId: Long) : AgendaConfigEvent
    data object ConfirmDeleteSession : AgendaConfigEvent
    data object DismissDeleteSession : AgendaConfigEvent
}

sealed interface AgendaConfigEffect {
    data class ShowMessage(val message: String) : AgendaConfigEffect
    data class ShowError(val message: String) : AgendaConfigEffect
}
