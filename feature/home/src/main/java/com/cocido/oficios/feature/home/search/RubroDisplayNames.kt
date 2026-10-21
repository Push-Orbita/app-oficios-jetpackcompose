package com.cocido.oficios.feature.home.search

import java.text.Normalizer

/**
 * Plural y singular de rubros para copy de resultados (Figma 40:334 / 40:336).
 * Prod envía nombres de oficio (plomería, electricidad), no el gentilicio.
 * Sin match: título y hint neutros — no pluralizar a ciegas ("Plomerías").
 */
object RubroDisplayNames {

    fun pluralTitle(rubroLabel: String?): String {
        val mapped = resolve(rubroLabel)
        return "${mapped?.first ?: FALLBACK_PLURAL} de Oficios Formosa"
    }

    fun searchHint(rubroLabel: String?): String {
        val mapped = resolve(rubroLabel)
        return if (mapped != null) {
            "¿Busca algún ${mapped.second}?"
        } else {
            FALLBACK_HINT
        }
    }

    fun pluralize(label: String): String = resolve(label)?.first ?: FALLBACK_PLURAL

    fun singularize(label: String): String = resolve(label)?.second ?: FALLBACK_SINGULAR

    internal fun resolve(rubroLabel: String?): Pair<String, String>? {
        val key = normalize(rubroLabel)
        if (key.isEmpty()) return null
        mapped[key]?.let { return it }
        return mapped.entries.firstOrNull { key.contains(it.key) || it.key.contains(key) }?.value
    }

    internal fun normalize(value: String?): String {
        val trimmed = value?.trim().orEmpty()
        if (trimmed.isEmpty()) return ""
        val decomposed = Normalizer.normalize(trimmed.lowercase(), Normalizer.Form.NFD)
        return ACCENT_MARKS.replace(decomposed, "")
    }

    private val ACCENT_MARKS = "\\p{M}+".toRegex()

    private const val FALLBACK_PLURAL = "Trabajadores"
    private const val FALLBACK_SINGULAR = "alguien"
    private const val FALLBACK_HINT = "¿Busca a alguien en específico?"

    private val mapped = mapOf(
        "plomeria" to ("Plomeros" to "plomero"),
        "plomero" to ("Plomeros" to "plomero"),
        "plomeros" to ("Plomeros" to "plomero"),
        "electricidad" to ("Electricistas" to "electricista"),
        "electricista" to ("Electricistas" to "electricista"),
        "electricistas" to ("Electricistas" to "electricista"),
        "pintura" to ("Pintores" to "pintor"),
        "pintor" to ("Pintores" to "pintor"),
        "pintores" to ("Pintores" to "pintor"),
        "albanileria" to ("Albañiles" to "albañil"),
        "albanil" to ("Albañiles" to "albañil"),
        "albaniles" to ("Albañiles" to "albañil"),
        "carpinteria" to ("Carpinteros" to "carpintero"),
        "carpintero" to ("Carpinteros" to "carpintero"),
        "carpinteros" to ("Carpinteros" to "carpintero"),
        "cerrajeria" to ("Cerrajeros" to "cerrajero"),
        "cerrajero" to ("Cerrajeros" to "cerrajero"),
        "cerrajeros" to ("Cerrajeros" to "cerrajero"),
        "gas" to ("Gasistas" to "gasista"),
        "gasista" to ("Gasistas" to "gasista"),
        "gasistas" to ("Gasistas" to "gasista"),
    )
}
