package com.cocido.oficios.domain.model

data class ProviderHomeContent(
    val userDisplayName: String,
    val isVerified: Boolean,
    val primarySpecialtyLabel: String?,
    val profileCompletionPercent: Int,
    val missingProfileItems: List<String>,
    val benefits: List<ProviderBenefit> = emptyList(),
    val hasActiveBooking: Boolean = false,
)

data class ProviderBenefit(
    val title: String,
    val description: String,
    val discountLabel: String? = null,
)
