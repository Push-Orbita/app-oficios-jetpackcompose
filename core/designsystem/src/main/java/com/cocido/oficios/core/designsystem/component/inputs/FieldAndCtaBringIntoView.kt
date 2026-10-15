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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import kotlin.math.min
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Alto visible del scroll, medido después de `imePadding()`. */
val LocalScrollViewportHeightPx = staticCompositionLocalOf { 0 }

@OptIn(ExperimentalFoundationApi::class)
internal val LocalFieldAndCtaBringIntoView = staticCompositionLocalOf<FieldAndCtaBringIntoView?> { null }

/**
 * Lleva a la vista el tramo que va del campo enfocado al CTA.
 * Si no entra en el alto libre sobre el teclado, alinea el borde inferior (el CTA).
 */
@OptIn(ExperimentalFoundationApi::class)
internal class FieldAndCtaBringIntoView(
    val requester: BringIntoViewRequester,
    private val scope: CoroutineScope,
) {
    var viewportHeightPx: Int = 0
    var focused by mutableStateOf(false)
    private var blockSize: IntSize = IntSize.Zero
    private var blockCoordinates: LayoutCoordinates? = null
    private var focusedTopPx: Float = 0f

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
        }
    }

    suspend fun bringIntoView() {
        val width = blockSize.width
        val height = blockSize.height
        if (width <= 0 || height <= 0) {
            requester.bringIntoView()
            return
        }
        val bottom = height.toFloat()
        val fieldTop = focusedTopPx.coerceIn(0f, bottom)
        val span = (bottom - fieldTop).coerceAtLeast(1f)
        val viewport = viewportHeightPx
        val visible = if (viewport > 1) min(span, viewport - 1f) else span
        val top = (bottom - visible).coerceAtLeast(0f)
        requester.bringIntoView(
            Rect(
                left = 0f,
                top = top,
                right = width.toFloat(),
                bottom = bottom,
            ),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun rememberFieldAndCtaBringIntoView(): FieldAndCtaBringIntoView {
    val requester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    val state = remember(requester) { FieldAndCtaBringIntoView(requester, scope) }
    val viewport = LocalScrollViewportHeightPx.current
    SideEffect { state.viewportHeightPx = viewport }
    LaunchedEffect(viewport) {
        state.viewportHeightPx = viewport
        if (state.focused) state.bringIntoView()
    }
    return state
}

/** Columna del campo (o campos) y el CTA. El borde inferior de este bloque es el botón. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FieldAndCtaBlock(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val state = rememberFieldAndCtaBringIntoView()
    CompositionLocalProvider(LocalFieldAndCtaBringIntoView provides state) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .bringIntoViewRequester(state.requester)
                .onGloballyPositioned { state.onBlockPositioned(it) },
            verticalArrangement = Arrangement.spacedBy(OficiosSpacing.lg),
            content = content,
        )
    }
}
