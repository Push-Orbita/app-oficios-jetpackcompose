package com.cocido.oficios.core.data.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BookingTypeLabelTest {

    @Test
    fun `el tipo del booking se muestra como etiqueta legible`() {
        assertEquals("Presupuesto", bookingTypeLabel("PRESUPUESTO"))
        assertEquals("Trabajo", bookingTypeLabel("TRABAJO"))
        assertEquals("Presupuesto y trabajo", bookingTypeLabel("ambos"))
        assertEquals("Otro", bookingTypeLabel("OTRO"))
        assertNull(bookingTypeLabel("  "))
        assertNull(bookingTypeLabel(null))
    }
}
