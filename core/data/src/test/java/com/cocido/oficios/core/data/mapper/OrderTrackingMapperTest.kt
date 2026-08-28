package com.cocido.oficios.core.data.mapper

import com.cocido.oficios.core.network.api.orders.dto.TrackingBookingDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OrderTrackingMapperTest {

    @Test
    fun `maps a valid Formosa tracking location`() {
        val tracking = trackingDto(lat = "-26.18490", lng = "-58.17310").toDomain()

        requireNotNull(tracking)
        assertEquals(-26.18490, tracking.location.latitude, 0.00001)
        assertEquals(-58.17310, tracking.location.longitude, 0.00001)
        assertTrue(tracking.location.isValid)
    }

    @Test
    fun `rejects the old placeholder coordinate`() {
        assertNull(trackingDto(lat = "0", lng = "0").toDomain())
    }

    @Test
    fun `rejects malformed and out of bounds coordinates`() {
        assertNull(trackingDto(lat = "sin-latitud", lng = "-58.17").toDomain())
        assertNull(trackingDto(lat = "-26.18", lng = "181").toDomain())
    }

    private fun trackingDto(lat: String, lng: String) = TrackingBookingDto(
        id = 11,
        bookingId = 22,
        trabajadorOficioId = 33,
        estado = "EN_CAMINO",
        lat = lat,
        lng = lng,
        createdAt = "2026-08-28T12:00:00Z",
    )
}
