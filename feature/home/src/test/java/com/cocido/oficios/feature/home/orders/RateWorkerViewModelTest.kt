package com.cocido.oficios.feature.home.orders

import androidx.lifecycle.SavedStateHandle
import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.repository.OrderRepository
import com.cocido.oficios.domain.usecase.OrderUseCases
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// Robolectric: el ViewModel decodifica workerName con android.net.Uri.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@OptIn(ExperimentalCoroutinesApi::class)
class RateWorkerViewModelTest {

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
    fun `5 estrellas se envian como calidad 5 en escala 1-5`() = runTest(testDispatcher) {
        val orders = RatingOrderRepository()
        val viewModel = viewModel(orders)

        viewModel.onEvent(RateWorkerEvent.QualitySelected(5))
        viewModel.onEvent(RateWorkerEvent.PunctualitySelected(4))
        viewModel.onEvent(RateWorkerEvent.Submit)
        advanceUntilIdle()

        assertEquals(listOf(Triple(28L, 5, 4)), orders.calls)
        assertTrue(viewModel.uiState.value is RateWorkerUiState.Submitted)
    }

    @Test
    fun `cada estrella se envia tal cual, sin escalar`() = runTest(testDispatcher) {
        for (stars in 1..5) {
            val orders = RatingOrderRepository()
            val viewModel = viewModel(orders)
            viewModel.onEvent(RateWorkerEvent.QualitySelected(stars))
            viewModel.onEvent(RateWorkerEvent.PunctualitySelected(stars))
            viewModel.onEvent(RateWorkerEvent.Submit)
            advanceUntilIdle()
            assertEquals(stars, orders.calls.single().second)
            assertEquals(stars, orders.calls.single().third)
        }
    }

    @Test
    fun `error del backend se muestra en el formulario y como aviso`() = runTest(testDispatcher) {
        val orders = RatingOrderRepository(
            result = AppResult.Error(
                AppError.Validation(
                    message = "Datos de entrada no válidos",
                    fieldErrors = mapOf("calidad" to "calidad must not be greater than 5"),
                    backendCode = "VA01",
                ),
            ),
        )
        val viewModel = viewModel(orders)
        val effects = mutableListOf<RateWorkerEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.onEvent(RateWorkerEvent.QualitySelected(5))
        viewModel.onEvent(RateWorkerEvent.PunctualitySelected(5))
        viewModel.onEvent(RateWorkerEvent.Submit)
        advanceUntilIdle()
        job.cancel()

        val form = viewModel.uiState.value as RateWorkerUiState.Form
        assertFalse(form.isSubmitting)
        assertEquals(RateWorkerViewModel.RATING_VALIDATION_MESSAGE, form.submitError)
        assertEquals(5, form.qualityStars)
        assertEquals(5, form.punctualityStars)
        assertEquals(
            listOf<RateWorkerEffect>(RateWorkerEffect.ShowMessage(RateWorkerViewModel.RATING_VALIDATION_MESSAGE)),
            effects,
        )
    }

    @Test
    fun `editar un campo despues del error limpia el mensaje`() = runTest(testDispatcher) {
        val orders = RatingOrderRepository(result = AppResult.Error(AppError.Network(message = "Network error")))
        val viewModel = viewModel(orders)
        viewModel.onEvent(RateWorkerEvent.QualitySelected(3))
        viewModel.onEvent(RateWorkerEvent.PunctualitySelected(3))
        viewModel.onEvent(RateWorkerEvent.Submit)
        advanceUntilIdle()
        assertTrue((viewModel.uiState.value as RateWorkerUiState.Form).submitError != null)

        viewModel.onEvent(RateWorkerEvent.QualitySelected(4))

        val form = viewModel.uiState.value as RateWorkerUiState.Form
        assertNull(form.submitError)
        assertEquals(4, form.qualityStars)
    }

    private fun viewModel(orders: OrderRepository) = RateWorkerViewModel(
        savedStateHandle = SavedStateHandle(mapOf("bookingId" to 28L, "workerName" to "Juan")),
        orderUseCases = OrderUseCases(orders),
    )
}

private class RatingOrderRepository(
    private val result: AppResult<Unit> = AppResult.Success(Unit),
    delegate: OrderRepository = CodeOrderRepository(detailCode = null, currentCode = null),
) : OrderRepository by delegate {
    val calls = mutableListOf<Triple<Long, Int, Int>>()

    override suspend fun submitRating(
        bookingId: Long,
        quality: Int,
        punctuality: Int,
        comment: String?,
    ): AppResult<Unit> {
        calls += Triple(bookingId, quality, punctuality)
        return result
    }
}
