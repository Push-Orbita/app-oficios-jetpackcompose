package com.cocido.oficios.core.data.di

import com.cocido.oficios.core.common.dispatcher.DefaultDispatcherProvider
import com.cocido.oficios.core.common.dispatcher.DispatcherProvider
import com.cocido.oficios.core.common.logging.AppLogger
import com.cocido.oficios.core.data.logging.AndroidAppLogger
import com.cocido.oficios.core.data.repository.HomeRepositoryImpl
import com.cocido.oficios.domain.repository.HomeRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindHomeRepository(
        impl: HomeRepositoryImpl,
    ): HomeRepository
}

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {

    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()

    @Provides
    @Singleton
    fun provideAppLogger(): AppLogger = AndroidAppLogger()
}
