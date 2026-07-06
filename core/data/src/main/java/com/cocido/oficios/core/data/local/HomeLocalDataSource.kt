package com.cocido.oficios.core.data.local

import com.cocido.oficios.core.database.dao.CachedItemDao
import com.cocido.oficios.core.database.entity.CachedItemEntity
import javax.inject.Inject

class HomeLocalDataSource @Inject constructor(
    private val cachedItemDao: CachedItemDao,
) {
    suspend fun getRecentCategoryIds(): List<Long> {
        val cached = cachedItemDao.getByKey(RECENT_CATEGORIES_KEY) ?: return emptyList()
        return cached.value
            .split(",")
            .mapNotNull { it.trim().toLongOrNull() }
    }

    suspend fun saveRecentCategoryIds(ids: List<Long>) {
        cachedItemDao.insert(
            CachedItemEntity(
                key = RECENT_CATEGORIES_KEY,
                value = ids.joinToString(","),
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }

    companion object {
        const val MAX_RECENT_CATEGORIES = 6
        private const val RECENT_CATEGORIES_KEY = "home_recent_categories"
    }
}
