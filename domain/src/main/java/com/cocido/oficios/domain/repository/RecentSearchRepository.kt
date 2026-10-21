package com.cocido.oficios.domain.repository

import com.cocido.oficios.domain.model.RecentSearchEntry
import kotlinx.coroutines.flow.Flow

interface RecentSearchRepository {
    suspend fun getRecents(): List<RecentSearchEntry>
    fun observeRecents(): Flow<List<RecentSearchEntry>>
    suspend fun track(entry: RecentSearchEntry)
}
