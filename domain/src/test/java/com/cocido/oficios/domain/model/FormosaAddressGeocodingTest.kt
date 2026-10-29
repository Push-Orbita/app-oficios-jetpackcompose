package com.cocido.oficios.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FormosaAddressGeocodingTest {

    @Test
    fun `query joins street number barrio and Formosa Argentina`() {
        val query = FormosaAddressGeocoding.buildQuery(
            LocalAddressQuery(
                street = "San Martín",
                number = "123",
                neighborhood = "Centro",
            ),
        )
        assertEquals("San Martín 123, Centro, Formosa, Formosa, Argentina", query)
    }

    @Test
    fun `query omits blank number`() {
        val query = FormosaAddressGeocoding.buildQuery(
            LocalAddressQuery(street = "Av. 25 de Mayo", neighborhood = "San Martín"),
        )
        assertEquals("Av. 25 de Mayo, San Martín, Formosa, Formosa, Argentina", query)
    }

    @Test
    fun `unique result inside Formosa is accepted`() {
        val outcome = FormosaAddressGeocoding.resolve(
            listOf(formosaPlace(-26.1849, -58.1731, "San Martín 123, Formosa")),
        )
        val unique = outcome as ForwardGeocodeOutcome.Unique
        assertEquals(-26.1849, unique.place.latitude, 0.0)
        assertEquals(-58.1731, unique.place.longitude, 0.0)
    }

    @Test
    fun `empty results map to none`() {
        assertEquals(ForwardGeocodeOutcome.None, FormosaAddressGeocoding.resolve(emptyList()))
    }

    @Test
    fun `googleplex emulator gps is outside Formosa`() {
        val outcome = FormosaAddressGeocoding.resolve(
            listOf(formosaPlace(37.421998, -122.084, "charleston rd, Mountain View")),
        )
        assertEquals(ForwardGeocodeOutcome.OutsideRegion, outcome)
    }

    @Test
    fun `two far Formosa hits are ambiguous`() {
        val outcome = FormosaAddressGeocoding.resolve(
            listOf(
                formosaPlace(-26.1849, -58.1731, "Centro, Formosa"),
                formosaPlace(-26.12, -58.22, "Villa del Carmen, Formosa"),
            ),
        )
        assertTrue(outcome is ForwardGeocodeOutcome.Ambiguous)
        assertEquals(2, (outcome as ForwardGeocodeOutcome.Ambiguous).candidates.size)
    }

    @Test
    fun `clustered Formosa hits collapse to unique`() {
        val outcome = FormosaAddressGeocoding.resolve(
            listOf(
                formosaPlace(-26.1849, -58.1731, "San Martín 123, Formosa"),
                formosaPlace(-26.1850, -58.1732, "San Martín 125, Formosa"),
            ),
        )
        assertTrue(outcome is ForwardGeocodeOutcome.Unique)
    }

    @Test
    fun `zero zero placeholder is ignored`() {
        assertEquals(
            ForwardGeocodeOutcome.None,
            FormosaAddressGeocoding.resolve(listOf(formosaPlace(0.0, 0.0, "Unknown"))),
        )
    }

    @Test
    fun `snapshot unique keeps coords unconfirmed for preview`() {
        val snapshot = FormosaAddressGeocoding.snapshotFor(
            ForwardGeocodeOutcome.Unique(formosaPlace(-26.1849, -58.1731, "San Martín")),
        )
        assertEquals(AddressLocationPhase.Preview, snapshot.phase)
        assertFalse(snapshot.confirmed)
        assertFalse(snapshot.canPersist)
        assertEquals(-26.1849, snapshot.latitude)
        assertEquals(AddressMapMode.Preview, snapshot.mapMode)
    }

    @Test
    fun `snapshot none asks for map without coords`() {
        val snapshot = FormosaAddressGeocoding.snapshotFor(ForwardGeocodeOutcome.None)
        assertEquals(AddressLocationPhase.NeedsMap, snapshot.phase)
        assertEquals(null, snapshot.latitude)
        assertEquals(AddressMapMode.NeedsMap, snapshot.mapMode)
    }

    @Test
    fun `confirming a pin does not require rewriting the street`() {
        val picking = FormosaAddressGeocoding.openMap(AddressLocationSnapshot())
        val moved = FormosaAddressGeocoding.pinMoved(picking, -26.19, -58.18)
        val confirmed = FormosaAddressGeocoding.confirmPin(moved)
        assertTrue(confirmed.confirmed)
        assertEquals(AddressLocationPhase.Confirmed, confirmed.phase)
        assertEquals(-26.19, confirmed.latitude)
        assertEquals(-58.18, confirmed.longitude)
    }

    @Test
    fun `live unique outcome moves the pin without confirming`() {
        val current = AddressLocationSnapshot(
            latitude = -26.1849,
            longitude = -58.1731,
            phase = AddressLocationPhase.PickingMap,
            confirmed = true,
        )
        val live = FormosaAddressGeocoding.applyLiveOutcome(
            ForwardGeocodeOutcome.Unique(formosaPlace(-26.177, -58.19, "O'Higgins 206")),
            current,
        )
        assertEquals(-26.177, live.latitude)
        assertEquals(-58.19, live.longitude)
        assertEquals(AddressLocationPhase.Preview, live.phase)
        assertFalse(live.confirmed)
        assertTrue(live.showMap)
    }

    @Test
    fun `live miss keeps a gps or dragged pin`() {
        val current = AddressLocationSnapshot(
            latitude = -26.19,
            longitude = -58.18,
            phase = AddressLocationPhase.PickingMap,
            confirmed = false,
        )
        val live = FormosaAddressGeocoding.applyLiveOutcome(ForwardGeocodeOutcome.None, current)
        assertEquals(-26.19, live.latitude)
        assertEquals(-58.18, live.longitude)
        assertEquals(AddressLocationPhase.PickingMap, live.phase)
    }

    @Test
    fun `invalidate drops gps leftover coords`() {
        val dirty = AddressLocationSnapshot(
            latitude = 37.421998,
            longitude = -122.084,
            confirmed = true,
            phase = AddressLocationPhase.Confirmed,
        )
        val clean = FormosaAddressGeocoding.invalidate()
        assertEquals(null, clean.latitude)
        assertEquals(null, clean.longitude)
        assertFalse(clean.confirmed)
        assertEquals(AddressLocationPhase.Idle, clean.phase)
        assertEquals(37.421998, dirty.latitude)
    }

    private fun formosaPlace(lat: Double, lng: Double, address: String) = GeocodedPlace(
        latitude = lat,
        longitude = lng,
        formattedAddress = address,
        locality = if (address.contains("Formosa", ignoreCase = true)) "Formosa" else null,
        adminArea = if (address.contains("Formosa", ignoreCase = true)) "Formosa" else "California",
        countryName = "Argentina",
    )
}
