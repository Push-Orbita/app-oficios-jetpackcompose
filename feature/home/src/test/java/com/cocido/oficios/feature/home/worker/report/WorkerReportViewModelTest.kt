package com.cocido.oficios.feature.home.worker.report

import androidx.lifecycle.SavedStateHandle
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.ReportReason
import com.cocido.oficios.domain.model.SubmittedReport
import com.cocido.oficios.domain.repository.ReportsRepository
import com.cocido.oficios.domain.usecase.safety.GetReportReasonsUseCase
import com.cocido.oficios.domain.usecase.safety.SubmitReportUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
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
class WorkerReportViewModelTest {

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
    fun `loads reasons and submits report`() = runTest(testDispatcher) {
        val repository = FakeReportsRepository()
        val viewModel = WorkerReportViewModel(
            savedStateHandle = SavedStateHandle(mapOf("workerId" to 9L, "workerName" to "Matias")),
            getReportReasonsUseCase = GetReportReasonsUseCase(repository),
            submitReportUseCase = SubmitReportUseCase(repository),
        )
        advanceUntilIdle()

        assertEquals("Matias", viewModel.uiState.value.workerName)
        assertEquals(1, viewModel.uiState.value.reasons.size)

        viewModel.onEvent(WorkerReportEvent.ReasonSelected("Estafa o cobro indebido"))
        viewModel.onEvent(WorkerReportEvent.DescriptionChanged("El perfil no corresponde a la persona que se presentó."))
        val effects = mutableListOf<WorkerReportEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }
        viewModel.onEvent(WorkerReportEvent.Submit)
        advanceUntilIdle()
        job.cancel()

        assertEquals("ESTAFA", repository.lastReasonCode)
        assertTrue(effects.any { it is WorkerReportEffect.NavigateBack })
    }

    @Test
    fun `short description does not call repository`() = runTest(testDispatcher) {
        val repository = FakeReportsRepository()
        val viewModel = WorkerReportViewModel(
            savedStateHandle = SavedStateHandle(mapOf("workerId" to 9L, "workerName" to "Matias")),
            getReportReasonsUseCase = GetReportReasonsUseCase(repository),
            submitReportUseCase = SubmitReportUseCase(repository),
        )
        advanceUntilIdle()
        viewModel.onEvent(WorkerReportEvent.ReasonSelected("Estafa o cobro indebido"))
        viewModel.onEvent(WorkerReportEvent.DescriptionChanged("corto"))
        viewModel.onEvent(WorkerReportEvent.Submit)
        advanceUntilIdle()

        assertEquals(0, repository.submitCount)
        assertTrue(!viewModel.uiState.value.canSubmit)
    }
}

private class FakeReportsRepository : ReportsRepository {
    var lastReasonCode: String? = null
    var submitCount = 0

    override suspend fun getReasons() = AppResult.Success(
        listOf(ReportReason("ESTAFA", "Estafa o cobro indebido")),
    )

    override suspend fun submit(
        workerId: Long,
        reasonCode: String,
        description: String,
        idempotencyKey: String,
        bookingId: Long?,
    ): AppResult<SubmittedReport> {
        submitCount += 1
        lastReasonCode = reasonCode
        return AppResult.Success(SubmittedReport(id = 1, status = "RECIBIDA"))
    }
}
