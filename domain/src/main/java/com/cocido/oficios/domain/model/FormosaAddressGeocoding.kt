package com.cocido.oficios.domain.model

/**
 * Lat/lng de un domicilio escrito a mano (calle, número, barrio) en Formosa.
 * Nunca se toma del GPS del dispositivo: el geocoding forward es la fuente,
 * y el pin del mapa es el fallback explícito.
 */
data class LocalAddressQuery(
    val street: String,
    val number: String = "",
    val neighborhood: String = "",
    val city: String = FormosaAddressGeocoding.CITY,
    val province: String = FormosaAddressGeocoding.PROVINCE,
    val country: String = FormosaAddressGeocoding.COUNTRY,
)

data class GeocodedPlace(
    val latitude: Double,
    val longitude: Double,
    val formattedAddress: String = "",
    val locality: String? = null,
    val subAdminArea: String? = null,
    val adminArea: String? = null,
    val countryName: String? = null,
) {
    val asGeoPoint: GeoPoint get() = GeoPoint(latitude, longitude)
}

enum class AddressLocationPhase {
    Idle,
    Geocoding,
    Preview,
    NeedsMap,
    PickingMap,
    Confirmed,
}

sealed interface ForwardGeocodeOutcome {
    data class Unique(val place: GeocodedPlace) : ForwardGeocodeOutcome
    data class Ambiguous(val candidates: List<GeocodedPlace>) : ForwardGeocodeOutcome
    data object None : ForwardGeocodeOutcome
    data object OutsideRegion : ForwardGeocodeOutcome
}

data class AddressLocationSnapshot(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val phase: AddressLocationPhase = AddressLocationPhase.Idle,
    val confirmed: Boolean = false,
    val message: String? = null,
) {
    val canPersist: Boolean get() = confirmed && latitude != null && longitude != null
    val mapMode: AddressMapMode
        get() = when (phase) {
            AddressLocationPhase.Idle, AddressLocationPhase.Geocoding -> AddressMapMode.Hidden
            AddressLocationPhase.Preview, AddressLocationPhase.Confirmed -> AddressMapMode.Preview
            AddressLocationPhase.NeedsMap -> AddressMapMode.NeedsMap
            AddressLocationPhase.PickingMap -> AddressMapMode.Picking
        }
    val showMap: Boolean get() = mapMode == AddressMapMode.Preview || mapMode == AddressMapMode.Picking
    val pinDraggable: Boolean get() = mapMode == AddressMapMode.Picking
    val showLocateOnMap: Boolean
        get() = phase != AddressLocationPhase.PickingMap && phase != AddressLocationPhase.Geocoding
    val showConfirm: Boolean
        get() = phase == AddressLocationPhase.Preview || phase == AddressLocationPhase.PickingMap
    val locateOnMapLabel: String
        get() = if (phase == AddressLocationPhase.Preview || phase == AddressLocationPhase.Confirmed) {
            "Ajustar en el mapa"
        } else {
            "Ubicar mi local en el mapa"
        }
}

enum class AddressMapMode { Hidden, Preview, NeedsMap, Picking }

object FormosaAddressGeocoding {
    const val CITY = "Formosa"
    const val PROVINCE = "Formosa"
    const val COUNTRY = "Argentina"
    const val CENTER_LATITUDE = -26.1849
    const val CENTER_LONGITUDE = -58.1731

    /** Caja de la ciudad de Formosa (no la provincia completa). */
    const val SOUTH = -26.35
    const val NORTH = -26.05
    const val WEST = -58.35
    const val EAST = -58.00

    const val CLUSTER_METERS = 200.0

    const val PREVIEW_MESSAGE =
        "Así se verá la ubicación. Confirme el pin para guardarla. La calle escrita no se modifica."
    const val NONE_MESSAGE =
        "No encontramos esa dirección en Formosa. Ubíquela en el mapa."
    const val AMBIGUOUS_MESSAGE =
        "Hay varias coincidencias para esa dirección. Ubíquela en el mapa."
    const val OUTSIDE_MESSAGE =
        "Esa búsqueda no está en Formosa. Ubíquela en el mapa."
    const val GEOCODER_ERROR_MESSAGE =
        "No pudimos ubicar esa dirección. Ubíquela en el mapa."

    fun buildQuery(query: LocalAddressQuery): String {
        val streetLine = listOf(query.street.trim(), query.number.trim())
            .filter { it.isNotBlank() }
            .joinToString(" ")
        return listOf(
            streetLine,
            query.neighborhood.trim(),
            query.city,
            query.province,
            query.country,
        ).filter { it.isNotBlank() }.joinToString(", ")
    }

    fun contains(latitude: Double, longitude: Double): Boolean =
        latitude in SOUTH..NORTH && longitude in WEST..EAST

    fun resolve(places: List<GeocodedPlace>): ForwardGeocodeOutcome {
        val valid = places.filter { it.asGeoPoint.isValid }
        if (valid.isEmpty()) return ForwardGeocodeOutcome.None
        val inFormosa = valid.filter(::isInFormosa)
        if (inFormosa.isEmpty()) return ForwardGeocodeOutcome.OutsideRegion
        val representative = cluster(inFormosa)
        return if (representative.size == 1) {
            ForwardGeocodeOutcome.Unique(representative.first())
        } else {
            ForwardGeocodeOutcome.Ambiguous(representative)
        }
    }

    fun snapshotFor(outcome: ForwardGeocodeOutcome): AddressLocationSnapshot = when (outcome) {
        is ForwardGeocodeOutcome.Unique -> AddressLocationSnapshot(
            latitude = outcome.place.latitude,
            longitude = outcome.place.longitude,
            phase = AddressLocationPhase.Preview,
            confirmed = false,
            message = PREVIEW_MESSAGE,
        )
        is ForwardGeocodeOutcome.Ambiguous -> AddressLocationSnapshot(
            phase = AddressLocationPhase.NeedsMap,
            message = AMBIGUOUS_MESSAGE,
        )
        ForwardGeocodeOutcome.None -> AddressLocationSnapshot(
            phase = AddressLocationPhase.NeedsMap,
            message = NONE_MESSAGE,
        )
        ForwardGeocodeOutcome.OutsideRegion -> AddressLocationSnapshot(
            phase = AddressLocationPhase.NeedsMap,
            message = OUTSIDE_MESSAGE,
        )
    }

    fun invalidate(): AddressLocationSnapshot = AddressLocationSnapshot()

    fun openMap(current: AddressLocationSnapshot): AddressLocationSnapshot = current.copy(
        phase = AddressLocationPhase.PickingMap,
        confirmed = false,
        message = "Arrastre el pin hasta la entrada y confirme. La calle escrita no se modifica.",
        latitude = current.latitude ?: CENTER_LATITUDE,
        longitude = current.longitude ?: CENTER_LONGITUDE,
    )

    fun pinMoved(
        current: AddressLocationSnapshot,
        latitude: Double,
        longitude: Double,
    ): AddressLocationSnapshot = current.copy(
        latitude = latitude,
        longitude = longitude,
        confirmed = false,
        phase = AddressLocationPhase.PickingMap,
    )

    fun confirmPin(current: AddressLocationSnapshot): AddressLocationSnapshot {
        val latitude = current.latitude
        val longitude = current.longitude
        if (latitude == null || longitude == null) return current.copy(
            phase = AddressLocationPhase.PickingMap,
            message = "Marque la entrada en el mapa para continuar.",
        )
        return current.copy(
            phase = AddressLocationPhase.Confirmed,
            confirmed = true,
            message = PREVIEW_MESSAGE,
        )
    }

    private fun isInFormosa(place: GeocodedPlace): Boolean =
        contains(place.latitude, place.longitude)

    private fun cluster(places: List<GeocodedPlace>): List<GeocodedPlace> {
        val unique = mutableListOf<GeocodedPlace>()
        places.forEach { candidate ->
            val nearExisting = unique.any { distanceMeters(it, candidate) <= CLUSTER_METERS }
            if (!nearExisting) unique += candidate
        }
        return unique
    }

    fun distanceMeters(a: GeocodedPlace, b: GeocodedPlace): Double {
        val earth = 6_371_000.0
        val lat1 = Math.toRadians(a.latitude)
        val lat2 = Math.toRadians(b.latitude)
        val dLat = lat2 - lat1
        val dLng = Math.toRadians(b.longitude - a.longitude)
        val h = haversin(dLat) + Math.cos(lat1) * Math.cos(lat2) * haversin(dLng)
        return 2 * earth * Math.asin(Math.sqrt(h.coerceIn(0.0, 1.0)))
    }

    private fun haversin(value: Double): Double {
        val s = Math.sin(value / 2.0)
        return s * s
    }
}
