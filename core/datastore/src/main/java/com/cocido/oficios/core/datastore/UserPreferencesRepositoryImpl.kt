package com.cocido.oficios.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cocido.oficios.core.model.AppConstants
import com.cocido.oficios.domain.model.ThemeMode
import com.cocido.oficios.domain.model.parseThemeMode
import com.cocido.oficios.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = AppConstants.DATASTORE_NAME,
)

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : UserPreferencesRepository {

    override val themeMode: Flow<ThemeMode> = context.userPreferencesDataStore.data
        .map { preferences -> parseThemeMode(preferences[THEME_MODE_KEY]) }
        .onEach { mode -> ThemeModePreferences.write(context, mode) }

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        ThemeModePreferences.write(context, themeMode)
        context.userPreferencesDataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = themeMode.name
        }
    }

    override suspend fun getRememberSession(): Boolean =
        context.userPreferencesDataStore.data.map { it[REMEMBER_SESSION_KEY] ?: true }.first()

    override suspend fun setRememberSession(remember: Boolean) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[REMEMBER_SESSION_KEY] = remember
        }
    }

    private companion object {
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        val REMEMBER_SESSION_KEY = booleanPreferencesKey("remember_session")
    }
}
