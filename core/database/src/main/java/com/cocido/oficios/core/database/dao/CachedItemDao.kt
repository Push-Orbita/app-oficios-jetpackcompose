package com.cocido.oficios.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cocido.oficios.core.database.entity.CachedItemEntity

@Dao
interface CachedItemDao {
    @Query("SELECT * FROM cached_items WHERE `key` = :key LIMIT 1")
    suspend fun getByKey(key: String): CachedItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CachedItemEntity)
}
