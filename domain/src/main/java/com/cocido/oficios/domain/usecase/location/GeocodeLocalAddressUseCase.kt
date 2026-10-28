package com.cocido.oficios.domain.usecase.location

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.FormosaAddressGeocoding
import com.cocido.oficios.domain.model.ForwardGeocodeOutcome
import com.cocido.oficios.domain.model.LocalAddressQuery
import com.cocido.oficios.domain.repository.LocationRepository
import javax.inject.Inject

class GeocodeLocalAddressUseCase @Inject constructor(
    private val repository: LocationRepository,
) {
    suspend operator fun invoke(query: LocalAddressQuery): AppResult<ForwardGeocodeOutcome> {
        val built = FormosaAddressGeocoding.buildQuery(query)
        if (query.street.isBlank()) {
            return AppResult.Success(ForwardGeocodeOutcome.None)
        }
        return when (val result = repository.geocodeForward(built)) {
            is AppResult.Error -> result
            is AppResult.Success -> AppResult.Success(FormosaAddressGeocoding.resolve(result.data))
        }
    }
}
