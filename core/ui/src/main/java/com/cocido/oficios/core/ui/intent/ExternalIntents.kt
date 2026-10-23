package com.cocido.oficios.core.ui.intent

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.cocido.oficios.core.model.AppConstants
import com.cocido.oficios.core.ui.gallery.GalleryMediaKind
import com.cocido.oficios.core.ui.gallery.toGalleryMediaKind

fun openTermsAndConditions(context: Context, openInApp: () -> Unit) {
    val url = AppConstants.TERMS_URL.trim()
    if (AppConstants.hasTermsUrl(url)) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } else {
        openInApp()
    }
}

fun isSupportPhoneConfigured(): Boolean = AppConstants.hasSupportPhone()

fun openSupportPhone(context: Context) {
    if (!AppConstants.hasSupportPhone()) return
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${AppConstants.SUPPORT_PHONE}"))
    context.startActivity(intent)
}

fun openRemoteDocument(context: Context, url: String) {
    if (url.isBlank()) return
    val uri = Uri.parse(url)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        if (url.toGalleryMediaKind() == GalleryMediaKind.Pdf) {
            setDataAndType(uri, "application/pdf")
        } else {
            data = uri
        }
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    runCatching { context.startActivity(Intent.createChooser(intent, null)) }
}
