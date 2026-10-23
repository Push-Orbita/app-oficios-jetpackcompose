package com.cocido.oficios.feature.home.orders

import com.cocido.oficios.feature.home.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.inputs.OficiosTextField
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing

@Composable
fun CounterOfferScreen(
    uiState: CounterOfferUiState,
    onEvent: (CounterOfferEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OficiosScaffold(
        modifier = modifier,
        topBar = { OficiosBackTopBar(title = stringResource(R.string.counter_offer_title), onBack = onNavigateBack) },
    ) { paddingValues ->
        if (uiState is CounterOfferUiState.Form) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .consumeWindowInsets(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = OficiosSpacing.md),
                verticalArrangement = Arrangement.spacedBy(OficiosSpacing.lg),
            ) {
                Text(
                    text = stringResource(R.string.counter_offer_body),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                DateTimePickerButton(
                    label = stringResource(R.string.counter_offer_pick),
                    selectedDateTime = uiState.dateTime,
                    onDateTimeSelected = { onEvent(CounterOfferEvent.DateTimeSelected(it)) },
                )
                if (uiState.hasAttemptedSubmit && uiState.dateTime == null) {
                    Text(
                        text = stringResource(R.string.counter_offer_required),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                OficiosTextField(
                    value = uiState.price,
                    onValueChange = { onEvent(CounterOfferEvent.PriceChanged(it)) },
                    label = stringResource(R.string.counter_offer_price),
                    placeholder = stringResource(R.string.counter_offer_price_hint),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { onEvent(CounterOfferEvent.Submit) }),
                    isError = uiState.priceError != null,
                    supportingText = uiState.priceError,
                )
                OficiosButton(
                    text = stringResource(R.string.counter_offer_submit),
                    onClick = { onEvent(CounterOfferEvent.Submit) },
                    loading = uiState.isSubmitting,
                    enabled = uiState.canSubmit && !uiState.isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                    variant = OficiosButtonVariant.Primary,
                    size = OficiosButtonSize.Form,
                )
            }
        }
    }
}
