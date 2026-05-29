package com.cocido.oficios

import com.cocido.oficios.navigation.AppRoutes
import org.junit.Assert.assertEquals
import org.junit.Test

class AppRoutesTest {

    @Test
    fun routes_areStable() {
        assertEquals("home", AppRoutes.HOME)
        assertEquals("settings", AppRoutes.SETTINGS)
    }
}
