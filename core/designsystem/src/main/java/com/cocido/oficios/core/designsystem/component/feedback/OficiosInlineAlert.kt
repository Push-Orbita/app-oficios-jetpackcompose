package com.cocido.oficios.core.designsystem.component.feedback

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

enum class OficiosInlineAlertTone {
    Default,
    OnBrand,
}

@Composable
fun OficiosInlineAlert(
    message: String,
    modifier: Modifier = Modifier,
    iconRes: Int = OficiosIcons.CircleAlert,
    tone: OficiosInlineAlertTone = OficiosInlineAlertTone.Default,
) {
    val iconTint: Color
    val textColor: Color
    val iconSize: Dp
    val arrangement: Arrangement.Horizontal
    when (tone) {
        OficiosInlineAlertTone.Default -> {
            iconTint = MaterialTheme.colorScheme.error
            textColor = MaterialTheme.colorScheme.onBackground
            iconSize = OficiosComponentSize.iconMd
            arrangement = Arrangement.spacedBy(OficiosSpacing.xs, Alignment.Start)
        }
        OficiosInlineAlertTone.OnBrand -> {
            iconTint = MaterialTheme.colorScheme.onSurface
            textColor = MaterialTheme.colorScheme.onSurface
            iconSize = OficiosComponentSize.authLoginErrorIconSize
            arrangement = Arrangement.spacedBy(OficiosSpacing.xs, Alignment.CenterHorizontally)
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = arrangement,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            tint = iconTint,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = textColor,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun OficiosInlineAlertPreview() {
    OficiosTheme {
        OficiosInlineAlert(message = "Los datos de sesión no coinciden")
    }
}
