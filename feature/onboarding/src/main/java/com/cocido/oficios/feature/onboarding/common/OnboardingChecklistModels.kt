package com.cocido.oficios.feature.onboarding.common

import com.cocido.oficios.domain.model.InitialDestination
import com.cocido.oficios.domain.model.OnboardingStep
import com.cocido.oficios.domain.model.OnboardingStepStatus
import com.cocido.oficios.domain.model.RegisterTradeDraft

sealed interface OnboardingChecklistUiState {
    data object Loading : OnboardingChecklistUiState
    data class Success(
        val steps: List<OnboardingStep>,
        /** Hay pasos [OnboardingStepStatus.SAVED] que se envían recién cuando aprueben el DNI. */
        val savedWaitingForKyc: Boolean = false,
    ) : OnboardingChecklistUiState
    data class Error(val message: String) : OnboardingChecklistUiState
}

sealed interface OnboardingCompletionPhase {
    data object Idle : OnboardingCompletionPhase
    data object Submitting : OnboardingCompletionPhase
    data class Confirmed(val destination: InitialDestination) : OnboardingCompletionPhase
}

sealed interface OnboardingChecklistEffect {
    data class NavigateAfterCompletion(val destination: InitialDestination) : OnboardingChecklistEffect
}

fun List<OnboardingStep>.hasActionableRequiredSteps(): Boolean =
    any { step ->
        step.isRequired &&
            (step.status == OnboardingStepStatus.PENDING || step.status == OnboardingStepStatus.REJECTED)
    }

fun List<OnboardingStep>.hasStepsInReview(): Boolean =
    any { it.status == OnboardingStepStatus.IN_REVIEW }

fun List<OnboardingStep>.shouldAutoFinalizeProviderProfile(): Boolean =
    size == 1 && first().id == "finalizar_perfil"

private val RUBRO_STEP_IDS = setOf("rubro_principal", "subrubros")
private const val AVAILABILITY_STEP_ID = "disponibilidad"
private const val PAYMENT_METHODS_STEP_ID = "metodos_pago"

internal const val SAVED_WAITING_KYC_DESCRIPTION =
    "Guardado. Se envía cuando aprueben su documentación."
internal const val SAVED_WAITING_AVAILABILITY_DESCRIPTION =
    "Guardado. Se envía al completar Disponibilidad."
internal const val SAVED_WAITING_PAYMENT_METHODS_DESCRIPTION =
    "Guardado. Se envía al completar Métodos de pago."

fun RegisterTradeDraft.hasTrade(): Boolean = rubroId > 0 && subRubroIds.isNotEmpty()

fun RegisterTradeDraft.hasAvailability(): Boolean =
    workDays.isNotEmpty() && (servesAtClientHome || servesAtLocal)

fun RegisterTradeDraft.hasPaymentMethods(): Boolean = paymentMethods.isNotEmpty()

/** Draft con todo lo que exige `POST /trabajador-oficio-rubro/full`. */
fun RegisterTradeDraft.isReadyForRubroFull(): Boolean =
    hasTrade() && hasAvailability() && hasPaymentMethods()

/**
 * Rubro, disponibilidad y métodos de pago se guardan primero en el draft local y se envían
 * juntos (`POST /full`) cuando el draft está completo y el DNI fue aprobado. Mientras tanto la
 * API los sigue listando en `faltantes` y el checklist mostraba "Falta completar" aunque el
 * trabajador ya los hubiera cargado. Acá se marcan como [OnboardingStepStatus.SAVED] con el
 * motivo de espera, para que el checklist refleje lo hecho y diga qué falta.
 *
 * Con DNI aprobado y draft completo no se toca nada: si la API sigue listando el paso, el
 * envío falló y el paso vuelve a ser "Falta completar".
 */
fun List<OnboardingStep>.withLocalDraftProgress(
    draft: RegisterTradeDraft?,
    isIdentityVerified: Boolean,
): List<OnboardingStep> {
    if (draft == null || !draft.hasTrade()) return this
    val missingIds = mapTo(mutableSetOf()) { it.id }
    val waitingDescription = when {
        !isIdentityVerified -> SAVED_WAITING_KYC_DESCRIPTION
        !draft.hasAvailability() && AVAILABILITY_STEP_ID in missingIds ->
            SAVED_WAITING_AVAILABILITY_DESCRIPTION
        !draft.hasPaymentMethods() && PAYMENT_METHODS_STEP_ID in missingIds ->
            SAVED_WAITING_PAYMENT_METHODS_DESCRIPTION
        else -> return this
    }
    return map { step ->
        if (step.status != OnboardingStepStatus.PENDING) return@map step
        val savedLocally = when (step.id) {
            in RUBRO_STEP_IDS -> true
            AVAILABILITY_STEP_ID -> draft.hasAvailability()
            PAYMENT_METHODS_STEP_ID -> draft.hasPaymentMethods()
            else -> false
        }
        if (savedLocally) {
            step.copy(status = OnboardingStepStatus.SAVED, description = waitingDescription)
        } else {
            step
        }
    }
}

fun List<OnboardingStep>.hasSavedSteps(): Boolean =
    any { it.status == OnboardingStepStatus.SAVED }
