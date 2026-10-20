package com.cocido.oficios.domain.model

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GeoCoordinatesTest {

    @Test
    fun `roundForApi keeps six decimal places from map doubles`() {
        assertEquals("-26.182230", format(GeoCoordinates.roundForApi(-26.182229772026542)))
        assertEquals("-58.173786", format(GeoCoordinates.roundForApi(-58.173786252737045)))
    }

    @Test
    fun `roundForApi leaves null coordinates null`() {
        assertNull(GeoCoordinates.roundForApi(null))
    }

    private fun format(value: Double) = String.format(Locale.US, "%.6f", value)
}
