package com.cocido.oficios.domain.usecase.location

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.ForwardGeocodeOutcome
import com.cocido.oficios.domain.model.GeocodedPlace
import com.cocido.oficios.domain.model.GeoPoint
import com.cocido.oficios.domain.model.LocalAddressQuery
import com.cocido.oficios.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeocodeLocalAddressUseCaseTest {

    @Test
    fun `maps repository hits through Formosa rules`() = runTest {
        val useCase = GeocodeLocalAddressUseCase(
            FakeGeocodingRepository(
                listOf(GeocodedPlace(-26.1849, -58.1731, "San Martín 123, Formosa")),
            ),
        )
        val result = useCase(
            LocalAddressQuery(street = "San Martín", number = "123", neighborhood = "Centro"),
        )
        val outcome = (result as AppResult.Success).data as ForwardGeocodeOutcome.Unique
        assertEquals(-26.1849, outcome.place.latitude, 0.0)
    }

    @Test
    fun `blank street is none without calling gps`() = runTest {
        val repo = FakeGeocodingRepository(listOf(GeocodedPlace(37.42, -122.08, "charleston rd")))
        val useCase = GeocodeLocalAddressUseCase(repo)
        val result = useCase(LocalAddressQuery(street = "  "))
        assertEquals(ForwardGeocodeOutcome.None, (result as AppResult.Success).data)
        assertEquals(0, repo.queries.size)
    }

    @Test
    fun `query sent to geocoder includes Formosa Argentina`() = runTest {
        val repo = FakeGeocodingRepository(emptyList())
        val useCase = GeocodeLocalAddressUseCase(repo)
        useCase(LocalAddressQuery(street = "San Martín", number = "123", neighborhood = "Centro"))
        assertTrue(repo.queries.single().contains("Formosa, Formosa, Argentina"))
        assertTrue(repo.queries.single().startsWith("San Martín 123, Centro"))
    }
}

private class FakeGeocodingRepository(
    private val places: List<GeocodedPlace>,
) : LocationRepository {
    val queries = mutableListOf<String>()

    override suspend fun getCurrentLocation(requirePrecise: Boolean) =
        error("GPS must not be used for address geocoding")

    override fun observeLocations(intervalMillis: Long): Flow<AppResult<GeoPoint>> = emptyFlow()

    override suspend fun geocodeForward(query: String): AppResult<List<GeocodedPlace>> {
        queries += query
        return AppResult.Success(places)
    }
}
