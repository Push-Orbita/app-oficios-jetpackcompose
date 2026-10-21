package com.cocido.oficios.core.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cocido.oficios.domain.model.RecentSearchEntry
import com.cocido.oficios.domain.repository.RecentSearchRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.recentSearchDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "home_recent_searches",
)

@Singleton
class RecentSearchDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json,
) : RecentSearchRepository {

    override suspend fun getRecents(): List<RecentSearchEntry> {
        return decode(context.recentSearchDataStore.data.first()[ENTRIES_KEY])
    }

    override fun observeRecents(): Flow<List<RecentSearchEntry>> {
        return context.recentSearchDataStore.data.map { preferences ->
            decode(preferences[ENTRIES_KEY])
        }
    }

    override suspend fun track(entry: RecentSearchEntry) {
        val current = getRecents()
            .filterNot { it.type == entry.type && it.id == entry.id }
        val updated = listOf(entry.copy(timestamp = System.currentTimeMillis())) + current
        context.recentSearchDataStore.edit { preferences ->
            preferences[ENTRIES_KEY] = json.encodeToString(updated.take(MAX_RECENTS))
        }
    }

    private fun decode(raw: String?): List<RecentSearchEntry> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            json.decodeFromString<List<RecentSearchEntry>>(raw)
        }.getOrDefault(emptyList())
    }

    private companion object {
        const val MAX_RECENTS = 6
        val ENTRIES_KEY = stringPreferencesKey("entries")
    }
}
