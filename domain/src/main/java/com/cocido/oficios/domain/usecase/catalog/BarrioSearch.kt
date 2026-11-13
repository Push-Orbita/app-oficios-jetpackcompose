package com.cocido.oficios.domain.usecase.catalog

import com.cocido.oficios.core.common.result.AppError
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
import kotlinx.coroutines.launch

/**
 * Debounced barrio search shared by every screen that uses the barrio autocomplete.
 *
 * Breaks the previous ANR loop where focus -> search -> isLoading -> expand menu ->
 * onExpandedChange/onFocused -> search again (empty query, pageSize=100 forever).
 *
 * Guarantees:
 * - Query held as [MutableStateFlow]; debounce (~300ms) between keystrokes.
 * - [distinctUntilChanged] so identical queries never hit the use case twice in a row,
 *   unless [retry] bumps the nonce (needed after a network error).
 * - [collectLatest] cancels the in-flight request when a newer query arrives.
 * - Selecting a suggestion must [clearPending] (not submit the label) so setting the
 *   selected value does not search and any in-flight result is dropped.
 * - A failed search calls [onError] instead of [onResult] with an empty list, so the UI
 *   can show a network message and retry instead of "no se encontraron barrios".
 */
class DebouncedBarrioSearcher(
    scope: CoroutineScope,
    private val searchBarrios: SearchBarriosUseCase,
    private val onLoading: () -> Unit = {},
    private val onResult: (List<CatalogOption>) -> Unit,
    private val onError: (AppError) -> Unit = { onResult(emptyList()) },
    private val debounceMs: Long = 300L,
) {
    private data class Request(
        val text: String,
        val nonce: Int,
        val immediate: Boolean,
    )

    private val request = MutableStateFlow<Request?>(null)
    private val generation = AtomicInteger(0)
    private val job: Job = scope.launch {
        request
            .drop(1) // ignore the initial null seed
            .distinctUntilChanged()
            .collectLatest { req ->
                if (req == null) return@collectLatest
                val q = req.text.trim()
                if (!req.immediate && q.isNotEmpty()) delay(debounceMs)
                val gen = generation.get()
                onLoading()
                when (val result = searchBarrios(q)) {
                    is AppResult.Success -> if (gen == generation.get()) onResult(result.data)
                    is AppResult.Error -> if (gen == generation.get()) onError(result.error)
                }
            }
    }

    fun submit(value: String) {
        val current = request.value
        request.value = Request(
            text = value,
            nonce = current?.nonce ?: 0,
            immediate = false,
        )
    }

    /** Repeats the last query even if the text did not change. */
    fun retry() {
        val current = request.value
        request.value = Request(
            text = current?.text.orEmpty(),
            nonce = (current?.nonce ?: 0) + 1,
            immediate = true,
        )
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
    onError: (AppError) -> Unit = { onResult(emptyList()) },
    debounceMs: Long = 300L,
): Job {
    previous?.cancel()
    return launch {
        val trimmed = query.trim()
        delay(if (trimmed.isEmpty()) 0L else debounceMs)
        when (val result = searchBarrios(trimmed)) {
            is AppResult.Success -> onResult(result.data)
            is AppResult.Error -> onError(result.error)
        }
    }
}
