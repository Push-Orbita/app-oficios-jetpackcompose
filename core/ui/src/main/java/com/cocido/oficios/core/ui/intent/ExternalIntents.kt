package com.cocido.oficios.core.ui.intent

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.cocido.oficios.core.model.AppConstants

fun openTermsAndConditions(context: Context, openInApp: () -> Unit) {
    val url = AppConstants.TERMS_URL.trim()
    if (AppConstants.hasTermsUrl(url)) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } else {
        openInApp()
    }
}

fun openSupportPhone(context: Context) {
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${AppConstants.SUPPORT_PHONE}"))
    context.startActivity(intent)
}
