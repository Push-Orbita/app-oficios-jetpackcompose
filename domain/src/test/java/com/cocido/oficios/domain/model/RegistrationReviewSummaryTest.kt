package com.cocido.oficios.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class RegistrationReviewSummaryTest {

    @Test
    fun `client review shows phone barrio and street from client draft`() {
        val summary = RegistrationReviewSummary.from(
            RegistrationDraft(
                role = RegistrationRole.CLIENT,
                personal = RegistrationPersonalDraft(
                    firstName = "Ana",
                    lastName = "Lopez",
                    email = "ana@test.com",
                ),
                client = RegistrationClientDraft(
                    phone = "93705219109",
                    barrioQuery = "Centro",
                    barrioId = 4,
                    calle = "Belgrano",
                    numero = "100",
                ),
            ),
        )
        assertEquals("Ana", summary.firstName)
        assertEquals("Lopez", summary.lastName)
        assertEquals("ana@test.com", summary.email)
        assertEquals("93705219109", summary.phone)
        assertEquals("Centro", summary.barrio)
        assertEquals("Belgrano 100", summary.street)
    }

    @Test
    fun `provider review shows zona barrio and local street not blank client fields`() {
        val summary = RegistrationReviewSummary.from(
            RegistrationDraft(
                role = RegistrationRole.PROVIDER,
                personal = RegistrationPersonalDraft(
                    firstName = "Matias",
                    lastName = "Britez",
                    email = "mati@test.com",
                ),
                client = RegistrationClientDraft(),
                provider = providerDraft(
                    neighborhoodQuery = "San Miguel",
                    barrioId = 11,
                    localCalle = "Rivadavia",
                    localNumero = "200",
                    servesAtLocal = true,
                ),
            ),
        )
        assertEquals("", summary.phone)
        assertEquals("San Miguel", summary.barrio)
        assertEquals("Rivadavia 200", summary.street)
    }

    @Test
    fun `provider review uses zona barrio even without a local address`() {
        val summary = RegistrationReviewSummary.from(
            RegistrationDraft(
                role = RegistrationRole.PROVIDER,
                personal = RegistrationPersonalDraft(firstName = "Matias", lastName = "QA"),
                client = RegistrationClientDraft(),
                provider = providerDraft(neighborhoodQuery = "San Miguel", barrioId = 11),
            ),
        )
        assertEquals("San Miguel", summary.barrio)
        assertEquals("", summary.street)
        assertEquals("", summary.phone)
    }

    private fun providerDraft(
        neighborhoodQuery: String,
        barrioId: Long? = null,
        localCalle: String? = null,
        localNumero: String? = null,
        servesAtLocal: Boolean = false,
    ) = RegisterTradeDraft(
        categoryId = 1,
        categoryName = "Hogar",
        rubroId = 2,
        rubroName = "Electricidad",
        subRubroIds = listOf(3),
        otherSpecialization = null,
        experienceRange = "3 a 5 años",
        neighborhoodQuery = neighborhoodQuery,
        barrioId = barrioId,
        servesAtClientHome = !servesAtLocal,
        servesAtLocal = servesAtLocal,
        timeSlots = emptyList(),
        localCalle = localCalle,
        localNumero = localNumero,
    )
}
