package com.cocido.oficios.core.ui.file

import android.content.Context
import android.net.Uri
import java.io.File

fun resolveUriToFilePath(context: Context, uri: Uri): String? = runCatching {
    val inputStream = context.contentResolver.openInputStream(uri) ?: return null
    val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
    tempFile.outputStream().use { output -> inputStream.copyTo(output) }
    tempFile.absolutePath
}.getOrNull()
