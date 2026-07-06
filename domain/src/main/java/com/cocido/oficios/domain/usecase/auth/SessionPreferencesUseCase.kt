package com.cocido.oficios.domain.usecase.auth

import com.cocido.oficios.domain.repository.SessionPreferencesRepository
import com.cocido.oficios.domain.repository.UserPreferencesRepository
import javax.inject.Inject

class ApplyLoginSessionPreferenceUseCase @Inject constructor(
    private val sessionPreferencesRepository: SessionPreferencesRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    suspend operator fun invoke(rememberMe: Boolean) {
        userPreferencesRepository.setRememberSession(rememberMe)
        sessionPreferencesRepository.setSessionEphemeral(!rememberMe)
    }
}

class ClearEphemeralSessionIfNeededUseCase @Inject constructor(
    private val sessionPreferencesRepository: SessionPreferencesRepository,
    private val sessionRepository: com.cocido.oficios.domain.repository.SessionRepository,
) {
    suspend operator fun invoke() {
        if (sessionPreferencesRepository.isSessionEphemeral()) {
            sessionRepository.clearSession()
        }
    }
}
