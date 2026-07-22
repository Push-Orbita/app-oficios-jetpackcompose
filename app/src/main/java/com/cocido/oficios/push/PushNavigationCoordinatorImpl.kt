package com.cocido.oficios.push

import com.cocido.oficios.core.common.navigation.PushNavigationCoordinator
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@Singleton
class PushNavigationCoordinatorImpl @Inject constructor() : PushNavigationCoordinator {

    private val _pendingRoutes = MutableSharedFlow<String>(extraBufferCapacity = 1)
    override val pendingRoutes: SharedFlow<String> = _pendingRoutes.asSharedFlow()

    override fun publishRoute(route: String) {
        _pendingRoutes.tryEmit(route)
    }
}
