package com.cocido.oficios.feature.settings.provider

import com.cocido.oficios.domain.model.WorkCertificateItem
import com.cocido.oficios.domain.model.WorkGalleryItem

data class PortfolioUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val gallery: List<WorkGalleryItem> = emptyList(),
    val certificates: List<WorkCertificateItem> = emptyList(),
    val certificateEditorVisible: Boolean = false,
    val editingCertificateId: Long? = null,
    val certificateName: String = "",
    val certificateFilePath: String? = null,
    val currentCertificateUrl: String? = null,
    val pendingDelete: PortfolioDeleteTarget? = null,
    val errorMessage: String? = null,
)

sealed interface PortfolioDeleteTarget {
    data class Gallery(val id: Long) : PortfolioDeleteTarget
    data class Certificate(val id: Long, val name: String) : PortfolioDeleteTarget
}

sealed interface PortfolioEvent {
    data object Retry : PortfolioEvent
    data object Back : PortfolioEvent
    data class GallerySelected(val path: String) : PortfolioEvent
    data object AddCertificate : PortfolioEvent
    data class EditCertificate(val item: WorkCertificateItem) : PortfolioEvent
    data object CloseCertificateEditor : PortfolioEvent
    data class CertificateNameChanged(val value: String) : PortfolioEvent
    data class CertificateFileSelected(val path: String) : PortfolioEvent
    data object SaveCertificate : PortfolioEvent
    data class RequestDelete(val target: PortfolioDeleteTarget) : PortfolioEvent
    data object ConfirmDelete : PortfolioEvent
    data object DismissDelete : PortfolioEvent
}

sealed interface PortfolioEffect {
    data object NavigateBack : PortfolioEffect
    data class ShowMessage(val message: String) : PortfolioEffect
}
