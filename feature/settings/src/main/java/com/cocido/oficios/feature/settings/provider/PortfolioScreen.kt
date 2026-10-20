package com.cocido.oficios.feature.settings.provider

import com.cocido.oficios.feature.settings.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import com.cocido.oficios.core.designsystem.component.OficiosDialog
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.cards.OficiosCard
import com.cocido.oficios.core.designsystem.component.cards.WorkGalleryCard
import com.cocido.oficios.core.designsystem.component.feedback.OficiosContentPhase
import com.cocido.oficios.core.designsystem.component.feedback.OficiosEmptyState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosInlineAlert
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosStateContent
import com.cocido.oficios.core.designsystem.component.inputs.OficiosTextField
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosColorSecondaryText
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.domain.model.WorkCertificateItem

@Composable
fun PortfolioScreen(
    uiState: PortfolioUiState,
    onEvent: (PortfolioEvent) -> Unit,
    onPickGalleryImage: () -> Unit,
    onPickCertificateFile: () -> Unit,
) {
    val title = if (uiState.certificateEditorVisible) {
        if (uiState.editingCertificateId == null) stringResource(R.string.portfolio_new_certificate) else stringResource(R.string.portfolio_edit_certificate)
    } else stringResource(R.string.portfolio_title)
    OficiosScaffold(
        topBar = {
            OficiosBackTopBar(
                title,
                onBack = { onEvent(if (uiState.certificateEditorVisible) PortfolioEvent.CloseCertificateEditor else PortfolioEvent.Back) },
            )
        },
    ) { padding ->
        OficiosStateContent(
            targetState = uiState,
            modifier = Modifier.padding(padding),
            contentKey = { it.phase() },
        ) { state ->
            when (state.phase()) {
                OficiosContentPhase.Loading -> OficiosLoadingState(Modifier.fillMaxSize())
                OficiosContentPhase.Error -> OficiosErrorState(
                    title = stringResource(R.string.portfolio_load_error),
                    description = state.errorMessage.orEmpty(),
                    onRetry = { onEvent(PortfolioEvent.Retry) },
                    modifier = Modifier.fillMaxSize(),
                )
                OficiosContentPhase.Empty,
                OficiosContentPhase.Content,
                -> if (state.certificateEditorVisible) {
                    CertificateEditor(state, onEvent, onPickCertificateFile)
                } else {
                    PortfolioList(state, onEvent, onPickGalleryImage)
                }
            }
        }
    }
    uiState.pendingDelete?.let { target ->
        OficiosDialog(
            title = when (target) {
                is PortfolioDeleteTarget.Gallery -> stringResource(R.string.portfolio_delete_photo)
                is PortfolioDeleteTarget.Certificate -> stringResource(R.string.portfolio_delete_named, target.name)
            },
            message = stringResource(R.string.portfolio_delete_message),
            confirmLabel = stringResource(R.string.portfolio_delete_confirm),
            isDangerous = true,
            onConfirm = { onEvent(PortfolioEvent.ConfirmDelete) },
            onDismiss = { onEvent(PortfolioEvent.DismissDelete) },
        )
    }
}

private fun PortfolioUiState.phase(): OficiosContentPhase {
    val isEmpty = gallery.isEmpty() && certificates.isEmpty()
    return when {
        isLoading && isEmpty -> OficiosContentPhase.Loading
        errorMessage != null && isEmpty && !certificateEditorVisible -> OficiosContentPhase.Error
        else -> OficiosContentPhase.Content
    }
}

@Composable
private fun PortfolioList(
    state: PortfolioUiState,
    onEvent: (PortfolioEvent) -> Unit,
    onPickGalleryImage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = OficiosSpacing.md),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
    ) {
        item {
            Text(
                stringResource(R.string.portfolio_intro),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        state.errorMessage?.let { item { OficiosInlineAlert(it) } }
        item { PortfolioHeader(stringResource(R.string.portfolio_works), stringResource(R.string.portfolio_add_photo), onPickGalleryImage) }
        if (state.gallery.isEmpty()) {
            item {
                OficiosEmptyState(
                    title = stringResource(R.string.portfolio_gallery_empty_title),
                    description = stringResource(R.string.portfolio_gallery_empty_description),
                    ctaLabel = stringResource(R.string.portfolio_add_photo),
                    onCta = onPickGalleryImage,
                )
            }
        } else {
            items(state.gallery.chunked(2), key = { row -> row.joinToString { it.id.toString() } }) { rowItems ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm)) {
                    rowItems.forEach { item ->
                        Column(Modifier.weight(1f)) {
                            WorkGalleryCard(item.imageUrl, stringResource(R.string.portfolio_work_description), modifier = Modifier.fillMaxWidth())
                            OficiosButton(
                                text = stringResource(R.string.portfolio_delete_confirm),
                                onClick = { onEvent(PortfolioEvent.RequestDelete(PortfolioDeleteTarget.Gallery(item.id))) },
                                modifier = Modifier.fillMaxWidth(),
                                variant = OficiosButtonVariant.Ghost,
                                leadingIcon = Icons.Outlined.DeleteOutline,
                                enabled = !state.isSaving,
                            )
                        }
                    }
                    if (rowItems.size == 1) Column(Modifier.weight(1f)) {}
                }
            }
        }
        item { PortfolioHeader(stringResource(R.string.portfolio_certificates), stringResource(R.string.portfolio_add), { onEvent(PortfolioEvent.AddCertificate) }) }
        if (state.certificates.isEmpty()) {
            item {
                OficiosEmptyState(
                    title = stringResource(R.string.portfolio_certificates_empty_title),
                    description = stringResource(R.string.portfolio_certificates_empty_description),
                    ctaLabel = stringResource(R.string.portfolio_add_certificate),
                    onCta = { onEvent(PortfolioEvent.AddCertificate) },
                )
            }
        } else {
            items(state.certificates, key = { it.id }) { item -> CertificateItem(item, state.isSaving, onEvent) }
        }
        item { Column(Modifier.padding(bottom = OficiosSpacing.xl)) {} }
    }
}

@Composable
private fun PortfolioHeader(title: String, action: String, onAction: () -> Unit) {
    val stackAction = LocalDensity.current.fontScale >= 1.3f
    if (stackAction) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            OficiosButton(
                text = action,
                onClick = onAction,
                variant = OficiosButtonVariant.Ghost,
                leadingIcon = Icons.Outlined.AddAPhoto,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    } else {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            OficiosButton(
                text = action,
                onClick = onAction,
                variant = OficiosButtonVariant.Ghost,
                leadingIcon = Icons.Outlined.AddAPhoto,
                modifier = Modifier.widthIn(max = OficiosComponentSize.contentMaxWidth),
            )
        }
    }
}

@Composable
private fun CertificateItem(item: WorkCertificateItem, disabled: Boolean, onEvent: (PortfolioEvent) -> Unit) {
    OficiosCard(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.WorkspacePremium, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
            Column(Modifier.weight(1f).padding(horizontal = OficiosSpacing.sm)) {
                Text(item.name, style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.portfolio_document_note), style = MaterialTheme.typography.bodySmall, color = OficiosColorSecondaryText)
            }
            IconButton(enabled = !disabled, onClick = { onEvent(PortfolioEvent.EditCertificate(item)) }) {
                Icon(
                    imageVector = ImageVector.vectorResource(OficiosIcons.Pencil),
                    contentDescription = stringResource(R.string.portfolio_edit_named, item.name)
                )
            }
            IconButton(enabled = !disabled, onClick = { onEvent(PortfolioEvent.RequestDelete(PortfolioDeleteTarget.Certificate(item.id, item.name))) }) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = stringResource(R.string.portfolio_delete_named, item.name))
            }
        }
    }
}

@Composable
private fun CertificateEditor(
    state: PortfolioUiState,
    onEvent: (PortfolioEvent) -> Unit,
    onPickCertificateFile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = OficiosSpacing.md, vertical = OficiosSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
    ) {
        Text(stringResource(R.string.portfolio_certificate_hint), style = MaterialTheme.typography.bodyMedium)
        OficiosTextField(
            value = state.certificateName,
            onValueChange = { onEvent(PortfolioEvent.CertificateNameChanged(it)) },
            label = stringResource(R.string.portfolio_certificate_name),
            placeholder = stringResource(R.string.portfolio_certificate_name_hint),
        )
        OficiosButton(
            text = if (state.certificateFilePath == null) stringResource(R.string.portfolio_select_file) else stringResource(R.string.portfolio_file_selected),
            onClick = onPickCertificateFile,
            modifier = Modifier.fillMaxWidth(),
            variant = OficiosButtonVariant.Outlined,
            leadingIcon = Icons.Outlined.AttachFile,
        )
        Text(
            if (state.editingCertificateId != null && state.certificateFilePath == null) stringResource(R.string.portfolio_keep_file) else stringResource(R.string.portfolio_file_formats),
            style = MaterialTheme.typography.bodySmall,
            color = OficiosColorSecondaryText,
        )
        state.errorMessage?.let { OficiosInlineAlert(it) }
        OficiosButton(
            text = stringResource(R.string.portfolio_save),
            onClick = { onEvent(PortfolioEvent.SaveCertificate) },
            modifier = Modifier.fillMaxWidth(),
            variant = OficiosButtonVariant.Secondary,
            size = OficiosButtonSize.Large,
            loading = state.isSaving,
            enabled = !state.isSaving,
        )
    }
}
