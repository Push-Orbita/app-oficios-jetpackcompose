package com.cocido.oficios

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.enableEdgeToEdge
import com.cocido.oficios.core.common.navigation.PushNavigationCoordinator
import com.cocido.oficios.push.OficiosFirebaseMessagingService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var pushNavigationCoordinator: PushNavigationCoordinator

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handlePushIntent(intent)
        setContent {
            OficiosRoot()
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
        pushNavigationCoordinator.publishRoute(route)
    }
}
