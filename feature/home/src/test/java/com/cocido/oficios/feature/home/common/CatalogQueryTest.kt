package com.cocido.oficios.feature.home.common

import org.junit.Assert.assertEquals
import org.junit.Test

class CatalogQueryTest {

    @Test
    fun `blank query keeps every item`() {
        val items = listOf("Construcción", "Bienestar")
        assertEquals(items, items.filterByCatalogQuery("  ") { it })
    }

    @Test
    fun `filter ignores accents and case`() {
        val items = listOf("Construcción", "Mantenimiento y refacción", "Bienestar")
        assertEquals(
            listOf("Construcción"),
            items.filterByCatalogQuery("construccion") { it },
        )
        assertEquals(
            listOf("Mantenimiento y refacción"),
            items.filterByCatalogQuery("REFACC") { it },
        )
    }
}
