package com.cocido.oficios.core.common.splash

import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SplashGateTest {

    @Before
    fun setUp() {
        SplashGate.resetForTests()
    }

    @After
    fun tearDown() {
        SplashGate.resetForTests()
    }

    @Test
    fun startsKeepingSplashThenReleases() {
        assertTrue(SplashGate.keepOnScreen)
        SplashGate.release()
        assertFalse(SplashGate.keepOnScreen)
    }
}
