package com.cocido.oficios.core.designsystem.component.feedback

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

@Composable
fun OficiosInlineAlert(
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.ErrorOutline,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(OficiosComponentSize.iconMd),
            tint = MaterialTheme.colorScheme.error,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
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
