package com.cocido.oficios.core.common.splash

/**
 * El splash de sistema ([androidx.core.splashscreen]) se mantiene visible mientras
 * se resuelve la sesión en un arranque en frío. Lectura síncrona desde [MainActivity];
 * escritura desde el SplashViewModel al terminar (éxito o error), o al restaurar
 * una Activity cuyo NavHost ya tiene destino (process death).
 *
 * En process death el proceso nuevo reinicia [keepOnScreen] en true, pero el
 * NavController restaura el wizard (u otra pantalla) y no vuelve a componer
 * SplashRoute, así que SplashViewModel nunca llama a [release]. Si no se libera
 * acá, el starting window queda clavado sobre la UI restaurada.
 */
object SplashGate {
    @Volatile
    var keepOnScreen: Boolean = true
        private set

    fun release() {
        keepOnScreen = false
    }

    /**
     * [hasSavedInstanceState] es true en recreation (rotación o process death
     * con estado). En rotación del mismo proceso [keepOnScreen] ya es false;
     * en process death hay que liberarlo para no bloquear el splash de sistema.
     */
    fun onActivityCreated(hasSavedInstanceState: Boolean) {
        if (hasSavedInstanceState) {
            release()
        }
    }

    fun resetForTests() {
        keepOnScreen = true
    }
}
