package com.cocido.oficios.domain.model

import java.text.Normalizer

const val MIN_TRADE_QUERY_LENGTH = 3

fun normalizeSearchText(value: String): String {
    val trimmed = value.trim().lowercase()
    val decomposed = Normalizer.normalize(trimmed, Normalizer.Form.NFD)
    return decomposed.replace("\\p{Mn}+".toRegex(), "")
}

/**
 * Elige el rubro/subrubro más cercano al texto: exacto, prefijo, contiene.
 * Empate: el nombre más corto. Sin match o query corta: null.
 */
fun matchCatalogByQuery(query: String, options: List<CatalogOption>): CatalogOption? {
    val needle = normalizeSearchText(query)
    if (needle.length < MIN_TRADE_QUERY_LENGTH) return null
    return catalogSuggestions(query, options, limit = 1).firstOrNull()
}

/** Sugerencias de rubro/subrubro mientras se escribe (acentos e mayúsculas no cuentan). */
fun catalogSuggestions(
    query: String,
    options: List<CatalogOption>,
    limit: Int = 6,
): List<CatalogOption> {
    val needle = normalizeSearchText(query)
    if (needle.isEmpty()) return emptyList()
    return options
        .mapNotNull { option ->
            val hay = normalizeSearchText(option.label)
            if (hay.isEmpty()) return@mapNotNull null
            val rank = when {
                hay == needle -> 0
                hay.startsWith(needle) -> 1
                hay.contains(needle) -> 2
                else -> return@mapNotNull null
            }
            Triple(rank, hay.length, option)
        }
        .sortedWith(compareBy({ it.first }, { it.second }, { it.third.id }))
        .map { it.third }
        .distinctBy { it.id }
        .take(limit)
}

fun WorkerSearchFilters.withResolvedTrade(
    rubros: List<CatalogOption>,
    subRubros: List<CatalogOption>,
): WorkerSearchFilters {
    val query = this.query?.trim().orEmpty()
    if (query.length < MIN_TRADE_QUERY_LENGTH) return this
    if (rubroId != null || subRubroId != null) return this
    matchCatalogByQuery(query, subRubros)?.let { match ->
        return copy(subRubroId = match.id, subRubroLabel = match.label, query = null)
    }
    matchCatalogByQuery(query, rubros)?.let { match ->
        return copy(rubroId = match.id, rubroLabel = match.label, query = null)
    }
    return this
}
