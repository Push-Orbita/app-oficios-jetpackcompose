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
                    document = "30111222",
                ),
                client = RegistrationClientDraft(
                    contactName = "Ana Lopez",
                    phone = "93705219109",
                    barrioQuery = "Centro",
                    barrioId = 4,
                    calle = "Belgrano",
                    numero = "100",
                    piso = "2",
                    departamento = "A",
                ),
            ),
        )
        assertEquals("Ana", summary.firstName)
        assertEquals("Lopez", summary.lastName)
        assertEquals("ana@test.com", summary.email)
        assertEquals("93705219109", summary.phone)
        assertEquals("Centro", summary.barrio)
        assertEquals("Belgrano 100", summary.street)
        assertEquals("30111222", summary.document)
        assertEquals("Ana Lopez", summary.contactName)
        assertEquals("2", summary.piso)
        assertEquals("A", summary.departamento)
        assertEquals("", summary.oficioCategory)
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
        assertEquals("Hogar", summary.oficioCategory)
        assertEquals("Electricidad", summary.oficioRubro)
        assertEquals("3 a 5 años", summary.experienceRange)
        assertEquals("Lunes, Martes, Miércoles, Jueves, Viernes", summary.workDays)
        assertEquals("Lunes: 09:00 a 18:00\nMartes: 09:00 a 18:00\nMiércoles: 09:00 a 18:00\nJueves: 09:00 a 18:00\nViernes: 09:00 a 18:00", summary.schedule)
        assertEquals("En local propio", summary.serviceArea)
        assertEquals("Efectivo, Transferencia", summary.paymentMethods)
        assertEquals(true, summary.showStreet)
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
        assertEquals("En el domicilio del cliente", summary.serviceArea)
        assertEquals(false, summary.showStreet)
    }

    @Test
    fun `provider review hides street when only serves at client home`() {
        val summary = RegistrationReviewSummary.from(
            RegistrationDraft(
                role = RegistrationRole.PROVIDER,
                personal = RegistrationPersonalDraft(firstName = "Matias", lastName = "QA"),
                client = RegistrationClientDraft(),
                provider = providerDraft(
                    neighborhoodQuery = "San Miguel",
                    barrioId = 11,
                    localCalle = "Rivadavia",
                    localNumero = "200",
                    servesAtLocal = false,
                ),
            ),
        )
        assertEquals(false, summary.showStreet)
        assertEquals("", summary.street)
        assertEquals("En el domicilio del cliente", summary.serviceArea)
    }

    @Test
    fun `provider review shows street when serves at local`() {
        val summary = RegistrationReviewSummary.from(
            RegistrationDraft(
                role = RegistrationRole.PROVIDER,
                personal = RegistrationPersonalDraft(firstName = "Matias", lastName = "QA"),
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
        assertEquals(true, summary.showStreet)
        assertEquals("Rivadavia 200", summary.street)
    }

    @Test
    fun `client review always shows street`() {
        val summary = RegistrationReviewSummary.from(
            RegistrationDraft(
                role = RegistrationRole.CLIENT,
                personal = RegistrationPersonalDraft(firstName = "Ana", lastName = "Lopez"),
                client = RegistrationClientDraft(calle = "Belgrano", numero = "100", barrioQuery = "Centro"),
            ),
        )
        assertEquals(true, summary.showStreet)
        assertEquals("Belgrano 100", summary.street)
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
        workDays = listOf("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES"),
        servesAtClientHome = !servesAtLocal,
        servesAtLocal = servesAtLocal,
        timeSlots = listOf(RegisterTradeTimeSlotDraft("09:00", "18:00")),
        localCalle = localCalle,
        localNumero = localNumero,
        paymentMethods = listOf(ProviderPaymentMethods.EFECTIVO, ProviderPaymentMethods.TRANSFERENCIA),
    )
}