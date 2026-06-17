package com.cocido.oficios.domain.model

data class ClientDomicilioRequest(
    val barrioId: Long,
    val personaId: Long,
    val nombre: String? = null,
    val calle: String? = null,
    val numero: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val favorito: Boolean = true,
)

data class ClientContactoRequest(
    val nombre: String,
    val telefonoWhatsapp: String,
    val email: String? = null,
    val favorito: Boolean = true,
)

data class ProviderProfessionalInfoRequest(
    val descripcion: String? = null,
    val experiencia: String? = null,
    val documento: String? = null,
)

data class ProviderRubroFullRequest(
    val rubroId: Long,
    val subRubroIds: List<Long>,
    val servesAtClientHome: Boolean,
    val servesAtLocal: Boolean,
    val workDays: List<String>,
    val timeSlots: List<RegisterTradeTimeSlotDraft>,
    val requiresTurns: Boolean = false,
    val domicilioLocal: ProviderLocalDomicilioRequest? = null,
)

data class ProviderLocalDomicilioRequest(
    val barrioId: Long,
    val calle: String,
    val nombre: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
)
