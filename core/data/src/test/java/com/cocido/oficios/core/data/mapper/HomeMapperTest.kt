package com.cocido.oficios.core.data.mapper

import com.cocido.oficios.core.network.api.GreetingDto
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeMapperTest {

    @Test
    fun `maps dto to domain model`() {
        val dto = GreetingDto(
            id = 1,
            title = "Hello",
            body = "World",
        )

        val domain = dto.toDomain()

        assertEquals("Hello", domain.title)
        assertEquals("World", domain.description)
    }
}
