package com.cocido.oficios.feature.home.search

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RubroDisplayNamesTest {

    @Test
    fun `prod plomeria maps to plomeros`() {
        assertEquals("Plomeros de Oficios Formosa", RubroDisplayNames.pluralTitle("Plomería"))
        assertEquals("¿Busca algún plomero?", RubroDisplayNames.searchHint("plomería"))
    }

    @Test
    fun `prod electricidad maps to electricistas`() {
        assertEquals("Electricistas de Oficios Formosa", RubroDisplayNames.pluralTitle("Electricidad"))
        assertEquals("¿Busca algún electricista?", RubroDisplayNames.searchHint("ELECTRICIDAD"))
    }

    @Test
    fun `prod pintura maps to pintores`() {
        assertEquals("Pintores de Oficios Formosa", RubroDisplayNames.pluralTitle("Pintura"))
        assertEquals("¿Busca algún pintor?", RubroDisplayNames.searchHint("pintura"))
    }

    @Test
    fun `prod albanileria maps ignoring accents`() {
        assertEquals("Albañiles de Oficios Formosa", RubroDisplayNames.pluralTitle("Albañilería"))
        assertEquals("¿Busca algún albañil?", RubroDisplayNames.searchHint("albanileria"))
    }

    @Test
    fun `prod carpinteria and cerrajeria and gas`() {
        assertEquals("Carpinteros de Oficios Formosa", RubroDisplayNames.pluralTitle("Carpintería"))
        assertEquals("Cerrajeros de Oficios Formosa", RubroDisplayNames.pluralTitle("Cerrajería"))
        assertEquals("Gasistas de Oficios Formosa", RubroDisplayNames.pluralTitle("Gas"))
        assertEquals("¿Busca algún gasista?", RubroDisplayNames.searchHint("GAS"))
    }

    @Test
    fun `gentilicio aliases still resolve`() {
        assertEquals("Plomeros de Oficios Formosa", RubroDisplayNames.pluralTitle("plomero"))
        assertEquals("Electricistas de Oficios Formosa", RubroDisplayNames.pluralTitle("Electricista"))
        assertEquals("¿Busca algún pintor?", RubroDisplayNames.searchHint("Pintores"))
    }

    @Test
    fun `unmatched uses neutral title and hint`() {
        assertEquals(
            "Trabajadores de Oficios Formosa",
            RubroDisplayNames.pluralTitle("Hogar y Construcción"),
        )
        assertEquals("¿Busca a alguien en específico?", RubroDisplayNames.searchHint("Hogar y Construcción"))
        assertEquals("Trabajadores de Oficios Formosa", RubroDisplayNames.pluralTitle(null))
        assertEquals("¿Busca a alguien en específico?", RubroDisplayNames.searchHint("   "))
        assertNull(RubroDisplayNames.resolve("Tecnología"))
    }

    @Test
    fun `does not invent plomerias plural`() {
        assertEquals("Plomeros de Oficios Formosa", RubroDisplayNames.pluralTitle("Plomería"))
        assertEquals("¿Busca algún plomero?", RubroDisplayNames.searchHint("Plomería"))
    }
}
