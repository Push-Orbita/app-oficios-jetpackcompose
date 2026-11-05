package com.cocido.oficios.domain.model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RegistrationDraftTest {

    private val json = Json { encodeDefaults = true }

    @Test
    fun `serialized draft never contains a password field`() {
        val encoded = json.encodeToString(
            RegistrationDraft(
                personal = RegistrationPersonalDraft(
                    firstName = "Ana",
                    lastName = "López",
                    email = "ana@test.com",
                    document = "12345678",
                    acceptedTerms = true,
                ),
                client = RegistrationClientDraft(
                    contactName = "Ana López",
                    phone = "93705219109",
                    barrioQuery = "Centro",
                    barrioId = 1,
                    calle = "Belgrano",
                ),
            ),
        )

        assertFalse(encoded.contains("password", ignoreCase = true))
        assertTrue(encoded.contains("ana@test.com"))
        assertTrue(encoded.contains("commitPhase"))
    }
}
