package com.cocido.oficios.core.ui.permission

import android.Manifest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RuntimePermissionLogicTest {

    @Test
    fun `camera predicate requires camera grant`() {
        val predicate = PermissionGrantPredicate { grants ->
            grants[Manifest.permission.CAMERA] == true
        }
        assertFalse(predicate.isGranted(mapOf(Manifest.permission.CAMERA to false)))
        assertTrue(predicate.isGranted(mapOf(Manifest.permission.CAMERA to true)))
    }

    @Test
    fun `location any-grant predicate accepts fine or coarse`() {
        val predicate = PermissionGrantPredicate { grants -> grants.values.any { it } }
        assertFalse(
            predicate.isGranted(
                mapOf(
                    Manifest.permission.ACCESS_FINE_LOCATION to false,
                    Manifest.permission.ACCESS_COARSE_LOCATION to false,
                ),
            ),
        )
        assertTrue(
            predicate.isGranted(
                mapOf(
                    Manifest.permission.ACCESS_FINE_LOCATION to false,
                    Manifest.permission.ACCESS_COARSE_LOCATION to true,
                ),
            ),
        )
        assertTrue(
            predicate.isGranted(
                mapOf(
                    Manifest.permission.ACCESS_FINE_LOCATION to true,
                    Manifest.permission.ACCESS_COARSE_LOCATION to false,
                ),
            ),
        )
    }

    @Test
    fun `tracking predicate requires fine location`() {
        val predicate = PermissionGrantPredicate { grants ->
            grants[Manifest.permission.ACCESS_FINE_LOCATION] == true
        }
        assertFalse(
            predicate.isGranted(
                mapOf(
                    Manifest.permission.ACCESS_FINE_LOCATION to false,
                    Manifest.permission.ACCESS_COARSE_LOCATION to true,
                ),
            ),
        )
        assertTrue(
            predicate.isGranted(
                mapOf(
                    Manifest.permission.ACCESS_FINE_LOCATION to true,
                    Manifest.permission.ACCESS_COARSE_LOCATION to false,
                ),
            ),
        )
    }
}
