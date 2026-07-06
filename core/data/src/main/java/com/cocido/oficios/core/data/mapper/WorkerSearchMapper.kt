package com.cocido.oficios.core.data.mapper

import com.cocido.oficios.core.network.api.catalog.dto.PaginatedResponseDto
import com.cocido.oficios.core.network.api.search.dto.WorkerSearchItemDto
import com.cocido.oficios.domain.model.PagedWorkers
import com.cocido.oficios.domain.model.WorkerSearchFilters
import com.cocido.oficios.domain.model.WorkerSummary

fun PaginatedResponseDto<WorkerSearchItemDto>.toPagedWorkers(
    filters: WorkerSearchFilters,
): PagedWorkers {
    val pageNumber = metadata?.pageNumber ?: 1
    val totalPages = metadata?.totalPages
    val items = data.map { it.toWorkerSummary(filters.rubroId, filters.subRubroId) }
    val hasMore = when {
        totalPages != null -> pageNumber < totalPages
        else -> items.size >= (metadata?.pageSize ?: 20)
    }
    return PagedWorkers(
        items = items,
        pageNumber = pageNumber,
        totalPages = totalPages,
        hasMore = hasMore,
    )
}

fun WorkerSearchItemDto.toWorkerSummary(
    activeRubroId: Long?,
    activeSubRubroId: Long?,
): WorkerSummary {
    val persona = usuario?.persona
    val displayName = listOfNotNull(persona?.nombres, persona?.apellido)
        .joinToString(" ")
        .ifBlank { "Trabajador" }

    val primaryRubro = rubros.firstOrNull { it.principal } ?: rubros.firstOrNull()
    val rubroName = when {
        activeSubRubroId != null -> {
            rubros.flatMap { it.subRubros }
                .firstOrNull { it.subRubro?.id == activeSubRubroId }
                ?.subRubro
                ?.nombre
        }
        activeRubroId != null -> {
            rubros.firstOrNull { it.rubro?.id == activeRubroId }?.rubro?.nombre
        }
        else -> null
    } ?: primaryRubro?.rubro?.nombre.orEmpty()

    val experienceSuffix = experiencia?.takeIf { it.isNotBlank() }?.let { " · $it" }.orEmpty()
    val specialtyLabel = (rubroName + experienceSuffix).trim().ifBlank {
        descripcion.orEmpty()
    }

    return WorkerSummary(
        id = id,
        displayName = displayName,
        specialtyLabel = specialtyLabel,
        rating = reputacion?.promedio?.toFloat(),
        avatarUrl = usuario?.fotoPerfil.toPhotoUrl(),
        isVerified = estado.equals("VALIDADO", ignoreCase = true) || usuario?.emailVerificado == true,
        primaryRubroId = primaryRubro?.rubro?.id,
    )
}
