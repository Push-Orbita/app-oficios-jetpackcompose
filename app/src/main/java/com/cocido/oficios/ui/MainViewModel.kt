package com.cocido.oficios.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.navigation.PushNavigationCoordinator
import com.cocido.oficios.core.datastore.ThemeModePreferences
import com.cocido.oficios.domain.model.ThemeMode
import com.cocido.oficios.domain.usecase.ObserveThemeModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor(
    pushNavigationCoordinator: PushNavigationCoordinator,
    observeThemeModeUseCase: ObserveThemeModeUseCase,
    @ApplicationContext context: Context,
) : ViewModel() {

    private val _pendingPushRoute = MutableStateFlow<String?>(null)
    val pendingPushRoute: StateFlow<String?> = _pendingPushRoute.asStateFlow()

    val themeMode: StateFlow<ThemeMode> = observeThemeModeUseCase()
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeModePreferences.read(context))

    init {
        viewModelScope.launch {
            pushNavigationCoordinator.pendingRoutes.collect { route ->
                _pendingPushRoute.value = route
            }
        }
    }

    fun consumePushRoute() {
        _pendingPushRoute.value = null
    }
}
