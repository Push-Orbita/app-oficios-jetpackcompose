package com.cocido.oficios.core.data.di

import com.cocido.oficios.core.common.dispatcher.DefaultDispatcherProvider
import com.cocido.oficios.core.common.dispatcher.DispatcherProvider
import com.cocido.oficios.core.common.logging.AppLogger
import com.cocido.oficios.core.common.time.DisplayDateTime
import java.time.Clock
import com.cocido.oficios.core.data.logging.AndroidAppLogger
import com.cocido.oficios.core.data.local.RecentSearchDataStore
import com.cocido.oficios.core.data.register.RegisterTradeDraftDataStore
import com.cocido.oficios.core.data.repository.AgendaRepositoryImpl
import com.cocido.oficios.core.data.repository.AvailabilityRepositoryImpl
import com.cocido.oficios.core.data.repository.AccountRepositoryImpl
import com.cocido.oficios.core.data.repository.AuthRepositoryImpl
import com.cocido.oficios.core.data.repository.CatalogRepositoryImpl
import com.cocido.oficios.core.data.repository.FavoritesRepositoryImpl
import com.cocido.oficios.core.data.repository.HomeRepositoryImpl
import com.cocido.oficios.core.data.repository.ClientProfileRepositoryImpl
import com.cocido.oficios.core.data.repository.IdentityDocumentRepositoryImpl
import com.cocido.oficios.core.data.repository.NotificationsRepositoryImpl
import com.cocido.oficios.core.data.repository.OnboardingRepositoryImpl
import com.cocido.oficios.core.data.repository.OrderRepositoryImpl
import com.cocido.oficios.core.data.repository.ProviderProfileRepositoryImpl
import com.cocido.oficios.core.data.repository.ReportsRepositoryImpl
import com.cocido.oficios.core.data.repository.WorkCertificateRepositoryImpl
import com.cocido.oficios.core.data.repository.WorkerSearchRepositoryImpl
import com.cocido.oficios.core.data.repository.WorkerProfileRepositoryImpl
import com.cocido.oficios.core.data.session.SessionManager
import com.cocido.oficios.core.data.session.SessionPreferencesRepositoryImpl
import com.cocido.oficios.domain.repository.AgendaRepository
import com.cocido.oficios.domain.repository.AvailabilityRepository
import com.cocido.oficios.domain.repository.AccountRepository
import com.cocido.oficios.domain.repository.AuthRepository
import com.cocido.oficios.domain.repository.CatalogRepository
import com.cocido.oficios.domain.repository.FavoritesRepository
import com.cocido.oficios.domain.repository.HomeRepository
import com.cocido.oficios.domain.repository.ClientProfileRepository
import com.cocido.oficios.domain.repository.IdentityDocumentRepository
import com.cocido.oficios.domain.repository.NotificationsRepository
import com.cocido.oficios.domain.repository.OnboardingRepository
import com.cocido.oficios.domain.repository.OrderRepository
import com.cocido.oficios.domain.repository.ProviderProfileRepository
import com.cocido.oficios.domain.repository.RecentSearchRepository
import com.cocido.oficios.domain.repository.RegisterTradeDraftRepository
import com.cocido.oficios.domain.repository.ReportsRepository
import com.cocido.oficios.domain.repository.SessionPreferencesRepository
import com.cocido.oficios.domain.repository.SessionRepository
import com.cocido.oficios.domain.repository.WorkCertificateRepository
import com.cocido.oficios.domain.repository.WorkerProfileRepository
import com.cocido.oficios.domain.repository.WorkerSearchRepository
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
    abstract fun bindAccountRepository(
        impl: AccountRepositoryImpl,
    ): AccountRepository

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

    @Binds
    @Singleton
    abstract fun bindSessionPreferencesRepository(
        impl: SessionPreferencesRepositoryImpl,
    ): SessionPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindCatalogRepository(
        impl: CatalogRepositoryImpl,
    ): CatalogRepository

    @Binds
    @Singleton
    abstract fun bindRegisterTradeDraftRepository(
        impl: RegisterTradeDraftDataStore,
    ): RegisterTradeDraftRepository

    @Binds
    @Singleton
    abstract fun bindProviderProfileRepository(
        impl: ProviderProfileRepositoryImpl,
    ): ProviderProfileRepository

    @Binds
    @Singleton
    abstract fun bindClientProfileRepository(
        impl: ClientProfileRepositoryImpl,
    ): ClientProfileRepository

    @Binds
    @Singleton
    abstract fun bindIdentityDocumentRepository(
        impl: IdentityDocumentRepositoryImpl,
    ): IdentityDocumentRepository

    @Binds
    @Singleton
    abstract fun bindNotificationsRepository(
        impl: NotificationsRepositoryImpl,
    ): NotificationsRepository

    @Binds
    @Singleton
    abstract fun bindWorkCertificateRepository(
        impl: WorkCertificateRepositoryImpl,
    ): WorkCertificateRepository

    @Binds
    @Singleton
    abstract fun bindWorkerSearchRepository(
        impl: WorkerSearchRepositoryImpl,
    ): WorkerSearchRepository

    @Binds
    @Singleton
    abstract fun bindWorkerProfileRepository(
        impl: WorkerProfileRepositoryImpl,
    ): WorkerProfileRepository

    @Binds
    @Singleton
    abstract fun bindRecentSearchRepository(
        impl: RecentSearchDataStore,
    ): RecentSearchRepository

    @Binds
    @Singleton
    abstract fun bindOrderRepository(
        impl: OrderRepositoryImpl,
    ): OrderRepository

    @Binds
    @Singleton
    abstract fun bindAgendaRepository(
        impl: AgendaRepositoryImpl,
    ): AgendaRepository

    @Binds
    @Singleton
    abstract fun bindAvailabilityRepository(
        impl: AvailabilityRepositoryImpl,
    ): AvailabilityRepository

    @Binds
    @Singleton
    abstract fun bindFavoritesRepository(
        impl: FavoritesRepositoryImpl,
    ): FavoritesRepository

    @Binds
    @Singleton
    abstract fun bindReportsRepository(
        impl: ReportsRepositoryImpl,
    ): ReportsRepository
}

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {

    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()

    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.system(DisplayDateTime.ARGENTINA)

    @Provides
    @Singleton
    fun provideAppLogger(): AppLogger = AndroidAppLogger()
}
