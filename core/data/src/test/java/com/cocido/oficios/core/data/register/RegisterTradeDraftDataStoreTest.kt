package com.cocido.oficios.core.data.register

import com.cocido.oficios.domain.model.RegisterTradeDraft
import com.cocido.oficios.domain.model.RegisterTradeTimeSlotDraft
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class RegisterTradeDraftDataStoreTest {

    private lateinit var dataStore: RegisterTradeDraftDataStore
    private val json = Json { ignoreUnknownKeys = true }

    @Before
    fun setup() {
        val context = RuntimeEnvironment.getApplication()
        dataStore = RegisterTradeDraftDataStore(context, json)
        runTest { dataStore.clear() }
    }

    @Test
    fun `save and get persists draft`() = runTest {
        val draft = sampleDraft()
        dataStore.save(draft)

        assertEquals(draft, dataStore.get())
    }

    @Test
    fun `clear removes draft`() = runTest {
        dataStore.save(sampleDraft())
        dataStore.clear()

        assertNull(dataStore.get())
    }

    @Test
    fun `draft survives new datastore instance`() = runTest {
        val context = RuntimeEnvironment.getApplication()
        val draft = sampleDraft()
        RegisterTradeDraftDataStore(context, json).save(draft)

        val reloaded = RegisterTradeDraftDataStore(context, json).get()

        assertEquals(draft.rubroId, reloaded?.rubroId)
        assertEquals(draft.workDays, reloaded?.workDays)
    }

    private fun sampleDraft() = RegisterTradeDraft(
        categoryId = 1,
        categoryName = "Hogar",
        rubroId = 10,
        rubroName = "Plomería",
        subRubroIds = listOf(100),
        otherSpecialization = null,
        experienceRange = "0 a 2 años",
        neighborhoodQuery = "Centro",
        workDays = listOf("LUNES", "VIERNES"),
        servesAtClientHome = true,
        servesAtLocal = false,
        timeSlots = listOf(RegisterTradeTimeSlotDraft("09:00", "18:00")),
    )
}
