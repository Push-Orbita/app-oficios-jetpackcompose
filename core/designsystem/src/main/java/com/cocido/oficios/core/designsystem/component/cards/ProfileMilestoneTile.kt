package com.cocido.oficios.core.designsystem.component.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.component.border.dashedBorder
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosProgressPercentStyle
import com.cocido.oficios.core.designsystem.theme.OficiosRadius
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

/**
 * Barra de progreso 40dp r16 con borde punteado y % a la derecha.
 * Figma `259:303`. Fill `primary` (azul de marca); el track queda sutil.
 */
@Composable
fun ProfileProgressBar(
    percent: Int,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(OficiosRadius.lg)
    val clamped = percent.coerceIn(0, 100)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(OficiosComponentSize.profileProgressHeight)
            .clip(shape)
            .dashedBorder(MaterialTheme.colorScheme.outline, OficiosRadius.lg),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(clamped / 100f)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.primary),
        )
        Text(
            text = "$clamped%",
            style = OficiosProgressPercentStyle,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = OficiosSpacing.md),
        )
    }
}

/**
 * Hito de perfil — Figma `259:307` / `259:311`.
 * Completo: fill sólido + circle-check. Pendiente: fondo navy, borde punteado, label Bold.
 */
@Composable
fun ProfileMilestoneTile(
    label: String,
    done: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(OficiosRadius.lg)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(shape)
            .background(if (done) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.background, shape)
            .then(
                if (done) {
                    Modifier.border(1.dp, MaterialTheme.colorScheme.outline, shape)
                } else {
                    Modifier.dashedBorder(MaterialTheme.colorScheme.outline, OficiosRadius.lg)
                },
            )
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (done) FontWeight.Normal else FontWeight.Bold,
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(OficiosSpacing.md),
        )
        Icon(
            imageVector = ImageVector.vectorResource(
                if (done) OficiosIcons.CircleCheck else OficiosIcons.CircleDashed,
            ),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(OficiosSpacing.xs)
                .size(OficiosComponentSize.iconMd),
        )
    }
}

@Preview(name = "Profile milestones", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun ProfileMilestonePreview() {
    OficiosTheme {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(OficiosSpacing.md),
        ) {
            ProfileProgressBar(percent = 80)
            Row {
                ProfileMilestoneTile("Agregue fotos a su galería", done = true, modifier = Modifier.weight(1f))
                ProfileMilestoneTile("Agregue su local comercial", done = false, modifier = Modifier.weight(1f))
            }
        }
    }
}
