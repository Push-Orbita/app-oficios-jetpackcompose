package com.cocido.oficios.core.designsystem.component.inputs

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import kotlinx.coroutines.delay

/**
 * Lleva el foco (o el scroll) al primer campo inválido tras un intento de envío.
 * Campos de texto: [focusRequester] + IME/[BringIntoViewRequester] interno de [OficiosTextField].
 * Bloques no focuseables (grillas, checkboxes, términos): [bringIntoViewRequester].
 *
 * Uso:
 * 1. `val navigator = rememberFirstInvalidFieldNavigator()`
 * 2. En cada campo: `focusRequester = navigator.focusRequester(FieldKey.Email)`
 *    o `Modifier.anchorFirstInvalid(navigator, FieldKey.Terms)`
 * 3. `FocusFirstInvalidEffect(attemptId, invalidKeysOrdenados, navigator)`
 */
@Stable
@OptIn(ExperimentalFoundationApi::class)
class FirstInvalidFieldNavigator {
    private val focusByKey = linkedMapOf<Any, FocusRequester>()
    private val bringByKey = linkedMapOf<Any, BringIntoViewRequester>()

    fun focusRequester(key: Any): FocusRequester =
        focusByKey.getOrPut(key) { FocusRequester() }

    fun bringIntoViewRequester(key: Any): BringIntoViewRequester =
        bringByKey.getOrPut(key) { BringIntoViewRequester() }

    suspend fun focusFirst(invalidKeys: List<Any>) {
        val key = invalidKeys.firstOrNull {
            focusByKey.containsKey(it) || bringByKey.containsKey(it)
        } ?: return
        withFrameNanos {}
        delay(FOCUS_SETTLE_MS)
        withFrameNanos {}
        focusByKey[key]?.let { runCatching { it.requestFocus() } }
        bringByKey[key]?.let { runCatching { it.bringIntoView() } }
    }
}

@Composable
fun rememberFirstInvalidFieldNavigator(): FirstInvalidFieldNavigator =
    remember { FirstInvalidFieldNavigator() }

/**
 * Dispara foco/scroll al primer inválido cuando [attemptId] cambia (> 0).
 * [invalidKeys] debe estar en orden visual de arriba hacia abajo.
 */
@Composable
fun FocusFirstInvalidEffect(
    attemptId: Int,
    invalidKeys: List<Any>,
    navigator: FirstInvalidFieldNavigator,
) {
    LaunchedEffect(attemptId) {
        if (attemptId <= 0 || invalidKeys.isEmpty()) return@LaunchedEffect
        navigator.focusFirst(invalidKeys)
    }
}

/** Ancla de scroll para secciones sin FocusRequester (grillas, checkboxes, términos). */
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.anchorFirstInvalid(
    navigator: FirstInvalidFieldNavigator,
    key: Any,
): Modifier = bringIntoViewRequester(navigator.bringIntoViewRequester(key))

private const val FOCUS_SETTLE_MS = 48L
