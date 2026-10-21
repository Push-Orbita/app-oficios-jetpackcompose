package com.cocido.oficios.domain.model

data class ProviderHomeContent(
    val userDisplayName: String,
    val isVerified: Boolean,
    val primarySpecialtyLabel: String?,
    val profileCompletionPercent: Int,
    val missingProfileItems: List<String>,
    val benefits: List<ProviderBenefit> = emptyList(),
    val hasActiveBooking: Boolean = false,
    val upcomingOrders: List<ServiceOrderListItem> = emptyList(),
    val profilePhotoUrl: String? = null,
    val hasAgendaConfigured: Boolean = false,
    val hasGalleryPhotos: Boolean = false,
    val hasCommercialLocal: Boolean = false,
    val hasCertificate: Boolean = false,
) {
    val headerSubtitle: String?
        get() = if (isVerified) primarySpecialtyLabel else null

    val showProfileCompletion: Boolean
        get() = profileCompletionPercent < 100
}

data class ProviderBenefit(
    val title: String,
    val description: String,
    val discountLabel: String? = null,
)
