package com.cocido.oficios.feature.auth.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.cards.OficiosCard
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.feature.auth.R

@Composable
fun AuthSupportCard(
    onContactClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    message: String = stringResource(R.string.auth_support_message),
) {
    OficiosCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(OficiosSpacing.md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
            OficiosButton(
                text = stringResource(R.string.auth_support_contact),
                onClick = onContactClick,
                enabled = enabled,
                variant = OficiosButtonVariant.Secondary,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun AuthSupportCardPreview() {
    OficiosTheme {
        AuthSupportCard(onContactClick = {}, modifier = Modifier.padding(OficiosSpacing.xl))
    }
}
