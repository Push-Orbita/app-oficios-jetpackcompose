package com.cocido.oficios.feature.settings.provider

import com.cocido.oficios.domain.model.WorkCertificateItem
import com.cocido.oficios.domain.model.WorkGalleryItem

data class ProfileCertificateUi(
    val id: Long,
    val name: String,
    val fileUrl: String?,
    val validated: Boolean,
)

data class ProfessionalProfileUiState(
    val isLoading: Boolean = true,
    val oficiosLabel: String = "",
    val specializedLabel: String = "",
    val gallery: List<WorkGalleryItem> = emptyList(),
    val certificates: List<ProfileCertificateUi> = emptyList(),
    val errorMessage: String? = null,
    val loadFailed: Boolean = false,
)

sealed interface ProfessionalProfileEvent {
    data object Retry : ProfessionalProfileEvent
    data object Back : ProfessionalProfileEvent
    data object EditRubro : ProfessionalProfileEvent
    data object EditAvailability : ProfessionalProfileEvent
    data object AddWork : ProfessionalProfileEvent
    data object AddCertificate : ProfessionalProfileEvent
    data object DeleteAccount : ProfessionalProfileEvent
}

sealed interface ProfessionalProfileEffect {
    data object NavigateBack : ProfessionalProfileEffect
    data object NavigateToServices : ProfessionalProfileEffect
    data object NavigateToAvailability : ProfessionalProfileEffect
    data object NavigateToPortfolio : ProfessionalProfileEffect
    data object NavigateToDeleteAccount : ProfessionalProfileEffect
}

internal fun WorkCertificateItem.toUi(validated: Boolean = false) = ProfileCertificateUi(
    id = id,
    name = name,
    fileUrl = fileUrl,
    validated = validated,
)
