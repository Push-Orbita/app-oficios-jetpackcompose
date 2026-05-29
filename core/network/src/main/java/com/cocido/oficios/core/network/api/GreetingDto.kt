package com.cocido.oficios.core.network.api

import kotlinx.serialization.Serializable

@Serializable
data class GreetingDto(
    val id: Int,
    val title: String,
    val body: String,
)
