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
            animate(CameraUpdateFactory.newLatLngZoom(target, zoom), CAMERA_ANIMATION_MS)
        }
    }
}

/** Duración de las animaciones de cámara: rápidas pero sin saltos bruscos. */
internal const val CAMERA_ANIMATION_MS = 450

/** Zoom al que se lleva una selección nueva si el usuario estaba lejos. */
internal const val SELECTION_ZOOM = 16f

/** Por debajo de este zoom la calle no se distingue: se acerca a [SELECTION_ZOOM]. */
internal const val MIN_DETAIL_ZOOM = 15f

/** ~1 m en grados: la cámara ya está sobre la selección (p. ej. vino del propio pan). */
private const val SAME_TARGET_EPSILON = 0.00001

/**
 * Seguir una selección nueva sin pelear con el usuario: si el centro ya coincide (la
 * selección vino de mover el mapa) no se anima nada, y se respeta el zoom que eligió si
 * ya alcanza para ver la calle.
 */
fun followSelectionZoom(currentZoom: Float): Float =
    if (currentZoom >= MIN_DETAIL_ZOOM) currentZoom else SELECTION_ZOOM

fun isSameTarget(a: LatLng, b: LatLng): Boolean =
    kotlin.math.abs(a.latitude - b.latitude) < SAME_TARGET_EPSILON &&
        kotlin.math.abs(a.longitude - b.longitude) < SAME_TARGET_EPSILON

suspend fun CameraPositionState.followSelection(
    target: LatLng,
    mapLoaded: Boolean,
    mapUnavailable: Boolean,
) {
    if (isSameTarget(position.target, target)) return
    moveSafely(
        target = target,
        zoom = followSelectionZoom(position.zoom),
        mapLoaded = mapLoaded,
        mapUnavailable = mapUnavailable,
    )
}
