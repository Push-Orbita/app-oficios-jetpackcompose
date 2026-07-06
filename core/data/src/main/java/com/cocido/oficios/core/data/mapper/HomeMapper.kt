package com.cocido.oficios.core.data.mapper

import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.domain.model.HomeCategoryItem

fun CatalogOption.toHomeCategoryItem(): HomeCategoryItem = HomeCategoryItem(
    id = id,
    name = label,
)

fun List<CatalogOption>.toHomeCategoryItems(): List<HomeCategoryItem> = map { it.toHomeCategoryItem() }
