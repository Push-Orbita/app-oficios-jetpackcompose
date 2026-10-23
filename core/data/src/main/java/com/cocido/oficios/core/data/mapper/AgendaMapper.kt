package com.cocido.oficios.core.data.mapper

import com.cocido.oficios.core.common.time.DisplayDateTime
import com.cocido.oficios.core.network.api.agenda.dto.SesionDto
import com.cocido.oficios.core.network.api.agenda.dto.SlotTurnoAgendaDto
import com.cocido.oficios.core.network.api.auth.dto.TrabajadorOficioPerfilMeDto
import com.cocido.oficios.domain.model.AgendaSessionConfig
import com.cocido.oficios.domain.model.AgendaSlot
import com.cocido.oficios.domain.model.AgendaSlotStatus
import com.cocido.oficios.domain.model.ProviderSubRubroOption

fun String.toAgendaSlotStatus(): AgendaSlotStatus = when (uppercase()) {
    "DISPONIBLE" -> AgendaSlotStatus.DISPONIBLE
    "RESERVADO" -> AgendaSlotStatus.RESERVADO
    "BLOQUEADO" -> AgendaSlotStatus.BLOQUEADO
    else -> AgendaSlotStatus.DISPONIBLE
}

fun SlotTurnoAgendaDto.toDomain(): AgendaSlot = AgendaSlot(
    id = id,
    subRubroId = trabajadorOficioSubRubro?.id ?: 0L,
    subRubroLabel = trabajadorOficioSubRubro?.subRubro?.nombre.orEmpty(),
    start = requireNotNull(DisplayDateTime.toZonedDateTime(inicio)) { "inicio inválido" }.toLocalDateTime(),
    end = requireNotNull(DisplayDateTime.toZonedDateTime(fin)) { "fin inválido" }.toLocalDateTime(),
    status = estado.toAgendaSlotStatus(),
    autoAcceptance = autoAceptacion,
    bookingId = bookingId,
)

fun SesionDto.toDomain(): AgendaSessionConfig = AgendaSessionConfig(
    id = id,
    subRubroId = trabajadorOficioSubRubroId,
    durationMinutes = duracion,
    intervalMinutes = intervalo?.toIntOrNull(),
    durationUnit = medidaDuracion.ifBlank { "minutos" },
    intervalUnit = medidaIntervalo,
)

fun TrabajadorOficioPerfilMeDto.toSubRubroOptions(): List<ProviderSubRubroOption> =
    rubros.flatMap { rubro ->
        rubro.subRubros.map { subRubro ->
            ProviderSubRubroOption(
                id = subRubro.id,
                subRubroLabel = subRubro.subRubro?.nombre.orEmpty(),
                rubroLabel = rubro.rubro?.nombre.orEmpty(),
                requiresTurns = subRubro.requiereTurnos,
            )
        }
    }
