package com.cocido.oficios.domain.model

data class AccountProfile(
    val user: User,
    val providerDetails: ProviderAccountDetails? = null,
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
)

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
