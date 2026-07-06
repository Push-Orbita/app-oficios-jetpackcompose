package com.cocido.oficios.domain.usecase.auth

import com.cocido.oficios.domain.model.ThemeMode
import com.cocido.oficios.domain.repository.SessionPreferencesRepository
import com.cocido.oficios.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApplyLoginSessionPreferenceUseCaseTest {

    @Test
    fun `rememberMe true persists session and clears ephemeral flag`() = runTest {
        var rememberSession: Boolean? = null
        var sessionEphemeral: Boolean? = null
        val useCase = ApplyLoginSessionPreferenceUseCase(
            sessionPreferencesRepository = object : SessionPreferencesRepository {
                override fun setSessionEphemeral(ephemeral: Boolean) {
                    sessionEphemeral = ephemeral
                }

                override fun isSessionEphemeral(): Boolean = sessionEphemeral == true
            },
            userPreferencesRepository = object : UserPreferencesRepository {
                override val themeMode: Flow<ThemeMode> = flowOf(ThemeMode.SYSTEM)
                override suspend fun setThemeMode(themeMode: ThemeMode) = Unit
                override suspend fun getRememberSession(): Boolean = rememberSession == true
                override suspend fun setRememberSession(remember: Boolean) {
                    rememberSession = remember
                }
            },
        )

        useCase(rememberMe = true)

        assertTrue(rememberSession == true)
        assertFalse(sessionEphemeral == true)
    }

    @Test
    fun `rememberMe false marks session ephemeral`() = runTest {
        var sessionEphemeral: Boolean? = null
        val useCase = ApplyLoginSessionPreferenceUseCase(
            sessionPreferencesRepository = object : SessionPreferencesRepository {
                override fun setSessionEphemeral(ephemeral: Boolean) {
                    sessionEphemeral = ephemeral
                }

                override fun isSessionEphemeral(): Boolean = sessionEphemeral == true
            },
            userPreferencesRepository = object : UserPreferencesRepository {
                override val themeMode: Flow<ThemeMode> = flowOf(ThemeMode.SYSTEM)
                override suspend fun setThemeMode(themeMode: ThemeMode) = Unit
                override suspend fun getRememberSession(): Boolean = false
                override suspend fun setRememberSession(remember: Boolean) = Unit
            },
        )

        useCase(rememberMe = false)

        assertTrue(sessionEphemeral == true)
    }
}
