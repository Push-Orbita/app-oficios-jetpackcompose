package com.cocido.oficios.feature.auth.register

import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.domain.model.WeeklyAvailability

data class RegisterTradeTimeSlot(
    val from: String = "",
    val to: String = "",
)

/** Especialidades elegidas para un rubro, recordadas al cambiar a otro rubro. */
data class TradeSpecializationSelection(
    val selected: Set<String> = emptySet(),
    val other: String = "",
)

sealed interface RegisterTradeEvent {
    data class CategoryChanged(val value: String) : RegisterTradeEvent
    data class CategoryGridSelected(val option: CatalogOption) : RegisterTradeEvent
    data class TradeChanged(val value: String) : RegisterTradeEvent
    data class SpecializationToggled(val id: String, val checked: Boolean) : RegisterTradeEvent
    data class OtherSpecializationChanged(val value: String) : RegisterTradeEvent
    data class ExperienceRangeChanged(val value: String) : RegisterTradeEvent
    data class NeighborhoodQueryChanged(val value: String) : RegisterTradeEvent
    data object NeighborhoodFocused : RegisterTradeEvent
    data class NeighborhoodSelected(val option: CatalogOption) : RegisterTradeEvent
    data object NeighborhoodSuggestionsDismissed : RegisterTradeEvent
    data class TimeSlotFromChanged(val index: Int, val value: String) : RegisterTradeEvent
    data class TimeSlotToChanged(val index: Int, val value: String) : RegisterTradeEvent
    data object AddTimeSlot : RegisterTradeEvent
    data class WorkDayToggled(val day: String, val checked: Boolean) : RegisterTradeEvent
    data class IncludeSunday(val included: Boolean) : RegisterTradeEvent
    data class WeekDayToggled(val day: String, val enabled: Boolean) : RegisterTradeEvent
    data class WeekRangeFrom(val day: String, val index: Int, val value: String) : RegisterTradeEvent
    data class WeekRangeTo(val day: String, val index: Int, val value: String) : RegisterTradeEvent
    data class AddWeekRange(val day: String) : RegisterTradeEvent
    data class RemoveWeekRange(val day: String, val index: Int) : RegisterTradeEvent
    data class InsertWeekRange(val day: String, val index: Int, val from: String, val to: String) : RegisterTradeEvent
    data object CopyMondayToWeekdays : RegisterTradeEvent
    data class ServesAtClientHomeChanged(val value: Boolean) : RegisterTradeEvent
    data class ServesAtLocalChanged(val value: Boolean) : RegisterTradeEvent
    data class RequiresTurnsChanged(val value: Boolean) : RegisterTradeEvent
    data class TurnDurationChanged(val value: String) : RegisterTradeEvent
    data class TurnIntervalChanged(val value: String) : RegisterTradeEvent
    data class PaymentMethodToggled(val method: String, val checked: Boolean) : RegisterTradeEvent
    data class LocalCalleChanged(val value: String) : RegisterTradeEvent
    data class LocalNumeroChanged(val value: String) : RegisterTradeEvent
    data object Continue : RegisterTradeEvent
    data object RetryLoadCatalog : RegisterTradeEvent
}

data class RegisterTradeUiState(
    val categoryOptions: List<CatalogOption> = emptyList(),
    val tradeOptions: List<CatalogOption> = emptyList(),
    val specializationOptions: List<CatalogOption> = emptyList(),
    val experienceRanges: List<String> = emptyList(),
    val timeOptions: List<String> = emptyList(),
    val workDayOptions: List<String> = emptyList(),
    val selectedWorkDays: Set<String> = emptySet(),
    val servesAtClientHome: Boolean = true,
    val servesAtLocal: Boolean = false,
    val requiresTurns: Boolean = false,
    val turnDurationMinutes: String = "",
    val turnIntervalMinutes: String = "",
    val selectedCategoryId: Long? = null,
    val selectedTradeId: Long? = null,
    val selectedSpecializations: Set<String> = emptySet(),
    val otherSpecialization: String = "",
    val specializationsByTrade: Map<Long, TradeSpecializationSelection> = emptyMap(),
    val selectedExperienceRange: String = "",
    val neighborhoodQuery: String = "",
    val neighborhoodOptions: List<CatalogOption> = emptyList(),
    val isSearchingBarrios: Boolean = false,
    val selectedBarrioId: Long? = null,
    val selectedPaymentMethods: Set<String> = emptySet(),
    val localCalle: String = "",
    val localNumero: String = "",
    val weeklySchedule: WeeklyAvailability = WeeklyAvailability.default(),
    val timeSlots: List<RegisterTradeTimeSlot> = listOf(RegisterTradeTimeSlot("09:00", "18:00")),
    val isLoadingCatalog: Boolean = false,
    val isLoadingTrades: Boolean = false,
    val isLoadingSpecializations: Boolean = false,
    val isLoading: Boolean = false,
    val catalogError: String? = null,
    val canContinue: Boolean = false,
) {
    val categories: List<String> get() = categoryOptions.map { it.label }
    val trades: List<String> get() = tradeOptions.map { it.label }
    val specializationLabels: List<String> get() = specializationOptions.map { it.label }

    val selectedCategory: String
        get() = categoryOptions.firstOrNull { it.id == selectedCategoryId }?.label.orEmpty()

    val selectedTrade: String
        get() = tradeOptions.firstOrNull { it.id == selectedTradeId }?.label.orEmpty()

    val isTradeEnabled: Boolean get() = selectedCategoryId != null && !isLoadingTrades

    val canContinueOficio: Boolean
        get() = selectedCategoryId != null &&
            selectedTradeId != null &&
            selectedSpecializations.isNotEmpty() &&
            selectedExperienceRange.isNotBlank()

    val canContinueZona: Boolean
        get() = selectedBarrioId != null &&
            neighborhoodQuery.isNotBlank() &&
            (servesAtClientHome || servesAtLocal)

    val canContinueAgenda: Boolean
        get() = com.cocido.oficios.domain.validation.ScheduleValidator.validate(weeklySchedule) == null &&
            (!requiresTurns || turnDurationMinutes.toIntOrNull()?.let { it in 15..480 } == true)

    val canContinueCobro: Boolean
        get() = selectedPaymentMethods.isNotEmpty()

    val canContinueIdentidad: Boolean
        get() = !servesAtLocal || localCalle.isNotBlank()

    companion object {
        val defaultExperienceRanges = listOf(
            "0 a 2 años",
            "3 a 5 años",
            "Más de 5 años",
        )

        val defaultWorkDays = listOf(
            "LUNES" to "Lunes",
            "MARTES" to "Martes",
            "MIERCOLES" to "Miércoles",
            "JUEVES" to "Jueves",
            "VIERNES" to "Viernes",
            "SABADO" to "Sábado",
            "DOMINGO" to "Domingo",
        )

        val defaultSelectedWorkDays = setOf("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES")

        fun defaultTimeOptions(): List<String> = buildList {
            for (hour in 0 until 24) {
                for (minute in listOf(0, 30)) {
                    add("%02d:%02d".format(hour, minute))
                }
            }
        }
    }
}

sealed interface RegisterTradeEffect {
    data object NavigateToOnboarding : RegisterTradeEffect
}

enum class RegisterTradeFormSection {
    FULL,
    OFICIO,
    ZONA,
    AGENDA,
    COBRO,
    IDENTIDAD,
}
