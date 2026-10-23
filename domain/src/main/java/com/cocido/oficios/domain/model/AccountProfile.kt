package com.cocido.oficios.domain.model

data class AccountProfile(
    val user: User,
    val providerDetails: ProviderAccountDetails? = null,
)

data class ProviderCommercialLocal(
    val id: Long? = null,
    val barrioId: Long? = null,
    val barrioName: String? = null,
    val street: String? = null,
    val number: String? = null,
    val name: String? = null,
)

data class ProviderAccountDetails(
    val workerId: Long,
    val status: String,
    val document: String?,
    val description: String?,
    val experience: String?,
    val paymentMethods: List<String>,
    val services: List<ManagedProviderService>,
    val gallery: List<WorkGalleryItem>,
    val certificates: List<WorkCertificateItem>,
    val commercialLocal: ProviderCommercialLocal? = null,
) {
    val hasCommercialLocal: Boolean
        get() = services.any { service -> service.subServices.any { it.servesAtLocal } } ||
            !commercialLocal?.street.isNullOrBlank()
}

data class ManagedProviderService(
    val id: Long,
    val rubroId: Long,
    val rubroName: String,
    val isPrimary: Boolean,
    val subServices: List<ManagedProviderSubService>,
    val workSchedule: List<ManagedWorkSchedule>,
)

data class ManagedProviderSubService(
    val id: Long,
    val catalogSubRubroId: Long,
    val name: String,
    val servesAtClientHome: Boolean,
    val servesAtLocal: Boolean,
    val requiresTurns: Boolean,
)

data class ManagedWorkSchedule(
    val dayFrom: String,
    val dayTo: String,
    val ranges: List<ManagedTimeRange>,
)

data class ManagedTimeRange(
    val from: String,
    val to: String,
)

private val WEEK_DAYS = listOf(
    "LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO", "DOMINGO",
)

fun ManagedWorkSchedule.expandedDays(): List<String> {
    val start = WEEK_DAYS.indexOf(dayFrom)
    val end = WEEK_DAYS.indexOf(dayTo)
    if (start < 0 || end < 0) return listOf(dayFrom)
    return if (start <= end) {
        WEEK_DAYS.subList(start, end + 1)
    } else {
        WEEK_DAYS.subList(start, WEEK_DAYS.size) + WEEK_DAYS.subList(0, end + 1)
    }
}

fun ManagedProviderService.toRubroFullRequest(
    paymentMethods: List<String>,
    servesAtLocal: Boolean = subServices.any { it.servesAtLocal },
    domicilioLocal: ProviderLocalDomicilioRequest? = null,
): ProviderRubroFullRequest {
    val workDays = workSchedule.flatMap { it.expandedDays() }.distinct()
    val timeSlots = workSchedule
        .flatMap { schedule -> schedule.ranges.map { RegisterTradeTimeSlotDraft(it.from, it.to) } }
        .distinct()
    return ProviderRubroFullRequest(
        rubroId = rubroId,
        subRubroIds = subServices.map { it.catalogSubRubroId }.filter { it > 0L },
        servesAtClientHome = subServices.any { it.servesAtClientHome },
        servesAtLocal = servesAtLocal,
        workDays = workDays.ifEmpty { listOf("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES") },
        timeSlots = timeSlots.ifEmpty { listOf(RegisterTradeTimeSlotDraft("09:00", "18:00")) },
        requiresTurns = subServices.any { it.requiresTurns },
        domicilioLocal = domicilioLocal,
        paymentMethods = paymentMethods,
    )
}

data class UpdateAccountProfileRequest(
    val userId: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val currentPhotoUrl: String? = null,
    val photoFilePath: String? = null,
    val removePhoto: Boolean = false,
)

data class UpdateClientDomicilioRequest(
    val barrioId: Long,
    val nombre: String?,
    val calle: String?,
    val numero: String?,
    val casaTorre: String?,
    val departamento: String?,
    val piso: String?,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val favorito: Boolean,
)

data class UpdateClientContactoRequest(
    val nombre: String,
    val telefonoWhatsapp: String,
    val email: String?,
    val favorito: Boolean,
)
