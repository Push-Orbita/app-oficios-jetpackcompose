package com.cocido.oficios.core.common.time

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Un ISO con `Z` u offset es un instante y se muestra en [ARGENTINA].
 * Sin offset es hora local de pared y se deja en ese reloj.
 */
object DisplayDateTime {
    val ARGENTINA: ZoneId = ZoneId.of("America/Argentina/Buenos_Aires")

    fun toZonedDateTime(raw: String?, zone: ZoneId = ARGENTINA): ZonedDateTime? {
        val text = raw?.trim().orEmpty()
        if (text.isEmpty()) return null
        return runCatching {
            if (hasZoneDesignator(text)) {
                OffsetDateTime.parse(text).atZoneSameInstant(zone)
            } else {
                LocalDateTime.parse(text).atZone(zone)
            }
        }.getOrNull()
    }

    internal fun hasZoneDesignator(text: String): Boolean {
        val trimmed = text.trim()
        if (trimmed.endsWith("Z", ignoreCase = true)) return true
        return OFFSET_AT_END.containsMatchIn(trimmed)
    }

    private val OFFSET_AT_END = Regex("""[+-]\d{2}:?\d{2}$""")
}
