package com.cocido.oficios.core.data.mapper

import com.cocido.oficios.core.network.api.account.dto.WorkGalleryDto
import com.cocido.oficios.core.network.api.auth.dto.TrabajadorOficioPerfilMeDto
import com.cocido.oficios.core.network.api.catalog.dto.BarrioDto
import com.cocido.oficios.core.network.api.catalog.dto.RubroDto
import com.cocido.oficios.core.network.api.catalog.dto.SubRubroDto
import com.cocido.oficios.core.network.api.profile.dto.CertificadoTrabajoDto
import com.cocido.oficios.core.network.api.profile.dto.ProviderDomicilioDto
import com.cocido.oficios.core.network.api.profile.dto.TrabajadorOficioRubroFullDto
import com.cocido.oficios.core.network.api.profile.dto.TrabajadorOficioSubRubroFullDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountMapperTest {

    @Test
    fun `maps gallery certificates and commercial local from GET perfil`() {
        val details = TrabajadorOficioPerfilMeDto(
            id = 9,
            estado = "VALIDADO",
            rubros = listOf(
                TrabajadorOficioRubroFullDto(
                    id = 11,
                    rubro = RubroDto(id = 2, nombre = "Plomería"),
                    principal = true,
                    subRubros = listOf(
                        TrabajadorOficioSubRubroFullDto(
                            id = 100,
                            subRubro = SubRubroDto(id = 21, nombre = "Caños"),
                            lugarDomicilio = true,
                            lugarLocal = false,
                        ),
                    ),
                ),
            ),
            domicilios = listOf(
                ProviderDomicilioDto(
                    id = 4,
                    barrioId = 7,
                    calle = "San Martín",
                    numero = "123",
                    nombre = "Local comercial",
                    barrio = BarrioDto(7, "Centro"),
                ),
            ),
            certificados = listOf(CertificadoTrabajoDto(1, "Matrícula", "https://example.com/c.pdf")),
            galeriaTrabajos = listOf(WorkGalleryDto(2, "https://example.com/g.jpg")),
        ).toAccountDetails()

        assertEquals(1, details.gallery.size)
        assertEquals(1, details.certificates.size)
        assertEquals(7L, details.commercialLocal?.barrioId)
        assertEquals("Centro", details.commercialLocal?.barrioName)
        assertEquals("San Martín", details.commercialLocal?.street)
        assertEquals("123", details.commercialLocal?.number)
        assertFalse(details.services.first().subServices.first().servesAtLocal)
        assertTrue(details.hasCommercialLocal)
    }
}
