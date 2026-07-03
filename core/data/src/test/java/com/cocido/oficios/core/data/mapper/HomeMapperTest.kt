package com.cocido.oficios.core.data.mapper

import com.cocido.oficios.domain.model.CatalogOption
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeMapperTest {

    @Test
    fun `maps catalog option to home category item`() {
        val item = CatalogOption(id = 3, label = "Construcción").toHomeCategoryItem()

        assertEquals(3L, item.id)
        assertEquals("Construcción", item.name)
    }
}
