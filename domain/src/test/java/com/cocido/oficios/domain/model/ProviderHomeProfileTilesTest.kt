package com.cocido.oficios.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProviderHomeProfileTilesTest {

    @Test
    fun `complete profile marks every tile done`() {
        val tiles = inferProviderHomeTiles(missingSteps = listOf("galería"), isProfileComplete = true)
        assertTrue(tiles.gallery)
        assertTrue(tiles.local)
        assertTrue(tiles.certificate)
    }

    @Test
    fun `missing steps mark gallery local and certificate incomplete`() {
        val tiles = inferProviderHomeTiles(
            missingSteps = listOf("Agrega fotos a tu galería", "Local comercial", "Certificado"),
            isProfileComplete = false,
        )
        assertFalse(tiles.gallery)
        assertFalse(tiles.local)
        assertFalse(tiles.certificate)
    }

    @Test
    fun `unknown missing steps keep tiles done`() {
        val tiles = inferProviderHomeTiles(
            missingSteps = listOf("Validación de identidad"),
            isProfileComplete = false,
        )
        assertTrue(tiles.gallery)
        assertTrue(tiles.local)
        assertTrue(tiles.certificate)
    }

    @Test
    fun `unverified header subtitle is null so UI can show pending copy`() {
        val content = ProviderHomeContent(
            userDisplayName = "Emanuel",
            isVerified = false,
            primarySpecialtyLabel = "Albañil",
            profileCompletionPercent = 50,
            missingProfileItems = emptyList(),
        )
        assertEquals(null, content.headerSubtitle)
    }
}
