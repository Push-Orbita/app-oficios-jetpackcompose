package com.cocido.oficios.core.datastore

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.cocido.oficios.core.model.AppConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Borrador de registro cifrado. Si el archivo no se puede leer, se borra SOLO ese archivo
 * (no la MasterKey de sesión).
 */
@Singleton
class EncryptedDraftPrefs @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs: SharedPreferences = open(context)

    fun read(): String? = prefs.getString(KEY, null)

    fun write(value: String) {
        prefs.edit().putString(KEY, value).apply()
    }

    fun clear() {
        prefs.edit().remove(KEY).apply()
    }

    private companion object {
        const val TAG = "RegistrationDraft"
        const val KEY = "draft_json"

        fun open(context: Context): SharedPreferences {
            val fileName = AppConstants.REGISTRATION_DRAFT_PREFS_NAME
            fun create(): SharedPreferences = EncryptedSharedPreferences.create(
                context,
                fileName,
                MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build(),
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
            try {
                return create()
            } catch (first: Exception) {
                Log.w(TAG, "Borrador cifrado ilegible; se recrea el archivo", first)
                context.deleteSharedPreferences(fileName)
            }
            return try {
                create()
            } catch (second: Exception) {
                Log.e(TAG, "No se pudo recrear el borrador cifrado", second)
                InMemorySharedPreferences()
            }
        }
    }
}
