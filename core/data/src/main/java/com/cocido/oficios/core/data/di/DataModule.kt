package com.cocido.oficios.core.data.di

import com.cocido.oficios.core.common.dispatcher.DefaultDispatcherProvider
import com.cocido.oficios.core.common.dispatcher.DispatcherProvider
import com.cocido.oficios.core.common.logging.AppLogger
import com.cocido.oficios.core.data.logging.AndroidAppLogger
import com.cocido.oficios.core.data.repository.AuthRepositoryImpl
import com.cocido.oficios.core.data.repository.HomeRepositoryImpl
import com.cocido.oficios.core.data.repository.OnboardingRepositoryImpl
import com.cocido.oficios.core.data.session.SessionManager
import com.cocido.oficios.domain.repository.AuthRepository
import com.cocido.oficios.domain.repository.HomeRepository
import com.cocido.oficios.domain.repository.OnboardingRepository
import com.cocido.oficios.domain.repository.SessionRepository
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

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl,
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindOnboardingRepository(
        impl: OnboardingRepositoryImpl,
    ): OnboardingRepository

    @Binds
    @Singleton
    abstract fun bindSessionRepository(
        impl: SessionManager,
    ): SessionRepository
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
