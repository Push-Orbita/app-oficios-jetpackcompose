package com.cocido.oficios.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * Borrador del alta/edición de servicio del trabajador. Se guarda en el SavedStateHandle
 * para que el formulario sobreviva a la muerte del proceso (Android mata la app en segundo
 * plano) sin depender de la red: al volver, el usuario encuentra lo que había cargado.
 *
 * Solo contiene datos que el usuario ingresó; catálogos y datos del servidor se recargan.
 */
@Serializable
data class AddServiceDraft(
    val step: String,
    val categoryId: Long? = null,
    val rubroId: Long? = null,
    val subRubroIds: List<Long> = emptyList(),
    val subRubroIdsByRubro: Map<Long, List<Long>> = emptyMap(),
    val servesAtClientHome: Boolean = true,
    val servesAtLocal: Boolean = false,
    val requiresTurns: Boolean = false,
    val plazoCancelacionHorasInput: String = "",
    val localName: String = "",
    val localBarrioQuery: String = "",
    val localBarrioId: Long? = null,
    val localStreet: String = "",
    val localNumber: String = "",
    val localLatitude: Double? = null,
    val localLongitude: Double? = null,
    val localPinConfirmed: Boolean = false,
    val weeklyAvailability: WeeklyAvailability = WeeklyAvailability.default(),
    val paymentMethods: List<String> = emptyList(),
    val editorSourceSubId: Long? = null,
    val availabilityDrafts: Map<Long, WeeklyAvailability> = emptyMap(),
)

object AddServiceDraftCodec {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

    fun encode(draft: AddServiceDraft): String = json.encodeToString(AddServiceDraft.serializer(), draft)

    /** Devuelve null si el borrador está corrupto o es de una versión incompatible. */
    fun decode(raw: String?): AddServiceDraft? {
        if (raw.isNullOrBlank()) return null
        return try {
            json.decodeFromString(AddServiceDraft.serializer(), raw)
        } catch (_: SerializationException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }
}
