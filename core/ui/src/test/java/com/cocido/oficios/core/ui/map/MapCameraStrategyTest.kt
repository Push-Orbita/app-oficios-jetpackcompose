package com.cocido.oficios.core.ui.map

import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MapCameraStrategyTest {

    @Test
    fun unavailableNeverTouchesCamera() {
        assertEquals(MapCameraStrategy.NONE, mapCameraStrategy(mapLoaded = false, mapUnavailable = true))
        assertEquals(MapCameraStrategy.NONE, mapCameraStrategy(mapLoaded = true, mapUnavailable = true))
    }

    @Test
    fun beforeLoadSetsPositionWithoutFactory() {
        assertEquals(
            MapCameraStrategy.SET_POSITION,
            mapCameraStrategy(mapLoaded = false, mapUnavailable = false),
        )
    }

    @Test
    fun afterLoadAnimatesWithFactory() {
        assertEquals(
            MapCameraStrategy.ANIMATE,
            mapCameraStrategy(mapLoaded = true, mapUnavailable = false),
        )
    }

    @Test
    fun followingSelectionKeepsTheZoomTheUserChose() {
        assertEquals(18f, followSelectionZoom(18f))
        assertEquals(15f, followSelectionZoom(15f))
    }

    @Test
    fun followingSelectionZoomsInFromCityLevel() {
        assertEquals(SELECTION_ZOOM, followSelectionZoom(12f))
    }

    @Test
    fun panToSameCenterIsNotANewMove() {
        val center = LatLng(-26.1849, -58.1731)
        assertTrue(isSameTarget(center, LatLng(-26.184901, -58.173101)))
        assertFalse(isSameTarget(center, LatLng(-26.1859, -58.1731)))
    }
}
