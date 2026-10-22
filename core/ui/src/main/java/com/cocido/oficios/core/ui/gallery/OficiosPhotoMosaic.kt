package com.cocido.oficios.core.ui.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.cocido.oficios.core.designsystem.component.feedback.OficiosSkeleton
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosRadius
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.ui.R

@Composable
fun OficiosPhotoMosaic(
    items: List<GalleryMedia>,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    previewLimit: Int? = null,
    emptyText: String? = null,
    seeAllLabel: (@Composable (Int) -> String)? = null,
    onSeeAll: (() -> Unit)? = null,
    onDelete: ((Int) -> Unit)? = null,
    deleteLabel: String? = null,
    onEdit: ((Int) -> Unit)? = null,
    editLabel: String? = null,
) {
    val slice = mosaicSlice(items.size, previewLimit)
    val visible = items.take(slice.visibleCount)
    val hidden = slice.hiddenCount
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
    ) {
        if (visible.isEmpty()) {
            if (emptyText != null) {
                Text(
                    text = emptyText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        } else {
            GalleryTile(
                media = visible[0],
                index = 0,
                featured = true,
                overflowCount = if (visible.size == 1 && hidden > 0) hidden else 0,
                onClick = { onItemClick(0) },
                onDelete = onDelete?.let { action -> { action(0) } },
                deleteLabel = deleteLabel,
                onEdit = onEdit?.let { action -> { action(0) } },
                editLabel = editLabel,
            )
            visible.drop(1).chunked(2).forEachIndexed { rowIndex, row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
                ) {
                    row.forEachIndexed { col, media ->
                        val index = 1 + rowIndex * 2 + col
                        val isLastVisible = index == visible.lastIndex && hidden > 0
                        GalleryTile(
                            media = media,
                            index = index,
                            featured = false,
                            overflowCount = if (isLastVisible) hidden else 0,
                            onClick = { onItemClick(index) },
                            onDelete = onDelete?.let { action -> { action(index) } },
                            deleteLabel = deleteLabel,
                            onEdit = onEdit?.let { action -> { action(index) } },
                            editLabel = editLabel,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
        if (hidden > 0 && seeAllLabel != null && onSeeAll != null) {
            Text(
                text = seeAllLabel(items.size),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable(onClick = onSeeAll)
                    .padding(vertical = OficiosSpacing.xs),
            )
        }
    }
}

@Composable
fun GalleryTile(
    media: GalleryMedia,
    index: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    featured: Boolean = false,
    overflowCount: Int = 0,
    onDelete: (() -> Unit)? = null,
    deleteLabel: String? = null,
    onEdit: (() -> Unit)? = null,
    editLabel: String? = null,
) {
    val shape = RoundedCornerShape(OficiosRadius.lg)
    val context = LocalContext.current
    val description = media.title ?: stringResource(R.string.gallery_thumb_cd, index + 1)
    Box(
        modifier = modifier
            .then(if (featured) Modifier.fillMaxWidth().height(200.dp) else Modifier.aspectRatio(1f))
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) { contentDescription = description },
    ) {
        when {
            media.kind == GalleryMediaKind.Pdf -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(OficiosSpacing.sm),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(OficiosComponentSize.iconLg),
                    )
                    if (!media.title.isNullOrBlank()) {
                        Text(
                            text = media.title,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            media.url.isViewableImage() -> {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(media.url)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = {
                        OficiosSkeleton(modifier = Modifier.fillMaxSize(), shape = shape)
                    },
                    error = {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Image,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(OficiosComponentSize.iconLg),
                            )
                        }
                    },
                )
            }
            else -> {
                Icon(
                    imageVector = Icons.Outlined.Image,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(OficiosComponentSize.iconLg),
                )
            }
        }
        if (overflowCount > 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "+$overflowCount",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
        if (!media.badge.isNullOrBlank()) {
            Text(
                text = media.badge,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.92f))
                    .padding(horizontal = OficiosSpacing.xs, vertical = OficiosSpacing.xxs),
            )
        }
        if (onEdit != null || onDelete != null) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(OficiosSpacing.xxs),
                horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xxs),
            ) {
                if (onEdit != null) {
                    TileActionButton(
                        onClick = onEdit,
                        contentDescription = editLabel ?: stringResource(R.string.gallery_edit),
                        icon = Icons.Outlined.Edit,
                    )
                }
                if (onDelete != null) {
                    TileActionButton(
                        onClick = onDelete,
                        contentDescription = deleteLabel ?: stringResource(R.string.gallery_delete),
                        icon = Icons.Outlined.DeleteOutline,
                    )
                }
            }
        }
    }
}

@Composable
private fun TileActionButton(
    onClick: () -> Unit,
    contentDescription: String,
    icon: ImageVector,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(OficiosComponentSize.iconLg)
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f), CircleShape),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(OficiosComponentSize.iconSm),
        )
    }
}
