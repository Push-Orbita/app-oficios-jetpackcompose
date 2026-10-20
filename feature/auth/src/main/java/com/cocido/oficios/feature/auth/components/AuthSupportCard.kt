package com.cocido.oficios.feature.auth.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import com.cocido.oficios.core.ui.intent.isSupportPhoneConfigured
import com.cocido.oficios.feature.auth.R

@Composable
fun AuthSupportCard(
    onContactClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    message: String = stringResource(R.string.auth_support_message),
) {
    val supportAvailable = isSupportPhoneConfigured()
    val resolvedMessage = if (supportAvailable) message else stringResource(R.string.auth_support_unavailable)
    OficiosCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(OficiosSpacing.md),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
        ) {
            Text(
                text = resolvedMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            OficiosButton(
                text = stringResource(R.string.auth_support_contact),
                onClick = onContactClick,
                enabled = enabled && supportAvailable,
                variant = OficiosButtonVariant.Secondary,
                modifier = Modifier.align(Alignment.End),
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
