package com.cocido.oficios.core.data.mapper

import com.cocido.oficios.core.network.api.catalog.dto.BarrioDto
import com.cocido.oficios.core.network.api.catalog.dto.CategoriaDto
import com.cocido.oficios.core.network.api.catalog.dto.RubroDto
import com.cocido.oficios.core.network.api.catalog.dto.SubRubroDto
import com.cocido.oficios.domain.model.CatalogOption

fun CategoriaDto.toCatalogOption(): CatalogOption = CatalogOption(
    id = id,
    label = nombre,
    iconUrl = iconUrl?.takeIf { it.isNotBlank() },
)

fun RubroDto.toCatalogOption(): CatalogOption = CatalogOption(
    id = id,
    label = nombre,
)

fun SubRubroDto.toCatalogOption(): CatalogOption = CatalogOption(
    id = id,
    label = nombre,
)

fun BarrioDto.toCatalogOption(): CatalogOption = CatalogOption(
    id = id,
    label = nombre,
)
