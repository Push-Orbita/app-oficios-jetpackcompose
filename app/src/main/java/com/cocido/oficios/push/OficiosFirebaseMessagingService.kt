package com.cocido.oficios.push

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.cocido.oficios.MainActivity
import com.cocido.oficios.R
import com.cocido.oficios.core.common.navigation.PushNavigationCoordinator
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.usecase.notifications.MarkNotificationReadUseCase
import com.cocido.oficios.feature.onboarding.navigation.OnboardingDeepLinkMapper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OficiosFirebaseMessagingService : FirebaseMessagingService() {

    @Inject lateinit var pushNavigationCoordinator: PushNavigationCoordinator
    @Inject lateinit var markNotificationReadUseCase: MarkNotificationReadUseCase
    @Inject lateinit var deviceTokenRegistrar: com.cocido.oficios.core.common.contract.DeviceTokenRegistrar

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        serviceScope.launch { deviceTokenRegistrar.syncCurrentToken() }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        if (data["type"] != KYC_PUSH_TYPE) return

        val deepLink = data["deep_link"]
        val notificationId = data["notificacionId"]?.toLongOrNull()
        val title = data["title"] ?: message.notification?.title ?: getString(R.string.app_name)
        val body = data["body"] ?: message.notification?.body.orEmpty()
        val internalRoute = OnboardingDeepLinkMapper.toInternalRoute(deepLink)

        serviceScope.launch {
            notificationId?.let { id ->
                when (markNotificationReadUseCase(id)) {
                    is AppResult.Success -> Unit
                    is AppResult.Error -> Unit
                }
            }
        }

        showNotification(
            title = title,
            body = body,
            deepLink = deepLink,
            internalRoute = internalRoute,
        )
    }

    private fun showNotification(
        title: String,
        body: String,
        deepLink: String?,
        internalRoute: String?,
    ) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        ensureChannel()
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_DEEP_LINK, deepLink)
            putExtra(EXTRA_INTERNAL_ROUTE, internalRoute)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            (internalRoute ?: deepLink).hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(this).notify(
            (internalRoute ?: title).hashCode(),
            notification,
        )
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_kyc_name),
            NotificationManager.IMPORTANCE_HIGH,
        )
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val KYC_PUSH_TYPE = "KYC_REVISION_DOCUMENTACION"
        const val CHANNEL_ID = "kyc_revision"
        const val EXTRA_DEEP_LINK = "extra_deep_link"
        const val EXTRA_INTERNAL_ROUTE = "extra_internal_route"
    }
}
