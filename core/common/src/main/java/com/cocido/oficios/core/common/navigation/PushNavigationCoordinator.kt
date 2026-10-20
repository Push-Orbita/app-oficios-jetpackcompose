package com.cocido.oficios.core.common.navigation

import kotlinx.coroutines.flow.SharedFlow

interface PushNavigationCoordinator {
    val pendingRoutes: SharedFlow<String>
    fun publishRoute(route: String)
}
