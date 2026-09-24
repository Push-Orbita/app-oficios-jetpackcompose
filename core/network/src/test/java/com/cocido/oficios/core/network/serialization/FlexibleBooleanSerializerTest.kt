package com.cocido.oficios.core.network.serialization

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FlexibleBooleanSerializerTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Serializable
    private data class Wrapper(
        @Serializable(with = FlexibleBooleanSerializer::class)
        val favorito: Boolean = false,
    )

    @Test
    fun decodeBooleanTrue() {
        assertTrue(json.decodeFromString<Wrapper>("""{"favorito":true}""").favorito)
    }

    @Test
    fun decodeIntOneAsTrue() {
        assertTrue(json.decodeFromString<Wrapper>("""{"favorito":1}""").favorito)
    }

    @Test
    fun decodeIntZeroAsFalse() {
        assertFalse(json.decodeFromString<Wrapper>("""{"favorito":0}""").favorito)
    }

    @Test
    fun decodeStringOneAsTrue() {
        assertTrue(json.decodeFromString<Wrapper>("""{"favorito":"1"}""").favorito)
    }
}