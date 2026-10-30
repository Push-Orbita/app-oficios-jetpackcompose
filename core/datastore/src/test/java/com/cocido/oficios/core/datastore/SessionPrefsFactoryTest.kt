package com.cocido.oficios.core.datastore

import android.content.Context
import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.security.GeneralSecurityException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SessionPrefsFactoryTest {

    private val context: Context = RuntimeEnvironment.getApplication()

    @Test
    fun returnsEncryptedPrefsWithoutResetWhenCreateWorks() {
        val expected = context.getSharedPreferences("ok", Context.MODE_PRIVATE)
        var resets = 0

        val prefs = SessionPrefsFactory.open(
            context = context,
            fileName = "ok",
            createEncrypted = { expected },
            resetStorage = { resets++ },
        )

        assertSame(expected, prefs)
        assertEquals(0, resets)
    }

    @Test
    fun resetsAndRecreatesWhenKeysetCannotBeDecrypted() {
        val recreated = context.getSharedPreferences("recreated", Context.MODE_PRIVATE)
        var attempts = 0
        var resets = 0

        val prefs = SessionPrefsFactory.open(
            context = context,
            fileName = "recreated",
            createEncrypted = {
                attempts++
                if (attempts == 1) throw GeneralSecurityException("keyset restaurado sin MasterKey")
                recreated
            },
            resetStorage = { resets++ },
        )

        assertSame(recreated, prefs)
        assertEquals(2, attempts)
        assertEquals(1, resets)
    }

    @Test
    fun fallsBackToInMemoryWhenRecreateAlsoFails() {
        val prefs = SessionPrefsFactory.open(
            context = context,
            fileName = "broken",
            createEncrypted = { throw IllegalStateException("Keystore roto") },
            resetStorage = { throw RuntimeException("reset roto") },
        )

        assertTrue(prefs is InMemorySharedPreferences)
        assertFalse(prefs.contains("access_token"))
        prefs.edit().putString("access_token", "abc").putLong("user_id", 7L).apply()
        assertEquals("abc", prefs.getString("access_token", null))
        assertEquals(7L, prefs.getLong("user_id", -1L))
        prefs.edit().clear().apply()
        assertFalse(prefs.contains("access_token"))
    }

    @Test
    fun resetDeletesCorruptPrefsFile() {
        val name = "oficios_session_corrupt"
        context.getSharedPreferences(name, Context.MODE_PRIVATE).edit()
            .putString("__androidx_security_crypto_encrypted_prefs_key_keyset__", "basura")
            .commit()

        SessionPrefsFactory.resetEncryptedStorage(context, name)

        val reopened: SharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)
        assertTrue(reopened.all.isEmpty())
    }
}
