package com.cocido.oficios.feature.onboarding.client

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.ClientContactoRequest
import com.cocido.oficios.domain.model.ClientDomicilioRequest
import com.cocido.oficios.domain.repository.ClientProfileRepository
import com.cocido.oficios.domain.usecase.profile.SaveClientContactoUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ClientContactoViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `empty form cannot submit`() {
        val viewModel = createViewModel()
        assertFalse(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun `valid form emits saved effect`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        val effects = mutableListOf<ClientContactoEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.onNombreChanged("Juan")
        viewModel.onTelefonoChanged("1122334455")
        viewModel.submit()
        advanceUntilIdle()

        assertTrue(effects.any { it is ClientContactoEffect.Saved })
        job.cancel()
    }

    private fun createViewModel(): ClientContactoViewModel = ClientContactoViewModel(
        saveClientContactoUseCase = SaveClientContactoUseCase(
            object : ClientProfileRepository {
                override suspend fun saveDomicilio(request: ClientDomicilioRequest) = AppResult.Success(Unit)
                override suspend fun saveContacto(request: ClientContactoRequest) = AppResult.Success(Unit)
            },
        ),
    )
}
