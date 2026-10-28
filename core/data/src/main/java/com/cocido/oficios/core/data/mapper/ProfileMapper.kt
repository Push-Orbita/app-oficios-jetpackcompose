package com.cocido.oficios.core.data.mapper

import com.cocido.oficios.core.network.api.profile.dto.CreateContactoRequestDto
import com.cocido.oficios.core.network.api.profile.dto.CreateDiaLaboralRequestDto
import com.cocido.oficios.core.network.api.profile.dto.CreateDomicilioLocalRequestDto
import com.cocido.oficios.core.network.api.profile.dto.CreateDomicilioRequestDto
import com.cocido.oficios.core.network.api.profile.dto.CreateFullAltaRubrosSubrubrosRequestDto
import com.cocido.oficios.core.network.api.profile.dto.CreateHorarioLaboralRequestDto
import com.cocido.oficios.core.network.api.profile.dto.CreateSesionItemRequestDto
import com.cocido.oficios.core.network.api.profile.dto.CreateSubrubroRequestDto
import com.cocido.oficios.core.network.api.profile.dto.UpdateTrabajadorPerfilProfesionalRequestDto
import com.cocido.oficios.core.network.api.profile.dto.UpdateContactoRequestDto
import com.cocido.oficios.core.network.api.profile.dto.UpdateDomicilioRequestDto
import com.cocido.oficios.core.network.api.profile.dto.ContactoSinPersonaDto
import com.cocido.oficios.core.network.api.profile.dto.DomicilioSinPersonaDto
import com.cocido.oficios.domain.model.ClientContacto
import com.cocido.oficios.domain.model.ClientContactoRequest
import com.cocido.oficios.domain.model.ClientDomicilio
import com.cocido.oficios.domain.model.ClientDomicilioRequest
import com.cocido.oficios.domain.model.ProviderProfessionalInfoRequest
import com.cocido.oficios.domain.model.ProviderRubroFullRequest
import com.cocido.oficios.domain.model.RegisterTradeTimeSlotDraft
import com.cocido.oficios.domain.model.toReplaceDays
import com.cocido.oficios.domain.model.UpdateClientContactoRequest
import com.cocido.oficios.domain.model.UpdateClientDomicilioRequest

fun ProviderProfessionalInfoRequest.toDto() = UpdateTrabajadorPerfilProfesionalRequestDto(
    descripcion = descripcion,
    experiencia = experiencia,
    documento = documento,
    metodosPago = paymentMethods,
)

fun ClientDomicilioRequest.toDto() = CreateDomicilioRequestDto(
    barrioId = barrioId,
    personaId = personaId,
    nombre = nombre,
    calle = calle,
    numero = numero,
    casaTorre = casaTorre,
    departamento = departamento,
    piso = piso,
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

fun UpdateClientDomicilioRequest.toDto() = UpdateDomicilioRequestDto(
    barrioId = barrioId,
    nombre = nombre,
    calle = calle,
    numero = numero,
    casaTorre = casaTorre,
    departamento = departamento,
    piso = piso,
    latitud = latitud,
    longitud = longitud,
    favorito = favorito,
)

fun UpdateClientContactoRequest.toDto() = UpdateContactoRequestDto(
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
            requiereTurnos = requiresTurns,
            sesion = if (requiresTurns) {
                CreateSesionItemRequestDto(
                    duracion = turnDurationMinutes ?: DEFAULT_TURN_DURATION_MINUTES,
                    medidaDuracion = DEFAULT_TURN_DURATION_UNIT,
                    intervalo = turnIntervalMinutes?.toString(),
                    medidaIntervalo = if (turnIntervalMinutes != null) DEFAULT_TURN_DURATION_UNIT else null,
                )
            } else {
                null
            },
        )
    },
    diasLaborales = weeklyAvailability?.toReplaceDays()?.map { day ->
        CreateDiaLaboralRequestDto(
            diaInicio = day.day,
            diaFin = day.day,
            horariosLaborales = day.ranges.map { range ->
                CreateHorarioLaboralRequestDto(horaInicio = range.start, horaFin = range.end)
            }.ifEmpty { listOf(CreateHorarioLaboralRequestDto("09:00", "18:00")) },
        )
    } ?: workDays.map { day ->
        CreateDiaLaboralRequestDto(
            diaInicio = day,
            diaFin = day,
            horariosLaborales = timeSlots.toHorarios(),
        )
    },
    domicilioLocal = domicilioLocal?.let {
        CreateDomicilioLocalRequestDto(
            barrioId = it.barrioId,
            calle = it.calle?.trim()?.ifBlank { null },
            numero = it.numero?.trim()?.ifBlank { null },
            nombre = it.nombre,
            latitud = it.latitud,
            longitud = it.longitud,
        )
    },
    metodosPago = paymentMethods.takeIf { it.isNotEmpty() },
)

private const val DEFAULT_TURN_DURATION_MINUTES = 60
private const val DEFAULT_TURN_DURATION_UNIT = "minutos"

private fun List<RegisterTradeTimeSlotDraft>.toHorarios(): List<CreateHorarioLaboralRequestDto> =
    filter { it.from.isNotBlank() && it.to.isNotBlank() }
        .map { CreateHorarioLaboralRequestDto(horaInicio = it.from, horaFin = it.to) }
        .ifEmpty { listOf(CreateHorarioLaboralRequestDto("09:00", "18:00")) }

fun DomicilioSinPersonaDto.toDomain() = ClientDomicilio(
    id = id,
    barrioId = barrio?.id ?: 0L,
    barrioName = barrio?.nombre,
    nombre = nombre,
    calle = calle,
    numero = numero,
    casaTorre = casaTorre,
    departamento = departamento,
    piso = piso,
    latitud = latitud,
    longitud = longitud,
    favorito = favorito,
)

fun ContactoSinPersonaDto.toDomain() = ClientContacto(
    id = id,
    nombre = nombre,
    telefonoWhatsapp = telefonoWhatsapp,
    telefonoVerificado = telefonoVerificado,
    email = email,
    favorito = favorito,
)
