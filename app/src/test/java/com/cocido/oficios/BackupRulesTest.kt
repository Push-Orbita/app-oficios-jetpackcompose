package com.cocido.oficios

import android.content.res.XmlResourceParser
import androidx.test.core.app.ApplicationProvider
import android.content.Context
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.xmlpull.v1.XmlPullParser

/**
 * La sesión cifrada (EncryptedSharedPreferences) no puede viajar en backups: su MasterKey vive en
 * el Keystore del equipo y no se respalda, así que un restore deja un keyset que no se descifra.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BackupRulesTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun legacyFullBackupExcludesEncryptedSession() {
        val excludes = sharedPrefExcludes(R.xml.backup_rules)
        assertTrue("excludes=$excludes", SESSION_FILE in excludes)
    }

    @Test
    fun dataExtractionRulesExcludeEncryptedSessionForCloudAndDeviceTransfer() {
        val bySection = mutableMapOf<String, MutableSet<String>>()
        var section = ""
        context.resources.getXml(R.xml.data_extraction_rules).use { parser ->
            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.eventType != XmlPullParser.START_TAG) continue
                when (parser.name) {
                    "cloud-backup", "device-transfer" -> section = parser.name
                    "exclude" -> if (parser.domain() == "sharedpref") {
                        bySection.getOrPut(section) { mutableSetOf() } += parser.path()
                    }
                }
            }
        }
        assertTrue("cloud=$bySection", SESSION_FILE in bySection["cloud-backup"].orEmpty())
        assertTrue("transfer=$bySection", SESSION_FILE in bySection["device-transfer"].orEmpty())
    }

    private fun sharedPrefExcludes(resId: Int): Set<String> {
        val result = mutableSetOf<String>()
        context.resources.getXml(resId).use { parser ->
            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.eventType == XmlPullParser.START_TAG &&
                    parser.name == "exclude" &&
                    parser.domain() == "sharedpref"
                ) {
                    result += parser.path()
                }
            }
        }
        return result
    }

    private fun XmlResourceParser.domain(): String? = getAttributeValue(null, "domain")

    private fun XmlResourceParser.path(): String = getAttributeValue(null, "path").orEmpty()

    private inline fun <T> XmlResourceParser.use(block: (XmlResourceParser) -> T): T =
        try { block(this) } finally { close() }

    private companion object {
        // = AppConstants.SESSION_PREFS_NAME + ".xml"
        const val SESSION_FILE = "oficios_session_encrypted.xml"
    }
}
