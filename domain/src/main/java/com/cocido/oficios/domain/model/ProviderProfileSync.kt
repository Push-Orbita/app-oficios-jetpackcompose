package com.cocido.oficios.domain.model

data class ProviderProfileSync(
    val tradeDraft: RegisterTradeDraft?,
    val paymentMethods: List<String> = emptyList(),
)
