package com.cocido.oficios.domain.usecase.catalog

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.CatalogOption
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Debounced barrio search shared by every screen that uses the barrio autocomplete.
 *
 * Breaks the previous ANR loop where focus -> search -> isLoading -> expand menu ->
 * onExpandedChange/onFocused -> search again (empty query, pageSize=100 forever).
 *
 * Guarantees:
 * - Query held as [MutableStateFlow]; debounce (~300ms) between keystrokes.
 * - [distinctUntilChanged] so identical queries never hit the use case twice in a row.
 * - [collectLatest] cancels the in-flight request when a newer query arrives.
 * - Selecting a suggestion must [clearPending] (not submit the label) so setting the
 *   selected value does not search and any in-flight result is dropped.
 */
class DebouncedBarrioSearcher(
    scope: CoroutineScope,
    private val searchBarrios: SearchBarriosUseCase,
    private val onLoading: () -> Unit = {},
    private val onResult: (List<CatalogOption>) -> Unit,
    private val debounceMs: Long = 300L,
) {
    private val query = MutableStateFlow<String?>(null)
    private val generation = AtomicInteger(0)
    private val job: Job = scope.launch {
        query
            .drop(1) // ignore the initial null seed
            .map { (it ?: "").trim() }
            .distinctUntilChanged()
            .collectLatest { q ->
                if (q.isNotEmpty()) delay(debounceMs)
                val gen = generation.get()
                onLoading()
                val options = when (val result = searchBarrios(q)) {
                    is AppResult.Success -> result.data
                    is AppResult.Error -> emptyList()
                }
                if (gen == generation.get()) {
                    onResult(options)
                }
            }
    }

    fun submit(value: String) {
        query.value = value
    }

    /** Drop any in-flight result (e.g. after the user picks a suggestion). */
    fun clearPending() {
        generation.incrementAndGet()
    }

    fun cancel() {
        generation.incrementAndGet()
        job.cancel()
    }
}

/**
 * Legacy one-shot helper kept for binary compatibility during migration.
 * Prefer [DebouncedBarrioSearcher].
 */
fun CoroutineScope.launchDebouncedBarrioSearch(
    previous: Job?,
    query: String,
    searchBarrios: SearchBarriosUseCase,
    onResult: (List<CatalogOption>) -> Unit,
    debounceMs: Long = 300L,
): Job {
    previous?.cancel()
    return launch {
        val trimmed = query.trim()
        delay(if (trimmed.isEmpty()) 0L else debounceMs)
        val options = when (val result = searchBarrios(trimmed)) {
            is AppResult.Success -> result.data
            is AppResult.Error -> emptyList()
        }
        onResult(options)
    }
}