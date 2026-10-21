package com.cocido.oficios.domain.model

import kotlinx.serialization.Serializable

enum class AttentionModality {
    DOMICILIO,
    LOCAL,
}

@Serializable
data class WorkerSearchFilters(
    val rubroId: Long? = null,
    val subRubroId: Long? = null,
    val barrioId: Long? = null,
    val localidadId: Long? = null,
    val modalidadAtencion: AttentionModality? = null,
    val fechaDeseada: String? = null,
    val query: String? = null,
    val rubroLabel: String? = null,
    val subRubroLabel: String? = null,
    val barrioLabel: String? = null,
    val categoryId: Long? = null,
    val categoryLabel: String? = null,
)

/** Criterios que el backend realmente acepta en GET trabajador-oficio/search. */
fun WorkerSearchFilters.hasSearchCriteria(): Boolean =
    !query.isNullOrBlank() ||
        rubroId != null ||
        subRubroId != null ||
        barrioId != null ||
        localidadId != null ||
        modalidadAtencion != null ||
        !fechaDeseada.isNullOrBlank()

data class WorkerSummary(
    val id: Long,
    val displayName: String,
    val specialtyLabel: String,
    val rating: Float?,
    val avatarUrl: String?,
    val isVerified: Boolean,
    val primaryRubroId: Long?,
)

data class PagedWorkers(
    val items: List<WorkerSummary>,
    val pageNumber: Int,
    val totalPages: Int?,
    val hasMore: Boolean,
)
