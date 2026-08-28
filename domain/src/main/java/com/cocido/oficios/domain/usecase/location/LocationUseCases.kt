package com.cocido.oficios.domain.usecase.location

import com.cocido.oficios.domain.repository.LocationRepository
import javax.inject.Inject

class GetCurrentLocationUseCase @Inject constructor(
    private val repository: LocationRepository,
) {
    suspend operator fun invoke(requirePrecise: Boolean = false) =
        repository.getCurrentLocation(requirePrecise)
}

class ObserveLocationUseCase @Inject constructor(
    private val repository: LocationRepository,
) {
    operator fun invoke(intervalMillis: Long) = repository.observeLocations(intervalMillis)
}
