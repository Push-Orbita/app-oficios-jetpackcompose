package com.cocido.oficios.core.common.auth

object TokenExpiry {
    const val REFRESH_BUFFER_MS = 60_000L

    fun expiresAtMillis(expiresIn: String?, nowMillis: Long = System.currentTimeMillis()): Long? {
        val seconds = expiresIn?.trim()?.toLongOrNull() ?: return null
        if (seconds <= 0L) return null
        return nowMillis + seconds * 1_000L
    }

    fun shouldRefresh(expiresAtMillis: Long?, nowMillis: Long = System.currentTimeMillis()): Boolean {
        if (expiresAtMillis == null) return false
        return nowMillis >= expiresAtMillis - REFRESH_BUFFER_MS
    }
}
