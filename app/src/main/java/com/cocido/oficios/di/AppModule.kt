package com.cocido.oficios.di

import com.cocido.oficios.push.FcmDeviceTokenRegistrar
import com.cocido.oficios.push.PushNavigationCoordinatorImpl
import com.cocido.oficios.core.common.contract.DeviceTokenRegistrar
import com.cocido.oficios.core.common.navigation.PushNavigationCoordinator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindDeviceTokenRegistrar(
        impl: FcmDeviceTokenRegistrar,
    ): DeviceTokenRegistrar

    @Binds
    @Singleton
    abstract fun bindPushNavigationCoordinator(
        impl: PushNavigationCoordinatorImpl,
    ): PushNavigationCoordinator
}
