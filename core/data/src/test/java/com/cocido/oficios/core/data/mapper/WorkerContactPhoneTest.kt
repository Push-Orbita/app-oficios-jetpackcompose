package com.cocido.oficios.core.data.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WorkerContactPhoneTest {

    @Test
    fun `el telefono del cliente no se usa para contactar al trabajador`() {
        assertNull(
            resolveWorkerPhone(
                workerWhatsapp = null,
                userWhatsapp = null,
                personaPhone = null,
            ),
        )
        assertNull(resolveWorkerPhone("  ", "", null))
    }

    @Test
    fun `prioriza el whatsapp del oficio y conserva el mas`() {
        assertEquals(
            "+5493704000000",
            resolveWorkerPhone(
                workerWhatsapp = " +5493704000000 ",
                userWhatsapp = "+5491111111111",
                personaPhone = "3704000000",
            ),
        )
        assertEquals(
            "+5491111111111",
            resolveWorkerPhone(workerWhatsapp = null, userWhatsapp = "+5491111111111", personaPhone = null),
        )
        assertEquals(
            "3704000000",
            resolveWorkerPhone(workerWhatsapp = null, userWhatsapp = null, personaPhone = "3704000000"),
        )
    }
}
