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
)

data class ProviderBenefit(
    val title: String,
    val description: String,
    val discountLabel: String? = null,
)
