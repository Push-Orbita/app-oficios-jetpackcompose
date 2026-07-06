package com.cocido.oficios.feature.onboarding.steps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocido.oficios.feature.onboarding.client.ClientContactDomicilioEffect
import com.cocido.oficios.feature.onboarding.client.ClientContactDomicilioViewModel
import com.cocido.oficios.feature.onboarding.client.ClientContactoEffect
import com.cocido.oficios.feature.onboarding.client.ClientContactoViewModel
import com.cocido.oficios.feature.onboarding.client.ClientDomicilioEffect
import com.cocido.oficios.feature.onboarding.client.ClientDomicilioViewModel
import com.cocido.oficios.feature.onboarding.provider.ProviderAvailabilityViewModel
import com.cocido.oficios.feature.onboarding.provider.ProviderDniViewModel
import com.cocido.oficios.feature.onboarding.provider.ProviderLocalViewModel
import com.cocido.oficios.feature.onboarding.provider.ProviderPaymentMethodsViewModel
import com.cocido.oficios.feature.onboarding.provider.ProviderProfessionalInfoViewModel
import com.cocido.oficios.feature.onboarding.provider.ProviderRubrosViewModel
import com.cocido.oficios.feature.onboarding.provider.ProviderStepEffect

@Composable
fun ClientContactDomicilioRoute(onSaved: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: ClientContactDomicilioViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { if (it is ClientContactDomicilioEffect.Saved) onSaved() }
    }
    ClientContactDomicilioStepScreen(
        uiState = uiState,
        onNombreChanged = viewModel::onNombreChanged,
        onTelefonoChanged = viewModel::onTelefonoChanged,
        onEmailChanged = viewModel::onEmailChanged,
        onBarrioQueryChanged = viewModel::onBarrioQueryChanged,
        onBarrioSelected = viewModel::onBarrioSelected,
        onBarrioSuggestionsDismissed = viewModel::dismissBarrioSuggestions,
        onCalleChanged = viewModel::onCalleChanged,
        onNumeroChanged = viewModel::onNumeroChanged,
        onPisoChanged = viewModel::onPisoChanged,
        onDepartamentoChanged = viewModel::onDepartamentoChanged,
        onSubmit = viewModel::submit,
    )
}

@Composable
fun ClientDomicilioRoute(onSaved: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: ClientDomicilioViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { if (it is ClientDomicilioEffect.Saved) onSaved() }
    }
    ClientDomicilioStepScreen(
        uiState = uiState,
        onBarrioQueryChanged = viewModel::onBarrioQueryChanged,
        onBarrioSelected = viewModel::onBarrioSelected,
        onBarrioSuggestionsDismissed = viewModel::dismissBarrioSuggestions,
        onCalleChanged = viewModel::onCalleChanged,
        onNumeroChanged = viewModel::onNumeroChanged,
        onSubmit = viewModel::submit,
    )
}

@Composable
fun ClientContactoRoute(onSaved: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: ClientContactoViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { if (it is ClientContactoEffect.Saved) onSaved() }
    }
    ClientContactoStepScreen(
        uiState = uiState,
        onNombreChanged = viewModel::onNombreChanged,
        onTelefonoChanged = viewModel::onTelefonoChanged,
        onEmailChanged = viewModel::onEmailChanged,
        onSubmit = viewModel::submit,
    )
}

@Composable
fun ProviderDniRoute(onSaved: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: ProviderDniViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { if (it is ProviderStepEffect.Saved) onSaved() }
    }
    ProviderDniStepScreen(
        uiState = uiState,
        onDocumentoChanged = viewModel::onDocumentoChanged,
        onFrontImage = viewModel::onFrontImage,
        onBackImage = viewModel::onBackImage,
        onSubmit = viewModel::submit,
    )
}

@Composable
fun ProviderProfessionalInfoRoute(onSaved: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: ProviderProfessionalInfoViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { if (it is ProviderStepEffect.Saved) onSaved() }
    }
    ProviderProfessionalInfoStepScreen(
        uiState = uiState,
        onDescripcionChanged = viewModel::onDescripcionChanged,
        onExperienciaChanged = viewModel::onExperienciaChanged,
        onSubmit = viewModel::submit,
    )
}

@Composable
fun ProviderRubrosRoute(onSaved: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: ProviderRubrosViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { if (it is ProviderStepEffect.Saved) onSaved() }
    }
    ProviderRubrosStepScreen(
        uiState = uiState,
        onCategorySelected = viewModel::onCategorySelected,
        onTradeSelected = viewModel::onTradeSelected,
        onSubRubroToggled = viewModel::onSubRubroToggled,
        onSubmit = viewModel::submit,
    )
}

@Composable
fun ProviderAvailabilityRoute(onSaved: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: ProviderAvailabilityViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { if (it is ProviderStepEffect.Saved) onSaved() }
    }
    ProviderAvailabilityStepScreen(
        uiState = uiState,
        onWorkDayToggled = viewModel::onWorkDayToggled,
        onTimeSlotFromChanged = viewModel::onTimeSlotFromChanged,
        onTimeSlotToChanged = viewModel::onTimeSlotToChanged,
        onAddTimeSlot = viewModel::onAddTimeSlot,
        onServesAtClientHomeChanged = viewModel::onServesAtClientHomeChanged,
        onServesAtLocalChanged = viewModel::onServesAtLocalChanged,
        onRequiresTurnsChanged = viewModel::onRequiresTurnsChanged,
        onSubmit = viewModel::submit,
    )
}

@Composable
fun ProviderPaymentMethodsRoute(onSaved: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: ProviderPaymentMethodsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { if (it is ProviderStepEffect.Saved) onSaved() }
    }
    ProviderPaymentMethodsStepScreen(
        uiState = uiState,
        onMethodToggled = viewModel::onMethodToggled,
        onSubmit = viewModel::submit,
    )
}

@Composable
fun ProviderLocalRoute(onSaved: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: ProviderLocalViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { if (it is ProviderStepEffect.Saved) onSaved() }
    }
    ProviderLocalStepScreen(
        uiState = uiState,
        onBarrioQueryChanged = viewModel::onBarrioQueryChanged,
        onBarrioSelected = viewModel::onBarrioSelected,
        onBarrioSuggestionsDismissed = viewModel::dismissBarrioSuggestions,
        onCalleChanged = viewModel::onCalleChanged,
        onSubmit = viewModel::submit,
    )
}
