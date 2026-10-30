package com.cocido.oficios.core.ui.map

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange

/**
 * El mapa vive dentro de formularios con scroll: sin esto, un arrastre vertical lo toma la
 * pantalla y el mapa se siente "duro" (no se puede mover hacia arriba/abajo, el pin salta).
 *
 * El `GoogleMap` (AndroidView) recibe el evento primero en el pase Main; acá, ya en el
 * ancestro, se consumen los movimientos para que el scroll de la pantalla no arranque. El
 * toque inicial no se consume: los taps sobre el mapa siguen funcionando.
 */
internal fun Modifier.keepGesturesInsideMap(): Modifier = pointerInput(Unit) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false)
        do {
            val event = awaitPointerEvent()
            event.changes.forEach { change ->
                if (change.positionChange() != Offset.Zero) change.consume()
            }
        } while (event.changes.any { it.pressed })
    }
}
