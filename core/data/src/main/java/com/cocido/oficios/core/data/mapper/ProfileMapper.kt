package com.cocido.oficios.core.data.mapper

import com.cocido.oficios.core.network.api.profile.dto.CreateContactoRequestDto
import com.cocido.oficios.core.network.api.profile.dto.CreateDiaLaboralRequestDto
import com.cocido.oficios.core.network.api.profile.dto.CreateDomicilioLocalRequestDto
import com.cocido.oficios.core.network.api.profile.dto.CreateDomicilioRequestDto
import com.cocido.oficios.core.network.api.profile.dto.CreateFullAltaRubrosSubrubrosRequestDto
import com.cocido.oficios.core.network.api.profile.dto.CreateHorarioLaboralRequestDto
import com.cocido.oficios.core.network.api.profile.dto.CreateSubrubroRequestDto
import com.cocido.oficios.core.network.api.profile.dto.UpdateTrabajadorPerfilProfesionalRequestDto
import com.cocido.oficios.domain.model.ClientContactoRequest
import com.cocido.oficios.domain.model.ClientDomicilioRequest
import com.cocido.oficios.domain.model.ProviderProfessionalInfoRequest
import com.cocido.oficios.domain.model.ProviderRubroFullRequest
import com.cocido.oficios.domain.model.RegisterTradeTimeSlotDraft

fun ProviderProfessionalInfoRequest.toDto() = UpdateTrabajadorPerfilProfesionalRequestDto(
    descripcion = descripcion,
    experiencia = experiencia,
    documento = documento,
)

fun ClientDomicilioRequest.toDto() = CreateDomicilioRequestDto(
    barrioId = barrioId,
    personaId = personaId,
    nombre = nombre,
    calle = calle,
    numero = numero,
    latitud = latitud,
    longitud = longitud,
    favorito = favorito,
)

fun ClientContactoRequest.toDto() = CreateContactoRequestDto(
    nombre = nombre,
    telefonoWhatsapp = telefonoWhatsapp,
    email = email,
    favorito = favorito,
)

fun ProviderRubroFullRequest.toDto() = CreateFullAltaRubrosSubrubrosRequestDto(
    rubroId = rubroId,
    subrubros = subRubroIds.map { subRubroId ->
        CreateSubrubroRequestDto(
            subRubroId = subRubroId,
            lugarDomicilio = servesAtClientHome,
            lugarLocal = servesAtLocal,
            requiereTurnos = false,
        )
    },
    diasLaborales = workDays.map { day ->
        CreateDiaLaboralRequestDto(
            diaInicio = day,
            diaFin = day,
            horariosLaborales = timeSlots.toHorarios(),
        )
    },
    domicilioLocal = domicilioLocal?.let {
        CreateDomicilioLocalRequestDto(
            barrioId = it.barrioId,
            calle = it.calle,
            nombre = it.nombre,
            latitud = it.latitud,
            longitud = it.longitud,
        )
    },
)

private fun List<RegisterTradeTimeSlotDraft>.toHorarios(): List<CreateHorarioLaboralRequestDto> =
    filter { it.from.isNotBlank() && it.to.isNotBlank() }
        .map { CreateHorarioLaboralRequestDto(horaInicio = it.from, horaFin = it.to) }
        .ifEmpty { listOf(CreateHorarioLaboralRequestDto("09:00", "18:00")) }
