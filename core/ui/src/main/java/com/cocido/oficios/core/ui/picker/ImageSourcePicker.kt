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
import com.cocido.oficios.core.ui.file.compressImageFilePath
import com.cocido.oficios.core.ui.file.createCameraImageCapture
import com.cocido.oficios.core.ui.file.resolveUriToFilePath

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
): ImageSourcePickerState {
    val context = LocalContext.current
    var showSheet by remember { mutableStateOf(false) }
    var pendingCameraPath by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri == null) {
            onImageSelected(null)
            return@rememberLauncherForActivityResult
        }
        val path = resolveUriToFilePath(context, uri, filePrefix)
        if (path == null) {
            onPickError(ImageUploadContract.IMAGE_READ_FAILED_MESSAGE)
            onImageSelected(null)
        } else {
            onImageSelected(path)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val rawPath = pendingCameraPath
        pendingCameraPath = null
        if (!success || rawPath == null) {
            onImageSelected(null)
            return@rememberLauncherForActivityResult
        }
        val compressed = compressImageFilePath(context, rawPath, filePrefix)
        if (compressed == null) {
            onPickError(ImageUploadContract.IMAGE_READ_FAILED_MESSAGE)
            onImageSelected(null)
        } else {
            onImageSelected(compressed)
        }
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
