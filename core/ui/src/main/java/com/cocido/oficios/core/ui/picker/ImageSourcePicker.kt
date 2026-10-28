package com.cocido.oficios.core.ui.picker

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.common.contract.ImageUploadContract
import com.cocido.oficios.core.ui.crop.ImageCropMode
import com.cocido.oficios.core.ui.crop.ImageCropRequest
import com.cocido.oficios.core.ui.crop.prepareCropSource
import com.cocido.oficios.core.ui.crop.rememberImageCropper
import com.cocido.oficios.core.ui.file.createCameraImageCapture

class ImageSourcePickerState internal constructor(
    val requestImage: () -> Unit,
)

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
): ImageSourcePickerState {
    val context = LocalContext.current
    var showSheet by remember { mutableStateOf(false) }
    var pendingCameraPath by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val cropper = rememberImageCropper()

    fun openCrop(uri: Uri?, path: String?) {
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
        openCrop(uri = uri, path = null)
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val rawPath = pendingCameraPath
        pendingCameraPath = null
        if (!success || rawPath == null) {
            onImageSelected(null)
            return@rememberLauncherForActivityResult
        }
        openCrop(uri = null, path = rawPath)
    }

    fun launchCamera() {
        val (uri, path) = createCameraImageCapture(context)
        pendingCameraPath = path
        cameraLauncher.launch(uri)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            launchCamera()
        }
    }

    fun requestCameraCapture() {
        when {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED -> launchCamera()
            else -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

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
                        requestCameraCapture()
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
