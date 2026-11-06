package com.cocido.oficios.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AddServiceDraftCodecTest {

    @Test
    fun `round trip keeps every user field`() {
        val draft = AddServiceDraft(
            step = "ATTENTION",
            categoryId = 1L,
            rubroId = 2L,
            subRubroIds = listOf(21L, 22L),
            subRubroIdsByRubro = mapOf(3L to listOf(31L)),
            servesAtLocal = true,
            plazoCancelacionHorasInput = "12",
            localBarrioId = 7L,
            localBarrioQuery = "Centro",
            localStreet = "Av. 25 de Mayo",
            localNumber = "450",
            localLatitude = -26.18,
            localLongitude = -58.17,
            localPinConfirmed = true,
            paymentMethods = listOf("EFECTIVO"),
            availabilityDrafts = mapOf(1L to WeeklyAvailability.default()),
        )

        assertEquals(draft, AddServiceDraftCodec.decode(AddServiceDraftCodec.encode(draft)))
    }

    @Test
    fun `corrupt payload is ignored`() {
        assertNull(AddServiceDraftCodec.decode("{no-json"))
        assertNull(AddServiceDraftCodec.decode(null))
    }
}
