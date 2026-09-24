package com.cocido.oficios.core.network.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull

/**
 * Staging sometimes serializes TINYINT favorito flags as 0/1 instead of JSON booleans.
 * Accepts boolean, 0/1 numbers, and common string forms; encodes as JSON boolean.
 */
object FlexibleBooleanSerializer : KSerializer<Boolean> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexibleBoolean", PrimitiveKind.BOOLEAN)

    override fun deserialize(decoder: Decoder): Boolean {
        val jsonDecoder = decoder as? JsonDecoder
            ?: return decoder.decodeBoolean()
        val element = jsonDecoder.decodeJsonElement()
        val primitive = element as? JsonPrimitive
            ?: error("Expected boolean-compatible primitive for FlexibleBoolean, got $element")
        primitive.booleanOrNull?.let { return it }
        primitive.intOrNull?.let { return it != 0 }
        primitive.longOrNull?.let { return it != 0L }
        val text = primitive.contentOrNull?.trim()?.lowercase()
        return when (text) {
            "true", "1", "yes", "si", "sí" -> true
            "false", "0", "no", "" -> false
            null -> false
            else -> error("Cannot parse FlexibleBoolean from '$text'")
        }
    }

    override fun serialize(encoder: Encoder, value: Boolean) {
        val jsonEncoder = encoder as? JsonEncoder
        if (jsonEncoder != null) {
            jsonEncoder.encodeJsonElement(JsonPrimitive(value))
        } else {
            encoder.encodeBoolean(value)
        }
    }
}
