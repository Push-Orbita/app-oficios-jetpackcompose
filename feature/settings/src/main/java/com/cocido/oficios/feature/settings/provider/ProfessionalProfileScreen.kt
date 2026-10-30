package com.cocido.oficios.feature.settings.provider

import com.cocido.oficios.feature.settings.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.cards.DashedAddTile
import com.cocido.oficios.core.designsystem.component.cards.OficiosSectionCard
import com.cocido.oficios.core.designsystem.component.feedback.OficiosContentPhase
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosStateContent
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.ui.gallery.GalleryMedia
import com.cocido.oficios.core.ui.gallery.OficiosGalleryViewer
import com.cocido.oficios.core.ui.gallery.OficiosPhotoMosaic
import com.cocido.oficios.core.ui.intent.openRemoteDocument
import com.cocido.oficios.domain.model.WorkCertificateItem
import com.cocido.oficios.domain.model.WorkGalleryItem

/** Datos profesionales del trabajador (Ajustes). */
@Composable
fun ProfessionalProfileScreen(
    uiState: ProfessionalProfileUiState,
    onEvent: (ProfessionalProfileEvent) -> Unit,
) {
    OficiosScaffold(
        topBar = {
            OficiosBackTopBar(
                title = stringResource(R.string.professional_profile_title),
                onBack = { onEvent(ProfessionalProfileEvent.Back) },
            )
        },
    ) { padding ->
        OficiosStateContent(
            targetState = uiState,
            modifier = Modifier.padding(padding),
            contentKey = { it.phase() },
        ) { state ->
            when (state.phase()) {
                OficiosContentPhase.Loading -> OficiosLoadingState(modifier = Modifier.fillMaxSize())
                OficiosContentPhase.Error -> OficiosErrorState(
                    title = stringResource(R.string.professional_profile_load_error),
                    description = state.errorMessage ?: stringResource(R.string.professional_profile_retry),
                    onRetry = { onEvent(ProfessionalProfileEvent.Retry) },
                    modifier = Modifier.fillMaxSize(),
                )
                OficiosContentPhase.Empty,
                OficiosContentPhase.Content,
                -> ProfessionalProfileForm(state = state, onEvent = onEvent)
            }
        }
    }
}

private fun ProfessionalProfileUiState.phase(): OficiosContentPhase = when {
    isLoading -> OficiosContentPhase.Loading
    loadFailed -> OficiosContentPhase.Error
    else -> OficiosContentPhase.Content
}

@Composable
private fun ProfessionalProfileForm(
    state: ProfessionalProfileUiState,
    onEvent: (ProfessionalProfileEvent) -> Unit,
) {
    val context = LocalContext.current
    var photoViewerIndex by rememberSaveable { mutableStateOf(-1) }
    var certViewerIndex by rememberSaveable { mutableStateOf(-1) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(OficiosSpacing.OficiosScreenPadding),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.screenGap),
    ) {
        Text(
            text = stringResource(R.string.professional_profile_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OficiosSectionCard(title = stringResource(R.string.professional_profile_oficios)) {
            Text(
                text = state.oficiosLabel.ifBlank { "—" },
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.professional_profile_specialized),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = OficiosSpacing.md),
            )
            Text(
                text = state.specializedLabel.ifBlank { "—" },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = OficiosSpacing.md),
                verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
            ) {
                OficiosButton(
                    text = stringResource(R.string.professional_profile_availability),
                    onClick = { onEvent(ProfessionalProfileEvent.EditAvailability) },
                    variant = OficiosButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth(),
                )
                OficiosButton(
                    text = stringResource(R.string.professional_profile_edit_rubro),
                    onClick = { onEvent(ProfessionalProfileEvent.EditRubro) },
                    variant = OficiosButtonVariant.Secondary,
                    leadingIcon = ImageVector.vectorResource(OficiosIcons.Pencil),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        OficiosSectionCard(title = stringResource(R.string.professional_profile_works)) {
            OficiosPhotoMosaic(
                items = state.gallery.map { it.toGalleryMedia() },
                onItemClick = { photoViewerIndex = it },
            )
            DashedAddTile(
                label = stringResource(R.string.professional_profile_add),
                onClick = { onEvent(ProfessionalProfileEvent.AddWork) },
            )
        }
        OficiosSectionCard(title = stringResource(R.string.professional_profile_certificates)) {
            OficiosPhotoMosaic(
                items = state.certificates.map { cert ->
                    cert.toGalleryMedia(
                        badge = stringResource(
                            if (cert.validated) {
                                R.string.professional_profile_validated
                            } else {
                                R.string.professional_profile_pending
                            },
                        ),
                    )
                },
                onItemClick = { certViewerIndex = it },
            )
            DashedAddTile(
                label = stringResource(R.string.professional_profile_add),
                onClick = { onEvent(ProfessionalProfileEvent.AddCertificate) },
            )
        }
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            OficiosButton(
                text = stringResource(R.string.professional_profile_delete_account),
                onClick = { onEvent(ProfessionalProfileEvent.DeleteAccount) },
                variant = OficiosButtonVariant.Danger,
                size = OficiosButtonSize.Small,
                leadingIcon = ImageVector.vectorResource(OficiosIcons.CircleAlert),
            )
        }
    }
    if (photoViewerIndex >= 0 && state.gallery.isNotEmpty()) {
        OficiosGalleryViewer(
            items = state.gallery.map { it.toGalleryMedia() },
            initialIndex = photoViewerIndex,
            onClose = { photoViewerIndex = -1 },
            onOpenExternal = { openRemoteDocument(context, it) },
        )
    }
    if (certViewerIndex >= 0 && state.certificates.isNotEmpty()) {
        OficiosGalleryViewer(
            items = state.certificates.map { it.toGalleryMedia() },
            initialIndex = certViewerIndex,
            onClose = { certViewerIndex = -1 },
            onOpenExternal = { openRemoteDocument(context, it) },
        )
    }
}

internal fun WorkGalleryItem.toGalleryMedia() = GalleryMedia(id = id, url = imageUrl)

private fun ProfileCertificateUi.toGalleryMedia(badge: String? = null) = GalleryMedia(
    id = id,
    url = fileUrl,
    title = name,
    badge = badge,
)

internal fun WorkCertificateItem.toGalleryMedia() = GalleryMedia(
    id = id,
    url = fileUrl,
    title = name,
)
