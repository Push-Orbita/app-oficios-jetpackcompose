package com.cocido.oficios.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_items")
data class CachedItemEntity(
    @PrimaryKey val key: String,
    val value: String,
    val updatedAt: Long,
)
