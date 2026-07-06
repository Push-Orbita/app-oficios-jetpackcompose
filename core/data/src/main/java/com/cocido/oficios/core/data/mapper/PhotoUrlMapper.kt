package com.cocido.oficios.core.data.mapper

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

fun JsonElement?.toPhotoUrl(): String? =
    (this as? JsonPrimitive)?.content?.takeIf { it.isNotBlank() }
