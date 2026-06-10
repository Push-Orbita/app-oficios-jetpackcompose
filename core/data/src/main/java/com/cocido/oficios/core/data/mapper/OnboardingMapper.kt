package com.cocido.oficios.core.data.mapper

import com.cocido.oficios.core.network.api.auth.dto.ClientePerfilEstadoDto
import com.cocido.oficios.core.network.api.auth.dto.TrabajadorOficioPerfilMeDto
import com.cocido.oficios.core.network.api.auth.dto.TrabajadorPerfilEstadoDto
import com.cocido.oficios.domain.model.AccountStatus
import com.cocido.oficios.domain.model.ClientProfileStatus
import com.cocido.oficios.domain.model.OnboardingStep
import com.cocido.oficios.domain.model.OnboardingStepStatus
import com.cocido.oficios.domain.model.ProviderProfileStatus

fun ClientePerfilEstadoDto.toDomain(accountStatus: AccountStatus): ClientProfileStatus = ClientProfileStatus(
    isComplete = perfilCompleto,
    missingSteps = faltantes,
    hasContact = requisitos?.tieneContacto == true,
    hasAddress = requisitos?.tieneDomicilio == true,
    emailVerified = requisitos?.emailVerificado == true,
    accountStatus = accountStatus,
)

fun TrabajadorPerfilEstadoDto.toDomain(
    rejectionReason: String? = null,
): ProviderProfileStatus = ProviderProfileStatus(
    accountStatus = estado.toProviderAccountStatus(),
    isConfigurationComplete = configuracionCompleta,
    isProfileComplete = perfilCompleto,
    isOperational = requisitos?.perfilOperativo == true,
    missingSteps = faltantes,
    rejectionReason = rejectionReason,
)

fun TrabajadorOficioPerfilMeDto.toRejectionReason(): String? =
    documentoIdentidad?.motivoRechazo

fun String.toProviderAccountStatus(): AccountStatus = when (uppercase()) {
    "PENDIENTE" -> AccountStatus.PENDING_APPROVAL
    "DOCUMENTACION_RECHAZADA" -> AccountStatus.REJECTED
    "HABILITADO" -> AccountStatus.PENDING_APPROVAL
    "VALIDADO" -> AccountStatus.APPROVED
    "RECHAZADO" -> AccountStatus.REJECTED
    else -> AccountStatus.UNKNOWN
}

fun String.toClientAccountStatus(): AccountStatus = when (uppercase()) {
    "ACTIVO" -> AccountStatus.ACTIVE
    "INACTIVO" -> AccountStatus.INACTIVE
    "BLOQUEADO" -> AccountStatus.BLOCKED
    else -> AccountStatus.UNKNOWN
}

fun ClientProfileStatus.toOnboardingSteps(): List<OnboardingStep> = buildList {
    add(
        OnboardingStep(
            id = "domicilio",
            title = "Domicilio",
            description = "Agregá al menos un domicilio para contratar servicios.",
            status = if (hasAddress) OnboardingStepStatus.COMPLETE else OnboardingStepStatus.PENDING,
        ),
    )
    add(
        OnboardingStep(
            id = "contacto",
            title = "Contacto",
            description = "Agregá al menos un contacto para recibir notificaciones.",
            status = if (hasContact) OnboardingStepStatus.COMPLETE else OnboardingStepStatus.PENDING,
        ),
    )
    if (!emailVerified) {
        add(
            OnboardingStep(
                id = "email_verificado",
                title = "Verificar email",
                description = "Confirmá tu correo electrónico.",
                status = OnboardingStepStatus.PENDING,
                isRequired = false,
            ),
        )
    }
}

fun ProviderProfileStatus.toOnboardingSteps(): List<OnboardingStep> = buildList {
    val stepMap = mapOf(
        "dni_numero" to ("DNI" to "Cargá tu número de documento."),
        "dni_imagenes" to ("Fotos del DNI" to "Subí las imágenes de tu documento."),
        "rubro_principal" to ("Rubro principal" to "Seleccioná tu rubro principal."),
        "subrubros" to ("Subrubros" to "Elegí los subrubros que ofrecés."),
        "disponibilidad" to ("Días laborables" to "Configurá tus días y horarios."),
        "agenda_turnos" to ("Agenda de turnos" to "Configurá turnos si aplica."),
        "local_comercial" to ("Local comercial" to "Agregá tu local si atendés en sitio."),
        "informacion_personal" to ("Información personal" to "Completá tu información básica."),
        "email_verificado" to ("Verificar email" to "Confirmá tu correo electrónico."),
    )

    missingSteps.forEach { missing ->
        val (title, description) = stepMap[missing] ?: (missing to "Completá este paso.")
        add(
            OnboardingStep(
                id = missing,
                title = title,
                description = description,
                status = OnboardingStepStatus.PENDING,
            ),
        )
    }

    if (missingSteps.isEmpty() && !isConfigurationComplete) {
        add(
            OnboardingStep(
                id = "perfil",
                title = "Completar perfil",
                description = "Finalizá la configuración de tu perfil profesional.",
                status = OnboardingStepStatus.PENDING,
            ),
        )
    }
}
