package com.cocido.oficios.core.ui.gallery

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.feedback.OficiosSkeleton
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.ui.R

@Composable
fun OficiosGalleryViewer(
    items: List<GalleryMedia>,
    initialIndex: Int,
    onClose: () -> Unit,
    onOpenExternal: (String) -> Unit = {},
) {
    if (items.isEmpty()) return
    val start = GalleryGesture.initialPage(initialIndex, items.size)
    val pagerState = rememberPagerState(initialPage = start, pageCount = { items.size })
    val scales = remember { mutableStateMapOf<Int, Float>() }
    val allowSwipe = (scales[pagerState.currentPage] ?: GalleryGesture.MIN_SCALE) <= GalleryGesture.REST_SCALE
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        BackHandler(onBack = onClose)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .testTag("gallery_viewer")
                .graphicsLayer { alpha = 1f },
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = allowSwipe,
            ) { page ->
                val item = items[page]
                if (item.kind == GalleryMediaKind.Pdf) {
                    PdfPage(
                        title = item.title,
                        url = item.url,
                        onOpen = { item.url?.let(onOpenExternal) },
                    )
                } else {
                    ZoomablePhoto(
                        url = item.url,
                        contentDescription = item.title ?: stringResource(
                            R.string.gallery_photo,
                            page + 1,
                            items.size,
                        ),
                        active = page == pagerState.currentPage,
                        onScaleChanged = { scales[page] = it },
                        onDismiss = onClose,
                    )
                }
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
                    contentDescription = stringResource(R.string.gallery_close),
                    tint = Color.White,
                )
            }
            Text(
                text = stringResource(
                    R.string.gallery_page,
                    pagerState.currentPage + 1,
                    items.size,
                ),
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(bottom = OficiosSpacing.md)
                    .background(Color.White.copy(alpha = 0.22f), RoundedCornerShape(50))
                    .padding(horizontal = OficiosSpacing.sm, vertical = OficiosSpacing.xs)
                    .testTag("gallery_page_indicator"),
            )
        }
    }
}

@Composable
private fun PdfPage(
    title: String?,
    url: String?,
    onOpen: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Outlined.Description,
            contentDescription = stringResource(R.string.gallery_pdf_cd),
            tint = Color.White,
            modifier = Modifier.size(OficiosComponentSize.iconLg * 2),
        )
        if (!title.isNullOrBlank()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.padding(OficiosSpacing.md),
            )
        }
        if (!url.isNullOrBlank()) {
            OficiosButton(
                text = stringResource(R.string.gallery_open_document),
                onClick = onOpen,
                variant = OficiosButtonVariant.Primary,
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
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var scale by remember { mutableFloatStateOf(GalleryGesture.MIN_SCALE) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var dismissY by remember { mutableFloatStateOf(0f) }

    fun applyScale(next: Float) {
        val coerced = next.coerceIn(GalleryGesture.MIN_SCALE, GalleryGesture.MAX_SCALE)
        scale = coerced
        if (coerced <= GalleryGesture.REST_SCALE) offset = Offset.Zero
        onScaleChanged(coerced)
    }

    LaunchedEffect(active) {
        if (!active) {
            applyScale(GalleryGesture.MIN_SCALE)
            dismissY = 0f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                translationY = dismissY
                alpha = (1f - (dismissY / 600f)).coerceIn(0.35f, 1f)
            }
            .pointerInput(Unit) {
                var lastTapUptime = 0L
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val downUptime = down.uptimeMillis
                    val downPosition = down.position
                    var moved = false
                    var pressedAny = true
                    var dragDismiss = 0f
                    while (pressedAny) {
                        val event = awaitPointerEvent()
                        val pressed = event.changes.count { it.pressed }
                        val zoom = event.calculateZoom()
                        val pan = event.calculatePan()
                        if ((event.changes.firstOrNull()?.position?.minus(downPosition)?.getDistance() ?: 0f) > viewConfiguration.touchSlop) {
                            moved = true
                        }
                        if (GalleryGesture.shouldDismissDrag(pressed, scale, pan.x, pan.y + dragDismiss)) {
                            dragDismiss = (dragDismiss + pan.y).coerceAtLeast(0f)
                            dismissY = dragDismiss
                            event.changes.forEach { change ->
                                if (change.positionChanged()) change.consume()
                            }
                        } else if (GalleryGesture.shouldConsumeDrag(pressed, scale)) {
                            val next = (scale * if (pressed >= 2) zoom else 1f)
                                .coerceIn(GalleryGesture.MIN_SCALE, GalleryGesture.MAX_SCALE)
                            val clamped = GalleryGesture.clampPan(
                                x = if (next <= GalleryGesture.REST_SCALE) 0f else offset.x + pan.x,
                                y = if (next <= GalleryGesture.REST_SCALE) 0f else offset.y + pan.y,
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
                    if (GalleryGesture.shouldCloseFromDismiss(dragDismiss)) {
                        onDismiss()
                    } else {
                        dismissY = 0f
                    }
                    if (!moved && downUptime - lastTapUptime < viewConfiguration.doubleTapTimeoutMillis) {
                        if (scale > GalleryGesture.REST_SCALE) applyScale(GalleryGesture.MIN_SCALE) else applyScale(2.5f)
                        lastTapUptime = 0L
                    } else if (!moved) {
                        lastTapUptime = downUptime
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(url)
                .size(Size(2048, 2048))
                .crossfade(true)
                .build(),
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
            loading = { OficiosSkeleton(modifier = Modifier.fillMaxSize()) },
        )
    }
}
