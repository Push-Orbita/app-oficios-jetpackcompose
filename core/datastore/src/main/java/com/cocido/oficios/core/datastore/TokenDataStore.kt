package com.cocido.oficios.core.datastore

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.cocido.oficios.core.common.auth.TokenExpiry
import com.cocido.oficios.core.model.AppConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenDataStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        AppConstants.SESSION_PREFS_NAME,
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    fun getUserId(): Long? {
        val value = prefs.getLong(KEY_USER_ID, -1L)
        return if (value == -1L) null else value
    }

    fun getUserRole(): String? = prefs.getString(KEY_USER_ROLE, null)

    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)

    fun getUserFirstName(): String? = prefs.getString(KEY_USER_FIRST_NAME, null)

    fun getUserLastName(): String? = prefs.getString(KEY_USER_LAST_NAME, null)

    fun isEmailVerified(): Boolean = prefs.getBoolean(KEY_EMAIL_VERIFIED, false)

    fun getClientRoleId(): Long? {
        val value = prefs.getLong(KEY_CLIENT_ROLE_ID, -1L)
        return if (value == -1L) null else value
    }

    fun getProviderRoleId(): Long? {
        val value = prefs.getLong(KEY_PROVIDER_ROLE_ID, -1L)
        return if (value == -1L) null else value
    }

    fun getAccessTokenExpiresAtMillis(): Long? {
        val value = prefs.getLong(KEY_ACCESS_TOKEN_EXPIRES_AT, -1L)
        return if (value == -1L) null else value
    }

    fun saveSession(
        accessToken: String,
        refreshToken: String,
        userId: Long,
        userRole: String,
        email: String,
        firstName: String,
        lastName: String,
        emailVerified: Boolean,
        accessTokenExpiresAtMillis: Long? = null,
    ) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putLong(KEY_USER_ID, userId)
            .putString(KEY_USER_ROLE, userRole)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_FIRST_NAME, firstName)
            .putString(KEY_USER_LAST_NAME, lastName)
            .putBoolean(KEY_EMAIL_VERIFIED, emailVerified)
            .apply {
                if (accessTokenExpiresAtMillis != null) {
                    putLong(KEY_ACCESS_TOKEN_EXPIRES_AT, accessTokenExpiresAtMillis)
                } else {
                    remove(KEY_ACCESS_TOKEN_EXPIRES_AT)
                }
            }
            .apply()
    }

    fun updateTokens(
        accessToken: String,
        refreshToken: String,
        accessTokenExpiresAtMillis: Long? = null,
    ) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply {
                if (accessTokenExpiresAtMillis != null) {
                    putLong(KEY_ACCESS_TOKEN_EXPIRES_AT, accessTokenExpiresAtMillis)
                } else {
                    remove(KEY_ACCESS_TOKEN_EXPIRES_AT)
                }
            }
            .apply()
    }

    fun updateTokensFromExpiresIn(accessToken: String, refreshToken: String, expiresIn: String?) {
        updateTokens(
            accessToken = accessToken,
            refreshToken = refreshToken,
            accessTokenExpiresAtMillis = TokenExpiry.expiresAtMillis(expiresIn),
        )
    }

    fun updateEmailVerified(emailVerified: Boolean) {
        prefs.edit()
            .putBoolean(KEY_EMAIL_VERIFIED, emailVerified)
            .apply()
    }

    fun saveRoleIds(clientRoleId: Long?, providerRoleId: Long?) {
        prefs.edit().apply {
            if (clientRoleId != null) putLong(KEY_CLIENT_ROLE_ID, clientRoleId)
            if (providerRoleId != null) putLong(KEY_PROVIDER_ROLE_ID, providerRoleId)
            apply()
        }
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    fun hasSession(): Boolean = !getAccessToken().isNullOrBlank()

    fun isSessionEphemeral(): Boolean = prefs.getBoolean(KEY_SESSION_EPHEMERAL, false)

    fun setSessionEphemeral(ephemeral: Boolean) {
        prefs.edit().putBoolean(KEY_SESSION_EPHEMERAL, ephemeral).apply()
    }

    private companion object {
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_USER_ID = "user_id"
        const val KEY_USER_ROLE = "user_role"
        const val KEY_USER_EMAIL = "user_email"
        const val KEY_USER_FIRST_NAME = "user_first_name"
        const val KEY_USER_LAST_NAME = "user_last_name"
        const val KEY_EMAIL_VERIFIED = "email_verified"
        const val KEY_CLIENT_ROLE_ID = "client_role_id"
        const val KEY_PROVIDER_ROLE_ID = "provider_role_id"
        const val KEY_SESSION_EPHEMERAL = "session_ephemeral"
        const val KEY_ACCESS_TOKEN_EXPIRES_AT = "access_token_expires_at"
    }
}
