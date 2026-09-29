package com.cocido.oficios.tracking

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.cocido.oficios.MainActivity
import com.cocido.oficios.R
import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.repository.LocationRepository
import com.cocido.oficios.domain.repository.OrderRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookingTrackingService : Service() {

    @Inject lateinit var locationRepository: LocationRepository
    @Inject lateinit var orderRepository: OrderRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var trackingJob: Job? = null
    private var activeBookingId: Long? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> stopTracking()
            ACTION_START -> {
                val bookingId = intent.getLongExtra(EXTRA_BOOKING_ID, -1L)
                if (bookingId > 0L) startTracking(bookingId) else stopSelf()
            }
            else -> stopSelf()
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        trackingJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun startTracking(bookingId: Long) {
        startAsForeground(buildNotification("Compartiendo tu ubicación durante el recorrido"))
        if (activeBookingId == bookingId && trackingJob?.isActive == true) return
        activeBookingId = bookingId
        trackingJob?.cancel()
        trackingJob = serviceScope.launch {
            locationRepository.observeLocations(TRACKING_INTERVAL_MILLIS).collectLatest { locationResult ->
                when (locationResult) {
                    is AppResult.Success -> {
                        when (orderRepository.markEnRoute(bookingId, locationResult.data)) {
                            is AppResult.Success -> updateNotification("Ubicación actualizada · servicio en camino")
                            is AppResult.Error -> updateNotification("Sin conexión. Vamos a reintentar automáticamente")
                        }
                    }
                    is AppResult.Error -> {
                        updateNotification(locationResult.error.safeTrackingMessage())
                        if (locationResult.error is AppError.Forbidden || locationResult.error is AppError.Validation) {
                            stopTracking()
                        }
                    }
                }
            }
        }
    }

    private fun stopTracking() {
        trackingJob?.cancel()
        trackingJob = null
        activeBookingId = null
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startAsForeground(notification: Notification) {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
        } else {
            0
        }
        ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, type)
    }

    private fun updateNotification(message: String) {
        getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, buildNotification(message))
    }

    private fun buildNotification(message: String): Notification {
        val openApp = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, BookingTrackingService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_oficios)
            .setContentTitle("Servicio en camino")
            .setContentText(message)
            .setContentIntent(openApp)
            .addAction(0, "Dejar de compartir", stopIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Ubicación durante servicios",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Informa cuando compartís ubicación durante un servicio activo."
            setShowBadge(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun AppError.safeTrackingMessage(): String = when (this) {
        is AppError.Forbidden -> "El seguimiento se detuvo porque falta el permiso de ubicación"
        is AppError.Validation -> "El seguimiento se detuvo porque la ubicación está desactivada"
        else -> "No pudimos actualizar la ubicación. Reintentaremos"
    }

    companion object {
        private const val ACTION_START = "com.cocido.oficios.tracking.START"
        private const val ACTION_STOP = "com.cocido.oficios.tracking.STOP"
        private const val EXTRA_BOOKING_ID = "booking_id"
        private const val CHANNEL_ID = "active_service_tracking"
        private const val NOTIFICATION_ID = 4206
        private const val TRACKING_INTERVAL_MILLIS = 30_000L

        fun start(context: Context, bookingId: Long) {
            val intent = Intent(context, BookingTrackingService::class.java)
                .setAction(ACTION_START)
                .putExtra(EXTRA_BOOKING_ID, bookingId)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            // No hay que arrancar un servicio sólo para detenerlo: Android 12+ puede
            // bloquear ese inicio si la app ya quedó en segundo plano.
            context.stopService(Intent(context, BookingTrackingService::class.java))
        }
    }
}
