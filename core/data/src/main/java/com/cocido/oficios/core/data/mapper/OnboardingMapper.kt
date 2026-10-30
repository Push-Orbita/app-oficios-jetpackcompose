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
    phoneVerified = requisitos?.telefonoVerificado == true,
    accountStatus = accountStatus,
)

fun TrabajadorPerfilEstadoDto.toDomain(
    rejectionReason: String? = null,
): ProviderProfileStatus = ProviderProfileStatus(
    accountStatus = estado.toProviderAccountStatus(),
    isConfigurationComplete = configuracionCompleta,
    isProfileComplete = perfilCompleto,
    isOperational = requisitos?.perfilOperativo == true,
    isIdentityVerified = requisitos?.identidadVerificada == true,
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

fun ClientProfileStatus.toOnboardingSteps(): List<OnboardingStep> {
    if (isComplete) return emptyList()
    return buildList {
        // "domicilio", "contacto" y "telefono_verificado" llevan todos a la misma pantalla.
        // El paso de teléfono solo existe si la API lo lista en faltantes. Un
        // requisitos.telefonoVerificado omitido no debe inventar "Verificar teléfono".
        val needsPhoneVerification = missingSteps.contains("telefono_verificado")
        var phoneVerificationMerged = false

    if (!hasAddress) {
        add(
            OnboardingStep(
                id = "domicilio",
                title = "Domicilio",
                description = if (needsPhoneVerification) {
                    "Agregue su dirección y confirme su WhatsApp para poder contratar servicios."
                } else {
                    "Agregue al menos un domicilio para contratar servicios."
                },
                status = OnboardingStepStatus.PENDING,
            ),
        )
        phoneVerificationMerged = needsPhoneVerification
    }
    if (!hasContact) {
        add(
            OnboardingStep(
                id = "contacto",
                title = "Contacto",
                description = if (needsPhoneVerification && !phoneVerificationMerged) {
                    "Agregue su contacto y confirme su WhatsApp para recibir notificaciones."
                } else {
                    "Agregue al menos un contacto para recibir notificaciones."
                },
                status = OnboardingStepStatus.PENDING,
            ),
        )
        phoneVerificationMerged = phoneVerificationMerged || needsPhoneVerification
    }
    if (!emailVerified) {
        add(
            OnboardingStep(
                id = "email_verificado",
                title = "Verificar email",
                description = "Confirme su correo electrónico.",
                status = OnboardingStepStatus.PENDING,
                isRequired = false,
            ),
        )
    }
    if (needsPhoneVerification && !phoneVerificationMerged) {
        add(
            OnboardingStep(
                id = "telefono_verificado",
                title = "Verificar teléfono",
                description = "Confirme su número de WhatsApp con el código que le enviamos.",
                status = OnboardingStepStatus.PENDING,
            ),
        )
    }
    }
}

fun ProviderProfileStatus.toOnboardingSteps(): List<OnboardingStep> = buildList {
    val preIdentityStepIds = setOf(
        "dni_numero",
        "dni_imagenes",
        "dni_pendiente_revision",
        "dni_rechazado",
        "selfie_imagen",
        "selfie_pendiente_revision",
        "selfie_rechazada",
        "email_verificado",
    )
    val stepMap = mapOf(
        "dni_numero" to ("DNI" to "Cargue su número de documento."),
        "dni_imagenes" to ("Fotos del DNI" to "Suba las imágenes de su documento."),
        "dni_pendiente_revision" to (
            "DNI en revisión" to
                "El equipo está validando su documento. Hay que esperar la aprobación antes de usar la app."
            ),
        "dni_rechazado" to ("DNI rechazado" to "Vuelva a cargar su documentación."),
        "selfie_imagen" to ("Foto de rostro" to "Suba una selfie para validar su identidad."),
        "selfie_pendiente_revision" to (
            "Selfie en revisión" to
                "El equipo está validando su foto. Hay que esperar la aprobación antes de usar la app."
            ),
        "selfie_rechazada" to ("Selfie rechazada" to "Vuelva a cargar su foto de rostro."),
        "descripcion" to ("Descripción profesional" to "Cuente qué servicios ofrece."),
        "experiencia" to ("Experiencia" to "Indique su experiencia en el rubro."),
        "rubro_principal" to ("Rubro y especialidades" to "Seleccione su rubro y las especialidades que ofrece."),
        "subrubros" to ("Rubro y especialidades" to "Seleccione su rubro y las especialidades que ofrece."),
        "disponibilidad" to ("Días laborables" to "Configure sus días y horarios."),
        "agenda_turnos" to ("Agenda de turnos" to "Configure turnos si aplica."),
        "local_comercial" to ("Local comercial" to "Agregue su local si atiende en sitio."),
        "metodos_pago" to ("Métodos de pago" to "Seleccione cómo quiere cobrar."),
        "informacion_personal" to ("Información personal" to "Complete su información básica."),
        "finalizar_perfil" to ("Finalizar perfil" to "Confirme su perfil para publicarlo."),
        "email_verificado" to ("Verificar email" to "Confirme su correo electrónico."),
    )

    missingSteps.forEach { missing ->
        if (!isIdentityVerified && missing !in preIdentityStepIds) return@forEach
        if (missing == "subrubros" && missingSteps.contains("rubro_principal")) return@forEach
        val (title, description) = stepMap[missing] ?: (missing to "Complete este paso.")
        val status = when (missing) {
            "dni_pendiente_revision", "selfie_pendiente_revision" -> OnboardingStepStatus.IN_REVIEW
            else -> OnboardingStepStatus.PENDING
        }
        add(
            OnboardingStep(
                id = missing,
                title = title,
                description = description,
                status = status,
            ),
        )
    }
}
