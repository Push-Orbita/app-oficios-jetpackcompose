package com.cocido.oficios.feature.home.help

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.cards.OficiosCard
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.ui.intent.openSupportPhone
import com.cocido.oficios.feature.home.R

@Composable
fun HelpRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    HelpScreen(
        onContactSupport = { openSupportPhone(context) },
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

@Composable
fun HelpScreen(
    onContactSupport: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OficiosScaffold(
        modifier = modifier,
        topBar = {
            OficiosBackTopBar(
                title = stringResource(R.string.help_title),
                onBack = onNavigateBack,
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(OficiosSpacing.lg)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(OficiosSpacing.lg),
        ) {
            Text(
                text = stringResource(R.string.help_intro),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FaqItem(
                question = stringResource(R.string.help_faq_how_to_hire_question),
                answer = stringResource(R.string.help_faq_how_to_hire_answer),
            )
            FaqItem(
                question = stringResource(R.string.help_faq_verify_phone_question),
                answer = stringResource(R.string.help_faq_verify_phone_answer),
            )
            FaqItem(
                question = stringResource(R.string.help_faq_cancel_order_question),
                answer = stringResource(R.string.help_faq_cancel_order_answer),
            )
            OficiosCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(OficiosSpacing.lg),
                    verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
                ) {
                    Text(
                        text = stringResource(R.string.help_support_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(R.string.help_support_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OficiosButton(
                        text = stringResource(R.string.home_support_contact),
                        onClick = onContactSupport,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun FaqItem(
    question: String,
    answer: String,
    modifier: Modifier = Modifier,
) {
    OficiosCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(OficiosSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
        ) {
            Text(
                text = question,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = answer,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
