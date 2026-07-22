package com.cocido.oficios.push

import android.content.Context
import com.cocido.oficios.core.common.contract.DeviceTokenRegistrar
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.usecase.notifications.RegisterDeviceTokenUseCase
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

@Singleton
class FcmDeviceTokenRegistrar @Inject constructor(
    @ApplicationContext private val context: Context,
    private val registerDeviceTokenUseCase: RegisterDeviceTokenUseCase,
) : DeviceTokenRegistrar {

    override suspend fun syncCurrentToken() {
        val token = fetchFcmToken() ?: return
        val deviceId = android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID,
        )
        when (registerDeviceTokenUseCase(token, deviceId)) {
            is AppResult.Success -> Unit
            is AppResult.Error -> Unit
        }
    }

    private suspend fun fetchFcmToken(): String? = suspendCancellableCoroutine { continuation ->
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token -> continuation.resume(token) }
            .addOnFailureListener { continuation.resume(null) }
    }
}
