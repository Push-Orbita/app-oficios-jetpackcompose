package com.cocido.oficios.core.network.api.auth.dto

import com.cocido.oficios.core.network.api.profile.dto.ProviderDomicilioDto
import com.cocido.oficios.core.network.api.profile.dto.CertificadoTrabajoDto
import com.cocido.oficios.core.network.api.profile.dto.TrabajadorOficioRubroFullDto
import com.cocido.oficios.core.network.api.account.dto.WorkGalleryDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class LoginRequestDto(
    val email: String,
    val contrasena: String,
)

@Serializable
data class LoginResponseDto(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("expires_in") val expiresIn: String? = null,
    val usuario: UsuarioDto,
)

@Serializable
data class SignupResponseDto(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("expires_in") val expiresIn: String? = null,
    val usuario: UsuarioDto,
    val message: String? = null,
    val emailSent: Boolean = false,
    val cliente: SimpleClienteResponseDto? = null,
    val trabajadorOficio: SimpleTrabajadorResponseDto? = null,
)

@Serializable
data class RefreshTokenRequestDto(
    @SerialName("refresh_token") val refreshToken: String,
)

@Serializable
data class RefreshTokenResponseDto(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("expires_in") val expiresIn: String? = null,
)

@Serializable
data class UsuarioDto(
    val id: Long,
    val email: String,
    val emailVerificado: Boolean = false,
    val fotoPerfil: JsonElement? = null,
    val persona: PersonaDto? = null,
    val rolUsuario: RolUsuarioDto? = null,
)

@Serializable
data class PersonaDto(
    val id: Long? = null,
    val nombres: String? = null,
    val apellido: String? = null,
)

@Serializable
data class RolUsuarioDto(
    val id: Long,
    val nombre: String,
)

@Serializable
data class SimpleClienteResponseDto(
    val id: Long,
    val estado: String,
    val documento: String? = null,
)

@Serializable
data class SimpleTrabajadorResponseDto(
    val id: Long,
    val estado: String,
    val documento: String? = null,
)

@Serializable
data class RequestPasswordResetCodeDto(
    val email: String,
)

@Serializable
data class VerifyPasswordResetCodeDto(
    val codigo: String,
)

@Serializable
data class VerifyEmailRequestDto(
    val codigo: String,
)

@Serializable
data class MessageResponseDto(
    val message: String,
)

@Serializable
data class VerifyPasswordResetCodeResponseDto(
    @SerialName("reset_token") val resetToken: String? = null,
)

@Serializable
data class ResetPasswordWithCodeDto(
    val contrasena: String,
    val confirmarContrasena: String,
)

@Serializable
data class RolUsuarioSearchResponseDto(
    val data: List<RolUsuarioDto>? = null,
    val items: List<RolUsuarioDto>? = null,
)

@Serializable
data class ClientePerfilEstadoDto(
    val perfilCompleto: Boolean,
    val requisitos: ClientePerfilEstadoRequisitosDto? = null,
    val faltantes: List<String> = emptyList(),
)

@Serializable
data class ClientePerfilEstadoRequisitosDto(
    val tieneContacto: Boolean = false,
    val tieneDomicilio: Boolean = false,
    val tieneContactoFavorito: Boolean = false,
    val tieneDomicilioFavorito: Boolean = false,
    val emailVerificado: Boolean = false,
    val telefonoVerificado: Boolean = false,
)

@Serializable
data class TrabajadorPerfilEstadoDto(
    val estado: String,
    val perfilCompleto: Boolean = false,
    val configuracionCompleta: Boolean = false,
    val requisitos: TrabajadorPerfilEstadoRequisitosDto? = null,
    val faltantes: List<String> = emptyList(),
)

@Serializable
data class TrabajadorPerfilEstadoRequisitosDto(
    val tieneDniNumero: Boolean = false,
    val tieneDniImagenes: Boolean = false,
    val identidadVerificada: Boolean = false,
    val tieneRubroPrincipal: Boolean = false,
    val tieneSubrubros: Boolean = false,
    val tieneDisponibilidad: Boolean = false,
    val tieneAgendaSiAplica: Boolean = false,
    val perfilOperativo: Boolean = false,
    val emailVerificado: Boolean = false,
    val requisitosOnboardingCompletos: Boolean = false,
)

@Serializable
data class TrabajadorOficioPerfilMeDto(
    val id: Long,
    val estado: String,
    val documento: String? = null,
    val descripcion: String? = null,
    val experiencia: String? = null,
    val perfilOperativo: Boolean = false,
    val rubros: List<TrabajadorOficioRubroFullDto> = emptyList(),
    val domicilios: List<ProviderDomicilioDto> = emptyList(),
    val metodosPago: List<String> = emptyList(),
    val documentoIdentidad: ImgDocumentoSimpleDto? = null,
    val certificados: List<CertificadoTrabajoDto> = emptyList(),
    val galeriaTrabajos: List<WorkGalleryDto> = emptyList(),
    val usuario: UsuarioDto? = null,
)

@Serializable
data class ImgDocumentoSimpleDto(
    val motivoRechazo: String? = null,
)

@Serializable
data class ClientePerfilMeDto(
    val id: Long,
    val estado: String,
    val documento: String? = null,
    val domicilios: List<JsonElement> = emptyList(),
    val contactos: List<JsonElement> = emptyList(),
    val usuario: UsuarioDto? = null,
)
