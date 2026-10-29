package com.cocido.oficios.core.data.mapper

import com.cocido.oficios.core.common.time.DisplayDateTime
import com.cocido.oficios.core.network.api.catalog.dto.PaginatedResponseDto
import com.cocido.oficios.core.network.api.profile.dto.DiaLaboralSimpleDto
import com.cocido.oficios.core.network.api.worker.dto.CalificacionDto
import com.cocido.oficios.core.network.api.worker.dto.GaleriaTrabajoSimpleDto
import com.cocido.oficios.core.network.api.worker.dto.TrabajadorOficioPerfilCompletoDto
import com.cocido.oficios.domain.model.PagedWorkerReviews
import com.cocido.oficios.domain.model.WorkCertificateItem
import com.cocido.oficios.domain.model.WorkGalleryItem
import com.cocido.oficios.domain.model.WorkerAvailability
import com.cocido.oficios.domain.model.WorkerProfile
import com.cocido.oficios.domain.model.WorkerReputation
import com.cocido.oficios.domain.model.WorkerReview
import com.cocido.oficios.domain.model.WorkerSpecialty
import com.cocido.oficios.domain.model.WorkerTimeRange
import com.cocido.oficios.domain.model.WorkerWorkAddress
import com.cocido.oficios.domain.model.WorkerWorkDay
import com.cocido.oficios.domain.model.normalizeStarRating
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// NOTE: This mapper is intentionally tolerant: the backend may return partial sections (null reputacion, empty arrays).

fun TrabajadorOficioPerfilCompletoDto.toWorkerProfile(): WorkerProfile {
    val persona = usuario.persona
    val displayName = listOfNotNull(persona?.nombres, persona?.apellido).joinToString(" ")
        .ifBlank { "Trabajador" }

    val specialties = rubros.map { it.toSpecialty() }.filter { it.rubroName.isNotBlank() }

    val availabilitySource = rubros.firstOrNull { it.principal } ?: rubros.firstOrNull()
    val availability = availabilitySource?.diasLaborales?.toAvailability()

    val reputation = reputacion?.toReputation()

    val gallery = galeriaTrabajos.mapNotNull { it.toGalleryItem() }
    val certificates = certificados.map { cert ->
        com.cocido.oficios.domain.model.WorkCertificateItem(
            id = cert.id,
            name = cert.nombre,
            fileUrl = cert.urlArchivo,
        )
    }
    val addresses = domicilios.mapNotNull { it.toWorkAddress() }

    return WorkerProfile(
        id = id,
        displayName = displayName,
        avatarUrl = usuario.fotoPerfil.toPhotoUrl(),
        isVerified = estado.equals("VALIDADO", ignoreCase = true) || usuario.emailVerificado,
        specialties = specialties,
        availability = availability,
        reputation = reputation,
        gallery = gallery,
        certificates = certificates,
        workAddresses = addresses,
        zoneLabel = addresses.firstNotNullOfOrNull { it.zoneLabel },
        about = descripcion?.trim()?.takeIf { it.isNotEmpty() },
    )
}

private fun List<DiaLaboralSimpleDto>.toAvailability(): WorkerAvailability? {
    if (isEmpty()) return null

    val workDays = mapNotNull { it.toWorkDay() }
    if (workDays.isEmpty()) return null

    val first = workDays.first()
    val last = workDays.last()
    val startTime = first.startTime.normalizeTime()
    val endTime = first.endTime.normalizeTime()

    val dayStart = first.dayStart.shortDayLabel()
    val dayEnd = last.dayEnd.shortDayLabel()

    val summary = if (dayStart == dayEnd) {
        "$dayStart · $startTime - $endTime"
    } else {
        "$dayStart - $dayEnd · $startTime - $endTime"
    }

    return WorkerAvailability(
        summaryLabel = summary,
        workDays = workDays,
    )
}

private fun DiaLaboralSimpleDto.toWorkDay(): WorkerWorkDay? {
    val hours = horariosLaborales.map { horario ->
        WorkerTimeRange(
            startTime = horario.horaInicio.normalizeTime(),
            endTime = horario.horaFin.normalizeTime(),
        )
    }
    if (hours.isEmpty()) return null
    return WorkerWorkDay(
        dayStart = diaInicio,
        dayEnd = diaFin,
        hours = hours,
    )
}

private fun String.normalizeTime(): String =
    takeIf { it.length >= 5 }?.take(5) ?: this

private fun String.shortDayLabel(): String = when (uppercase()) {
    "LUNES" -> "Lun"
    "MARTES" -> "Mar"
    "MIERCOLES" -> "Mie"
    "JUEVES" -> "Jue"
    "VIERNES" -> "Vie"
    "SABADO" -> "Sab"
    "DOMINGO" -> "Dom"
    else -> this
}

private fun com.cocido.oficios.core.network.api.profile.dto.TrabajadorOficioRubroFullDto.toSpecialty(): WorkerSpecialty {
    val rubroName = rubro?.nombre.orEmpty()
    val subRubroNames = subRubros.mapNotNull { it.subRubro?.nombre }
    val servesAtHome = subRubros.any { it.lugarDomicilio }
    val servesAtLocal = subRubros.any { it.lugarLocal }
    val subRubroOptions = subRubros.mapNotNull { subRubro ->
        val name = subRubro.subRubro?.nombre ?: return@mapNotNull null
        com.cocido.oficios.domain.model.WorkerSubRubroOption(id = subRubro.id, name = name)
    }
    return WorkerSpecialty(
        rubroName = rubroName,
        subRubroNames = subRubroNames,
        servesAtHome = servesAtHome,
        servesAtLocal = servesAtLocal,
        isPrimary = principal,
        subRubros = subRubroOptions,
    )
}

private fun com.cocido.oficios.core.network.api.search.dto.ReputacionResumenDto.toReputation(): WorkerReputation {
    return WorkerReputation(
        rating = normalizeStarRating(puntajeGlobal ?: promedio ?: rating),
        reviewCount = comentariosTotales ?: cantidadCalificaciones ?: reviewCount,
    )
}

private fun GaleriaTrabajoSimpleDto.toGalleryItem(): WorkGalleryItem? {
    val imageId = id ?: return null
    return WorkGalleryItem(
        id = imageId,
        imageUrl = urlArchivo,
    )
}

private fun com.cocido.oficios.core.network.api.worker.dto.DomicilioEnrichedDto.toWorkAddress(): WorkerWorkAddress? {
    val addressId = id ?: return null
    val zone = formatWorkerZone(barrio?.nombre, barrio?.localidad)
    return WorkerWorkAddress(
        id = addressId,
        barrioId = barrioId,
        label = zone,
        calle = null,
        zoneLabel = zone,
    )
}

fun formatWorkerZone(barrio: String?, localidad: String?): String? {
    val parts = listOfNotNull(barrio?.trim()?.takeIf { it.isNotEmpty() }, localidad?.trim()?.takeIf { it.isNotEmpty() })
    return parts.joinToString(", ").ifBlank { null }
}

fun PaginatedResponseDto<CalificacionDto>.toPagedWorkerReviews(
    pageSize: Int,
): PagedWorkerReviews {
    val pageNumber = metadata?.pageNumber ?: 1
    val totalPages = metadata?.totalPages
    val items = data.mapNotNull { it.toWorkerReview() }
    val hasMore = when {
        totalPages != null -> pageNumber < totalPages
        else -> items.size >= pageSize
    }

    return PagedWorkerReviews(
        items = items,
        pageNumber = pageNumber,
        totalPages = totalPages,
        hasMore = hasMore,
    )
}

private fun CalificacionDto.toWorkerReview(): WorkerReview? {
    val reviewId = id ?: return null
    return WorkerReview(
        id = reviewId,
        qualityScore = calidad,
        punctualityScore = puntualidad,
        comment = comentario,
        clientLabel = reviewAuthorLabel(booking?.contactoTexto),
        reviewedAt = formatReviewDate(booking?.finConfirmado ?: booking?.inicioConfirmado),
        bookingId = booking?.id,
    )
}

/** El contacto del booking viene como "Nombre Apellido | Tel: … | Email: …". */
fun reviewAuthorLabel(contactoTexto: String?): String? {
    val head = contactoTexto
        ?.substringBefore("|")
        ?.substringBefore("Tel:")
        ?.substringBefore("Email:")
        ?.trim()
        .orEmpty()
    val parts = head.split(Regex("\\s+")).filter { part ->
        part.isNotBlank() && !part.contains("@") && part.any { it.isLetter() }
    }
    if (parts.isEmpty()) return null
    if (parts.size == 1) return parts.first()
    return "${parts.dropLast(1).joinToString(" ")} ${parts.last().first().uppercaseChar()}."
}

fun formatReviewDate(iso: String?, zone: ZoneId = DisplayDateTime.ARGENTINA): String? {
    val zoned = DisplayDateTime.toZonedDateTime(iso, zone) ?: return null
    return DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm", Locale.forLanguageTag("es-AR"))
        .format(zoned)
}

