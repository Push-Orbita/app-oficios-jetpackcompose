package com.cocido.oficios.domain.model

fun inferProviderHomeTiles(
    missingSteps: List<String>,
    isProfileComplete: Boolean,
): ProviderHomeTiles {
    if (isProfileComplete) {
        return ProviderHomeTiles(gallery = true, local = true, certificate = true)
    }
    val joined = missingSteps.joinToString(" ").lowercase()
    return ProviderHomeTiles(
        gallery = !joined.containsAny("galer", "foto", "imagen"),
        local = !joined.containsAny("local", "comercio", "domicilio"),
        certificate = !joined.containsAny("certific", "documento"),
    )
}

data class ProviderHomeTiles(
    val gallery: Boolean,
    val local: Boolean,
    val certificate: Boolean,
) {
    val completionPercent: Int
        get() {
            val done = listOf(gallery, local, certificate).count { it }
            return (done * 100) / 3
        }
}

private fun String.containsAny(vararg needles: String): Boolean = needles.any { contains(it) }
