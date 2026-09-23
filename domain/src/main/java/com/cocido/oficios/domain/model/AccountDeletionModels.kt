package com.cocido.oficios.domain.model

data class AccountDeletionRequest(
    val requestId: Long,
    val status: AccountDeletionStatus,
    val requestedAtIso: String?,
    val scheduledForIso: String?,
    val canCancelUntilIso: String?,
)

enum class AccountDeletionStatus {
    PENDING_GRACE_PERIOD,
    CANCELLED,
    COMPLETED,
    UNKNOWN,
}

data class RequestAccountDeletionParams(
    val currentPassword: String,
    val confirmation: String,
    val reasonCode: String = "NO_LONGER_NEEDED",
    val idempotencyKey: String,
)
