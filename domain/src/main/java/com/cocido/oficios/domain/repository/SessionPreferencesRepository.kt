package com.cocido.oficios.domain.repository

interface SessionPreferencesRepository {
    fun setSessionEphemeral(ephemeral: Boolean)
    fun isSessionEphemeral(): Boolean
}
