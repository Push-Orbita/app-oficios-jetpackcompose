package com.cocido.oficios.domain.repository

import com.cocido.oficios.domain.model.RegistrationDraft

interface RegistrationDraftRepository {
    suspend fun save(draft: RegistrationDraft)
    suspend fun get(): RegistrationDraft?
    suspend fun clear()
}
