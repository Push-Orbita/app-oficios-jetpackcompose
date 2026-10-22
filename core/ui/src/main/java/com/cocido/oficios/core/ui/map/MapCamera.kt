package com.cocido.oficios.core.ui.map

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState

/**
 * CameraUpdateFactory / BitmapDescriptorFactory requieren que el mapa de Google
 * haya llamado onMapLoaded. Antes de eso, o si el mapa no está disponible, no
 * se toca el factory.
 */
enum class MapCameraStrategy {
    NONE,
    SET_POSITION,
    ANIMATE,
}

fun mapCameraStrategy(
    mapLoaded: Boolean,
    mapUnavailable: Boolean,
): MapCameraStrategy = when {
    mapUnavailable -> MapCameraStrategy.NONE
    mapLoaded -> MapCameraStrategy.ANIMATE
    else -> MapCameraStrategy.SET_POSITION
}

suspend fun CameraPositionState.moveSafely(
    target: LatLng,
    zoom: Float,
    mapLoaded: Boolean,
    mapUnavailable: Boolean,
) {
    when (mapCameraStrategy(mapLoaded, mapUnavailable)) {
        MapCameraStrategy.NONE -> Unit
        MapCameraStrategy.SET_POSITION -> {
            position = CameraPosition.fromLatLngZoom(target, zoom)
        }
        MapCameraStrategy.ANIMATE -> {
            animate(CameraUpdateFactory.newLatLngZoom(target, zoom))
        }
    }
}
