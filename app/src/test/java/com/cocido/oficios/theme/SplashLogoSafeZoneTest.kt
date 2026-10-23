package com.cocido.oficios.theme

import android.util.TypedValue
import androidx.core.content.res.ResourcesCompat
import com.cocido.oficios.R
import kotlin.math.sqrt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SplashLogoSafeZoneTest {

    @Test
    fun lightSplashIconIs288dpCanvas() {
        assertSplashCanvas288Dp()
    }

    @Test
    @Config(qualifiers = "night")
    fun nightSplashIconIs288dpCanvas() {
        assertSplashCanvas288Dp()
    }

    @Test
    fun lightThemeHasNoIconBackgroundAndOffWhiteWindow() {
        val app = RuntimeEnvironment.getApplication()
        val attrs = intArrayOf(
            androidx.core.splashscreen.R.attr.windowSplashScreenAnimatedIcon,
            androidx.core.splashscreen.R.attr.windowSplashScreenIconBackgroundColor,
            androidx.core.splashscreen.R.attr.windowSplashScreenBackground,
        )
        val ta = app.obtainStyledAttributes(R.style.Theme_Oficios_Splash, attrs)
        assertEquals(R.drawable.ic_splash_logo, ta.getResourceId(0, 0))
        assertFalse(
            "windowSplashScreenIconBackgroundColor reduce el círculo a 160dp",
            ta.hasValue(1),
        )
        assertEquals(0xFFF4F5F7.toInt(), ta.getColor(2, 0))
        ta.recycle()
    }

    @Test
    @Config(qualifiers = "night")
    fun nightThemeHasNoIconBackgroundAndNavyWindow() {
        val app = RuntimeEnvironment.getApplication()
        val attrs = intArrayOf(
            androidx.core.splashscreen.R.attr.windowSplashScreenIconBackgroundColor,
            androidx.core.splashscreen.R.attr.windowSplashScreenBackground,
        )
        val ta = app.obtainStyledAttributes(R.style.Theme_Oficios_Splash, attrs)
        assertFalse(ta.hasValue(0))
        assertEquals(0xFF00358B.toInt(), ta.getColor(1, 0))
        ta.recycle()
    }

    @Test
    fun isotypeFitsInside192dpCircle() {
        val app = RuntimeEnvironment.getApplication()
        val width = app.resources.getDimension(R.dimen.oficios_splash_isotype_outline_width)
        val height = app.resources.getDimension(R.dimen.oficios_splash_isotype_height)
        val density = app.resources.displayMetrics.density
        val widthDp = width / density
        val heightDp = height / density
        val diagonal = sqrt(widthDp * widthDp + heightDp * heightDp)
        assertTrue("diagonal $diagonal dp debe caber en el círculo de 192dp", diagonal < 192f)
        assertEquals(152f, heightDp, 0.51f)
    }

    @Test
    fun splashLogoXmlIsNotYellow() {
        listOf(
            "src/main/res/drawable/ic_splash_logo.xml",
            "src/main/res/drawable-night/ic_splash_logo.xml",
        ).forEach { path ->
            val xml = java.io.File(path).readText()
            val lower = xml.lowercase()
            assertFalse(path, lower.contains("#ffd"))
            assertFalse(path, lower.contains("#fc0"))
            assertFalse(path, lower.contains("#ffc107"))
            assertFalse(path, lower.contains("yellow"))
            assertTrue(path, xml.contains("ic_oficios_logotipo"))
            assertTrue(path, xml.contains("oficios_splash_icon_canvas"))
        }
    }

    private fun assertSplashCanvas288Dp() {
        val app = RuntimeEnvironment.getApplication()
        val drawable = ResourcesCompat.getDrawable(app.resources, R.drawable.ic_splash_logo, app.theme)
            ?: error("ic_splash_logo missing")
        val density = app.resources.displayMetrics.density
        val widthDp = drawable.intrinsicWidth / density
        val heightDp = drawable.intrinsicHeight / density
        assertEquals(288f, widthDp, 0.51f)
        assertEquals(288f, heightDp, 0.51f)
        val canvas = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            288f,
            app.resources.displayMetrics,
        )
        assertEquals(canvas.toInt(), drawable.intrinsicWidth)
        assertEquals(canvas.toInt(), drawable.intrinsicHeight)
    }
}
