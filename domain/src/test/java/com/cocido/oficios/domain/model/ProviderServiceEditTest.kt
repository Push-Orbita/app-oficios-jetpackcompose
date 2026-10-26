package com.cocido.oficios.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProviderServiceEditTest {

    @Test
    fun `availability puts do not clone wizard week onto sibling subs`() {
        val first = week("14:00", "18:00")
        val second = week("09:00", "18:00")
        val puts = availabilityPutsForEdit(
            persistedSubIds = listOf(35, 36),
            originalBySubId = mapOf(35L to first, 36L to second),
            editorSourceSubId = 35L,
            wizardWeek = first,
        )
        assertTrue(puts.isEmpty())
    }

    @Test
    fun `dirty editor puts only that subrubro week`() {
        val first = week("14:00", "18:00")
        val second = week("09:00", "18:00")
        val dirty = week("16:00", "20:00")
        val puts = availabilityPutsForEdit(
            persistedSubIds = listOf(35, 36),
            originalBySubId = mapOf(35L to first, 36L to second),
            editorSourceSubId = 35L,
            wizardWeek = dirty,
        )
        assertEquals(listOf(35L), puts.map { it.subRubroId })
        assertEquals(dirty, puts.single().week)
    }

    @Test
    fun `draft of a sibling is put without cloning the editor week`() {
        val first = week("14:00", "18:00")
        val second = week("09:00", "18:00")
        val secondDraft = week("10:00", "12:00")
        val puts = availabilityPutsForEdit(
            persistedSubIds = listOf(35, 36),
            originalBySubId = mapOf(35L to first, 36L to second),
            editorSourceSubId = 35L,
            wizardWeek = first,
            draftBySubId = mapOf(36L to secondDraft),
        )
        assertEquals(listOf(36L), puts.map { it.subRubroId })
        assertEquals(secondDraft, puts.single().week)
    }

    @Test
    fun `new subrubro receives wizard week without touching siblings`() {
        val first = week("14:00", "18:00")
        val second = week("09:00", "18:00")
        val puts = availabilityPutsForEdit(
            persistedSubIds = listOf(35, 36, 99),
            originalBySubId = mapOf(35L to first, 36L to second),
            editorSourceSubId = 35L,
            wizardWeek = first,
        )
        assertEquals(listOf(99L), puts.map { it.subRubroId })
        assertEquals(first, puts.single().week)
    }

    private fun week(from: String, to: String) = WeeklyAvailability(
        days = listOf(DayAvailability("LUNES", listOf(TimeRange(from, to)))),
        includeSunday = false,
    )
}
