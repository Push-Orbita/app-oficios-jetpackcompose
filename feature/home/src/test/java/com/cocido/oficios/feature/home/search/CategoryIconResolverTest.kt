package com.cocido.oficios.feature.home.search

import com.cocido.oficios.core.designsystem.R
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.component.categories.CategoryIconResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CategoryIconResolverTest {

    @Test
    fun `pintura matches pintor illustration`() {
        assertEquals(R.drawable.ill_pintor, CategoryIconResolver.illustrationRes("Pintura"))
        assertEquals(R.drawable.ill_pintor, CategoryIconResolver.illustrationRes("pint"))
    }

    @Test
    fun `seguridad and personales fall back to lucide`() {
        assertNull(CategoryIconResolver.illustrationRes("Seguridad en casa"))
        assertNull(CategoryIconResolver.illustrationRes("Serv. Personales"))
        assertEquals(OficiosIcons.ShieldCheck, CategoryIconResolver.vectorRes(1, "Seguridad en casa"))
        assertEquals(OficiosIcons.Users, CategoryIconResolver.vectorRes(2, "Serv. Personales"))
        assertEquals(OficiosIcons.Key, CategoryIconResolver.vectorRes(3, "Cerrajería"))
    }
}
