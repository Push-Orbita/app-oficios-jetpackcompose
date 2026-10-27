package com.cocido.oficios.core.data.mapper

import com.cocido.oficios.core.network.api.auth.dto.PersonaDto
import com.cocido.oficios.core.network.api.auth.dto.UsuarioDto
import com.cocido.oficios.core.network.api.catalog.dto.RubroDto
import com.cocido.oficios.core.network.api.orders.dto.ClienteSimpleDto
import com.cocido.oficios.core.network.api.orders.dto.SolicitudServicioSimpleDto
import com.cocido.oficios.core.network.api.orders.dto.TrabajadorOficioSimpleDto
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OrderListItemAvatarMappingTest {

    @Test
    fun `trabajador ve fotoPerfil del cliente`() {
        val item = solicitud(
            clientPhoto = "https://cdn.example/cliente.jpg",
            workerPhoto = "https://cdn.example/trabajador.jpg",
        ).toListItem(isWorkerViewing = true)
        assertEquals("https://cdn.example/cliente.jpg", item.avatarUrl)
        assertEquals("Ana Paz", item.counterpartName)
    }

    @Test
    fun `cliente ve fotoPerfil del trabajador`() {
        val item = solicitud(
            clientPhoto = "https://cdn.example/cliente.jpg",
            workerPhoto = "https://cdn.example/trabajador.jpg",
        ).toListItem(isWorkerViewing = false)
        assertEquals("https://cdn.example/trabajador.jpg", item.avatarUrl)
        assertEquals("Luis Pérez", item.counterpartName)
    }

    @Test
    fun `sin fotoPerfil el listado deja avatar nulo para iniciales`() {
        val item = solicitud(clientPhoto = null, workerPhoto = null)
            .toListItem(isWorkerViewing = true)
        assertNull(item.avatarUrl)
    }

    private fun solicitud(clientPhoto: String?, workerPhoto: String?) = SolicitudServicioSimpleDto(
        id = 5,
        estado = "ACEPTADA",
        rubro = RubroDto(id = 1, nombre = "Plomería"),
        cliente = ClienteSimpleDto(
            id = 7,
            usuario = UsuarioDto(
                fotoPerfil = clientPhoto?.let { JsonPrimitive(it) },
                persona = PersonaDto(nombres = "Ana", apellido = "Paz"),
            ),
        ),
        trabajadorOficio = TrabajadorOficioSimpleDto(
            id = 9,
            usuario = UsuarioDto(
                fotoPerfil = workerPhoto?.let { JsonPrimitive(it) },
                persona = PersonaDto(nombres = "Luis", apellido = "Pérez"),
            ),
        ),
    )
}
