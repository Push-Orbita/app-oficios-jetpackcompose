package com.cocido.oficios.core.data.mapper

import com.cocido.oficios.core.network.api.account.dto.AccountDeletionRequestDto
import com.cocido.oficios.core.network.api.account.dto.WorkGalleryDto
import com.cocido.oficios.domain.model.AccountDeletionRequest
import com.cocido.oficios.domain.model.AccountDeletionStatus
import com.cocido.oficios.core.network.api.auth.dto.TrabajadorOficioPerfilMeDto
import com.cocido.oficios.core.network.api.profile.dto.CertificadoTrabajoDto
import com.cocido.oficios.core.network.api.profile.dto.ProviderDomicilioDto
import com.cocido.oficios.core.network.api.profile.dto.TrabajadorOficioRubroFullDto
import com.cocido.oficios.domain.model.ManagedProviderService
import com.cocido.oficios.domain.model.ManagedProviderSubService
import com.cocido.oficios.domain.model.ManagedTimeRange
import com.cocido.oficios.domain.model.ManagedWorkSchedule
import com.cocido.oficios.domain.model.ProviderAccountDetails
import com.cocido.oficios.domain.model.ProviderCommercialLocal
import com.cocido.oficios.domain.model.WorkCertificateItem
import com.cocido.oficios.domain.model.WorkGalleryItem

fun TrabajadorOficioPerfilMeDto.toAccountDetails() = ProviderAccountDetails(
    workerId = id,
    status = estado,
    document = documento,
    description = descripcion,
    experience = experiencia,
    paymentMethods = metodosPago,
    services = rubros.map { it.toManagedService() },
    gallery = galeriaTrabajos.map { it.toDomainItem() },
    certificates = certificados.map { it.toDomainItem() },
    commercialLocal = domicilios.toCommercialLocal(),
)

private fun List<ProviderDomicilioDto>.toCommercialLocal(): ProviderCommercialLocal? {
    val dto = firstOrNull { !it.calle.isNullOrBlank() } ?: firstOrNull() ?: return null
    return ProviderCommercialLocal(
        id = dto.id,
        barrioId = dto.barrioId ?: dto.barrio?.id,
        barrioName = dto.barrio?.nombre,
        street = dto.calle,
        number = dto.numero,
        name = dto.nombre,
    )
}

private fun TrabajadorOficioRubroFullDto.toManagedService() = ManagedProviderService(
    id = id,
    rubroId = rubro?.id ?: 0L,
    rubroName = rubro?.nombre.orEmpty().ifBlank { "Servicio sin nombre" },
    isPrimary = principal,
    subServices = subRubros.map { item ->
        ManagedProviderSubService(
            id = item.id,
            catalogSubRubroId = item.subRubro?.id ?: 0L,
            name = item.subRubro?.nombre.orEmpty().ifBlank { "Especialidad sin nombre" },
            servesAtClientHome = item.lugarDomicilio,
            servesAtLocal = item.lugarLocal,
            requiresTurns = item.requiereTurnos,
        )
    },
    workSchedule = diasLaborales.map { day ->
        ManagedWorkSchedule(
            dayFrom = day.diaInicio,
            dayTo = day.diaFin,
            ranges = day.horariosLaborales.map { range ->
                ManagedTimeRange(from = scheduleClock(range.horaInicio), to = scheduleClock(range.horaFin))
            },
        )
    },
)

fun WorkGalleryDto.toDomainItem() = WorkGalleryItem(id = id, imageUrl = urlArchivo)

fun CertificadoTrabajoDto.toDomainItem() = WorkCertificateItem(
    id = id,
    name = nombre,
    fileUrl = urlArchivo,
)

fun AccountDeletionRequestDto.toDomain(): AccountDeletionRequest = AccountDeletionRequest(
    requestId = requestId,
    status = when (status.uppercase()) {
        "PENDING_GRACE_PERIOD" -> AccountDeletionStatus.PENDING_GRACE_PERIOD
        "CANCELLED" -> AccountDeletionStatus.CANCELLED
        "COMPLETED" -> AccountDeletionStatus.COMPLETED
        else -> AccountDeletionStatus.UNKNOWN
    },
    requestedAtIso = requestedAt,
    scheduledForIso = scheduledFor,
    canCancelUntilIso = canCancelUntil,
)

fun scheduleClock(raw: String): String {
    val match = Regex("""(\d{1,2}):(\d{2})""").find(raw.trim()) ?: return raw.trim()
    return "${match.groupValues[1].padStart(2, '0')}:${match.groupValues[2]}"
}
