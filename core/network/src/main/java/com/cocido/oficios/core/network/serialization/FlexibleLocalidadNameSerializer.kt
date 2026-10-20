package com.cocido.oficios.core.network.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * Staging manda `barrio.localidad` a veces como texto y a veces como
 * `{ "id", "nombre" }`. Queda el nombre para mostrarlo en la zona.
 */
object FlexibleLocalidadNameSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexibleLocalidadName", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String {
        val jsonDecoder = decoder as? JsonDecoder ?: return decoder.decodeString()
        return when (val element = jsonDecoder.decodeJsonElement()) {
            is JsonNull -> ""
            is JsonPrimitive -> element.contentOrNull.orEmpty()
            is JsonObject -> {
                val nombre = element["nombre"]
                if (nombre is JsonPrimitive) nombre.contentOrNull.orEmpty() else ""
            }
            else -> ""
        }
    }

    override fun serialize(encoder: Encoder, value: String) {
        val jsonEncoder = encoder as? JsonEncoder
        if (jsonEncoder != null) {
            jsonEncoder.encodeJsonElement(JsonPrimitive(value))
        } else {
            encoder.encodeString(value)
        }
    }
}
