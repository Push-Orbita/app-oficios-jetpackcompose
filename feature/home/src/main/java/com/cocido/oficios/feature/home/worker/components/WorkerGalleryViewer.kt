package com.cocido.oficios.feature.home.worker.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.domain.model.WorkGalleryItem
import com.cocido.oficios.feature.home.R
import com.cocido.oficios.feature.home.worker.GalleryGesture

@Composable
fun WorkerGalleryViewer(
    photos: List<WorkGalleryItem>,
    initialIndex: Int,
    onClose: () -> Unit,
) {
    if (photos.isEmpty()) return
    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, photos.lastIndex),
        pageCount = { photos.size },
    )
    val scales = remember { androidx.compose.runtime.mutableStateMapOf<Int, Float>() }
    val allowSwipe = (scales[pagerState.currentPage] ?: GalleryGesture.MIN_SCALE) <= 1.01f
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        BackHandler(onBack = onClose)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = allowSwipe,
            ) { page ->
                ZoomablePhoto(
                    url = photos[page].imageUrl,
                    contentDescription = stringResource(
                        R.string.home_worker_gallery_photo,
                        page + 1,
                        photos.size,
                    ),
                    active = page == pagerState.currentPage,
                    onScaleChanged = { scales[page] = it },
                )
            }
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(OficiosSpacing.xs)
                    .size(OficiosComponentSize.buttonHeight),
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.home_worker_gallery_close),
                    tint = Color.White,
                )
            }
            Text(
                text = stringResource(
                    R.string.home_worker_gallery_page,
                    pagerState.currentPage + 1,
                    photos.size,
                ),
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(bottom = OficiosSpacing.md)
                    .background(Color.White.copy(alpha = 0.22f), RoundedCornerShape(50))
                    .padding(horizontal = OficiosSpacing.sm, vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun ZoomablePhoto(
    url: String?,
    contentDescription: String,
    active: Boolean,
    onScaleChanged: (Float) -> Unit,
) {
    var scale by remember { mutableFloatStateOf(GalleryGesture.MIN_SCALE) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    fun applyScale(next: Float) {
        val coerced = next.coerceIn(GalleryGesture.MIN_SCALE, GalleryGesture.MAX_SCALE)
        scale = coerced
        if (coerced <= 1.01f) offset = Offset.Zero
        onScaleChanged(coerced)
    }

    LaunchedEffect(active) {
        if (!active) applyScale(GalleryGesture.MIN_SCALE)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                var lastTapUptime = 0L
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val downUptime = down.uptimeMillis
                    val downPosition = down.position
                    var moved = false
                    var pressedAny = true
                    while (pressedAny) {
                        val event = awaitPointerEvent()
                        val pressed = event.changes.count { it.pressed }
                        val zoom = event.calculateZoom()
                        val pan = event.calculatePan()
                        if ((event.changes.firstOrNull()?.position?.minus(downPosition)?.getDistance() ?: 0f) > viewConfiguration.touchSlop) {
                            moved = true
                        }
                        if (GalleryGesture.shouldConsumeDrag(pressed, scale)) {
                            val next = (scale * if (pressed >= 2) zoom else 1f)
                                .coerceIn(GalleryGesture.MIN_SCALE, GalleryGesture.MAX_SCALE)
                            val clamped = GalleryGesture.clampPan(
                                x = if (next <= 1.01f) 0f else offset.x + pan.x,
                                y = if (next <= 1.01f) 0f else offset.y + pan.y,
                                scale = next,
                                viewportWidth = size.width.toFloat(),
                                viewportHeight = size.height.toFloat(),
                            )
                            scale = next
                            offset = Offset(clamped.x, clamped.y)
                            onScaleChanged(next)
                            event.changes.forEach { change ->
                                if (change.positionChanged()) change.consume()
                            }
                        }
                        pressedAny = event.changes.any { it.pressed }
                    }
                    if (!moved && downUptime - lastTapUptime < viewConfiguration.doubleTapTimeoutMillis) {
                        if (scale > 1.01f) applyScale(GalleryGesture.MIN_SCALE) else applyScale(2.5f)
                        lastTapUptime = 0L
                    } else if (!moved) {
                        lastTapUptime = downUptime
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        SubcomposeAsyncImage(
            model = url,
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                },
        )
    }
}
