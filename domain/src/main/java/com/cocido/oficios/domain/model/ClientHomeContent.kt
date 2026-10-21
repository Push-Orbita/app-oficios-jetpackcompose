package com.cocido.oficios.domain.model

data class ClientHomeContent(
    val userDisplayName: String,
    val emailVerified: Boolean,
    val profilePhotoUrl: String?,
    val recentSearches: List<RecentSearchEntry>,
    val categories: List<HomeCategoryItem> = emptyList(),
    val featuredWorkers: List<WorkerSummary> = emptyList(),
)

data class HomeCategoryItem(
    val id: Long,
    val name: String,
    val iconUrl: String? = null,
)
