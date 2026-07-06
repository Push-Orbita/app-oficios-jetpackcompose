package com.cocido.oficios.core.designsystem.component.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import com.cocido.oficios.core.designsystem.theme.OficiosColorMutedText
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

// ---------------------------------------------------------------------------
// WorkGalleryCard — imagen de trabajo del portafolio del trabajador
// ---------------------------------------------------------------------------

@Composable
fun WorkGalleryCard(
    imageUrl: String?,
    caption: String?,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val clickableModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier

    Column(modifier = modifier) {
        // Imagen — placeholder hasta integrar Coil
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .then(clickableModifier),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Image,
                contentDescription = null,
                tint = OficiosColorMutedText,
                modifier = Modifier.size(OficiosComponentSize.iconLg),
            )
        }
        if (caption != null) {
            Text(
                text = caption,
                style = MaterialTheme.typography.bodyMedium,
                color = OficiosColorMutedText,
                modifier = Modifier.padding(top = OficiosSpacing.xs),
            )
        }
    }
}

@Preview(name = "Work Gallery Card", showBackground = true, backgroundColor = 0xFF231D38)
@Composable
private fun WorkGalleryCardPreview() {
    OficiosTheme {
        Column(modifier = Modifier.padding(OficiosSpacing.md)) {
            WorkGalleryCard(imageUrl = null, caption = "Remodelación de baño completo", onClick = {})
        }
    }
}
