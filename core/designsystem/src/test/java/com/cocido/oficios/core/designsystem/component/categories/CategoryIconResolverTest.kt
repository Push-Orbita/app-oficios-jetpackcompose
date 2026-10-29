package com.cocido.oficios.core.designsystem.component.categories

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Science
import com.cocido.oficios.core.designsystem.R
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryIconResolverTest {

    @Test
    fun screenshotCategoriesEachGetADistinctThematicIcon() {
        val labels = listOf(
            "Metal",
            "Test",
            "Eventos y Servicios",
            "Mascotas",
            "Tecnología",
            "Salud y Belleza",
            "Hogar y Construcción",
        )
        val specs = labels.mapIndexed { index, label ->
            CategoryIconResolver.resolve(categoryId = index.toLong() + 1, categoryName = label)
        }

        assertEquals(material(Icons.Outlined.Build), specs[0].vector)
        assertEquals(material(Icons.Outlined.Science), specs[1].vector)
        assertEquals(material(Icons.Outlined.Event), specs[2].vector)
        assertEquals(material(Icons.Outlined.Pets), specs[3].vector)
        assertEquals(material(Icons.Outlined.Devices), specs[4].vector)
        assertEquals(material(Icons.Outlined.HealthAndSafety), specs[5].vector)
        assertEquals(lucide(OficiosIcons.House), specs[6].vector)

        specs.forEach { spec ->
            assertNull(spec.illustrationRes)
            assertTrue(spec.vector != lucide(OficiosIcons.Hammer))
        }
        assertEquals(labels.size, specs.map { it.vector }.distinct().size)
    }

    @Test
    fun accentAndSlugVariantsStillMap() {
        assertEquals(
            lucide(OficiosIcons.House),
            CategoryIconResolver.resolve(1, "Hogar y Construccion").vector,
        )
        assertEquals(
            material(Icons.Outlined.Devices),
            CategoryIconResolver.resolve(1, "tecnologia").vector,
        )
        assertEquals(
            material(Icons.Outlined.Pets),
            CategoryIconResolver.resolve(1, "mascotas").vector,
        )
    }

    @Test
    fun remoteUrlWinsWhenPresentAndIsIgnoredWhenBlank() {
        val withUrl = CategoryIconResolver.resolve(
            categoryId = 1,
            categoryName = "Metal",
            iconUrl = "https://cdn.example/metal.png",
        )
        assertEquals("https://cdn.example/metal.png", withUrl.remoteUrl)
        assertEquals(material(Icons.Outlined.Build), withUrl.vector)

        assertNull(CategoryIconResolver.resolve(1, "Metal", "   ").remoteUrl)
        assertNull(CategoryIconResolver.resolve(1, "Metal", null).remoteUrl)
    }

    @Test
    fun tradeIllustrationsStayAvailableAsTintedFallback() {
        assertEquals(R.drawable.ill_pintor, CategoryIconResolver.illustrationRes("Pintura"))
        assertEquals(R.drawable.ill_plomero, CategoryIconResolver.illustrationRes("Plomería"))
        assertNull(CategoryIconResolver.illustrationRes("Hogar y Construcción"))
        assertNull(CategoryIconResolver.illustrationRes("Seguridad en casa"))
    }

    @Test
    fun lucideMappingsForSecurityPeopleAndLocksmith() {
        assertEquals(OficiosIcons.ShieldCheck, CategoryIconResolver.vectorRes(1, "Seguridad en casa"))
        assertEquals(OficiosIcons.Users, CategoryIconResolver.vectorRes(2, "Serv. Personales"))
        assertEquals(OficiosIcons.Key, CategoryIconResolver.vectorRes(3, "Cerrajería"))
    }

    @Test
    fun unknownAdjacentRubrosDoNotShareTheHammerOrEachOther() {
        val first = CategoryIconResolver.resolve(10, "Rubro inventado Alfa")
        val second = CategoryIconResolver.resolve(11, "Rubro inventado Alfa")
        assertNotEquals(first.vector, second.vector)
        assertTrue(first.vector != lucide(OficiosIcons.Hammer))
        assertTrue(second.vector != lucide(OficiosIcons.Hammer))
        assertTrue(first.vector is CategoryVectorIcon.Material)
        assertTrue(second.vector is CategoryVectorIcon.Material)
    }

    @Test
    fun seeAllMapsToCatalogIcon() {
        assertEquals(
            lucide(OficiosIcons.BookUser),
            CategoryIconResolver.resolve(-1, "Ver todos").vector,
        )
    }

    private fun material(icon: androidx.compose.ui.graphics.vector.ImageVector) =
        CategoryVectorIcon.Material(icon)

    private fun lucide(resId: Int) = CategoryVectorIcon.Lucide(resId)
}
