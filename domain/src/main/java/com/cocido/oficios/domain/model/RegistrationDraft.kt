package com.cocido.oficios.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class RegistrationRole {
    CLIENT,
    PROVIDER,
}

@Serializable
enum class RegistrationCommitPhase {
    NOT_STARTED,
    ACCOUNT_CREATED,
    PROFILE_SAVED,
    COMPLETED,
}

@Serializable
data class RegistrationPersonalDraft(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val document: String = "",
    val acceptedTerms: Boolean = false,
)

@Serializable
data class RegistrationClientDraft(
    val contactName: String = "",
    val phone: String = "",
    val barrioQuery: String = "",
    val barrioId: Long? = null,
    val calle: String = "",
    val numero: String = "",
    val piso: String = "",
    val departamento: String = "",
)

/**
 * Borrador del wizard de registro. La contraseña NUNCA se serializa ni se guarda en disco.
 */
@Serializable
data class RegistrationDraft(
    val role: RegistrationRole = RegistrationRole.CLIENT,
    val personal: RegistrationPersonalDraft = RegistrationPersonalDraft(),
    val client: RegistrationClientDraft = RegistrationClientDraft(),
    val provider: RegisterTradeDraft? = null,
    val commitPhase: RegistrationCommitPhase = RegistrationCommitPhase.NOT_STARTED,
    val createdUserEmail: String? = null,
)
