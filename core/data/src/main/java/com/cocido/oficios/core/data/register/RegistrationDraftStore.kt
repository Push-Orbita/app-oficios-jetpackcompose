package com.cocido.oficios.core.data.register

import com.cocido.oficios.core.datastore.EncryptedDraftPrefs
import com.cocido.oficios.domain.model.RegistrationDraft
import com.cocido.oficios.domain.repository.RegistrationDraftRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Singleton
class RegistrationDraftStore @Inject constructor(
    private val prefs: EncryptedDraftPrefs,
    private val json: Json,
) : RegistrationDraftRepository {

    override suspend fun save(draft: RegistrationDraft) = withContext(Dispatchers.IO) {
        prefs.write(json.encodeToString(draft))
    }

    override suspend fun get(): RegistrationDraft? = withContext(Dispatchers.IO) {
        val raw = prefs.read() ?: return@withContext null
        runCatching { json.decodeFromString<RegistrationDraft>(raw) }.getOrNull()
    }

    override suspend fun clear() = withContext(Dispatchers.IO) {
        prefs.clear()
    }
}
