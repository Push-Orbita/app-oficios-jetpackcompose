package com.cocido.oficios.core.data.mapper

import com.cocido.oficios.domain.model.ClientContactoRequest
import com.cocido.oficios.domain.model.ClientDomicilioRequest
import com.cocido.oficios.domain.model.ProviderLocalDomicilioRequest
import com.cocido.oficios.domain.model.ProviderProfessionalInfoRequest
import com.cocido.oficios.domain.model.ProviderRubroFullRequest
import com.cocido.oficios.domain.model.RegisterTradeTimeSlotDraft
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileMapperTest {

    @Test
    fun `maps professional info request to dto`() {
        val dto = ProviderProfessionalInfoRequest(
            descripcion = "Plomero",
            experiencia = "3 a 5 años",
            documento = "12345678",
        ).toDto()

        assertEquals("Plomero", dto.descripcion)
        assertEquals("3 a 5 años", dto.experiencia)
        assertEquals("12345678", dto.documento)
    }

    @Test
    fun `maps rubro full request to dto`() {
        val dto = ProviderRubroFullRequest(
            rubroId = 10,
            subRubroIds = listOf(100, 101),
            servesAtClientHome = true,
            servesAtLocal = true,
            workDays = listOf("LUNES", "MARTES"),
            timeSlots = listOf(RegisterTradeTimeSlotDraft("08:00", "12:00")),
            domicilioLocal = ProviderLocalDomicilioRequest(
                barrioId = 5,
                calle = "Av. Siempre Viva",
                numero = "100",
                nombre = "Local",
            ),
        ).toDto()

        assertEquals(10L, dto.rubroId)
        assertEquals(2, dto.subrubros.size)
        assertTrue(dto.subrubros.all { it.lugarDomicilio })
        assertEquals(2, dto.diasLaborales.size)
        assertEquals("LUNES", dto.diasLaborales[0].diaInicio)
        assertEquals("LUNES", dto.diasLaborales[0].diaFin)
        assertEquals("08:00", dto.diasLaborales[0].horariosLaborales.single().horaInicio)
        assertEquals("12:00", dto.diasLaborales[0].horariosLaborales.single().horaFin)
        assertEquals("MARTES", dto.diasLaborales[1].diaInicio)
        assertEquals("MARTES", dto.diasLaborales[1].diaFin)
        assertEquals(5L, dto.domicilioLocal?.barrioId)
        assertEquals("Av. Siempre Viva", dto.domicilioLocal?.calle)
        assertEquals("100", dto.domicilioLocal?.numero)
        assertTrue(dto.subrubros.all { it.sesion == null })
    }

    @Test
    fun `maps sesion when rubro requires turns`() {
        val dto = ProviderRubroFullRequest(
            rubroId = 10,
            subRubroIds = listOf(100),
            servesAtClientHome = true,
            servesAtLocal = false,
            workDays = listOf("LUNES"),
            timeSlots = listOf(RegisterTradeTimeSlotDraft("08:00", "12:00")),
            requiresTurns = true,
        ).toDto()

        val sesion = dto.subrubros.single().sesion
        assertEquals(true, dto.subrubros.single().requiereTurnos)
        assertEquals(60, sesion?.duracion)
        assertEquals("minutos", sesion?.medidaDuracion)
        assertEquals("15", sesion?.intervalo)
        assertEquals("minutos", sesion?.medidaIntervalo)
    }

    @Test
    fun `maps payment methods in rubro full request`() {
        val dto = ProviderRubroFullRequest(
            rubroId = 10,
            subRubroIds = listOf(100),
            servesAtClientHome = true,
            servesAtLocal = false,
            workDays = listOf("LUNES"),
            timeSlots = listOf(RegisterTradeTimeSlotDraft("08:00", "12:00")),
            paymentMethods = listOf("EFECTIVO", "MERCADOPAGO"),
        ).toDto()

        assertEquals(listOf("EFECTIVO", "MERCADOPAGO"), dto.metodosPago)
        assertEquals(null, dto.subrubros.single().sesion)
    }

    @Test
    fun `maps client domicilio and contacto`() {
        val domicilio = ClientDomicilioRequest(
            barrioId = 1,
            personaId = 2,
            calle = "Calle",
            numero = "10",
            favorito = true,
        ).toDto()
        val contacto = ClientContactoRequest(
            nombre = "Ana",
            telefonoWhatsapp = "1122334455",
            email = "ana@test.com",
            favorito = true,
        ).toDto()

        assertEquals("Calle", domicilio.calle)
        assertEquals("Ana", contacto.nombre)
        assertEquals("1122334455", contacto.telefonoWhatsapp)
    }
}
