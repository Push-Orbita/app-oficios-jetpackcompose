package com.cocido.oficios.domain.model

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Staging rechaza lat/lng con demasiados decimales (VA01).
 * 6 lugares alcanzan ~0,1 m y entran en el constraint del backend.
 */
object GeoCoordinates {
    const val API_DECIMAL_PLACES = 6

    fun roundForApi(value: Double): Double =
        BigDecimal(value.toString())
            .setScale(API_DECIMAL_PLACES, RoundingMode.HALF_UP)
            .toDouble()

    fun roundForApi(value: Double?): Double? = value?.let(::roundForApi)
}
