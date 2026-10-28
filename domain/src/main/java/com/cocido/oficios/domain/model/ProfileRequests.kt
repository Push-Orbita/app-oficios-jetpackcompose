package com.cocido.oficios.domain.model

data class ClientDomicilioRequest(
    val barrioId: Long,
    val personaId: Long,
    val nombre: String? = null,
    val calle: String? = null,
    val numero: String? = null,
    val casaTorre: String? = null,
    val departamento: String? = null,
    val piso: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val favorito: Boolean = true,
)

data class ClientDomicilio(
    val id: Long,
    val barrioId: Long,
    val barrioName: String?,
    val nombre: String?,
    val calle: String?,
    val numero: String?,
    val casaTorre: String?,
    val departamento: String?,
    val piso: String?,
    val favorito: Boolean,
    val latitud: Double? = null,
    val longitud: Double? = null,
)

data class ClientContacto(
    val id: Long,
    val nombre: String,
    val telefonoWhatsapp: String,
    val telefonoVerificado: Boolean = false,
    val email: String?,
    val favorito: Boolean,
)

data class PhoneVerificationSendResult(
    val message: String,
    val resendCooldownSeconds: Int?,
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
    val paymentMethods: List<String>? = null,
)

data class ProviderRubroFullRequest(
    val rubroId: Long,
    val subRubroIds: List<Long>,
    val servesAtClientHome: Boolean,
    val servesAtLocal: Boolean,
    val workDays: List<String>,
    val timeSlots: List<RegisterTradeTimeSlotDraft>,
    val weeklyAvailability: WeeklyAvailability? = null,
    val requiresTurns: Boolean = false,
    val turnDurationMinutes: Int? = null,
    val turnIntervalMinutes: Int? = null,
    val domicilioLocal: ProviderLocalDomicilioRequest? = null,
    val paymentMethods: List<String> = emptyList(),
)

data class ProviderLocalDomicilioRequest(
    val barrioId: Long,
    val calle: String? = null,
    val numero: String? = null,
    val nombre: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
)
