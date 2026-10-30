package com.cocido.oficios.feature.onboarding.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.component.cards.OficiosCard
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.feature.onboarding.R

internal const val DOCS_WAITING_CARD_TEST_TAG = "docs_waiting_card"

/**
 * Aviso de espera mientras DNI/selfie están En revisión.
 * Inventado en app (sin frame Figma exacto): card + reloj + copy rioplatense formal.
 */
@Composable
fun DocsWaitingCard(
    modifier: Modifier = Modifier,
) {
    OficiosCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag(DOCS_WAITING_CARD_TEST_TAG),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(OficiosIcons.Clock),
                contentDescription = null,
                modifier = Modifier.size(OficiosComponentSize.iconMd),
                tint = OficiosTheme.colors.warning,
            )
            Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xxs)) {
                Text(
                    text = stringResource(R.string.onboarding_docs_waiting_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.onboarding_docs_waiting_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun DocsWaitingCardPreview() {
    OficiosTheme {
        DocsWaitingCard()
    }
}
