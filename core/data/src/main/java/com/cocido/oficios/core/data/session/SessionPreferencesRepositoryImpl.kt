package com.cocido.oficios.core.data.session

import com.cocido.oficios.core.datastore.TokenDataStore
import com.cocido.oficios.domain.repository.SessionPreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionPreferencesRepositoryImpl @Inject constructor(
    private val tokenDataStore: TokenDataStore,
) : SessionPreferencesRepository {

    override fun setSessionEphemeral(ephemeral: Boolean) {
        tokenDataStore.setSessionEphemeral(ephemeral)
    }

    override fun isSessionEphemeral(): Boolean = tokenDataStore.isSessionEphemeral()
}
