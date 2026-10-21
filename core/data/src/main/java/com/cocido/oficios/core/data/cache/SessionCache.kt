package com.cocido.oficios.core.data.cache

import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

fun interface SessionCache {
    fun clear()
}

/**
 * Hub de caches in-memory por sesión. Logout / cambio de cuenta llama [clearAll]
 * para no mezclar pedidos, galería o favoritos del usuario anterior.
 */
@Singleton
class SessionCacheCoordinator @Inject constructor() {
    private val caches = CopyOnWriteArrayList<SessionCache>()

    fun register(cache: SessionCache) {
        caches += cache
    }

    fun clearAll() {
        caches.forEach { it.clear() }
    }
}

class CachedValue<T>(
    coordinator: SessionCacheCoordinator? = null,
) : SessionCache {
    private val _state = MutableStateFlow<T?>(null)
    val state: StateFlow<T?> = _state.asStateFlow()

    init {
        coordinator?.register(this)
    }

    fun get(): T? = _state.value

    fun set(value: T?) {
        _state.value = value
    }

    fun update(transform: (T) -> T) {
        _state.update { current -> current?.let(transform) }
    }

    override fun clear() {
        _state.value = null
    }
}
