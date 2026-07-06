package com.cocido.oficios.core.network.api.profile.dto

import kotlinx.serialization.Serializable

@Serializable
data class MisDomiciliosResponseDto(
    val domicilios: List<DomicilioSinPersonaDto> = emptyList(),
)

@Serializable
data class DomicilioSinPersonaDto(
    val id: Long,
    val barrio: BarrioDto? = null,
    val nombre: String? = null,
    val calle: String? = null,
    val numero: String? = null,
    val casaTorre: String? = null,
    val departamento: String? = null,
    val piso: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val favorito: Boolean = false,
)

@Serializable
data class BarrioDto(
    val id: Long,
    val nombre: String? = null,
)

@Serializable
data class MisContactosResponseDto(
    val contactos: List<ContactoSinPersonaDto> = emptyList(),
)

@Serializable
data class ContactoSinPersonaDto(
    val id: Long,
    val nombre: String,
    val telefonoWhatsapp: String,
    val email: String? = null,
    val favorito: Boolean = false,
)

@Serializable
data class CertificadoTrabajoDto(
    val id: Long,
    val nombre: String,
    val urlArchivo: String? = null,
)

@Serializable
data class ImgDocumentoSearchResponseDto(
    val data: List<ImgDocumentoGroupDto> = emptyList(),
)

@Serializable
data class ImgDocumentoGroupDto(
    val documentos: List<ImgDocumentoItemDto> = emptyList(),
)

@Serializable
data class ImgDocumentoItemDto(
    val id: Long? = null,
    val frenteUrl: String? = null,
    val reversoUrl: String? = null,
    val frenteEstado: String? = null,
    val reversoEstado: String? = null,
    val frenteNombreArchivo: String? = null,
    val reversoNombreArchivo: String? = null,
)
