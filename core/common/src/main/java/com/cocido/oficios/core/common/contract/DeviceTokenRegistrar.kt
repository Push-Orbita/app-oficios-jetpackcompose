package com.cocido.oficios.core.common.contract

interface DeviceTokenRegistrar {
    suspend fun syncCurrentToken()
}
