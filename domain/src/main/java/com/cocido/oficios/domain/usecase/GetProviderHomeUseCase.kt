package com.cocido.oficios.domain.usecase

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.ProviderHomeContent
import com.cocido.oficios.domain.repository.HomeRepository
import javax.inject.Inject

class GetProviderHomeUseCase @Inject constructor(
    private val homeRepository: HomeRepository,
) {
    suspend operator fun invoke(): AppResult<ProviderHomeContent> = homeRepository.getProviderHome()
}
