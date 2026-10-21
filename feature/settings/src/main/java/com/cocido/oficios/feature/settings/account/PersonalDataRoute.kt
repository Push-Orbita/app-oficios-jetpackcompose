package com.cocido.oficios.feature.settings.account

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocido.oficios.core.ui.picker.rememberImageSourcePicker

@Composable
fun PersonalDataRoute(
    onNavigateBack: () -> Unit,
    onShowMessage: (String) -> Unit,
    viewModel: PersonalDataViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val picker = rememberImageSourcePicker(
        title = "Foto de perfil",
        cameraLabel = "Sacar una foto",
        galleryLabel = "Elegir de la galería",
        cancelLabel = "Cancelar",
        onImageSelected = { path -> path?.let { viewModel.onEvent(PersonalDataEvent.PhotoSelected(it)) } },
        onPickError = onShowMessage,
        filePrefix = "perfil",
    )
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                PersonalDataEffect.NavigateBack -> onNavigateBack()
                is PersonalDataEffect.Saved -> {
                    onShowMessage(effect.message)
                    onNavigateBack()
                }
            }
        }
    }
    PersonalDataScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onRequestPhoto = picker.requestImage,
    )
}
