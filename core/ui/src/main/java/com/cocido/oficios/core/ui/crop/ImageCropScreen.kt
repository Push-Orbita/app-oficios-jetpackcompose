package com.cocido.oficios.core.ui.crop

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.component.rememberOficiosIcon
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.interaction.OficiosTouchTarget
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.ui.R
import kotlin.math.roundToInt

@Composable
fun ImageCropScreen(
    sourcePath: String,
    mode: ImageCropMode,
    onConfirm: (transform: ImageCropTransform) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sourceBitmap = remember(sourcePath) { BitmapFactory.decodeFile(sourcePath) }
    DisposableEffect(sourceBitmap) {
        onDispose { if (sourceBitmap != null && !sourceBitmap.isRecycled) sourceBitmap.recycle() }
    }
    LaunchedEffect(sourceBitmap) {
        if (sourceBitmap == null) onCancel()
    }
    if (sourceBitmap == null) return

    var aspect by remember(mode) { mutableStateOf(mode.defaultAspect()) }
    var rotation by remember { mutableIntStateOf(0) }
    var scale by remember { mutableFloatStateOf(1f) }
    var panX by remember { mutableFloatStateOf(0f) }
    var panY by remember { mutableFloatStateOf(0f) }
    var crop by remember {
        mutableStateOf(initialCropRect(sourceBitmap.width, sourceBitmap.height, mode.defaultAspect()))
    }
    var draggingHandle by remember { mutableStateOf<CropHandle?>(null) }
    var viewport by remember { mutableStateOf(IntSize.Zero) }
    val imageBitmap = remember(sourceBitmap) { sourceBitmap.asImageBitmap() }
    val density = LocalDensity.current
    val touchRadius = with(density) { OficiosComponentSize.minTouchTarget.toPx() }
    val paddingPx = with(density) { OficiosSpacing.md.toPx() }
    val cropAreaDescription = stringResource(R.string.image_crop_area_cd)
    val photoDescription = stringResource(R.string.image_crop_photo_cd)

    fun rotatedBounds() = rotatedSize(sourceBitmap.width, sourceBitmap.height, rotation)

    fun currentTransform() = ImageCropTransform(
        rotationDegrees = rotation,
        scale = scale,
        panX = panX,
        panY = panY,
        aspect = aspect,
        crop = crop,
    )

    fun resetCropToAspect(nextAspect: ImageCropAspect, nextRotation: Int = rotation) {
        aspect = nextAspect
        rotation = nextRotation
        scale = 1f
        panX = 0f
        panY = 0f
        crop = cropAfterRotation(sourceBitmap.width, sourceBitmap.height, nextRotation, nextAspect)
    }

    OficiosScaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            OficiosBackTopBar(
                title = stringResource(R.string.image_crop_title),
                onBack = onCancel,
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = OficiosSpacing.md)
                    .padding(bottom = OficiosSpacing.md),
                verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
                ) {
                    CropToolButton(
                        icon = OficiosIcons.RotateCw,
                        label = stringResource(R.string.image_crop_rotate),
                        onClick = {
                            resetCropToAspect(aspect, rotateClockwise(rotation))
                        },
                        modifier = Modifier.weight(1f),
                    )
                    CropToolButton(
                        icon = OficiosIcons.RefreshCw,
                        label = stringResource(R.string.image_crop_reset),
                        onClick = { resetCropToAspect(mode.defaultAspect(), 0) },
                        modifier = Modifier.weight(1f),
                    )
                    if (mode.allowsFreeAspect()) {
                        CropToolButton(
                            label = stringResource(R.string.image_crop_ratio_4_3),
                            selected = aspect == ImageCropAspect.Gallery43,
                            onClick = { resetCropToAspect(ImageCropAspect.Gallery43) },
                            modifier = Modifier.weight(1f),
                        )
                        CropToolButton(
                            label = stringResource(R.string.image_crop_free),
                            selected = aspect == ImageCropAspect.Free,
                            onClick = { resetCropToAspect(ImageCropAspect.Free) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
                ) {
                    OficiosButton(
                        text = stringResource(R.string.image_crop_cancel),
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        variant = OficiosButtonVariant.Ghost,
                        size = OficiosButtonSize.Small,
                    )
                    OficiosButton(
                        text = stringResource(R.string.image_crop_use),
                        onClick = { onConfirm(currentTransform()) },
                        modifier = Modifier.weight(1f),
                        variant = OficiosButtonVariant.Primary,
                        size = OficiosButtonSize.Small,
                        enabled = viewport.width > 0,
                    )
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Text(
                text = stringResource(R.string.image_crop_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = OficiosSpacing.md, vertical = OficiosSpacing.sm),
            )
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .semantics { contentDescription = cropAreaDescription }
                    .onSizeChanged { viewport = it }
                    .pointerInput(aspect, rotation) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val (rw, rh) = rotatedBounds()
                            val layout = fittedImageLayout(
                                viewportWidth = size.width.toFloat(),
                                viewportHeight = size.height.toFloat(),
                                rotatedWidth = rw,
                                rotatedHeight = rh,
                                userScale = scale,
                                panX = panX,
                                panY = panY,
                                padding = paddingPx,
                            )
                            val handle = hitCropHandle(
                                screenX = down.position.x,
                                screenY = down.position.y,
                                crop = crop,
                                layout = layout,
                                touchRadius = touchRadius,
                            )
                            if (handle != null) {
                                draggingHandle = handle
                                val minSize = maxOf(
                                    cropMinSize(rw, rh),
                                    (touchRadius / layout.drawScale).roundToInt(),
                                )
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull() ?: break
                                    if (!change.pressed) break
                                    val latestLayout = fittedImageLayout(
                                        viewportWidth = size.width.toFloat(),
                                        viewportHeight = size.height.toFloat(),
                                        rotatedWidth = rw,
                                        rotatedHeight = rh,
                                        userScale = scale,
                                        panX = panX,
                                        panY = panY,
                                        padding = paddingPx,
                                    )
                                    crop = resizeCrop(
                                        crop = crop,
                                        handle = handle,
                                        pointerImageX = screenToImageX(change.position.x, latestLayout),
                                        pointerImageY = screenToImageY(change.position.y, latestLayout),
                                        imageWidth = rw,
                                        imageHeight = rh,
                                        aspect = aspect,
                                        minSize = minSize,
                                    )
                                    change.consume()
                                }
                                draggingHandle = null
                            } else {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val pressed = event.changes.filter { it.pressed }
                                    if (pressed.isEmpty()) break
                                    val zoomChange = event.calculateZoom()
                                    val panChange = event.calculatePan()
                                    if (pressed.size >= 2 || zoomChange != 1f) {
                                        scale = clampUserScale(scale * zoomChange)
                                        panX += panChange.x
                                        panY += panChange.y
                                    } else {
                                        val imageX = screenToImageX(pressed[0].position.x, layout)
                                        val imageY = screenToImageY(pressed[0].position.y, layout)
                                        if (crop.contains(imageX, imageY)) {
                                            val (currentRw, currentRh) = rotatedBounds()
                                            crop = moveCrop(
                                                crop = crop,
                                                deltaX = panChange.x / layout.drawScale,
                                                deltaY = panChange.y / layout.drawScale,
                                                imageWidth = currentRw,
                                                imageHeight = currentRh,
                                            )
                                        } else {
                                            panX += panChange.x
                                            panY += panChange.y
                                        }
                                    }
                                    pressed.forEach { it.consume() }
                                }
                            }
                        }
                    },
            ) {
                val vp = IntSize(constraints.maxWidth, constraints.maxHeight)
                if (vp.width == 0 || vp.height == 0) return@BoxWithConstraints
                val (rotatedW, rotatedH) = rotatedBounds()
                val layout = fittedImageLayout(
                    viewportWidth = vp.width.toFloat(),
                    viewportHeight = vp.height.toFloat(),
                    rotatedWidth = rotatedW,
                    rotatedHeight = rotatedH,
                    userScale = scale,
                    panX = panX,
                    panY = panY,
                    padding = paddingPx,
                )
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .semantics { contentDescription = photoDescription },
                ) {
                    translate(layout.left + layout.width / 2f, layout.top + layout.height / 2f) {
                        rotate(rotation.toFloat(), pivot = Offset.Zero) {
                            scale(layout.drawScale, pivot = Offset.Zero) {
                                drawImage(
                                    image = imageBitmap,
                                    topLeft = Offset(
                                        -sourceBitmap.width / 2f,
                                        -sourceBitmap.height / 2f,
                                    ),
                                )
                            }
                        }
                    }
                }
                CropOverlay(
                    crop = crop,
                    layout = layout,
                    circular = mode.circularMask() && aspect == ImageCropAspect.Square,
                    showGrid = draggingHandle != null,
                    modifier = Modifier.fillMaxSize(),
                )
                CropHandleMarkers(
                    crop = crop,
                    layout = layout,
                    touchPx = touchRadius,
                )
            }
        }
    }
}

@Composable
private fun CropToolButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Int? = null,
    selected: Boolean = false,
) {
    val container = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    OficiosTouchTarget(
        onClick = onClick,
        modifier = modifier,
        accessibilityText = label,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(container, RoundedCornerShape(8.dp))
                .padding(vertical = OficiosSpacing.xs),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            if (icon != null) {
                Icon(
                    imageVector = rememberOficiosIcon(icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(OficiosComponentSize.iconMd),
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun CropOverlay(
    crop: PixelRect,
    layout: FittedImageLayout,
    circular: Boolean,
    showGrid: Boolean,
    modifier: Modifier = Modifier,
) {
    val handleColor = MaterialTheme.colorScheme.onSurface
    Canvas(
        modifier = modifier.graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen },
    ) {
        drawRect(Color.Black.copy(alpha = 0.55f))
        val left = imageToScreenX(crop.left.toFloat(), layout)
        val top = imageToScreenY(crop.top.toFloat(), layout)
        val right = imageToScreenX(crop.right.toFloat(), layout)
        val bottom = imageToScreenY(crop.bottom.toFloat(), layout)
        val width = (right - left).coerceAtLeast(1f)
        val height = (bottom - top).coerceAtLeast(1f)
        if (circular) {
            drawCircle(
                color = Color.Transparent,
                radius = minOf(width, height) / 2f,
                center = Offset(left + width / 2f, top + height / 2f),
                blendMode = BlendMode.Clear,
            )
            drawCircle(
                color = handleColor,
                radius = minOf(width, height) / 2f,
                center = Offset(left + width / 2f, top + height / 2f),
                style = Stroke(width = 2.dp.toPx()),
            )
        } else {
            drawRect(
                color = Color.Transparent,
                topLeft = Offset(left, top),
                size = Size(width, height),
                blendMode = BlendMode.Clear,
            )
            drawRect(
                color = handleColor,
                topLeft = Offset(left, top),
                size = Size(width, height),
                style = Stroke(width = 2.dp.toPx()),
            )
        }
        if (showGrid) {
            val thirdX = width / 3f
            val thirdY = height / 3f
            val grid = handleColor.copy(alpha = 0.7f)
            val stroke = Stroke(width = 1.dp.toPx())
            drawLine(grid, Offset(left + thirdX, top), Offset(left + thirdX, bottom), strokeWidth = stroke.width)
            drawLine(grid, Offset(left + thirdX * 2, top), Offset(left + thirdX * 2, bottom), strokeWidth = stroke.width)
            drawLine(grid, Offset(left, top + thirdY), Offset(right, top + thirdY), strokeWidth = stroke.width)
            drawLine(grid, Offset(left, top + thirdY * 2), Offset(right, top + thirdY * 2), strokeWidth = stroke.width)
        }
        val handle = 12.dp.toPx()
        val edge = 18.dp.toPx()
        val edgeThick = 4.dp.toPx()
        val cx = left + width / 2f
        val cy = top + height / 2f
        fun corner(x: Float, y: Float) {
            drawRect(
                color = handleColor,
                topLeft = Offset(x - handle / 2f, y - handle / 2f),
                size = Size(handle, handle),
            )
        }
        fun edgeH(x: Float, y: Float) {
            drawRect(
                color = handleColor,
                topLeft = Offset(x - edge / 2f, y - edgeThick / 2f),
                size = Size(edge, edgeThick),
            )
        }
        fun edgeV(x: Float, y: Float) {
            drawRect(
                color = handleColor,
                topLeft = Offset(x - edgeThick / 2f, y - edge / 2f),
                size = Size(edgeThick, edge),
            )
        }
        corner(left, top)
        corner(right, top)
        corner(left, bottom)
        corner(right, bottom)
        edgeH(cx, top)
        edgeH(cx, bottom)
        edgeV(left, cy)
        edgeV(right, cy)
    }
}

@Composable
private fun CropHandleMarkers(
    crop: PixelRect,
    layout: FittedImageLayout,
    touchPx: Float,
) {
    val left = imageToScreenX(crop.left.toFloat(), layout)
    val top = imageToScreenY(crop.top.toFloat(), layout)
    val right = imageToScreenX(crop.right.toFloat(), layout)
    val bottom = imageToScreenY(crop.bottom.toFloat(), layout)
    val cx = (left + right) / 2f
    val cy = (top + bottom) / 2f
    val descriptions = mapOf(
        CropHandle.TopLeft to stringResource(R.string.image_crop_handle_top_left),
        CropHandle.TopRight to stringResource(R.string.image_crop_handle_top_right),
        CropHandle.BottomLeft to stringResource(R.string.image_crop_handle_bottom_left),
        CropHandle.BottomRight to stringResource(R.string.image_crop_handle_bottom_right),
        CropHandle.Top to stringResource(R.string.image_crop_handle_top),
        CropHandle.Bottom to stringResource(R.string.image_crop_handle_bottom),
        CropHandle.Left to stringResource(R.string.image_crop_handle_left),
        CropHandle.Right to stringResource(R.string.image_crop_handle_right),
    )
    val points = listOf(
        CropHandle.TopLeft to Offset(left, top),
        CropHandle.TopRight to Offset(right, top),
        CropHandle.BottomLeft to Offset(left, bottom),
        CropHandle.BottomRight to Offset(right, bottom),
        CropHandle.Top to Offset(cx, top),
        CropHandle.Bottom to Offset(cx, bottom),
        CropHandle.Left to Offset(left, cy),
        CropHandle.Right to Offset(right, cy),
    )
    val half = touchPx / 2f
    points.forEach { (handle, point) ->
        Box(
            modifier = Modifier
                .offset {
                    IntOffset((point.x - half).roundToInt(), (point.y - half).roundToInt())
                }
                .size(with(LocalDensity.current) { touchPx.toDp() })
                .semantics { contentDescription = descriptions.getValue(handle) },
        )
    }
}
