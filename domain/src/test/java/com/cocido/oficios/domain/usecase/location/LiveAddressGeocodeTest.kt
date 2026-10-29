package com.cocido.oficios.domain.usecase.location

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.FormosaAddressGeocoding
import com.cocido.oficios.domain.model.ForwardGeocodeOutcome
import com.cocido.oficios.domain.model.GeocodedPlace
import com.cocido.oficios.domain.model.GeoPoint
import com.cocido.oficios.domain.model.LocalAddressQuery
import com.cocido.oficios.domain.repository.LocationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LiveAddressGeocodeTest {

    @Test
    fun `street and number are enough to geocode live`() {
        assertTrue(
            LocalAddressQuery(street = "O'Higgins", number = "206", neighborhood = "San Miguel")
                .isReadyForLiveGeocode(),
        )
        assertFalse(
            LocalAddressQuery(street = "O'Higgins", number = "", neighborhood = "San Miguel")
                .isReadyForLiveGeocode(),
        )
        assertFalse(
            LocalAddressQuery(street = "  ", number = "206").isReadyForLiveGeocode(),
        )
    }

    @Test
    fun `debounce waits before calling the geocoder`() = runTest {
        val repo = RecordingGeocodingRepository()
        val useCase = GeocodeLocalAddressUseCase(repo)
        val results = mutableListOf<AppResult<ForwardGeocodeOutcome>>()
        launchDebouncedForwardGeocode(
            previous = null,
            query = LocalAddressQuery("O'Higgins", "206", "San Miguel"),
            geocode = useCase,
            onResult = { results += it },
        )
        advanceTimeBy(FormosaAddressGeocoding.LIVE_DEBOUNCE_MS - 1)
        runCurrent()
        assertEquals(0, repo.queries.size)
        advanceTimeBy(1)
        runCurrent()
        assertEquals(1, repo.queries.size)
        assertEquals(1, results.size)
    }

    @Test
    fun `newer query cancels the previous geocode`() = runTest {
        val repo = RecordingGeocodingRepository()
        val useCase = GeocodeLocalAddressUseCase(repo)
        val first = launchDebouncedForwardGeocode(
            previous = null,
            query = LocalAddressQuery("España", "100", "Centro"),
            geocode = useCase,
            onResult = {},
        )
        launchDebouncedForwardGeocode(
            previous = first,
            query = LocalAddressQuery("O'Higgins", "206", "San Miguel"),
            geocode = useCase,
            onResult = {},
        )
        advanceTimeBy(FormosaAddressGeocoding.LIVE_DEBOUNCE_MS)
        runCurrent()
        assertEquals(listOf("O'Higgins 206, San Miguel, Formosa, Formosa, Argentina"), repo.queries)
    }

    @Test
    fun `incomplete address does not hit the geocoder`() = runTest {
        val repo = RecordingGeocodingRepository()
        launchDebouncedForwardGeocode(
            previous = null,
            query = LocalAddressQuery("O'Higgins", "", "San Miguel"),
            geocode = GeocodeLocalAddressUseCase(repo),
            onResult = {},
        )
        advanceTimeBy(FormosaAddressGeocoding.LIVE_DEBOUNCE_MS)
        runCurrent()
        assertEquals(0, repo.queries.size)
    }
}

private class RecordingGeocodingRepository : LocationRepository {
    val queries = mutableListOf<String>()

    override suspend fun getCurrentLocation(requirePrecise: Boolean) =
        error("GPS must not be used for address geocoding")

    override fun observeLocations(intervalMillis: Long): Flow<AppResult<GeoPoint>> = emptyFlow()

    override suspend fun geocodeForward(query: String): AppResult<List<GeocodedPlace>> {
        queries += query
        return AppResult.Success(listOf(GeocodedPlace(-26.177, -58.19, query)))
    }
}
