package com.cocido.oficios.domain.repository

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.HomeContent

interface HomeRepository {
    suspend fun getHomeContent(forceError: Boolean = false): AppResult<HomeContent>
}
