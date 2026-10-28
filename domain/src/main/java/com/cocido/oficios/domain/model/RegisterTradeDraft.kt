package com.cocido.oficios.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class RegisterTradeTimeSlotDraft(
    val from: String,
    val to: String,
)

@Serializable
data class RegisterTradeDraft(
    val categoryId: Long,
    val categoryName: String,
    val rubroId: Long,
    val rubroName: String,
    val subRubroIds: List<Long>,
    val otherSpecialization: String?,
    val experienceRange: String,
    val neighborhoodQuery: String,
    val barrioId: Long? = null,
    val workDays: List<String> = emptyList(),
    val servesAtClientHome: Boolean = true,
    val servesAtLocal: Boolean = false,
    val requiresTurns: Boolean = false,
    val turnDurationMinutes: Int? = null,
    val turnIntervalMinutes: Int? = null,
    val timeSlots: List<RegisterTradeTimeSlotDraft>,
    val paymentMethods: List<String> = emptyList(),
)
