package com.cocido.oficios.core.data.register

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cocido.oficios.domain.model.RegisterTradeDraft
import com.cocido.oficios.domain.repository.RegisterTradeDraftRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.registerTradeDraftDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "register_trade_draft",
)

@Singleton
class RegisterTradeDraftDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json,
) : RegisterTradeDraftRepository {

    override suspend fun save(draft: RegisterTradeDraft) {
        context.registerTradeDraftDataStore.edit { preferences ->
            preferences[DRAFT_KEY] = json.encodeToString(draft)
        }
    }

    override suspend fun get(): RegisterTradeDraft? {
        val raw = context.registerTradeDraftDataStore.data.first()[DRAFT_KEY] ?: return null
        return runCatching { json.decodeFromString<RegisterTradeDraft>(raw) }.getOrNull()
    }

    override suspend fun clear() {
        context.registerTradeDraftDataStore.edit { preferences ->
            preferences.remove(DRAFT_KEY)
        }
    }

    private companion object {
        val DRAFT_KEY = stringPreferencesKey("draft")
    }
}
