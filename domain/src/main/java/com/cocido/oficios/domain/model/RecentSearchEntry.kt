package com.cocido.oficios.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class RecentSearchType {
    CATEGORY,
    RUBRO,
    SUBRUBRO,
    QUERY,
}

@Serializable
data class RecentSearchEntry(
    val type: RecentSearchType,
    val id: Long,
    val label: String,
    val categoryId: Long? = null,
    val rubroId: Long? = null,
    val timestamp: Long = System.currentTimeMillis(),
)
