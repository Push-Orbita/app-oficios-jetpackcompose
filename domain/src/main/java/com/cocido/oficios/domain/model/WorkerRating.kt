package com.cocido.oficios.domain.model

import java.util.Locale

/**
 * El backend mezcla escalas: promedio 0–5, puntajeGlobal 0–10 (estrellas × 2).
 * Calidad hoy es 1–5; reseñas viejas pueden traer 1–10. La UI siempre muestra 0–5.
 */
fun normalizeStarRating(raw: Double?): Float? {
    if (raw == null || raw <= 0.0) return null
    val stars = if (raw > 5.0) raw / 2.0 else raw
    return stars.toFloat().coerceIn(0f, 5f)
}

fun qualityScoreToStars(score: Int?): Float? {
    val value = score?.takeIf { it > 0 } ?: return null
    val stars = if (value > 5) value / 2.0 else value.toDouble()
    return stars.toFloat().coerceIn(0.5f, 5f)
}

fun List<WorkerReview>.averageStarRating(): Float? {
    val stars = mapNotNull { review -> qualityScoreToStars(review.qualityScore) }
    if (stars.isEmpty()) return null
    return stars.average().toFloat()
}

fun WorkerReputation?.orComputedFrom(reviews: List<WorkerReview>): WorkerReputation? {
    val computedRating = reviews.averageStarRating()
    val computedCount = reviews.size.takeIf { it > 0 }
    val rating = this?.rating?.takeIf { it > 0f } ?: computedRating
    val count = this?.reviewCount?.takeIf { it > 0 } ?: computedCount
    if (rating == null && count == null) return this
    return WorkerReputation(rating = rating, reviewCount = count)
}

fun formatStarRatingLabel(rating: Float, reviewCount: Int? = null): String {
    val number = if (rating % 1f == 0f) {
        rating.toInt().toString()
    } else {
        String.format(Locale.forLanguageTag("es-AR"), "%.1f", rating)
    }
    val count = reviewCount?.takeIf { it > 0 } ?: return number
    return "$number ($count)"
}
