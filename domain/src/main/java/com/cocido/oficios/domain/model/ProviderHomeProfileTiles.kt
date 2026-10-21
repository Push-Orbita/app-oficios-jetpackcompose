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

enum class ProviderProfileCompletionTarget {
    GALLERY,
    COMMERCIAL_LOCAL,
    CERTIFICATE,
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

    fun firstIncompleteTarget(): ProviderProfileCompletionTarget? = when {
        !gallery -> ProviderProfileCompletionTarget.GALLERY
        !local -> ProviderProfileCompletionTarget.COMMERCIAL_LOCAL
        !certificate -> ProviderProfileCompletionTarget.CERTIFICATE
        else -> null
    }
}

fun resolveProviderHomeTiles(
    details: ProviderAccountDetails?,
    missingSteps: List<String>,
    isProfileComplete: Boolean,
): ProviderHomeTiles {
    if (details == null) {
        return inferProviderHomeTiles(
            missingSteps = missingSteps,
            isProfileComplete = isProfileComplete,
        )
    }
    return ProviderHomeTiles(
        gallery = details.gallery.isNotEmpty(),
        local = details.hasCommercialLocal,
        certificate = details.certificates.isNotEmpty(),
    )
}

private fun String.containsAny(vararg needles: String): Boolean = needles.any { contains(it) }
