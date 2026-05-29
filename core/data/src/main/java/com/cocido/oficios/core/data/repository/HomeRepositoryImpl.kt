package com.cocido.oficios.core.data.repository

import com.cocido.oficios.core.common.dispatcher.DispatcherProvider
import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.data.local.HomeLocalDataSource
import com.cocido.oficios.core.data.mapper.toDomain
import com.cocido.oficios.core.data.remote.HomeRemoteDataSource
import com.cocido.oficios.domain.model.HomeContent
import com.cocido.oficios.domain.repository.HomeRepository
import javax.inject.Inject
import kotlinx.coroutines.withContext

class HomeRepositoryImpl @Inject constructor(
    private val remoteDataSource: HomeRemoteDataSource,
    private val localDataSource: HomeLocalDataSource,
    private val dispatcherProvider: DispatcherProvider,
) : HomeRepository {

    override suspend fun getHomeContent(forceError: Boolean): AppResult<HomeContent> {
        return withContext(dispatcherProvider.io) {
            if (forceError) {
                return@withContext AppResult.Error(
                    AppError.Validation(message = "Simulated error for demo purposes"),
                )
            }

            when (val remoteResult = remoteDataSource.fetchHomeContent()) {
                is AppResult.Success -> {
                    val content = remoteResult.data.toDomain()
                    localDataSource.cacheHomeContent(
                        title = content.title,
                        description = content.description,
                    )
                    AppResult.Success(content)
                }
                is AppResult.Error -> {
                    getCachedContent() ?: getDemoFallbackContent()
                }
            }
        }
    }

    private suspend fun getDemoFallbackContent(): AppResult<HomeContent> {
        localDataSource.cacheHomeContent(
            title = HomeDemoFallbackContent.title,
            description = HomeDemoFallbackContent.description,
        )
        return AppResult.Success(HomeDemoFallbackContent)
    }

    private suspend fun getCachedContent(): AppResult<HomeContent>? {
        val cached = localDataSource.getCachedHomeContent() ?: return null
        val parts = cached.value.split("|", limit = 2)
        if (parts.size != 2) return null
        return AppResult.Success(
            HomeContent(
                title = parts[0],
                description = parts[1],
            ),
        )
    }
}
