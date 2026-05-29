package com.cocido.oficios.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cocido.oficios.core.database.dao.CachedItemDao
import com.cocido.oficios.core.database.entity.CachedItemEntity

@Database(
    entities = [CachedItemEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cachedItemDao(): CachedItemDao
}
