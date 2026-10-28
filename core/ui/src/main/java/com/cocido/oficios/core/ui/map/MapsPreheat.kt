package com.cocido.oficios.core.ui.map

import android.content.Context
import com.google.android.gms.maps.MapsInitializer
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Inicializa el renderer de Maps en background para que el primer GoogleMap
 * de Paso 2 no pague los ~8–15 s de arranque en frío del SDK.
 */
object MapsPreheat {
    private val started = AtomicBoolean(false)

    fun start(context: Context) {
        if (!started.compareAndSet(false, true)) return
        val app = context.applicationContext
        Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "maps-preheat").apply { isDaemon = true }
        }.execute {
            try {
                MapsInitializer.initialize(app, MapsInitializer.Renderer.LATEST) { }
            } catch (_: Exception) {
                started.set(false)
            }
        }
    }
}
