package com.cocido.oficios.core.data.local

import com.cocido.oficios.core.database.dao.CachedItemDao
import com.cocido.oficios.core.database.entity.CachedItemEntity
import javax.inject.Inject

class HomeLocalDataSource @Inject constructor(
    private val cachedItemDao: CachedItemDao,
) {
    suspend fun getCachedHomeContent(): CachedItemEntity? {
        return cachedItemDao.getByKey(HOME_CONTENT_KEY)
    }

    suspend fun cacheHomeContent(title: String, description: String) {
        cachedItemDao.insert(
            CachedItemEntity(
                key = HOME_CONTENT_KEY,
                value = "$title|$description",
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }

    private companion object {
        const val HOME_CONTENT_KEY = "home_content"
    }
}
