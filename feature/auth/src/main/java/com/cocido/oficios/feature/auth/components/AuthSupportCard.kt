package com.cocido.oficios.feature.auth.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
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
    Box(modifier = modifier.fillMaxWidth()) {
        OficiosCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(OficiosSpacing.md),
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

        OficiosButton(
            text = "?",
            onClick = onContactClick,
            enabled = enabled,
            variant = OficiosButtonVariant.Ghost,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = OficiosSpacing.xl, y = -OficiosSpacing.xl),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF231D38)
@Composable
private fun AuthSupportCardPreview() {
    OficiosTheme {
        AuthSupportCard(onContactClick = {}, modifier = Modifier.padding(OficiosSpacing.xl))
    }
}
