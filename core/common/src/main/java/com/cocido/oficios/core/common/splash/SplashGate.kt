package com.cocido.oficios.core.common.splash

/**
 * El splash de sistema ([androidx.core.splashscreen]) se mantiene visible mientras
 * se resuelve la sesión. Lectura síncrona desde [MainActivity]; escritura desde
 * el SplashViewModel al terminar (éxito o error).
 */
object SplashGate {
    @Volatile
    var keepOnScreen: Boolean = true
        private set

    fun release() {
        keepOnScreen = false
    }

    fun resetForTests() {
        keepOnScreen = true
    }
}
