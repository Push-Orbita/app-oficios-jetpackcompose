package com.cocido.oficios.core.datastore

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.MasterKey
import java.security.KeyStore

/**
 * Abre las prefs cifradas de sesión sin tirar abajo el proceso.
 *
 * Si el keyset de Tink guardado en el archivo no se puede descifrar con la MasterKey del
 * Keystore (típico después de un restore de backup / cambio de equipo, o Keystore roto),
 * `EncryptedSharedPreferences.create` lanza en el arranque. En ese caso se borra el archivo
 * y la MasterKey, y se vuelve a crear: se pierde la sesión (el usuario va al login) pero la
 * app abre. Si aun así falla, se usa un almacenamiento en memoria para no persistir tokens
 * en claro.
 */
internal object SessionPrefsFactory {

    private const val TAG = "SessionPrefs"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"

    fun open(
        context: Context,
        fileName: String,
        createEncrypted: () -> SharedPreferences,
        resetStorage: () -> Unit = { resetEncryptedStorage(context, fileName) },
    ): SharedPreferences {
        attempt("create", createEncrypted)?.let { return it }

        Log.w(TAG, "Sesión cifrada ilegible; se borra y se recrea ($fileName)")
        attempt("reset", resetStorage)

        attempt("recreate", createEncrypted)?.let { return it }

        Log.e(TAG, "No se pudo recrear la sesión cifrada; se usa sesión en memoria")
        return InMemorySharedPreferences()
    }

    /** Borra el archivo de prefs (incluye el keyset de Tink) y la MasterKey por defecto. */
    fun resetEncryptedStorage(context: Context, fileName: String) {
        context.deleteSharedPreferences(fileName)
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            if (keyStore.containsAlias(MasterKey.DEFAULT_MASTER_KEY_ALIAS)) {
                keyStore.deleteEntry(MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            }
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo borrar la MasterKey del Keystore", e)
        }
    }

    private fun <T> attempt(step: String, block: () -> T): T? =
        try {
            block()
        } catch (e: Exception) {
            Log.w(TAG, "Falló $step de la sesión cifrada", e)
            null
        }
}
