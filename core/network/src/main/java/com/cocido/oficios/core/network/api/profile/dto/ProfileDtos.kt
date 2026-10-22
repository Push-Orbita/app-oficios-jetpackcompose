package com.cocido.oficios.core.network.api.profile.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateTrabajadorPerfilProfesionalRequestDto(
    val descripcion: String? = null,
    val experiencia: String? = null,
    val documento: String? = null,
    val metodosPago: List<String>? = null,
)

@Serializable
data class CreateFullAltaRubrosSubrubrosRequestDto(
    val rubroId: Long,
    val autoAceptacion: Boolean = false,
    val subrubros: List<CreateSubrubroRequestDto>,
    val diasLaborales: List<CreateDiaLaboralRequestDto>,
    val domicilioLocal: CreateDomicilioLocalRequestDto? = null,
    val metodosPago: List<String>? = null,
)

@Serializable
data class CreateSubrubroRequestDto(
    val subRubroId: Long,
    val lugarDomicilio: Boolean,
    val lugarLocal: Boolean,
    val requiereTurnos: Boolean = false,
)

@Serializable
data class CreateDiaLaboralRequestDto(
    val diaInicio: String,
    val diaFin: String,
    val horariosLaborales: List<CreateHorarioLaboralRequestDto>,
)

@Serializable
data class CreateHorarioLaboralRequestDto(
    val horaInicio: String,
    val horaFin: String,
)

@Serializable
data class CreateDomicilioLocalRequestDto(
    val barrioId: Long,
    val calle: String? = null,
    val numero: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val nombre: String? = null,
)

@Serializable
data class CreateDomicilioRequestDto(
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
    val favorito: Boolean = false,
)

@Serializable
data class CreateContactoRequestDto(
    val nombre: String,
    val telefonoWhatsapp: String,
    val email: String? = null,
    val favorito: Boolean = false,
)

@Serializable
data class UpdateDomicilioRequestDto(
    val barrioId: Long,
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
data class UpdateContactoRequestDto(
    val nombre: String,
    val telefonoWhatsapp: String,
    val email: String? = null,
    val favorito: Boolean = false,
)

@Serializable
data class UpdateFavoriteRequestDto(
    val favorito: Boolean,
)
