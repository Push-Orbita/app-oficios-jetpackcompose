package com.cocido.oficios.core.ui.file

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

fun fileProviderAuthority(context: Context): String = "${context.packageName}.fileprovider"

fun createCameraImageCapture(context: Context): Pair<Uri, String> {
    val file = File(context.cacheDir, "camera_${UUID.randomUUID()}.jpg")
    val uri = FileProvider.getUriForFile(context, fileProviderAuthority(context), file)
    return uri to file.absolutePath
}
