package com.cocido.oficios.feature.home.common

import java.text.Normalizer

fun String.foldCatalogQuery(): String =
    Normalizer.normalize(lowercase(), Normalizer.Form.NFD)
        .replace("\\p{M}+".toRegex(), "")
        .trim()

fun <T> List<T>.filterByCatalogQuery(query: String, label: (T) -> String): List<T> {
    val needle = query.foldCatalogQuery()
    if (needle.isEmpty()) return this
    return filter { label(it).foldCatalogQuery().contains(needle) }
}
