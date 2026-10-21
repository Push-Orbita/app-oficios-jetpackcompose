package com.cocido.oficios.feature.settings.provider

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocido.oficios.core.ui.file.resolveCertificateUriToFilePath
import com.cocido.oficios.core.ui.picker.rememberImageSourcePicker

@Composable
fun PortfolioRoute(
    onNavigateBack: () -> Unit,
    onShowMessage: (String) -> Unit,
    viewModel: PortfolioViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val imagePicker = rememberImageSourcePicker(
        title = "Foto de trabajo",
        cameraLabel = "Sacar una foto",
        galleryLabel = "Elegir de la galería",
        cancelLabel = "Cancelar",
        onImageSelected = { path -> path?.let { viewModel.onEvent(PortfolioEvent.GallerySelected(it)) } },
    )
    val certificatePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val path = resolveCertificateUriToFilePath(context, uri)
        if (path == null) onShowMessage("Seleccione un PDF o una imagen de hasta 10 MB.")
        else viewModel.onEvent(PortfolioEvent.CertificateFileSelected(path))
    }
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                PortfolioEffect.NavigateBack -> onNavigateBack()
                is PortfolioEffect.ShowMessage -> onShowMessage(effect.message)
            }
        }
    }
    PortfolioScreen(
        uiState = state,
        onEvent = viewModel::onEvent,
        onPickGalleryImage = imagePicker.requestImage,
        onPickCertificateFile = { certificatePicker.launch("*/*") },
    )
}
