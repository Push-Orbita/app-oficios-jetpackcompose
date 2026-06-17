package com.cocido.oficios.domain.model

data class ProviderRegistrationOutcome(
    val session: UserSession,
    val profileSynced: Boolean,
    val profileSyncMessage: String? = null,
)
