package com.cocido.oficios.core.network.api.profile.dto

import com.cocido.oficios.core.network.serialization.FlexibleBooleanSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MisDomiciliosResponseDto(
    val domicilios: List<DomicilioSinPersonaDto> = emptyList(),
)

@Serializable
data class DomicilioSinPersonaDto(
    val id: Long = 0L,
    val barrio: BarrioDto? = null,
    val nombre: String? = null,
    val calle: String? = null,
    val numero: String? = null,
    val casaTorre: String? = null,
    val departamento: String? = null,
    val piso: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    @Serializable(with = FlexibleBooleanSerializer::class)
    val favorito: Boolean = false,
)

@Serializable
data class BarrioDto(
    val id: Long = 0L,
    val nombre: String? = null,
)

@Serializable
data class MisContactosResponseDto(
    val contactos: List<ContactoSinPersonaDto> = emptyList(),
)

@Serializable
data class ContactoSinPersonaDto(
    val id: Long = 0L,
    val nombre: String = "",
    val telefonoWhatsapp: String = "",
    val telefonoVerificado: Boolean = false,
    val email: String? = null,
    @Serializable(with = FlexibleBooleanSerializer::class)
    val favorito: Boolean = false,
)

@Serializable
data class CreateContactoResponseDto(
    val id: Long,
)

@Serializable
data class EnviarCodigoTelefonoResponseDto(
    val message: String,
    val canal: String = "whatsapp",
    val enviado: Boolean = true,
    val expiraEnMinutos: Int = 10,
    val proximoReenvioEnSegundos: Int? = null,
)

@Serializable
data class VerifyTelefonoCodigoRequestDto(
    @SerialName("codigo") val codigo: String,
)

@Serializable
data class VerificarTelefonoCodigoResponseDto(
    val message: String,
    val contacto: ContactoSinPersonaDto? = null,
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
    val tipoDocumento: String? = null,
    val frenteUrl: String? = null,
    val reversoUrl: String? = null,
    val selfieUrl: String? = null,
    val frenteEstado: String? = null,
    val reversoEstado: String? = null,
    val selfieEstado: String? = null,
    val frenteNombreArchivo: String? = null,
    val reversoNombreArchivo: String? = null,
    val motivoRechazo: String? = null,
)