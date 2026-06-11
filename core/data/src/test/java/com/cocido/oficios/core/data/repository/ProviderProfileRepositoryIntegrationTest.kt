package com.cocido.oficios.core.data.repository

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.data.remote.ProviderProfileRemoteDataSource
import com.cocido.oficios.core.data.test.NetworkTestHelper
import com.cocido.oficios.domain.model.ProviderProfessionalInfoRequest
import com.cocido.oficios.domain.model.ProviderRubroFullRequest
import com.cocido.oficios.domain.model.RegisterTradeTimeSlotDraft
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProviderProfileRepositoryIntegrationTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: ProviderProfileRepositoryImpl

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val api = NetworkTestHelper.createProviderProfileApi(server)
        repository = ProviderProfileRepositoryImpl(ProviderProfileRemoteDataSource(api))
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `updateProfessionalInfo returns success`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(NetworkTestHelper.loadFixture("profile/patch_trabajador_ok.json")),
        )

        val result = repository.updateProfessionalInfo(
            ProviderProfessionalInfoRequest(descripcion = "Plomero"),
        )

        assertTrue(result is AppResult.Success)
    }

    @Test
    fun `submitRubroFull maps validation error`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(400)
                .setBody(NetworkTestHelper.loadFixture("profile/rubro_full_error_dni.json")),
        )

        val result = repository.submitRubroFull(
            ProviderRubroFullRequest(
                rubroId = 10,
                subRubroIds = listOf(1),
                servesAtClientHome = true,
                servesAtLocal = false,
                workDays = listOf("LUNES"),
                timeSlots = listOf(RegisterTradeTimeSlotDraft("09:00", "18:00")),
            ),
        )

        assertTrue(result is AppResult.Error)
        val error = (result as AppResult.Error).error
        assertTrue(error is AppError.Validation)
        assertTrue(error.message.contains("DNI"))
    }
}
