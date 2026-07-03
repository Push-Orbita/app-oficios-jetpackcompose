package com.cocido.oficios.domain.repository

import com.cocido.oficios.domain.model.RecentSearchEntry

interface RecentSearchRepository {
    suspend fun getRecents(): List<RecentSearchEntry>
    suspend fun track(entry: RecentSearchEntry)
}
