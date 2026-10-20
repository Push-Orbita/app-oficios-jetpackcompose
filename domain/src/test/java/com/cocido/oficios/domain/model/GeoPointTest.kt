package com.cocido.oficios.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeoPointTest {
    @Test fun `formosa coordinate is valid`() {
        assertTrue(GeoPoint(-26.1849, -58.1731).isValid)
    }

    @Test fun `zero zero placeholder is rejected`() {
        assertFalse(GeoPoint(0.0, 0.0).isValid)
    }

    @Test fun `out of range coordinate is rejected`() {
        assertFalse(GeoPoint(-91.0, -58.0).isValid)
    }
}
