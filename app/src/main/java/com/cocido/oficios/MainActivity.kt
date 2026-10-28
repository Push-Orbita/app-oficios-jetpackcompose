package com.cocido.oficios

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.cocido.oficios.core.common.navigation.PushNavigationCoordinator
import com.cocido.oficios.core.common.splash.SplashGate
import com.cocido.oficios.core.datastore.ThemeModePreferences
import com.cocido.oficios.domain.usecase.notifications.MarkNotificationReadUseCase
import com.cocido.oficios.push.OficiosFirebaseMessagingService
import com.cocido.oficios.theme.OficiosSystemBars
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var pushNavigationCoordinator: PushNavigationCoordinator
    @Inject lateinit var markNotificationReadUseCase: MarkNotificationReadUseCase

    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { SplashGate.keepOnScreen }
        splashScreen.setOnExitAnimationListener { splashView ->
            OficiosSystemBars.applyFromPreferences(this)
            // Fade (sin zoom): el isotipo Compose tiene el mismo alto que el de sistema.
            splashView.view.animate()
                .alpha(0f)
                .setDuration(SPLASH_EXIT_FADE_MS)
                .withEndAction { splashView.remove() }
                .start()
        }
        super.onCreate(savedInstanceState)
        OficiosSystemBars.applyFromPreferences(this)
        handlePushIntent(intent)
        setContent {
            OficiosRoot(initialThemeMode = ThemeModePreferences.read(this))
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handlePushIntent(intent)
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

    private companion object {
        const val SPLASH_EXIT_FADE_MS = 250L
    }
}
