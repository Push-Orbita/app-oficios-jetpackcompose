package com.cocido.oficios.domain.model

/**
 * Ranking bayesiano para no poner primero a quien tiene una sola reseña de 5.
 *
 * `score = (C × m + n × R) / (C + n)` con R en estrellas 0–5.
 * Sin calificación queda último. El orden es estable por `id`.
 */
object WorkerRanking {
    const val PRIOR_MEAN = 3.5
    const val PRIOR_WEIGHT = 8.0

    const val ORDER_BY_PUNTAJE_GLOBAL = "puntajeGlobal"
    const val ORDER_DIRECTION_DESC = "DESC"

    fun score(stars: Float?, reviewCount: Int?): Double {
        val rating = stars?.toDouble() ?: return 0.0
        if (rating <= 0.0) return 0.0
        val n = (reviewCount ?: 0).coerceAtLeast(0).toDouble()
        return (PRIOR_WEIGHT * PRIOR_MEAN + n * rating) / (PRIOR_WEIGHT + n)
    }

    fun compare(left: WorkerSummary, right: WorkerSummary): Int {
        val byScore = score(right.rating, right.reviewCount).compareTo(score(left.rating, left.reviewCount))
        if (byScore != 0) return byScore
        val byCount = (right.reviewCount ?: 0).compareTo(left.reviewCount ?: 0)
        if (byCount != 0) return byCount
        return left.id.compareTo(right.id)
    }

    fun rank(workers: List<WorkerSummary>): List<WorkerSummary> = workers.sortedWith(::compare)
}
