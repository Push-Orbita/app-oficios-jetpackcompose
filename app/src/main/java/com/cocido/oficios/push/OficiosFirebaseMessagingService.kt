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
import com.cocido.oficios.BuildConfig
import com.cocido.oficios.MainActivity
import com.cocido.oficios.R
import com.cocido.oficios.core.common.navigation.PushDeepLinkMapper
import com.cocido.oficios.core.common.navigation.PushNavigationCoordinator
import com.cocido.oficios.domain.usecase.notifications.MarkNotificationReadUseCase
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
        if (!BuildConfig.FIREBASE_ENABLED) return
        serviceScope.launch { deviceTokenRegistrar.syncCurrentToken() }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        if (!BuildConfig.FIREBASE_ENABLED) return
        val data = message.data
        val type = data["type"] ?: "GENERIC"
        val deepLink = data["deep_link"] ?: data["deepLink"]
        val notificationId = (data["notificacionId"] ?: data["notificationId"])?.toLongOrNull()
        val title = data["title"] ?: message.notification?.title ?: getString(R.string.app_name)
        val body = data["body"] ?: message.notification?.body.orEmpty()
        if (title.isBlank() && body.isBlank() && deepLink.isNullOrBlank()) return
        val internalRoute = PushDeepLinkMapper.toInternalRoute(deepLink)

        showNotification(
            type = type,
            title = title,
            body = body,
            deepLink = deepLink,
            internalRoute = internalRoute,
            notificationId = notificationId,
        )
    }

    private fun showNotification(
        type: String,
        title: String,
        body: String,
        deepLink: String?,
        internalRoute: String?,
        notificationId: Long?,
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

        val channelId = channelForType(type)
        ensureChannel(channelId, channelNameFor(channelId))

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_DEEP_LINK, deepLink)
            putExtra(EXTRA_INTERNAL_ROUTE, internalRoute)
            notificationId?.let { putExtra(EXTRA_NOTIFICATION_ID, it) }
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            (internalRoute ?: deepLink ?: title).hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_stat_oficios)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(
                if (channelId == CHANNEL_TRACKING) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT,
            )
            .build()

        NotificationManagerCompat.from(this).notify(
            (internalRoute ?: title).hashCode(),
            notification,
        )
    }

    private fun channelForType(type: String): String = when {
        type == TYPE_KYC -> CHANNEL_KYC
        type.startsWith("BOOKING") || type.contains("FINALIZ") -> CHANNEL_BOOKING
        type.contains("CAMINO") || type.contains("LLEG") -> CHANNEL_TRACKING
        else -> CHANNEL_ORDERS
    }

    private fun channelNameFor(channelId: String): String = when (channelId) {
        CHANNEL_KYC -> getString(R.string.notification_channel_kyc_name)
        CHANNEL_BOOKING -> getString(R.string.notification_channel_booking_name)
        CHANNEL_TRACKING -> getString(R.string.notification_channel_tracking_name)
        else -> getString(R.string.notification_channel_orders_name)
    }

    private fun ensureChannel(channelId: String, name: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val importance = when (channelId) {
            CHANNEL_TRACKING, CHANNEL_KYC -> NotificationManager.IMPORTANCE_HIGH
            else -> NotificationManager.IMPORTANCE_DEFAULT
        }
        val channel = NotificationChannel(channelId, name, importance)
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val TYPE_KYC = "KYC_REVISION_DOCUMENTACION"
        const val CHANNEL_KYC = "kyc_revision"
        const val CHANNEL_ORDERS = "orders"
        const val CHANNEL_BOOKING = "booking"
        const val CHANNEL_TRACKING = "tracking"
        const val EXTRA_DEEP_LINK = "extra_deep_link"
        const val EXTRA_INTERNAL_ROUTE = "extra_internal_route"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }
}
