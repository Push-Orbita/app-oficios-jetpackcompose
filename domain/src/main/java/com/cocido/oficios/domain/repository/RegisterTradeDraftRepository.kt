package com.cocido.oficios.domain.repository

import com.cocido.oficios.domain.model.RegisterTradeDraft

interface RegisterTradeDraftRepository {
    suspend fun save(draft: RegisterTradeDraft)
    suspend fun get(): RegisterTradeDraft?
    suspend fun clear()
}
