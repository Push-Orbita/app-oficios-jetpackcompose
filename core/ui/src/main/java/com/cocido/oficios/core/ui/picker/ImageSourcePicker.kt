package com.cocido.oficios.core.ui.picker

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cocido.oficios.core.common.contract.ImageUploadContract
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.ui.R
import com.cocido.oficios.core.ui.crop.ImageCropMode
import com.cocido.oficios.core.ui.crop.ImageCropRequest
import com.cocido.oficios.core.ui.crop.prepareCropSource
import com.cocido.oficios.core.ui.crop.rememberImageCropper
import com.cocido.oficios.core.ui.file.createCameraImageCapture
import com.cocido.oficios.core.ui.file.prepareImageForUpload
import com.cocido.oficios.core.ui.permission.RuntimePermissionLabels
import com.cocido.oficios.core.ui.permission.rememberRuntimePermissionRequest
import androidx.compose.ui.platform.LocalContext

class ImageSourcePickerState internal constructor(
    val requestImage: () -> Unit,
)

/**
 * Picker cámara / galería. Por defecto abre [ImageCropScreen] según [cropMode].
 *
 * [skipCrop] = true entrega el JPEG completo (comprimido, sin recorte) — selfie /
 * foto de rostro de identidad. Foto de perfil, DNI, galería y certificado siguen recortando.
 *
 * Cámara: rationale → request → si niega, diálogo con Abrir ajustes (nunca silencioso).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberImageSourcePicker(
    title: String,
    cameraLabel: String,
    galleryLabel: String,
    cancelLabel: String,
    onImageSelected: (String?) -> Unit,
    onPickError: (String) -> Unit = {},
    filePrefix: String = "imagen",
    cropMode: ImageCropMode = ImageCropMode.Document,
    skipCrop: Boolean = false,
): ImageSourcePickerState {
    val context = LocalContext.current
    var showSheet by remember { mutableStateOf(false) }
    var pendingCameraPath by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val cropper = rememberImageCropper()

    fun deliverPickedImage(uri: Uri?, path: String?) {
        if (skipCrop) {
            runCatching { prepareImageForUpload(context, uri, path, filePrefix) }
                .onSuccess { preparedPath -> onImageSelected(preparedPath) }
                .onFailure { error ->
                    onPickError(ImageUploadContract.userFacingFileFailure(error))
                    onImageSelected(null)
                }
            return
        }
        runCatching { prepareCropSource(context, uri, path, filePrefix) }
            .onSuccess { workingPath ->
                cropper.start(
                    ImageCropRequest(
                        sourcePath = workingPath,
                        mode = cropMode,
                        filePrefix = filePrefix,
                        onDone = onImageSelected,
                        onCancel = { onImageSelected(null) },
                        onError = { message ->
                            onPickError(message)
                            onImageSelected(null)
                        },
                    ),
                )
            }
            .onFailure {
                onPickError(ImageUploadContract.IMAGE_READ_FAILED_MESSAGE)
                onImageSelected(null)
            }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri == null) {
            onImageSelected(null)
            return@rememberLauncherForActivityResult
        }
        deliverPickedImage(uri = uri, path = null)
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val rawPath = pendingCameraPath
        pendingCameraPath = null
        if (!success || rawPath == null) {
            onImageSelected(null)
            return@rememberLauncherForActivityResult
        }
        deliverPickedImage(uri = null, path = rawPath)
    }

    fun launchCamera() {
        val (uri, path) = createCameraImageCapture(context)
        pendingCameraPath = path
        cameraLauncher.launch(uri)
    }

    val cameraPermission = rememberRuntimePermissionRequest(
        permissions = arrayOf(Manifest.permission.CAMERA),
        labels = RuntimePermissionLabels(
            rationaleTitle = stringResource(R.string.permission_camera_rationale_title),
            rationaleMessage = stringResource(R.string.permission_camera_rationale_message),
            rationaleConfirm = stringResource(R.string.permission_continue),
            deniedTitle = stringResource(R.string.permission_camera_denied_title),
            deniedMessage = stringResource(R.string.permission_camera_denied_message),
            openSettings = stringResource(R.string.permission_open_settings),
            dismiss = stringResource(R.string.permission_not_now),
        ),
        onGranted = { launchCamera() },
        onDenied = {
            onPickError(context.getString(R.string.permission_camera_denied_message))
        },
    )

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = OficiosSpacing.md)
                    .padding(bottom = OficiosSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                OficiosButton(
                    text = cameraLabel,
                    onClick = {
                        showSheet = false
                        cameraPermission.launch()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    variant = OficiosButtonVariant.Outlined,
                )
                OficiosButton(
                    text = galleryLabel,
                    onClick = {
                        showSheet = false
                        galleryLauncher.launch("image/*")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    variant = OficiosButtonVariant.Outlined,
                )
                OficiosButton(
                    text = cancelLabel,
                    onClick = { showSheet = false },
                    modifier = Modifier.fillMaxWidth(),
                    variant = OficiosButtonVariant.Ghost,
                )
            }
        }
    }

    return ImageSourcePickerState(requestImage = { showSheet = true })
}
