package com.cocido.oficios.domain.model

data class WorkerProfile(
    val id: Long,
    val displayName: String,
    val avatarUrl: String?,
    val isVerified: Boolean,
    val specialties: List<WorkerSpecialty> = emptyList(),
    val availability: WorkerAvailability? = null,
    val reputation: WorkerReputation? = null,
    val gallery: List<WorkGalleryItem> = emptyList(),
    val certificates: List<WorkCertificateItem> = emptyList(),
    val workAddresses: List<WorkerWorkAddress> = emptyList(),
)

data class WorkerSpecialty(
    val rubroName: String,
    val subRubroNames: List<String>,
    val servesAtHome: Boolean,
    val servesAtLocal: Boolean,
    val isPrimary: Boolean,
)

data class WorkerAvailability(
    val summaryLabel: String,
    val workDays: List<WorkerWorkDay> = emptyList(),
)

data class WorkerWorkDay(
    val dayStart: String,
    val dayEnd: String,
    val startTime: String,
    val endTime: String,
)

data class WorkerReputation(
    val rating: Float?,
    val reviewCount: Int?,
)

data class WorkGalleryItem(
    val id: Long,
    val imageUrl: String?,
)

data class WorkCertificateItem(
    val id: Long,
    val name: String,
    val fileUrl: String?,
)

data class WorkerWorkAddress(
    val id: Long,
    val barrioId: Long?,
    val label: String?,
    val calle: String?,
)

data class WorkerReview(
    val id: Long,
    val qualityScore: Int?,
    val punctualityScore: Int?,
    val comment: String?,
    val clientLabel: String?,
)

data class PagedWorkerReviews(
    val items: List<WorkerReview>,
    val pageNumber: Int,
    val totalPages: Int?,
    val hasMore: Boolean,
)

