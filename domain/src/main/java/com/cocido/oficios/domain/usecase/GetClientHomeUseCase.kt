package com.cocido.oficios.domain.usecase

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.ClientHomeContent
import com.cocido.oficios.domain.repository.HomeRepository
import javax.inject.Inject

class GetClientHomeUseCase @Inject constructor(
    private val homeRepository: HomeRepository,
) {
    suspend operator fun invoke(): AppResult<ClientHomeContent> = homeRepository.getClientHome()
}
