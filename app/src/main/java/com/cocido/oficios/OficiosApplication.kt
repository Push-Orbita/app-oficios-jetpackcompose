package com.cocido.oficios

import android.app.Application
import android.content.Context
import com.cocido.oficios.core.datastore.ThemeModePreferences
import com.cocido.oficios.core.ui.map.MapsPreheat
import com.cocido.oficios.domain.repository.UserPreferencesRepository
import com.cocido.oficios.theme.ThemeNightMode
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
open class OficiosApplication : Application() {

    @Inject lateinit var userPreferencesRepository: UserPreferencesRepository

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        ThemeNightMode.applyFromPreferences(base)
    }

    override fun onCreate() {
        ThemeNightMode.applyFromPreferences(this)
        super.onCreate()
        MapsPreheat.start(this)
        applicationScope.launch(Dispatchers.Main.immediate) {
            userPreferencesRepository.themeMode.collect { mode ->
                ThemeModePreferences.write(this@OficiosApplication, mode)
                ThemeNightMode.apply(this@OficiosApplication, mode)
            }
        }
    }
}
