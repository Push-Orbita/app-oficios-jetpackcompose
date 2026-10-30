package com.cocido.oficios.domain.usecase.profile

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.ClientDomicilioRequest
import com.cocido.oficios.domain.repository.AccountRepository
import javax.inject.Inject

/**
 * Guarda el barrio que el trabajador elige en "Registro de oficio" como domicilio propio.
 *
 * `POST /trabajador-oficio-rubro/full` sólo acepta `domicilioLocal` cuando algún subrubro es
 * `lugarLocal`; un trabajador que atiende a domicilio no tenía dónde mandar su barrio y la cuenta
 * quedaba sin domicilios (`GET /trabajador-oficio/search?barrioId=` filtra por domicilio/local).
 */
interface ProviderBarrioDomicilioSync {
    suspend operator fun invoke(barrioId: Long, barrioName: String): AppResult<Unit>
}

/**
 * `POST /domicilios/mi` (persona del token). Idempotente: si ya hay un domicilio en ese barrio no
 * crea otro, así los reintentos del checklist no duplican.
 */
class EnsureProviderBarrioDomicilioUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
) : ProviderBarrioDomicilioSync {

    override suspend fun invoke(barrioId: Long, barrioName: String): AppResult<Unit> {
        val existing = when (val result = accountRepository.getClientDomicilios()) {
            is AppResult.Success -> result.data
            is AppResult.Error -> return result
        }
        if (existing.any { it.barrioId == barrioId }) return AppResult.Success(Unit)
        return accountRepository.createClientDomicilio(
            ClientDomicilioRequest(
                barrioId = barrioId,
                // /domicilios/mi resuelve la persona desde el token; personaId no viaja.
                personaId = 0L,
                nombre = barrioName.trim().ifBlank { null },
                favorito = existing.none { it.favorito },
            ),
        )
    }
}