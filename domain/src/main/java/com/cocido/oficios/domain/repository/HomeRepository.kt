package com.cocido.oficios.domain.repository

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.ClientHomeContent
import com.cocido.oficios.domain.model.HomeCategoryItem
import com.cocido.oficios.domain.model.RecentSearchEntry

interface HomeRepository {
    suspend fun getClientHome(): AppResult<ClientHomeContent>

    suspend fun getCategories(): AppResult<List<HomeCategoryItem>>

    suspend fun trackRecentSearch(entry: RecentSearchEntry)
}
