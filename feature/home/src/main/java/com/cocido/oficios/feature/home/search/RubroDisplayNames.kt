package com.cocido.oficios.feature.home.search

/**
 * Plural y singular de rubros para copy de resultados (Figma 40:334 / 40:336).
 * La API no envía plural; si el nombre no está en el mapa se aplica una regla simple.
 */
object RubroDisplayNames {

    fun pluralTitle(rubroLabel: String?): String {
        val plural = pluralize(rubroLabel?.trim().orEmpty().ifBlank { "Trabajadores" })
        return "$plural de Oficios Formosa"
    }

    fun searchHint(rubroLabel: String?): String {
        val singular = singularize(rubroLabel?.trim().orEmpty().ifBlank { "oficio" }).lowercase()
        return "¿Busca algún $singular en específico?"
    }

    fun pluralize(label: String): String {
        val key = label.lowercase()
        mapped[key]?.let { return it.first.replaceFirstChar { c -> c.titlecase() } }
        mapped.entries.firstOrNull { key.contains(it.key) }?.let {
            return it.value.first.replaceFirstChar { c -> c.titlecase() }
        }
        val capitalized = label.replaceFirstChar { it.titlecase() }
        return when {
            capitalized.endsWith("s", ignoreCase = true) -> capitalized
            capitalized.endsWith("z", ignoreCase = true) -> capitalized + "es"
            capitalized.endsWith("ón", ignoreCase = true) -> capitalized.dropLast(2) + "ones"
            capitalized.endsWith("l", ignoreCase = true) -> capitalized + "es"
            else -> capitalized + "s"
        }
    }

    fun singularize(label: String): String {
        val key = label.lowercase()
        mapped[key]?.let { return it.second }
        mapped.entries.firstOrNull { key.contains(it.key) }?.let { return it.value.second }
        return label.ifBlank { "oficio" }
    }

    private val mapped = mapOf(
        "albañil" to ("Albañiles" to "albañil"),
        "albañiles" to ("Albañiles" to "albañil"),
        "plomero" to ("Plomeros" to "plomero"),
        "cerrajero" to ("Cerrajeros" to "cerrajero"),
        "instalador" to ("Instaladores" to "instalador"),
        "pintor" to ("Pintores" to "pintor"),
        "carpintero" to ("Carpinteros" to "carpintero"),
        "gasista" to ("Gasistas" to "gasista"),
        "electricista" to ("Electricistas" to "electricista"),
        "ingeniero" to ("Ingenieros" to "ingeniero"),
    )
}
