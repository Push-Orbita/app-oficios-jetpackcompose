package com.cocido.oficios.core.data.cache

import com.cocido.oficios.domain.model.AgendaSessionsSnapshot
import com.cocido.oficios.domain.model.AgendaSlotsSnapshot
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgendaStore @Inject constructor(
    coordinator: SessionCacheCoordinator,
) {
    val slots = CachedValue<AgendaSlotsSnapshot>(coordinator)
    val sessions = CachedValue<AgendaSessionsSnapshot>(coordinator)
}
