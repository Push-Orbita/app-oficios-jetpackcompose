package com.cocido.oficios.core.model

object AppConstants {
    const val DATABASE_NAME = "template_jetcom.db"
    const val DATASTORE_NAME = "user_preferences"
    const val SESSION_PREFS_NAME = "oficios_session_encrypted"
    const val MIN_PASSWORD_LENGTH = 6
    /**
     * Dirección pública de los términos. Vacía hasta que haya una URL que no redirija en loop:
     * en ese caso la app muestra la pantalla interna.
     */
    const val TERMS_URL = ""

    fun hasTermsUrl(url: String = TERMS_URL): Boolean {
        val trimmed = url.trim()
        return trimmed.startsWith("https://") || trimmed.startsWith("http://")
    }
    const val SUPPORT_PHONE = "+5491112345678"

    fun hasSupportPhone(phone: String = SUPPORT_PHONE): Boolean = phone.isNotBlank()
}
