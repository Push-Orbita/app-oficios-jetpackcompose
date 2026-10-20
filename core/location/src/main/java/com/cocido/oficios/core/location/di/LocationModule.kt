package com.cocido.oficios.core.location.di

import android.content.Context
import com.cocido.oficios.core.location.AndroidLocationRepository
import com.cocido.oficios.domain.repository.LocationRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocationBindingModule {
    @Binds
    @Singleton
    abstract fun bindLocationRepository(implementation: AndroidLocationRepository): LocationRepository
}

@Module
@InstallIn(SingletonComponent::class)
object LocationProviderModule {
    @Provides
    @Singleton
    fun provideFusedLocationClient(
        @ApplicationContext context: Context,
    ): FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
}
