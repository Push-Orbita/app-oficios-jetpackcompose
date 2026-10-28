package com.cocido.oficios.core.data.mapper

import com.cocido.oficios.core.network.api.catalog.dto.PaginatedResponseDto
import com.cocido.oficios.core.network.api.safety.dto.DenunciaDto
import com.cocido.oficios.core.network.api.safety.dto.DenunciaMotivoItemDto
import com.cocido.oficios.core.network.api.social.dto.FavoritoItemDto
import com.cocido.oficios.core.network.api.social.dto.FavoritoStatusDto
import com.cocido.oficios.domain.model.FavoritePage
import com.cocido.oficios.domain.model.FavoriteStatus
import com.cocido.oficios.domain.model.FavoriteWorker
import com.cocido.oficios.domain.model.normalizeStarRating
import com.cocido.oficios.domain.model.ReportReason
import com.cocido.oficios.domain.model.SubmittedReport

fun FavoritoItemDto.toDomain(): FavoriteWorker = FavoriteWorker(
    favoriteId = id,
    workerId = trabajadorOficioId,
    displayName = displayName.ifBlank { "Trabajador" },
    specialtyLabel = specialtyLabel.orEmpty(),
    rating = normalizeStarRating(puntajeGlobal ?: promedio ?: rating),
    avatarUrl = profilePhotoUrl,
    isVerified = verified,
    reviewCount = cantidadCalificaciones ?: reviewCount,
)

fun PaginatedResponseDto<FavoritoItemDto>.toFavoritePage(): FavoritePage {
    val pageNumber = metadata?.pageNumber ?: 1
    val totalPages = metadata?.totalPages
    return FavoritePage(
        items = data.map { it.toDomain() },
        pageNumber = pageNumber,
        hasMore = totalPages != null && pageNumber < totalPages,
    )
}

fun FavoritoStatusDto.toDomain(): FavoriteStatus = FavoriteStatus(
    isFavorite = isFavorite,
    favoriteId = id,
)

fun DenunciaMotivoItemDto.toDomain(): ReportReason = ReportReason(
    code = code,
    label = label,
)

fun DenunciaDto.toDomain(): SubmittedReport = SubmittedReport(
    id = id,
    status = status.orEmpty(),
)
