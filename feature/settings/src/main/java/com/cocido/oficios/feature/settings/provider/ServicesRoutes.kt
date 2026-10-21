package com.cocido.oficios.feature.settings.provider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ServicesRoute(
    onNavigateBack: () -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    onShowMessage: (String) -> Unit,
    viewModel: ServicesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) viewModel.onResume() }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ServicesEffect.NavigateBack -> onNavigateBack()
                ServicesEffect.NavigateToAdd -> onNavigateToAdd()
                is ServicesEffect.NavigateToEdit -> onNavigateToEdit(effect.serviceId)
                is ServicesEffect.ShowMessage -> onShowMessage(effect.message)
            }
        }
    }
    ServicesScreen(state, viewModel::onEvent)
}

@Composable
fun AddServiceRoute(
    onNavigateBack: () -> Unit,
    onShowMessage: (String) -> Unit,
    viewModel: AddServiceViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                AddServiceEffect.NavigateBack -> onNavigateBack()
                is AddServiceEffect.Saved -> {
                    onShowMessage(effect.message)
                    onNavigateBack()
                }
            }
        }
    }
    AddServiceScreen(state, viewModel::onEvent)
}
