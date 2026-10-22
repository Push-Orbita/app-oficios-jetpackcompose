package com.cocido.oficios.feature.settings.account

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocido.oficios.core.designsystem.component.feedback.LocalOficiosToastController
import com.cocido.oficios.core.ui.crop.ImageCropMode
import com.cocido.oficios.core.ui.lifecycle.RefreshOnResume
import com.cocido.oficios.core.ui.picker.rememberImageSourcePicker

@Composable
fun PersonalDataRoute(
    onNavigateBack: () -> Unit,
    onShowMessage: (String) -> Unit,
    viewModel: PersonalDataViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val toastController = LocalOficiosToastController.current
    RefreshOnResume(viewModel::onResume)
    val picker = rememberImageSourcePicker(
        title = "Foto de perfil",
        cameraLabel = "Sacar una foto",
        galleryLabel = "Elegir de la galería",
        cancelLabel = "Cancelar",
        onImageSelected = { path -> path?.let { viewModel.onEvent(PersonalDataEvent.PhotoSelected(it)) } },
        onPickError = onShowMessage,
        filePrefix = "perfil",
        cropMode = ImageCropMode.Profile,
    )
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                PersonalDataEffect.NavigateBack -> onNavigateBack()
                is PersonalDataEffect.Saved -> {
                    toastController.showSuccess(effect.message)
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
