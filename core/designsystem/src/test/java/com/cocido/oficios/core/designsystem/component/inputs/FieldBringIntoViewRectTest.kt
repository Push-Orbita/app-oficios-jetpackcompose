package com.cocido.oficios.core.designsystem.component.inputs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FieldBringIntoViewRectTest {

    @Test
    fun `prioritizes focused field over block bottom when viewport is smaller than field to cta span`() {
        val rect = fieldFocusedBringIntoViewRect(
            blockWidth = 360,
            blockHeight = 900,
            fieldTopPx = 80f,
            fieldHeightPx = 140f,
            fieldMarginPx = 12f,
            viewportHeightPx = 320,
        )

        assertEquals(68f, rect.top)
        assertEquals(232f, rect.bottom)
        assertTrue(rect.bottom < 900f)
    }

    @Test
    fun `when field exceeds viewport shows top of field`() {
        val rect = fieldFocusedBringIntoViewRect(
            blockWidth = 360,
            blockHeight = 600,
            fieldTopPx = 40f,
            fieldHeightPx = 500f,
            fieldMarginPx = 8f,
            viewportHeightPx = 200,
        )

        assertEquals(32f, rect.top)
        assertEquals(231f, rect.bottom)
    }
}
