package com.cocido.oficios.core.ui.crop

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.chips.OficiosSegmentChip
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosColorWhite
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.ui.R

@Composable
fun ImageCropScreen(
    sourcePath: String,
    mode: ImageCropMode,
    onConfirm: (transform: ImageCropTransform, viewportWidth: Float, viewportHeight: Float) -> Unit,
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
    var viewport by remember { mutableStateOf(IntSize.Zero) }
    val imageBitmap = remember(sourceBitmap) { sourceBitmap.asImageBitmap() }
    val cropAreaDescription = stringResource(R.string.image_crop_area_cd)
    val photoDescription = stringResource(R.string.image_crop_photo_cd)

    fun currentTransform() = ImageCropTransform(
        rotationDegrees = rotation,
        scale = scale,
        panX = panX,
        panY = panY,
        aspect = aspect,
    )

    fun clampCurrentPan(size: IntSize = viewport) {
        if (size.width == 0 || size.height == 0) return
        val crop = cropWindow(size.width.toFloat(), size.height.toFloat(), aspect)
        val (rw, rh) = rotatedSize(sourceBitmap.width, sourceBitmap.height, rotation)
        val clamped = clampPan(panX, panY, rw, rh, crop, scale)
        panX = clamped.first
        panY = clamped.second
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
                if (mode.allowsFreeAspect()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm)) {
                        OficiosSegmentChip(
                            label = stringResource(R.string.image_crop_ratio_4_3),
                            selected = aspect == ImageCropAspect.Gallery43,
                            onClick = {
                                aspect = ImageCropAspect.Gallery43
                                panX = 0f
                                panY = 0f
                                clampCurrentPan()
                            },
                        )
                        OficiosSegmentChip(
                            label = stringResource(R.string.image_crop_free),
                            selected = aspect == ImageCropAspect.Free,
                            onClick = {
                                aspect = ImageCropAspect.Free
                                panX = 0f
                                panY = 0f
                                clampCurrentPan()
                            },
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
                ) {
                    OficiosButton(
                        text = stringResource(R.string.image_crop_reset),
                        onClick = {
                            rotation = 0
                            scale = 1f
                            panX = 0f
                            panY = 0f
                            aspect = mode.defaultAspect()
                        },
                        modifier = Modifier.weight(1f),
                        variant = OficiosButtonVariant.Outlined,
                        size = OficiosButtonSize.Small,
                    )
                    OficiosButton(
                        text = stringResource(R.string.image_crop_rotate),
                        onClick = {
                            rotation = rotateClockwise(rotation)
                            panX = 0f
                            panY = 0f
                            clampCurrentPan()
                        },
                        modifier = Modifier.weight(1f),
                        variant = OficiosButtonVariant.Outlined,
                        size = OficiosButtonSize.Small,
                    )
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
                        onClick = {
                            onConfirm(
                                currentTransform(),
                                viewport.width.toFloat(),
                                viewport.height.toFloat(),
                            )
                        },
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
                    .onSizeChanged {
                        viewport = it
                        clampCurrentPan(it)
                    }
                    .pointerInput(aspect, rotation) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = clampScale(scale * zoom)
                            panX += pan.x
                            panY += pan.y
                            clampCurrentPan()
                        }
                    },
            ) {
                val vp = IntSize(constraints.maxWidth, constraints.maxHeight)
                if (vp.width == 0 || vp.height == 0) return@BoxWithConstraints
                val crop = cropWindow(vp.width.toFloat(), vp.height.toFloat(), aspect)
                val (rotatedW, rotatedH) = rotatedSize(sourceBitmap.width, sourceBitmap.height, rotation)
                val (clampedX, clampedY) = clampPan(panX, panY, rotatedW, rotatedH, crop, scale)
                val drawScale = coverScale(
                    rotatedW,
                    rotatedH,
                    crop.width.toFloat(),
                    crop.height.toFloat(),
                ) * clampScale(scale)
                val displayedW = rotatedW * drawScale
                val displayedH = rotatedH * drawScale
                val imgLeft = (crop.left + crop.right) / 2f - displayedW / 2f + clampedX
                val imgTop = (crop.top + crop.bottom) / 2f - displayedH / 2f + clampedY
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .semantics { contentDescription = photoDescription },
                ) {
                    translate(imgLeft + displayedW / 2f, imgTop + displayedH / 2f) {
                        rotate(rotation.toFloat(), pivot = Offset.Zero) {
                            scale(drawScale, pivot = Offset.Zero) {
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
                    circular = mode.circularMask() && aspect == ImageCropAspect.Square,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun CropOverlay(
    crop: PixelRect,
    circular: Boolean,
    modifier: Modifier = Modifier,
) {
    Canvas(
        modifier = modifier.graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen },
    ) {
        drawRect(Color.Black.copy(alpha = 0.55f))
        val left = crop.left.toFloat()
        val top = crop.top.toFloat()
        val width = crop.width.toFloat()
        val height = crop.height.toFloat()
        if (circular) {
            drawCircle(
                color = Color.Transparent,
                radius = minOf(width, height) / 2f,
                center = Offset(left + width / 2f, top + height / 2f),
                blendMode = BlendMode.Clear,
            )
            drawCircle(
                color = OficiosColorWhite,
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
                color = OficiosColorWhite,
                topLeft = Offset(left, top),
                size = Size(width, height),
                style = Stroke(width = 2.dp.toPx()),
            )
        }
    }
}
