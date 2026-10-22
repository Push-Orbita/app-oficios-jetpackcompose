package com.cocido.oficios.core.designsystem.component.navigation

import com.cocido.oficios.core.designsystem.theme.DarkOficiosColors
import com.cocido.oficios.core.designsystem.theme.LightOficiosColors
import com.cocido.oficios.core.designsystem.theme.OficiosColorBackground
import com.cocido.oficios.core.designsystem.theme.OficiosColorWhite
import com.cocido.oficios.core.designsystem.theme.selectedContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class BottomNavSelectionColorsTest {

    @Test
    fun lightAllItemsUseBrandBlueNeverGray() {
        val light = LightOficiosColors
        assertEquals(light.primary, bottomNavLabelColor(selected = true, light))
        assertEquals(light.primary, bottomNavLabelColor(selected = false, light))
        assertEquals(OficiosColorBackground, bottomNavLabelColor(selected = true, light))
        assertNotEquals(OficiosColorWhite, bottomNavLabelColor(selected = true, light))
        assertNotEquals(light.onSurfaceVariant, bottomNavLabelColor(selected = false, light))
        assertEquals(OficiosColorWhite, bottomNavIconTint(selected = true, light))
        assertEquals(light.onIconContainer, bottomNavIconTint(selected = true, light))
        assertEquals(light.primary, bottomNavIconTint(selected = false, light))
        assertNotEquals(light.onSurfaceVariant, bottomNavIconTint(selected = false, light))
    }

    @Test
    fun darkKeepsCurrentNavyStyle() {
        val dark = DarkOficiosColors
        assertEquals(OficiosColorWhite, bottomNavLabelColor(selected = true, dark))
        assertEquals(OficiosColorWhite, bottomNavIconTint(selected = true, dark))
        assertEquals(dark.onSurfaceVariant, bottomNavLabelColor(selected = false, dark))
        assertEquals(dark.onSurfaceVariant, bottomNavIconTint(selected = false, dark))
    }

    @Test
    fun selectedContentIsOnPrimaryOnlyWhenFilledPrimary() {
        val light = LightOficiosColors
        assertEquals(OficiosColorWhite, light.selectedContent(onFilledPrimary = true))
        assertEquals(OficiosColorBackground, light.selectedContent(onFilledPrimary = false))
        val dark = DarkOficiosColors
        assertEquals(OficiosColorWhite, dark.selectedContent(onFilledPrimary = true))
        assertEquals(OficiosColorWhite, dark.selectedContent(onFilledPrimary = false))
    }
}
