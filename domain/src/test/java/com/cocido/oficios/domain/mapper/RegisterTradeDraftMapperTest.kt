package com.cocido.oficios.domain.mapper

import com.cocido.oficios.domain.model.RegisterTradeDraft
import com.cocido.oficios.domain.model.RegisterTradeTimeSlotDraft
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RegisterTradeDraftMapperTest {

    @Test
    fun `toRubroFullRequest maps work days and subrubros`() {
        val draft = RegisterTradeDraft(
            categoryId = 1,
            categoryName = "Hogar",
            rubroId = 11,
            rubroName = "Plomería",
            subRubroIds = listOf(101L, 102L),
            otherSpecialization = null,
            experienceRange = "3 a 5 años",
            neighborhoodQuery = "Centro",
            workDays = listOf("LUNES", "VIERNES"),
            servesAtClientHome = true,
            servesAtLocal = false,
            timeSlots = listOf(RegisterTradeTimeSlotDraft("09:00", "18:00")),
            paymentMethods = listOf("EFECTIVO", "TRANSFERENCIA"),
        )

        val request = draft.toRubroFullRequest()

        assertEquals(11L, request.rubroId)
        assertEquals(listOf(101L, 102L), request.subRubroIds)
        assertEquals(listOf("LUNES", "VIERNES"), request.workDays)
        assertTrue(request.servesAtClientHome)
        assertEquals(listOf("EFECTIVO", "TRANSFERENCIA"), request.paymentMethods)
    }

    @Test
    fun `toRubroFullRequest does not put barrio name in calle`() {
        val request = RegisterTradeDraft(
            categoryId = 1,
            categoryName = "Hogar",
            rubroId = 11,
            rubroName = "Plomería",
            subRubroIds = listOf(101L),
            otherSpecialization = null,
            experienceRange = "3 a 5 años",
            neighborhoodQuery = "Centro",
            barrioId = 8L,
            workDays = listOf("LUNES"),
            servesAtClientHome = false,
            servesAtLocal = true,
            timeSlots = listOf(RegisterTradeTimeSlotDraft("09:00", "18:00")),
        ).toRubroFullRequest()

        assertEquals(8L, request.domicilioLocal?.barrioId)
        assertNull(request.domicilioLocal?.calle)
        assertNull(request.domicilioLocal?.numero)
    }
}
