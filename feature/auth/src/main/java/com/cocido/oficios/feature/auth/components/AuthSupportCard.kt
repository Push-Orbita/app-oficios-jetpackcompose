package com.cocido.oficios.feature.auth.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.cocido.oficios.core.designsystem.component.cards.OficiosSupportCard
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.core.ui.intent.isSupportPhoneConfigured
import com.cocido.oficios.feature.auth.R

@Composable
fun AuthSupportCard(
    onContactClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    minHeight: Dp = OficiosComponentSize.authLoginSupportCardMinHeight,
    message: String = stringResource(R.string.auth_support_message),
) {
    val supportAvailable = isSupportPhoneConfigured()
    val resolvedMessage = if (supportAvailable) message else stringResource(R.string.auth_support_unavailable)
    OficiosSupportCard(
        message = resolvedMessage,
        contactLabel = stringResource(R.string.auth_support_contact),
        onContactClick = onContactClick,
        modifier = modifier,
        enabled = enabled && supportAvailable,
        minHeight = minHeight,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun AuthSupportCardPreview() {
    OficiosTheme {
        AuthSupportCard(onContactClick = {}, modifier = Modifier.padding(OficiosSpacing.xl))
    }
}
