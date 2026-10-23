package com.cocido.oficios.feature.home.worker

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkerProfileOwnershipTest {

    @Test
    fun `ficha propia si el id público coincide con el trabajador del usuario`() {
        assertTrue(
            isOwnPublicWorkerProfile(
                publicWorkerId = 42L,
                currentUserId = 7L,
                currentProviderWorkerId = 42L,
            ),
        )
        assertTrue(
            isOwnPublicWorkerProfile(
                publicWorkerId = 42L,
                currentUserId = 42L,
                currentProviderWorkerId = null,
            ),
        )
        assertFalse(
            isOwnPublicWorkerProfile(
                publicWorkerId = 42L,
                currentUserId = 7L,
                currentProviderWorkerId = 9L,
            ),
        )
        assertFalse(
            isOwnPublicWorkerProfile(
                publicWorkerId = 0L,
                currentUserId = 0L,
                currentProviderWorkerId = 0L,
            ),
        )
    }
}
