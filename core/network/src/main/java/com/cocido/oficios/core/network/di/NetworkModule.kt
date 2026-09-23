package com.cocido.oficios.core.network.di

import com.cocido.oficios.core.network.BuildConfig
import com.cocido.oficios.core.network.api.DemoApiService
import com.cocido.oficios.core.network.api.account.AccountApi
import com.cocido.oficios.core.network.api.account.WorkGalleryApi
import com.cocido.oficios.core.network.api.agenda.AgendaApi
import com.cocido.oficios.core.network.api.auth.AuthApi
import com.cocido.oficios.core.network.api.catalog.CatalogApi
import com.cocido.oficios.core.network.api.profile.ClientProfileApi
import com.cocido.oficios.core.network.api.profile.IdentityDocumentApi
import com.cocido.oficios.core.network.api.profile.ProviderProfileApi
import com.cocido.oficios.core.network.api.profile.WorkCertificateApi
import com.cocido.oficios.core.network.api.search.WorkerSearchApi
import com.cocido.oficios.core.network.api.worker.WorkerProfileApi
import com.cocido.oficios.core.network.api.worker.WorkerReviewsApi
import com.cocido.oficios.core.network.api.notifications.NotificationsApi
import com.cocido.oficios.core.network.api.orders.OrdersApi
import com.cocido.oficios.core.network.api.safety.ReportsApi
import com.cocido.oficios.core.network.api.social.FavoritesApi
import com.cocido.oficios.core.network.authenticator.TokenAuthenticator
import com.cocido.oficios.core.network.interceptor.AuthInterceptor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val TIMEOUT_SECONDS = 30L

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .authenticator(tokenAuthenticator)
            .addInterceptor(authInterceptor)

        if (BuildConfig.DEBUG) {
            builder.addInterceptor(SanitizedLoggingInterceptor())
        }

        return builder.build()
    }

    @Provides
    @Singleton
    @ApiRetrofit
    fun provideApiRetrofit(
        okHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    @DemoRetrofit
    fun provideDemoRetrofit(json: Json): Retrofit {
        val contentType = "application/json".toMediaType()
        val demoClient = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(SanitizedLoggingInterceptor())
                }
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.DEMO_BASE_URL)
            .client(demoClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(@ApiRetrofit retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAccountApi(@ApiRetrofit retrofit: Retrofit): AccountApi =
        retrofit.create(AccountApi::class.java)

    @Provides
    @Singleton
    fun provideWorkGalleryApi(@ApiRetrofit retrofit: Retrofit): WorkGalleryApi =
        retrofit.create(WorkGalleryApi::class.java)

    @Provides
    @Singleton
    fun provideCatalogApi(@ApiRetrofit retrofit: Retrofit): CatalogApi {
        return retrofit.create(CatalogApi::class.java)
    }

    @Provides
    @Singleton
    fun provideProviderProfileApi(@ApiRetrofit retrofit: Retrofit): ProviderProfileApi {
        return retrofit.create(ProviderProfileApi::class.java)
    }

    @Provides
    @Singleton
    fun provideClientProfileApi(@ApiRetrofit retrofit: Retrofit): ClientProfileApi {
        return retrofit.create(ClientProfileApi::class.java)
    }

    @Provides
    @Singleton
    fun provideIdentityDocumentApi(@ApiRetrofit retrofit: Retrofit): IdentityDocumentApi {
        return retrofit.create(IdentityDocumentApi::class.java)
    }

    @Provides
    @Singleton
    fun provideWorkCertificateApi(@ApiRetrofit retrofit: Retrofit): WorkCertificateApi {
        return retrofit.create(WorkCertificateApi::class.java)
    }

    @Provides
    @Singleton
    fun provideWorkerSearchApi(@ApiRetrofit retrofit: Retrofit): WorkerSearchApi {
        return retrofit.create(WorkerSearchApi::class.java)
    }

    @Provides
    @Singleton
    fun provideWorkerProfileApi(@ApiRetrofit retrofit: Retrofit): WorkerProfileApi {
        return retrofit.create(WorkerProfileApi::class.java)
    }

    @Provides
    @Singleton
    fun provideNotificationsApi(@ApiRetrofit retrofit: Retrofit): NotificationsApi {
        return retrofit.create(NotificationsApi::class.java)
    }

    @Provides
    @Singleton
    fun provideWorkerReviewsApi(@ApiRetrofit retrofit: Retrofit): WorkerReviewsApi {
        return retrofit.create(WorkerReviewsApi::class.java)
    }

    @Provides
    @Singleton
    fun provideOrdersApi(@ApiRetrofit retrofit: Retrofit): OrdersApi {
        return retrofit.create(OrdersApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAgendaApi(@ApiRetrofit retrofit: Retrofit): AgendaApi {
        return retrofit.create(AgendaApi::class.java)
    }

    @Provides
    @Singleton
    fun provideFavoritesApi(@ApiRetrofit retrofit: Retrofit): FavoritesApi {
        return retrofit.create(FavoritesApi::class.java)
    }

    @Provides
    @Singleton
    fun provideReportsApi(@ApiRetrofit retrofit: Retrofit): ReportsApi {
        return retrofit.create(ReportsApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDemoApiService(@DemoRetrofit retrofit: Retrofit): DemoApiService {
        return retrofit.create(DemoApiService::class.java)
    }
}
