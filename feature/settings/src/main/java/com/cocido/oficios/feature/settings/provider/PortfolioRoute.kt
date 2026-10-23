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
import com.cocido.oficios.core.common.contract.ImageUploadContract
import com.cocido.oficios.core.designsystem.component.feedback.LocalOficiosToastController
import com.cocido.oficios.core.ui.lifecycle.RefreshOnResume
import com.cocido.oficios.core.ui.crop.ImageCropMode
import com.cocido.oficios.core.ui.crop.ImageCropRequest
import com.cocido.oficios.core.ui.crop.prepareImageForCropFromPath
import com.cocido.oficios.core.ui.crop.rememberImageCropper
import com.cocido.oficios.core.ui.file.CertificateFilePick
import com.cocido.oficios.core.ui.file.pickCertificateFile
import com.cocido.oficios.core.ui.picker.rememberImageSourcePicker

@Composable
fun PortfolioRoute(
    onNavigateBack: () -> Unit,
    onShowMessage: (String) -> Unit,
    viewModel: PortfolioViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val toastController = LocalOficiosToastController.current
    RefreshOnResume(viewModel::onResume)
    val context = LocalContext.current
    val imagePicker = rememberImageSourcePicker(
        title = "Foto de trabajo",
        cameraLabel = "Sacar una foto",
        galleryLabel = "Elegir de la galería",
        cancelLabel = "Cancelar",
        onImageSelected = { path -> path?.let { viewModel.onEvent(PortfolioEvent.GallerySelected(it)) } },
        onPickError = onShowMessage,
        filePrefix = "galeria",
        cropMode = ImageCropMode.Gallery,
    )
    val cropper = rememberImageCropper()
    val certificatePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        when (val pick = pickCertificateFile(context, uri)) {
            is CertificateFilePick.Ready -> {
                if (pick.isPdf) {
                    viewModel.onEvent(PortfolioEvent.CertificateFileSelected(pick.path, pick.displayName))
                } else {
                    runCatching { prepareImageForCropFromPath(context, pick.path, "certificate") }
                        .onSuccess { working ->
                            cropper.start(
                                ImageCropRequest(
                                    sourcePath = working,
                                    mode = ImageCropMode.Certificate,
                                    filePrefix = "certificate",
                                    onDone = { cropped ->
                                        viewModel.onEvent(
                                            PortfolioEvent.CertificateFileSelected(cropped, pick.displayName),
                                        )
                                    },
                                    onCancel = {},
                                    onError = onShowMessage,
                                ),
                            )
                        }
                        .onFailure {
                            onShowMessage(ImageUploadContract.IMAGE_READ_FAILED_MESSAGE)
                        }
                }
            }
            is CertificateFilePick.Rejected -> onShowMessage(pick.message)
        }
    }
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                PortfolioEffect.NavigateBack -> onNavigateBack()
                is PortfolioEffect.ShowMessage -> toastController.showSuccess(effect.message)
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
