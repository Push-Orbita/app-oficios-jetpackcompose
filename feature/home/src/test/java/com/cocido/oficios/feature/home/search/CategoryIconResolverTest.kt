package com.cocido.oficios.feature.home.search

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pets
import com.cocido.oficios.core.designsystem.R
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.component.categories.CategoryIconResolver
import com.cocido.oficios.core.designsystem.component.categories.CategoryVectorIcon
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

    @Test
    fun `hogar is house not the white construction illustration`() {
        assertNull(CategoryIconResolver.illustrationRes("Hogar y Construcción"))
        assertEquals(
            CategoryVectorIcon.Lucide(OficiosIcons.House),
            CategoryIconResolver.resolve(1, "Hogar y Construcción").vector,
        )
    }

    @Test
    fun `mascotas uses pets material icon`() {
        assertEquals(
            CategoryVectorIcon.Material(Icons.Outlined.Pets),
            CategoryIconResolver.resolve(4, "Mascotas").vector,
        )
    }
}
