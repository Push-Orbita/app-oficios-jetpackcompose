package com.cocido.oficios.domain.usecase.auth

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.ClientContactoRequest
import com.cocido.oficios.domain.model.ClientDomicilioRequest
import com.cocido.oficios.domain.model.RegisterClientRequest
import com.cocido.oficios.domain.model.RegisterProviderRequest
import com.cocido.oficios.domain.model.RegistrationCommitPhase
import com.cocido.oficios.domain.model.RegistrationDraft
import com.cocido.oficios.domain.model.RegistrationRole
import com.cocido.oficios.domain.repository.RegistrationDraftRepository
import com.cocido.oficios.domain.repository.SessionRepository
import com.cocido.oficios.domain.usecase.profile.SaveClientContactoUseCase
import com.cocido.oficios.domain.usecase.profile.SaveClientDomicilioUseCase
import com.cocido.oficios.domain.validation.AuthValidators
import javax.inject.Inject

sealed interface RegistrationCommitResult {
    data object Completed : RegistrationCommitResult
    data class EmailConflict(val message: String) : RegistrationCommitResult
    data class Failed(val message: String) : RegistrationCommitResult
}

/**
 * Saga idempotente: cuenta → perfil. Si el proceso muere a mitad, reanuda desde [RegistrationCommitPhase]
 * y la sesión cifrada. La contraseña solo viaja en memoria.
 */
class RegistrationCommitter @Inject constructor(
    private val registerClientUseCase: RegisterClientUseCase,
    private val registerProviderUseCase: RegisterProviderUseCase,
    private val saveClientContactoUseCase: SaveClientContactoUseCase,
    private val saveClientDomicilioUseCase: SaveClientDomicilioUseCase,
    private val draftRepository: RegistrationDraftRepository,
    private val sessionRepository: SessionRepository,
) {
    suspend fun commit(draft: RegistrationDraft, password: String): RegistrationCommitResult {
        val stored = draftRepository.get()
        val effective = if (stored != null && stored.role == draft.role) {
            draft.copy(
                commitPhase = maxOf(stored.commitPhase, draft.commitPhase),
                createdUserEmail = stored.createdUserEmail ?: draft.createdUserEmail,
            )
        } else {
            draft
        }
        val ensured = ensureAccount(effective, password)
        val afterAccount = when (ensured) {
            is Step.Ok -> ensured
            is Step.Conflict -> return RegistrationCommitResult.EmailConflict(ensured.message)
            is Step.Fail -> return RegistrationCommitResult.Failed(ensured.message)
        }
        if (afterAccount.draft.role == RegistrationRole.CLIENT) {
            val personaId = afterAccount.personaId
                ?: sessionRepository.getCachedUser()?.personaId
            if (personaId == null) {
                return RegistrationCommitResult.Failed("No se pudo obtener su perfil")
            }
            when (val profile = saveClientProfile(afterAccount.draft, personaId)) {
                is Step.Ok -> Unit
                is Step.Conflict -> return RegistrationCommitResult.EmailConflict(profile.message)
                is Step.Fail -> return RegistrationCommitResult.Failed(profile.message)
            }
        }
        draftRepository.clear()
        return RegistrationCommitResult.Completed
    }

    private suspend fun ensureAccount(draft: RegistrationDraft, password: String): Step {
        if (hasUsableSession(draft)) {
            val marked = draft.copy(
                commitPhase = maxOf(draft.commitPhase, RegistrationCommitPhase.ACCOUNT_CREATED),
                createdUserEmail = draft.createdUserEmail ?: draft.personal.email.trim(),
            )
            draftRepository.save(marked)
            return Step.Ok(marked, sessionRepository.getCachedUser()?.personaId)
        }
        val personal = draft.personal
        val result = when (draft.role) {
            RegistrationRole.CLIENT -> registerClientUseCase(
                RegisterClientRequest(
                    firstName = personal.firstName.trim(),
                    lastName = personal.lastName.trim(),
                    email = personal.email.trim(),
                    password = password,
                    document = personal.document.trim(),
                ),
            )
            RegistrationRole.PROVIDER -> registerProviderUseCase(
                RegisterProviderRequest(
                    firstName = personal.firstName.trim(),
                    lastName = personal.lastName.trim(),
                    email = personal.email.trim(),
                    password = password,
                    document = personal.document.trim(),
                ),
            )
        }
        return when (result) {
            is AppResult.Success -> {
                val next = draft.copy(
                    commitPhase = RegistrationCommitPhase.ACCOUNT_CREATED,
                    createdUserEmail = personal.email.trim(),
                )
                draftRepository.save(next)
                Step.Ok(next, result.data.user.personaId)
            }
            is AppResult.Error -> when (result.error) {
                is AppError.Conflict -> Step.Conflict(
                    result.error.message.ifBlank { "Este correo ya está registrado" },
                )
                else -> Step.Fail(result.error.userMessage())
            }
        }
    }

    private suspend fun saveClientProfile(draft: RegistrationDraft, personaId: Long): Step {
        val client = draft.client
        val barrioId = client.barrioId
            ?: return Step.Fail("Complete barrio y calle antes de crear la cuenta")
        when (
            val contacto = saveClientContactoUseCase(
                ClientContactoRequest(
                    nombre = client.contactName.trim().ifBlank {
                        listOf(draft.personal.firstName, draft.personal.lastName).joinToString(" ").trim()
                    },
                    telefonoWhatsapp = AuthValidators.normalizeArgentinePhone(client.phone),
                    email = draft.personal.email.trim().ifBlank { null },
                    favorito = true,
                ),
            )
        ) {
            is AppResult.Error -> return Step.Fail(contacto.error.userMessage())
            is AppResult.Success -> Unit
        }
        when (
            val domicilio = saveClientDomicilioUseCase(
                ClientDomicilioRequest(
                    barrioId = barrioId,
                    personaId = personaId,
                    nombre = "Domicilio principal",
                    calle = client.calle.trim(),
                    numero = client.numero.trim().ifBlank { null },
                    piso = client.piso.trim().ifBlank { null },
                    departamento = client.departamento.trim().ifBlank { null },
                    favorito = true,
                ),
            )
        ) {
            is AppResult.Error -> return Step.Fail(domicilio.error.userMessage())
            is AppResult.Success -> Unit
        }
        val next = draft.copy(commitPhase = RegistrationCommitPhase.PROFILE_SAVED)
        draftRepository.save(next)
        return Step.Ok(next)
    }

    private suspend fun hasUsableSession(draft: RegistrationDraft): Boolean {
        if (sessionRepository.getAccessToken().isNullOrBlank()) return false
        val cachedEmail = sessionRepository.getCachedUser()?.email?.trim()?.lowercase()
        val draftEmail = (draft.createdUserEmail ?: draft.personal.email).trim().lowercase()
        if (cachedEmail != null && cachedEmail == draftEmail) return true
        return draft.commitPhase >= RegistrationCommitPhase.ACCOUNT_CREATED
    }

    private sealed interface Step {
        data class Ok(val draft: RegistrationDraft, val personaId: Long? = null) : Step
        data class Conflict(val message: String) : Step
        data class Fail(val message: String) : Step
    }
}
