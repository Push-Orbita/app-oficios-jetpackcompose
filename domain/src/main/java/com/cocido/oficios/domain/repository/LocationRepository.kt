package com.cocido.oficios.domain.repository

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.GeocodedPlace
import com.cocido.oficios.domain.model.GeoPoint
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    suspend fun getCurrentLocation(requirePrecise: Boolean = false): AppResult<GeoPoint>
    fun observeLocations(intervalMillis: Long): Flow<AppResult<GeoPoint>>

    /** Geocoding forward. El GPS del dispositivo no entra acá. */
    suspend fun geocodeForward(query: String): AppResult<List<GeocodedPlace>> =
        AppResult.Error(AppError.Network("No pudimos ubicar esa dirección."))
}
