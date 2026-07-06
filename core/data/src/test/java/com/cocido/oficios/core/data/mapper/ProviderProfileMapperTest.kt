package com.cocido.oficios.core.data.mapper

import com.cocido.oficios.core.network.api.auth.dto.TrabajadorOficioPerfilMeDto
import com.cocido.oficios.core.network.api.catalog.dto.CategoriaDto
import com.cocido.oficios.core.network.api.catalog.dto.RubroDto
import com.cocido.oficios.core.network.api.catalog.dto.SubRubroDto
import com.cocido.oficios.core.network.api.profile.dto.DiaLaboralSimpleDto
import com.cocido.oficios.core.network.api.profile.dto.HorarioLaboralSimpleDto
import com.cocido.oficios.core.network.api.profile.dto.ProviderDomicilioDto
import com.cocido.oficios.core.network.api.profile.dto.TrabajadorOficioRubroFullDto
import com.cocido.oficios.core.network.api.profile.dto.TrabajadorOficioSubRubroFullDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProviderProfileMapperTest {

    @Test
    fun `returns null when rubros are empty`() {
        val profile = TrabajadorOficioPerfilMeDto(
            id = 1,
            estado = "PENDIENTE",
            rubros = emptyList(),
        )

        assertNull(profile.toRegisterTradeDraft())
    }

    @Test
    fun `maps principal rubro subrubros days and domicilio`() {
        val profile = TrabajadorOficioPerfilMeDto(
            id = 1,
            estado = "PENDIENTE",
            descripcion = "Plomero matriculado",
            experiencia = "5 años",
            rubros = listOf(
                TrabajadorOficioRubroFullDto(
                    id = 10,
                    rubro = RubroDto(
                        id = 1,
                        nombre = "Plomería",
                        categoria = CategoriaDto(id = 2, nombre = "Hogar"),
                    ),
                    principal = true,
                    subRubros = listOf(
                        TrabajadorOficioSubRubroFullDto(
                            id = 100,
                            subRubro = SubRubroDto(id = 50, nombre = "Destapaciones"),
                            lugarDomicilio = true,
                            lugarLocal = false,
                            requiereTurnos = true,
                        ),
                    ),
                    diasLaborales = listOf(
                        DiaLaboralSimpleDto(
                            diaInicio = "LUNES",
                            diaFin = "VIERNES",
                            horariosLaborales = listOf(
                                HorarioLaboralSimpleDto(horaInicio = "09:00", horaFin = "18:00"),
                            ),
                        ),
                    ),
                ),
            ),
            domicilios = listOf(
                ProviderDomicilioDto(
                    barrioId = 7,
                    calle = "San Martín 123",
                    barrio = com.cocido.oficios.core.network.api.catalog.dto.BarrioDto(7, "Centro"),
                ),
            ),
            metodosPago = listOf("EFECTIVO"),
        )

        val draft = profile.toRegisterTradeDraft()

        assertNotNull(draft)
        requireNotNull(draft)
        assertEquals(2L, draft.categoryId)
        assertEquals("Hogar", draft.categoryName)
        assertEquals(1L, draft.rubroId)
        assertEquals(listOf(50L), draft.subRubroIds)
        assertEquals("Plomero matriculado", draft.otherSpecialization)
        assertEquals("5 años", draft.experienceRange)
        assertEquals(7L, draft.barrioId)
        assertTrue(draft.workDays.containsAll(listOf("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES")))
        assertEquals("09:00", draft.timeSlots.first().from)
        assertTrue(draft.servesAtClientHome)
        assertTrue(draft.requiresTurns)
        assertEquals(listOf("EFECTIVO"), draft.paymentMethods)
    }
}
