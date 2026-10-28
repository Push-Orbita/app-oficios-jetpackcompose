package com.cocido.oficios.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WorkerRatingTest {

    @Test
    fun `puntaje 0 a 5 se muestra tal cual`() {
        assertEquals(4.8f, normalizeStarRating(4.8)!!, 0.01f)
        assertEquals(5f, normalizeStarRating(5.0)!!, 0.01f)
        assertNull(normalizeStarRating(0.0))
        assertNull(normalizeStarRating(null))
    }

    @Test
    fun `puntaje mayor a 5 se convierte de escala 10`() {
        assertEquals(4.2f, normalizeStarRating(8.4)!!, 0.01f)
        assertEquals(5f, normalizeStarRating(10.0)!!, 0.01f)
    }

    @Test
    fun `promedio de reseñas 3 5 5 3 5 es 4,2`() {
        val reviews = listOf(3, 5, 5, 3, 5).mapIndexed { index, quality ->
            WorkerReview(
                id = index.toLong(),
                qualityScore = quality,
                punctualityScore = 5,
                comment = null,
                clientLabel = null,
            )
        }
        assertEquals(4.2f, reviews.averageStarRating()!!, 0.01f)
        val reputation = null.orComputedFrom(reviews)
        assertEquals(4.2f, reputation?.rating!!, 0.01f)
        assertEquals(5, reputation.reviewCount)
        assertEquals("4,2 (5)", formatStarRatingLabel(4.2f, 5))
    }

    @Test
    fun `calidad 1-10 se convierte a estrellas antes de promediar`() {
        val reviews = listOf(6, 10, 10, 6, 10).mapIndexed { index, quality ->
            WorkerReview(
                id = index.toLong(),
                qualityScore = quality,
                punctualityScore = 5,
                comment = null,
                clientLabel = null,
            )
        }
        assertEquals(4.2f, reviews.averageStarRating()!!, 0.01f)
    }

    @Test
    fun `respeta promedio del backend si ya viene`() {
        val reviews = listOf(
            WorkerReview(1, 3, 5, null, null),
        )
        val resolved = WorkerReputation(rating = 4.8f, reviewCount = 24).orComputedFrom(reviews)
        assertEquals(4.8f, resolved?.rating!!, 0.01f)
        assertEquals(24, resolved.reviewCount)
    }
}
