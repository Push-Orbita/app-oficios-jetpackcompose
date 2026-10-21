package com.cocido.oficios.feature.home.search

import org.junit.Assert.assertEquals
import org.junit.Test

class RubroDisplayNamesTest {

    @Test
    fun `plural title uses mapped albañil`() {
        assertEquals("Albañiles de Oficios Formosa", RubroDisplayNames.pluralTitle("Albañil"))
    }

    @Test
    fun `search hint is usted and singular`() {
        assertEquals("¿Busca algún albañil en específico?", RubroDisplayNames.searchHint("Albañiles"))
    }

    @Test
    fun `fallback plural adds s`() {
        assertEquals("Pintores de Oficios Formosa", RubroDisplayNames.pluralTitle("Pintor"))
    }
}
