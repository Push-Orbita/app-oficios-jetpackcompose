package com.cocido.oficios.domain.usecase

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.HomeContent
import com.cocido.oficios.domain.repository.HomeRepository
import javax.inject.Inject

class GetHomeContentUseCase @Inject constructor(
    private val homeRepository: HomeRepository,
) {
    suspend operator fun invoke(forceError: Boolean = false): AppResult<HomeContent> {
        return homeRepository.getHomeContent(forceError = forceError)
    }
}
