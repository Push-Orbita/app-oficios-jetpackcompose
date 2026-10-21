package com.cocido.oficios.feature.home.worker

import com.cocido.oficios.feature.home.worker.components.reviewStars
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WorkerReviewStarsTest {

    @Test
    fun `quality 10 maps to five stars`() {
        assertEquals(5, reviewStars(10))
    }

    @Test
    fun `quality 8 maps to four stars`() {
        assertEquals(4, reviewStars(8))
    }

    @Test
    fun `missing quality has no stars`() {
        assertNull(reviewStars(null))
        assertNull(reviewStars(0))
    }
}
