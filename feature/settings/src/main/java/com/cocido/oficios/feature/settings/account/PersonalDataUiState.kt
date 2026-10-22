package com.cocido.oficios.feature.settings.account

import com.cocido.oficios.domain.validation.AuthValidators

data class PersonalDataUiState(
    val isLoading: Boolean = true,
    val userId: Long = 0L,
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val currentPhotoUrl: String? = null,
    val selectedPhotoPath: String? = null,
    val removePhoto: Boolean = false,
    val firstNameError: String? = null,
    val lastNameError: String? = null,
    val emailError: String? = null,
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
) {
    val canSave: Boolean
        get() = AuthValidators.validateName(firstName, "nombres") == null &&
            AuthValidators.validateName(lastName, "apellido") == null &&
            AuthValidators.validateEmail(email.trim()) == null
}

sealed interface PersonalDataEvent {
    data object Retry : PersonalDataEvent
    data class FirstNameChanged(val value: String) : PersonalDataEvent
    data class LastNameChanged(val value: String) : PersonalDataEvent
    data class EmailChanged(val value: String) : PersonalDataEvent
    data class PhotoSelected(val path: String) : PersonalDataEvent
    data object RemovePhoto : PersonalDataEvent
    data object Save : PersonalDataEvent
    data object Back : PersonalDataEvent
}

sealed interface PersonalDataEffect {
    data object NavigateBack : PersonalDataEffect
    data class Saved(val message: String) : PersonalDataEffect
}
