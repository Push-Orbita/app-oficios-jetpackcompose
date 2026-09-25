package com.cocido.oficios.core.designsystem.component.avatar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

@Composable
fun OficiosAvatar(
    name: String,
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    size: Dp = OficiosComponentSize.avatarMd,
    showBorder: Boolean = false,
    circular: Boolean = false,
) {
    val shape = if (circular) CircleShape else RoundedCornerShape(OficiosSpacing.sm)
    val clippedModifier = modifier
        .size(size)
        .clip(shape)
        .then(
            if (showBorder) {
                Modifier.border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = shape,
                )
            } else {
                Modifier
            },
        )

    if (!imageUrl.isNullOrBlank()) {
        SubcomposeAsyncImage(
            model = imageUrl,
            contentDescription = name,
            contentScale = ContentScale.Crop,
            modifier = clippedModifier.background(MaterialTheme.colorScheme.surfaceVariant),
            loading = { AvatarInitials(name) },
            error = { AvatarInitials(name) },
        )
    } else {
        Box(
            modifier = clippedModifier.background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            AvatarInitials(name)
        }
    }
}

@Composable
private fun AvatarInitials(name: String) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Text(
            text = name.firstOrNull()?.uppercaseChar()?.toString().orEmpty(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(name = "Oficios Avatar", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun OficiosAvatarPreview() {
    OficiosTheme {
        OficiosAvatar(name = "Carlos Rodríguez")
    }
}
