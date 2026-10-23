package com.cocido.oficios.core.ui.crop

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cocido.oficios.core.common.contract.ImageUploadContract
import com.cocido.oficios.core.common.result.ClientValidationException
import java.io.File

/**
 * Recortador Compose propio (no CanHub Android-Image-Cropper).
 *
 * CanHub es Apache-2.0 y se mantiene, pero abre una Activity View con su propio
 * tema. No replica fondo navy, D-DIN, copy en usted, botones Small/Primary,
 * máscara circular de perfil ni touch 48 dp del design system. Un cropper
 * Compose en [OficiosScaffold] reutiliza el DS, aplica EXIF en
 * [prepareImageForCrop] y no necesita reglas R8/ProGuard extra.
 */
data class ImageCropRequest(
    val sourcePath: String,
    val mode: ImageCropMode,
    val filePrefix: String,
    val onDone: (String) -> Unit,
    val onCancel: () -> Unit,
    val onError: (String) -> Unit,
)

class ImageCropperState internal constructor(
    private val startRequest: (ImageCropRequest) -> Unit,
) {
    fun start(request: ImageCropRequest) = startRequest(request)
}

@Composable
fun rememberImageCropper(): ImageCropperState {
    val context = LocalContext.current
    var request by remember { mutableStateOf<ImageCropRequest?>(null) }
    val state = remember {
        ImageCropperState { pending -> request = pending }
    }
    request?.let { pending ->
        Dialog(
            onDismissRequest = {
                pending.onCancel()
                request = null
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
                decorFitsSystemWindows = false,
            ),
        ) {
            ImageCropScreen(
                modifier = Modifier.fillMaxSize(),
                sourcePath = pending.sourcePath,
                mode = pending.mode,
                onCancel = {
                    pending.onCancel()
                    request = null
                },
                onConfirm = { transform ->
                    runCatching {
                        applyImageCrop(
                            context = context,
                            sourcePath = pending.sourcePath,
                            transform = transform,
                            filePrefix = pending.filePrefix,
                        )
                    }.onSuccess { path ->
                        pending.onDone(path)
                        request = null
                    }.onFailure { error ->
                        pending.onError(ImageUploadContract.userFacingFileFailure(error))
                        request = null
                    }
                },
            )
        }
    }
    return state
}

fun prepareCropSource(
    context: Context,
    uri: Uri?,
    path: String?,
    filePrefix: String,
): String {
    return when {
        uri != null -> prepareImageForCrop(context, uri, filePrefix)
        !path.isNullOrBlank() && File(path).exists() -> prepareImageForCropFromPath(context, path, filePrefix)
        else -> throw ClientValidationException(ImageUploadContract.IMAGE_READ_FAILED_MESSAGE)
    }
}
