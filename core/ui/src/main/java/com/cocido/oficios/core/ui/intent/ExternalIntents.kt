package com.cocido.oficios.core.ui.intent

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.cocido.oficios.core.model.AppConstants

fun openTermsAndConditions(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AppConstants.TERMS_URL))
    context.startActivity(intent)
}

fun openSupportPhone(context: Context) {
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${AppConstants.SUPPORT_PHONE}"))
    context.startActivity(intent)
}
