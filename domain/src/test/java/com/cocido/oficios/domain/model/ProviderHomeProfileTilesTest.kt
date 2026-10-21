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

    @Test
    fun `completion percent is thirds of the three tiles`() {
        assertEquals(0, ProviderHomeTiles(gallery = false, local = false, certificate = false).completionPercent)
        assertEquals(33, ProviderHomeTiles(gallery = true, local = false, certificate = false).completionPercent)
        assertEquals(66, ProviderHomeTiles(gallery = true, local = true, certificate = false).completionPercent)
        assertEquals(100, ProviderHomeTiles(gallery = true, local = true, certificate = true).completionPercent)
    }

    @Test
    fun `showProfileCompletion hides Completa Tu Perfil at 100 percent`() {
        assertTrue(providerHome(0).showProfileCompletion)
        assertTrue(providerHome(33).showProfileCompletion)
        assertTrue(providerHome(80).showProfileCompletion)
        assertTrue(providerHome(99).showProfileCompletion)
        assertFalse(providerHome(100).showProfileCompletion)
    }

    private fun providerHome(percent: Int) = ProviderHomeContent(
        userDisplayName = "Emanuel",
        isVerified = true,
        primarySpecialtyLabel = "Albañil",
        profileCompletionPercent = percent,
        missingProfileItems = emptyList(),
    )
}
