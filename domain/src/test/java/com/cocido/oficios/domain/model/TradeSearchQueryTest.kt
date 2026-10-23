package com.cocido.oficios.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TradeSearchQueryTest {

    private val plomeria = CatalogOption(1, "Plomería")
    private val electricista = CatalogOption(2, "Electricista")
    private val gasista = CatalogOption(3, "Gasista")
    private val canerias = CatalogOption(11, "Reparación de cañerías")
    private val rubros = listOf(plomeria, electricista, gasista)
    private val subRubros = listOf(canerias)

    @Test
    fun `strips accents and case`() {
        assertEquals("plomeria", normalizeSearchText(" Plomería "))
        assertEquals("plomeria", normalizeSearchText("PLOMERIA"))
        assertEquals("reparacion de canerias", normalizeSearchText("Reparación de cañerías"))
    }

    @Test
    fun `prefix without accent resolves to Plomeria`() {
        assertEquals(plomeria, matchCatalogByQuery("plom", rubros))
        assertEquals(plomeria, matchCatalogByQuery("Plomeria", rubros))
        assertEquals(plomeria, matchCatalogByQuery("PLOMERÍA", rubros))
    }

    @Test
    fun `short query does not match a trade`() {
        assertNull(matchCatalogByQuery("pl", rubros))
        assertNull(matchCatalogByQuery("  ", rubros))
    }

    @Test
    fun `name-like query does not match a trade`() {
        assertNull(matchCatalogByQuery("Carlos", rubros))
    }

    @Test
    fun `filters resolve oficio query to rubroId and drop q`() {
        val resolved = WorkerSearchFilters(query = "plom").withResolvedTrade(rubros, subRubros)
        assertEquals(1L, resolved.rubroId)
        assertEquals("Plomería", resolved.rubroLabel)
        assertNull(resolved.query)
        assertNull(resolved.subRubroId)
    }

    @Test
    fun `filters resolve subrubro query without accent`() {
        val resolved = WorkerSearchFilters(query = "canerias").withResolvedTrade(rubros, subRubros)
        assertEquals(11L, resolved.subRubroId)
        assertEquals("Reparación de cañerías", resolved.subRubroLabel)
        assertNull(resolved.query)
        assertNull(resolved.rubroId)
    }

    @Test
    fun `suggestions include prefix matches under three characters`() {
        assertEquals(listOf(plomeria), catalogSuggestions("pl", rubros))
        assertEquals(listOf(canerias), catalogSuggestions("caner", subRubros))
    }

    @Test
    fun `hasSearchCriteria ignores category-only filters`() {
        assertEquals(false, WorkerSearchFilters().hasSearchCriteria())
        assertEquals(false, WorkerSearchFilters(categoryId = 1, categoryLabel = "Hogar").hasSearchCriteria())
        assertEquals(true, WorkerSearchFilters(query = "plomero").hasSearchCriteria())
        assertEquals(true, WorkerSearchFilters(rubroId = 3).hasSearchCriteria())
    }

    @Test
    fun `existing rubro filter keeps the typed query for name search`() {
        val resolved = WorkerSearchFilters(query = "plom", rubroId = 9).withResolvedTrade(rubros, subRubros)
        assertEquals(9L, resolved.rubroId)
        assertEquals("plom", resolved.query)
    }
}
