package com.cocido.oficios.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProviderServiceEditTest {

    @Test
    fun `plan creates only new catalog ids and keeps existing`() {
        val existing = listOf(sub(id = 21, catalog = 100), sub(id = 22, catalog = 101))
        val plan = planSubRubroEdits(existing, listOf(100, 101, 200))
        assertEquals(listOf(200L), plan.toCreateCatalogIds)
        assertEquals(emptyList<Long>(), plan.toDeleteIds)
        assertEquals(listOf(21L, 22L), plan.toKeep.map { it.id })
    }

    @Test
    fun `plan deletes removed catalog ids`() {
        val existing = listOf(sub(id = 21, catalog = 100), sub(id = 22, catalog = 101))
        val plan = planSubRubroEdits(existing, listOf(101))
        assertEquals(emptyList<Long>(), plan.toCreateCatalogIds)
        assertEquals(listOf(21L), plan.toDeleteIds)
        assertEquals(listOf(22L), plan.toKeep.map { it.id })
    }

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

    @Test
    fun `merged display uses specialty hours not a default 09 to 18`() {
        val merged = listOf(week("14:00", "18:00")).mergedForDisplay()
        assertEquals(listOf(TimeRange("14:00", "18:00")), merged.rangesOn("LUNES"))
        val schedules = merged.toManagedSchedules()
        assertEquals("14:00", schedules.first().ranges.first().from)
        assertEquals("18:00", schedules.first().ranges.first().to)
    }

    private fun sub(id: Long, catalog: Long) = ManagedProviderSubService(
        id = id,
        catalogSubRubroId = catalog,
        name = "Esp $catalog",
        servesAtClientHome = true,
        servesAtLocal = false,
        requiresTurns = false,
    )

    private fun week(from: String, to: String) = WeeklyAvailability(
        days = listOf(DayAvailability("LUNES", listOf(TimeRange(from, to)))),
        includeSunday = false,
    )
}
