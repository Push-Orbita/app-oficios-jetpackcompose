package com.cocido.oficios.feature.home.providerhome

import com.cocido.oficios.domain.model.ProviderHomeContent
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZonedDateTime

sealed interface ProviderHomeUiState {
    data object Loading : ProviderHomeUiState
    data class Success(
        val content: ProviderHomeContent,
        val now: ZonedDateTime,
        val isRefreshing: Boolean = false,
        val selectedAgendaDate: LocalDate = now.toLocalDate(),
        val visibleAgendaMonth: YearMonth = YearMonth.from(selectedAgendaDate),
        val userPickedAgendaDate: Boolean = false,
    ) : ProviderHomeUiState
    data class Error(
        val message: String,
        val isRefreshing: Boolean = false,
    ) : ProviderHomeUiState
}
