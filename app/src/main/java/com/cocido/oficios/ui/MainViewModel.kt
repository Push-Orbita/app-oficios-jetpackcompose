package com.cocido.oficios.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.navigation.PushNavigationCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor(
    pushNavigationCoordinator: PushNavigationCoordinator,
) : ViewModel() {

    private val _pendingPushRoute = MutableStateFlow<String?>(null)
    val pendingPushRoute: StateFlow<String?> = _pendingPushRoute.asStateFlow()

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
