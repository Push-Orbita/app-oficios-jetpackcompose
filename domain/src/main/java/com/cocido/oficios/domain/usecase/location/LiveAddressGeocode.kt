package com.cocido.oficios.domain.usecase.location

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.FormosaAddressGeocoding
import com.cocido.oficios.domain.model.ForwardGeocodeOutcome
import com.cocido.oficios.domain.model.LocalAddressQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun LocalAddressQuery.isReadyForLiveGeocode(): Boolean =
    street.trim().isNotBlank() && number.trim().isNotBlank()

fun CoroutineScope.launchDebouncedForwardGeocode(
    previous: Job?,
    query: LocalAddressQuery,
    geocode: GeocodeLocalAddressUseCase,
    onResult: (AppResult<ForwardGeocodeOutcome>) -> Unit,
    debounceMs: Long = FormosaAddressGeocoding.LIVE_DEBOUNCE_MS,
): Job {
    previous?.cancel()
    return launch {
        if (!query.isReadyForLiveGeocode()) return@launch
        delay(debounceMs)
        onResult(geocode(query))
    }
}
