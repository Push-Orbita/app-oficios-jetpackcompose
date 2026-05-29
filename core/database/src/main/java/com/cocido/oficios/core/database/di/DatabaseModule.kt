package com.cocido.oficios.core.database.di

import android.content.Context
import androidx.room.Room
import com.cocido.oficios.core.database.AppDatabase
import com.cocido.oficios.core.database.dao.CachedItemDao
import com.cocido.oficios.core.model.AppConstants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppConstants.DATABASE_NAME,
        ).build()
    }

    @Provides
    fun provideCachedItemDao(database: AppDatabase): CachedItemDao {
        return database.cachedItemDao()
    }
}
