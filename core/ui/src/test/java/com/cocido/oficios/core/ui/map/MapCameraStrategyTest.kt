package com.cocido.oficios.core.ui.map

import org.junit.Assert.assertEquals
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
}
