package com.cocido.oficios.feature.home

import com.cocido.oficios.domain.model.RecentSearchEntry
import com.cocido.oficios.domain.repository.RecentSearchRepository

class FakeRecentSearchRepository : RecentSearchRepository {
    override suspend fun getRecents(): List<RecentSearchEntry> = emptyList()
    override suspend fun track(entry: RecentSearchEntry) = Unit
}
