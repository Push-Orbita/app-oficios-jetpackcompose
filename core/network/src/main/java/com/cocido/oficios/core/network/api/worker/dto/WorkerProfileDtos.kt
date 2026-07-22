package com.cocido.oficios.core.network.api.worker.dto

import com.cocido.oficios.core.network.api.auth.dto.PersonaDto
import com.cocido.oficios.core.network.api.auth.dto.UsuarioDto
import com.cocido.oficios.core.network.api.catalog.dto.BarrioDto
import com.cocido.oficios.core.network.api.profile.dto.CertificadoTrabajoDto
import com.cocido.oficios.core.network.api.profile.dto.TrabajadorOficioRubroFullDto
import com.cocido.oficios.core.network.api.search.dto.ReputacionResumenDto
import kotlinx.serialization.Serializable

@Serializable
data class TrabajadorOficioPerfilCompletoDto(
    val id: Long,
    val usuario: UsuarioDto,
    val estado: String,
    val documento: String? = null,
    val rubros: List<TrabajadorOficioRubroFullDto> = emptyList(),
    val reputacion: ReputacionResumenDto? = null,
    val certificados: List<CertificadoTrabajoDto> = emptyList(),
    val galeriaTrabajos: List<GaleriaTrabajoSimpleDto> = emptyList(),
    val domicilios: List<DomicilioEnrichedDto> = emptyList(),

    // No aparece en Swagger del endpoint de detalle en staging, pero lo dejamos opcional para robustez.
    val descripcion: String? = null,
    val experiencia: String? = null,
)

@Serializable
data class GaleriaTrabajoSimpleDto(
    val id: Long? = null,
    val urlArchivo: String? = null,
)

@Serializable
data class DomicilioEnrichedDto(
    val id: Long? = null,
    val barrioId: Long? = null,
    val personaId: Long? = null,
    val persona: PersonaDto? = null,
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

// ---------------------------------------------------------------------------
// Calificaciones / reseñas
// ---------------------------------------------------------------------------

@Serializable
data class BookingSimpleDto(
    val id: Long? = null,
    val inicioConfirmado: String? = null,
    val finConfirmado: String? = null,
    val tipo: String? = null,
    val estado: String? = null,
    val domicilioTexto: String? = null,
    val contactoTexto: String? = null,
)

@Serializable
data class CalificacionDto(
    val id: Long? = null,
    val booking: BookingSimpleDto? = null,
    val calidad: Int? = null,
    val puntualidad: Int? = null,
    val comentario: String? = null,
)

