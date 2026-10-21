package com.cocido.oficios.core.data.mapper

import com.cocido.oficios.core.network.api.auth.dto.PersonaDto
import com.cocido.oficios.core.network.api.auth.dto.RolUsuarioDto
import com.cocido.oficios.core.network.api.auth.dto.UsuarioDto
import com.cocido.oficios.domain.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthMapperTest {

    @Test
    fun `maps persona nombres and apellido without splitting`() {
        val user = UsuarioDto(
            id = 7,
            email = "matias@test.com",
            emailVerificado = true,
            persona = PersonaDto(
                id = 13,
                nombres = "Matias Federico",
                apellido = "Britez",
            ),
            rolUsuario = RolUsuarioDto(id = 1, nombre = "Cliente"),
        ).toDomainUser()

        assertEquals("Matias Federico", user.firstName)
        assertEquals("Britez", user.lastName)
        assertEquals(UserRole.CLIENT, user.role)
    }

    @Test
    fun `maps missing persona names to empty strings`() {
        val user = UsuarioDto(
            id = 1,
            email = "a@b.c",
            persona = PersonaDto(id = 2),
        ).toDomainUser()

        assertEquals("", user.firstName)
        assertEquals("", user.lastName)
    }
}
