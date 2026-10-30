package com.cocido.oficios.core.designsystem.component.inputs

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Alto visible del scroll medido después de `imePadding()`.
 *
 * Mutar [px] **no** dispara recomposición: el IME anima el alto en muchos frames;
 * si se publicara vía CompositionLocal `Int`, cada tick invalidaría el árbol y
 * re-dispararía [bringIntoView] (lag / bajos FPS en login).
 */
class ScrollViewportHeight {
    var px: Int = 0
}

val LocalScrollViewportHeightPx = staticCompositionLocalOf { ScrollViewportHeight() }

@OptIn(ExperimentalFoundationApi::class)
internal val LocalFieldAndCtaBringIntoView = staticCompositionLocalOf<FieldAndCtaBringIntoView?> { null }

/**
 * Lleva a la vista el tramo que va del campo enfocado al CTA.
 * Si no entra el bloque completo en el viewport, prioriza que el campo enfocado quede visible;
 * el CTA sigue alcanzable con scroll.
 */
@OptIn(ExperimentalFoundationApi::class)
internal class FieldAndCtaBringIntoView(
    val requester: BringIntoViewRequester,
    private val scope: CoroutineScope,
) {
    /** Lectura lazy del viewport (holder) para no cachear un alto a mitad de animación IME. */
    var viewportPx: () -> Int = { 0 }
    var focused by mutableStateOf(false)
    private var blockSize: IntSize = IntSize.Zero
    private var blockCoordinates: LayoutCoordinates? = null
    private var focusedTopPx: Float = 0f
    private var focusedHeightPx: Float = 0f
    var fieldMarginPx: Float = 0f

    fun onBlockPositioned(coordinates: LayoutCoordinates) {
        blockCoordinates = coordinates
        blockSize = coordinates.size
    }

    fun onFieldFocus(isFocused: Boolean, field: LayoutCoordinates?) {
        focused = isFocused
        if (!isFocused) return
        updateFocusedTop(field)
        request()
    }

    fun request() {
        scope.launch { bringIntoView() }
    }

    private fun updateFocusedTop(field: LayoutCoordinates?) {
        val block = blockCoordinates
        if (field != null && block != null && block.isAttached && field.isAttached) {
            focusedTopPx = block.localPositionOf(field, Offset.Zero).y.coerceAtLeast(0f)
            focusedHeightPx = field.size.height.toFloat().coerceAtLeast(0f)
        }
    }

    suspend fun bringIntoView() {
        val coords = blockCoordinates
        if (coords == null || !coords.isAttached) return

        val width = blockSize.width
        val height = blockSize.height
        if (width <= 0 || height <= 0) {
            runCatching { requester.bringIntoView() }
            return
        }
        val rect = fieldFocusedBringIntoViewRect(
            blockWidth = width,
            blockHeight = height,
            fieldTopPx = focusedTopPx,
            fieldHeightPx = focusedHeightPx,
            fieldMarginPx = fieldMarginPx,
            viewportHeightPx = viewportPx(),
        )
        runCatching { requester.bringIntoView(rect) }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun rememberFieldAndCtaBringIntoView(): FieldAndCtaBringIntoView {
    val requester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    val state = remember(requester) { FieldAndCtaBringIntoView(requester, scope) }
    val density = LocalDensity.current
    val viewportHolder = LocalScrollViewportHeightPx.current
    state.viewportPx = { viewportHolder.px }
    state.fieldMarginPx = with(density) { OficiosSpacing.sm.toPx() }

    // Poll del holder (sin CompositionLocal Int ni WindowInsets.ime en composition):
    // bringIntoView solo cuando el alto del viewport se asienta tras abrir/cerrar IME.
    LaunchedEffect(state, viewportHolder) {
        var lastSeen = viewportHolder.px
        var lastSettled = viewportHolder.px
        var stableMs = 0L
        while (isActive) {
            delay(VIEWPORT_POLL_MS)
            val now = viewportHolder.px
            if (now != lastSeen) {
                lastSeen = now
                stableMs = 0L
            } else {
                stableMs += VIEWPORT_POLL_MS
                if (stableMs >= IME_SETTLE_DEBOUNCE_MS && now != lastSettled) {
                    lastSettled = now
                    if (state.focused) state.bringIntoView()
                }
            }
        }
    }
    return state
}

/** Columna del campo (o campos) y el CTA. El borde inferior de este bloque es el botón. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FieldAndCtaBlock(
    modifier: Modifier = Modifier,
    spacing: Dp = OficiosSpacing.lg,
    content: @Composable ColumnScope.() -> Unit,
) {
    val state = rememberFieldAndCtaBringIntoView()
    CompositionLocalProvider(LocalFieldAndCtaBringIntoView provides state) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .bringIntoViewRequester(state.requester)
                .onGloballyPositioned { state.onBlockPositioned(it) },
            verticalArrangement = Arrangement.spacedBy(spacing),
            content = content,
        )
    }
}

private const val VIEWPORT_POLL_MS = 16L
private const val IME_SETTLE_DEBOUNCE_MS = 64L
