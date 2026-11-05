package com.cocido.oficios.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkerRankingTest {

    @Test
    fun unratedWorkersScoreZero() {
        assertEquals(0.0, WorkerRanking.score(null, 10), 0.0)
        assertEquals(0.0, WorkerRanking.score(0f, 4), 0.0)
    }

    @Test
    fun manySolidReviewsBeatASingleFiveStar() {
        val oneFive = WorkerRanking.score(5f, 1)
        val manyFours = WorkerRanking.score(4.5f, 40)
        assertTrue(manyFours > oneFive)
    }

    @Test
    fun rankPutsHigherBayesianScoreFirstAndKeepsUnratedLast() {
        val unrated = worker(1, rating = null, reviews = null)
        val oneFive = worker(2, rating = 5f, reviews = 1)
        val manyGood = worker(3, rating = 4.5f, reviews = 40)
        val ranked = WorkerRanking.rank(listOf(unrated, oneFive, manyGood))
        assertEquals(listOf(3L, 2L, 1L), ranked.map { it.id })
    }

    @Test
    fun equalScoresKeepStableOrderById() {
        val a = worker(10, rating = 4f, reviews = 8)
        val b = worker(4, rating = 4f, reviews = 8)
        assertEquals(listOf(4L, 10L), WorkerRanking.rank(listOf(a, b)).map { it.id })
    }

    private fun worker(id: Long, rating: Float?, reviews: Int?) = WorkerSummary(
        id = id,
        displayName = "T$id",
        specialtyLabel = "",
        rating = rating,
        avatarUrl = null,
        isVerified = true,
        primaryRubroId = null,
        reviewCount = reviews,
    )
}
