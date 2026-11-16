package com.cocido.oficios.core.ui.permission

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cocido.oficios.core.designsystem.component.OficiosDialog
import com.cocido.oficios.core.ui.intent.openApplicationDetailsSettings

/**
 * Flujo único de permiso runtime: rationale → system prompt → feedback + Abrir ajustes.
 *
 * - Si ya está concedido: llama [onGranted] al toque.
 * - Si falta: muestra rationale y recién ahí pide el permiso del sistema.
 * - Si niega: diálogo con mensaje y CTA a ajustes de la app (nunca silencioso).
 * - Si el deny es permanente: el siguiente toque va directo al diálogo con Settings.
 */
data class RuntimePermissionLabels(
    val rationaleTitle: String,
    val rationaleMessage: String,
    val rationaleConfirm: String,
    val deniedTitle: String,
    val deniedMessage: String,
    val openSettings: String,
    val dismiss: String,
)

fun interface PermissionGrantPredicate {
    fun isGranted(grants: Map<String, Boolean>): Boolean
}

class RuntimePermissionRequest internal constructor(
    private var onRequest: () -> Unit,
) {
    fun launch() = onRequest()

    internal fun bind(onRequest: () -> Unit) {
        this.onRequest = onRequest
    }
}

@Composable
fun rememberRuntimePermissionRequest(
    permissions: Array<String>,
    labels: RuntimePermissionLabels,
    onGranted: () -> Unit,
    onDenied: () -> Unit = {},
    isSatisfied: PermissionGrantPredicate = PermissionGrantPredicate { grants ->
        permissions.all { permission -> grants[permission] == true }
    },
): RuntimePermissionRequest {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    var showRationale by remember { mutableStateOf(false) }
    var showDenied by remember { mutableStateOf(false) }
    var askedOnce by remember { mutableStateOf(false) }
    val latestOnGranted by rememberUpdatedState(onGranted)
    val latestOnDenied by rememberUpdatedState(onDenied)
    val latestIsSatisfied by rememberUpdatedState(isSatisfied)

    fun currentGrants(): Map<String, Boolean> = permissions.associateWith { permission ->
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        val merged = currentGrants() + grants
        if (latestIsSatisfied.isGranted(merged)) {
            latestOnGranted()
        } else {
            showDenied = true
            latestOnDenied()
        }
    }

    fun shouldExplain(): Boolean {
        if (!askedOnce) return true
        if (activity == null) return true
        return permissions.any { permission ->
            ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        }
    }

    fun launchSystemPrompt() {
        askedOnce = true
        launcher.launch(permissions)
    }

    fun request() {
        if (latestIsSatisfied.isGranted(currentGrants())) {
            latestOnGranted()
            return
        }
        if (shouldExplain()) {
            showRationale = true
        } else {
            showDenied = true
            latestOnDenied()
        }
    }

    if (showRationale) {
        OficiosDialog(
            title = labels.rationaleTitle,
            message = labels.rationaleMessage,
            confirmLabel = labels.rationaleConfirm,
            dismissLabel = labels.dismiss,
            onConfirm = {
                showRationale = false
                launchSystemPrompt()
            },
            onDismiss = { showRationale = false },
        )
    }

    if (showDenied) {
        OficiosDialog(
            title = labels.deniedTitle,
            message = labels.deniedMessage,
            confirmLabel = labels.openSettings,
            dismissLabel = labels.dismiss,
            onConfirm = {
                showDenied = false
                context.openApplicationDetailsSettings()
            },
            onDismiss = { showDenied = false },
        )
    }

    val stableRequest = remember { RuntimePermissionRequest(onRequest = {}) }
    // Reasignar en cada recomposición para que launch() use el closure actual (estado + callbacks).
    stableRequest.bind(onRequest = { request() })
    return stableRequest
}

fun Context.findActivity(): Activity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return current as? Activity
}
