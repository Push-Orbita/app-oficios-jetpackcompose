package com.cocido.oficios.core.data.remote

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.network.api.DemoApiService
import com.cocido.oficios.core.network.api.GreetingDto
import com.cocido.oficios.core.network.safeApiCall
import javax.inject.Inject

class HomeRemoteDataSource @Inject constructor(
    private val demoApiService: DemoApiService,
) {
    suspend fun fetchHomeContent(): AppResult<GreetingDto> {
        return safeApiCall { demoApiService.getPost(id = 1) }
    }
}
