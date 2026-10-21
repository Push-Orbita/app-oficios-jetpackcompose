package com.cocido.oficios.core.designsystem.component.scaffold

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class OficiosContentPaddingAfterTopBarTest {

    @Test
    fun `adds Figma gap after the top bar without changing other sides`() {
        val padding = PaddingValues(start = 8.dp, top = 56.dp, end = 12.dp, bottom = 24.dp)
        val resolved = oficiosContentPaddingAfterTopBar(
            padding = padding,
            extraTop = 16.dp,
            layoutDirection = LayoutDirection.Ltr,
        )
        assertEquals(8.dp, resolved.calculateLeftPadding(LayoutDirection.Ltr))
        assertEquals(72.dp, resolved.calculateTopPadding())
        assertEquals(12.dp, resolved.calculateRightPadding(LayoutDirection.Ltr))
        assertEquals(24.dp, resolved.calculateBottomPadding())
    }
}
