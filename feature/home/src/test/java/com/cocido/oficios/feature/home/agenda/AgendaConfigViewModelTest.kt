package com.cocido.oficios.feature.home.agenda

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.AgendaSessionConfig
import com.cocido.oficios.domain.model.AgendaSlot
import com.cocido.oficios.domain.model.CreateAgendaSlotParams
import com.cocido.oficios.domain.model.ProviderSubRubroOption
import com.cocido.oficios.domain.model.SaveAgendaSessionParams
import com.cocido.oficios.domain.model.UpdateAgendaSlotParams
import com.cocido.oficios.domain.repository.AgendaRepository
import com.cocido.oficios.domain.repository.ProviderProfileRepository
import com.cocido.oficios.domain.usecase.AgendaUseCases
import com.cocido.oficios.domain.usecase.GetMySubRubrosUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AgendaConfigViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `save posts duration interval and units without requiring days or hours`() = runTest(testDispatcher) {
        val agenda = RecordingAgendaRepository()
        val viewModel = AgendaConfigViewModel(
            getMySubRubrosUseCase = GetMySubRubrosUseCase(FakeProviderProfileRepository()),
            agendaUseCases = AgendaUseCases(agenda),
        )
        advanceUntilIdle()

        val loaded = viewModel.uiState.value as AgendaConfigUiState.Success
        assertTrue(loaded.canSubmit)
        assertEquals(setOf(10L), loaded.selectedSubRubroIds)

        viewModel.onEvent(AgendaConfigEvent.DurationChanged("90"))
        viewModel.onEvent(AgendaConfigEvent.DurationUnitChanged(AgendaDurationUnitHours))
        viewModel.onEvent(AgendaConfigEvent.IntervalChanged("15"))
        viewModel.onEvent(AgendaConfigEvent.IntervalUnitChanged(AgendaDurationUnitMinutes))
        viewModel.onEvent(AgendaConfigEvent.Save)
        advanceUntilIdle()

        val saved = agenda.lastSave
        assertEquals(10L, saved?.subRubroId)
        assertEquals(90, saved?.durationMinutes)
        assertEquals(AgendaDurationUnitHours, saved?.durationUnit)
        assertEquals(15, saved?.intervalMinutes)
        assertEquals(AgendaDurationUnitMinutes, saved?.intervalUnit)
    }
}

private class FakeProviderProfileRepository : ProviderProfileRepository {
    override suspend fun getMySubRubros() = AppResult.Success(
        listOf(
            ProviderSubRubroOption(
                id = 10L,
                subRubroLabel = "Albañilería",
                rubroLabel = "Construcción",
                requiresTurns = true,
            ),
        ),
    )

    override suspend fun updateProfessionalInfo(request: com.cocido.oficios.domain.model.ProviderProfessionalInfoRequest) =
        unused()
    override suspend fun submitRubroFull(request: com.cocido.oficios.domain.model.ProviderRubroFullRequest) = unused()
    override suspend fun submitSecondaryRubroFull(request: com.cocido.oficios.domain.model.ProviderRubroFullRequest) =
        unused()
    override suspend fun finalizeProfile() = unused()
    override suspend fun loadProviderProfileForSync() = unused()

    private fun unused(): Nothing = error("unused")
}

private class RecordingAgendaRepository : AgendaRepository {
    var lastSave: SaveAgendaSessionParams? = null

    override suspend fun getSessionsForSubRubro(subRubroId: Long) =
        AppResult.Success(emptyList<AgendaSessionConfig>())

    override suspend fun saveSession(params: SaveAgendaSessionParams): AppResult<Unit> {
        lastSave = params
        return AppResult.Success(Unit)
    }

    override suspend fun deleteSession(sessionId: Long) = AppResult.Success(Unit)
    override suspend fun getAvailableSlots(subRubroId: Long, fromIso: String, toIso: String) =
        AppResult.Success(emptyList<AgendaSlot>())
    override suspend fun getSlotsForSubRubro(subRubroId: Long, fromIso: String?, toIso: String?) = unused()
    override suspend fun createSlot(params: CreateAgendaSlotParams) = unused()
    override suspend fun updateSlot(params: UpdateAgendaSlotParams) = unused()
    override suspend fun deleteSlot(slotId: Long) = unused()

    private fun unused(): Nothing = error("unused")
}
