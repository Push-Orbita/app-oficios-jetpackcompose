package com.cocido.oficios.feature.home

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.HomeContent
import com.cocido.oficios.domain.repository.HomeRepository

class FakeHomeRepository(
    private var result: AppResult<HomeContent> = AppResult.Success(
        HomeContent(
            title = "Test title",
            description = "Test description",
        ),
    ),
) : HomeRepository {

    var forceErrorRequested: Boolean = false

    override suspend fun getHomeContent(forceError: Boolean): AppResult<HomeContent> {
        forceErrorRequested = forceError
        return if (forceError) {
            AppResult.Error(
                com.cocido.oficios.core.common.result.AppError.Validation(
                    message = "Simulated error",
                ),
            )
        } else {
            result
        }
    }
}
