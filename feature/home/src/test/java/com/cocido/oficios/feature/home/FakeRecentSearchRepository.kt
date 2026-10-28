package com.cocido.oficios.feature.home

import com.cocido.oficios.domain.model.RecentSearchEntry
import com.cocido.oficios.domain.repository.RecentSearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow

class FakeRecentSearchRepository(
    private val liveUpdates: Boolean = false,
) : RecentSearchRepository {
    val tracked = mutableListOf<RecentSearchEntry>()
    private val stored = mutableListOf<RecentSearchEntry>()
    private val recentsFlow = MutableSharedFlow<List<RecentSearchEntry>>(replay = 1)

    override suspend fun getRecents(): List<RecentSearchEntry> = stored.toList()

    override fun observeRecents(): Flow<List<RecentSearchEntry>> {
        return if (liveUpdates) recentsFlow else emptyFlow()
    }

    override suspend fun track(entry: RecentSearchEntry) {
        tracked += entry
        stored.removeAll { it.type == entry.type && it.id == entry.id }
        stored.add(0, entry)
        if (liveUpdates) recentsFlow.emit(stored.toList())
    }

    override suspend fun remove(entry: RecentSearchEntry) {
        stored.removeAll { it.type == entry.type && it.id == entry.id }
        if (liveUpdates) recentsFlow.emit(stored.toList())
    }

    suspend fun emitRecents(entries: List<RecentSearchEntry>) {
        stored.clear()
        stored.addAll(entries)
        recentsFlow.emit(entries)
    }
}
