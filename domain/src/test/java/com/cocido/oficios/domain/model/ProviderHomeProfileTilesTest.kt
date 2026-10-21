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
        assertEquals(66, ProviderHomeTiles(gallery = true, local = false, certificate = true).completionPercent)
        assertEquals(66, ProviderHomeTiles(gallery = true, local = true, certificate = false).completionPercent)
        assertEquals(100, ProviderHomeTiles(gallery = true, local = true, certificate = true).completionPercent)
    }

    @Test
    fun `first incomplete target follows gallery then local then certificate`() {
        assertEquals(
            ProviderProfileCompletionTarget.GALLERY,
            ProviderHomeTiles(gallery = false, local = false, certificate = false).firstIncompleteTarget(),
        )
        assertEquals(
            ProviderProfileCompletionTarget.COMMERCIAL_LOCAL,
            ProviderHomeTiles(gallery = true, local = false, certificate = true).firstIncompleteTarget(),
        )
        assertEquals(
            ProviderProfileCompletionTarget.CERTIFICATE,
            ProviderHomeTiles(gallery = true, local = true, certificate = false).firstIncompleteTarget(),
        )
        assertEquals(
            null,
            ProviderHomeTiles(gallery = true, local = true, certificate = true).firstIncompleteTarget(),
        )
    }

    @Test
    fun `resolve tiles uses gallery certificates and local from perfil`() {
        val details = ProviderAccountDetails(
            workerId = 1,
            status = "VALIDADO",
            document = null,
            description = null,
            experience = null,
            paymentMethods = emptyList(),
            services = listOf(
                ManagedProviderService(
                    id = 11,
                    rubroId = 2,
                    rubroName = "Plomería",
                    isPrimary = true,
                    subServices = listOf(
                        ManagedProviderSubService(1, 21, "Caños", true, false, false),
                    ),
                    workSchedule = emptyList(),
                ),
            ),
            gallery = listOf(WorkGalleryItem(1, "https://example.com/a.jpg")),
            certificates = listOf(WorkCertificateItem(1, "Matrícula", null)),
            commercialLocal = null,
        )
        val tiles = resolveProviderHomeTiles(
            details = details,
            missingSteps = listOf("local"),
            isProfileComplete = false,
        )
        assertTrue(tiles.gallery)
        assertFalse(tiles.local)
        assertTrue(tiles.certificate)
        assertEquals(66, tiles.completionPercent)
    }

    @Test
    fun `local tile is done when GET perfil has a commercial street`() {
        val details = ProviderAccountDetails(
            workerId = 1,
            status = "VALIDADO",
            document = null,
            description = null,
            experience = null,
            paymentMethods = emptyList(),
            services = emptyList(),
            gallery = emptyList(),
            certificates = emptyList(),
            commercialLocal = ProviderCommercialLocal(barrioId = 7, barrioName = "Centro", street = "San Martín 123"),
        )
        assertTrue(details.hasCommercialLocal)
        val tiles = resolveProviderHomeTiles(details, missingSteps = emptyList(), isProfileComplete = false)
        assertTrue(tiles.local)
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
