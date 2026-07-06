package com.cocido.oficios.feature.home.common

import androidx.lifecycle.SavedStateHandle
import com.cocido.oficios.domain.model.WorkerSearchFilters

fun SavedStateHandle.toWorkerSearchFilters(): WorkerSearchFilters =
    WorkerSearchFilters(
        rubroId = get<Long>("rubroId")?.takeIf { it > 0 },
        subRubroId = get<Long>("subRubroId")?.takeIf { it > 0 },
        barrioId = get<Long>("barrioId")?.takeIf { it > 0 },
        query = get<String>("query")?.takeIf { it.isNotBlank() },
        rubroLabel = get<String>("rubroLabel")?.takeIf { it.isNotBlank() },
        subRubroLabel = get<String>("subRubroLabel")?.takeIf { it.isNotBlank() },
        barrioLabel = get<String>("barrioLabel")?.takeIf { it.isNotBlank() },
        categoryId = get<Long>("categoryId")?.takeIf { it > 0 },
        categoryLabel = get<String>("categoryLabel")?.takeIf { it.isNotBlank() },
    )

fun WorkerSearchFilters.persistTo(savedStateHandle: SavedStateHandle) {
    savedStateHandle["rubroId"] = rubroId ?: -1L
    savedStateHandle["subRubroId"] = subRubroId ?: -1L
    savedStateHandle["barrioId"] = barrioId ?: -1L
    savedStateHandle["query"] = query.orEmpty()
    savedStateHandle["rubroLabel"] = rubroLabel.orEmpty()
    savedStateHandle["subRubroLabel"] = subRubroLabel.orEmpty()
    savedStateHandle["barrioLabel"] = barrioLabel.orEmpty()
    savedStateHandle["categoryId"] = categoryId ?: -1L
    savedStateHandle["categoryLabel"] = categoryLabel.orEmpty()
    savedStateHandle["modalidadAtencion"] = modalidadAtencion?.name.orEmpty()
    savedStateHandle["fechaDeseada"] = fechaDeseada.orEmpty()
    savedStateHandle["localidadId"] = localidadId ?: -1L
}

fun SavedStateHandle.toWorkerSearchFiltersWithExtras(): WorkerSearchFilters {
    val base = toWorkerSearchFilters()
    val modality = get<String>("modalidadAtencion")?.takeIf { it.isNotBlank() }
    return base.copy(
        modalidadAtencion = modality?.let {
            runCatching { com.cocido.oficios.domain.model.AttentionModality.valueOf(it) }.getOrNull()
        },
        fechaDeseada = get<String>("fechaDeseada")?.takeIf { it.isNotBlank() },
        localidadId = get<Long>("localidadId")?.takeIf { it > 0 },
    )
}
