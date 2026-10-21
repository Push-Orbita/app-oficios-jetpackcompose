package com.cocido.oficios.domain.usecase.catalog

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.CatalogOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun CoroutineScope.launchDebouncedBarrioSearch(
    previous: Job?,
    query: String,
    searchBarrios: SearchBarriosUseCase,
    onResult: (List<CatalogOption>) -> Unit,
    debounceMs: Long = 250L,
): Job {
    previous?.cancel()
    return launch {
        delay(if (query.isBlank()) 0L else debounceMs)
        val options = when (val result = searchBarrios(query.trim())) {
            is AppResult.Success -> result.data
            is AppResult.Error -> emptyList()
        }
        onResult(options)
    }
}
