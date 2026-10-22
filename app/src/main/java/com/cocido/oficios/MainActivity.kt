package com.cocido.oficios

import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.cocido.oficios.core.common.navigation.PushNavigationCoordinator
import com.cocido.oficios.domain.model.isDark
import com.cocido.oficios.domain.repository.UserPreferencesRepository
import com.cocido.oficios.domain.usecase.notifications.MarkNotificationReadUseCase
import com.cocido.oficios.push.OficiosFirebaseMessagingService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var pushNavigationCoordinator: PushNavigationCoordinator
    @Inject lateinit var markNotificationReadUseCase: MarkNotificationReadUseCase
    @Inject lateinit var userPreferencesRepository: UserPreferencesRepository

    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setOnExitAnimationListener { splashView ->
            // Sin zoom del icono de sistema: el lockup Compose ya está centrado sobre el mismo azul.
            splashView.remove()
        }
        super.onCreate(savedInstanceState)
        val themeMode = runBlocking { userPreferencesRepository.themeMode.first() }
        val dark = themeMode.isDark(isSystemInDarkTheme())
        applySystemBars(dark)
        handlePushIntent(intent)
        setContent {
            OficiosRoot(initialThemeMode = themeMode)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handlePushIntent(intent)
    }

    private fun isSystemInDarkTheme(): Boolean {
        val night = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return night == Configuration.UI_MODE_NIGHT_YES
    }

    private fun applySystemBars(dark: Boolean) {
        val barStyle = if (dark) {
            SystemBarStyle.dark(Color.TRANSPARENT)
        } else {
            SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        }
        enableEdgeToEdge(statusBarStyle = barStyle, navigationBarStyle = barStyle)
        window.setBackgroundDrawable(
            ColorDrawable(Color.parseColor(if (dark) "#00358B" else "#F4F5F7")),
        )
    }

    private fun handlePushIntent(intent: Intent?) {
        val route = intent?.getStringExtra(OficiosFirebaseMessagingService.EXTRA_INTERNAL_ROUTE)
            ?: return
        val notificationId = intent.getLongExtra(OficiosFirebaseMessagingService.EXTRA_NOTIFICATION_ID, -1L)
        if (notificationId > 0L) {
            activityScope.launch(Dispatchers.IO) {
                markNotificationReadUseCase(notificationId)
            }
        }
        pushNavigationCoordinator.publishRoute(route)
    }
}
