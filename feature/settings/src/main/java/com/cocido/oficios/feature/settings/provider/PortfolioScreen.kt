package com.cocido.oficios.feature.settings.provider

import com.cocido.oficios.feature.settings.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cocido.oficios.core.common.contract.ImageUploadContract
import com.cocido.oficios.core.designsystem.component.OficiosDialog
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.component.border.dashedBorder
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.cards.OficiosCard
import com.cocido.oficios.core.designsystem.component.feedback.OficiosContentPhase
import com.cocido.oficios.core.designsystem.component.feedback.OficiosEmptyState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosInlineAlert
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosStateContent
import com.cocido.oficios.core.designsystem.component.inputs.OficiosTextField
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosRadius
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.ui.gallery.OficiosGalleryViewer
import com.cocido.oficios.core.ui.gallery.OficiosPhotoMosaic
import com.cocido.oficios.core.ui.intent.openRemoteDocument
import java.io.File

@Composable
fun PortfolioScreen(
    uiState: PortfolioUiState,
    onEvent: (PortfolioEvent) -> Unit,
    onPickGalleryImage: () -> Unit,
    onPickCertificateFile: () -> Unit,
) {
    val context = LocalContext.current
    var photoViewerIndex by rememberSaveable { mutableStateOf(-1) }
    var certViewerIndex by rememberSaveable { mutableStateOf(-1) }
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
                    PortfolioList(
                        state = state,
                        onEvent = onEvent,
                        onPickGalleryImage = onPickGalleryImage,
                        onOpenPhoto = { photoViewerIndex = it },
                        onOpenCertificate = { certViewerIndex = it },
                    )
                }
            }
        }
    }
    if (!uiState.certificateEditorVisible && photoViewerIndex >= 0 && uiState.gallery.isNotEmpty()) {
        OficiosGalleryViewer(
            items = uiState.gallery.map { it.toGalleryMedia() },
            initialIndex = photoViewerIndex,
            onClose = { photoViewerIndex = -1 },
            onOpenExternal = { openRemoteDocument(context, it) },
        )
    }
    if (!uiState.certificateEditorVisible && certViewerIndex >= 0 && uiState.certificates.isNotEmpty()) {
        OficiosGalleryViewer(
            items = uiState.certificates.map { it.toGalleryMedia() },
            initialIndex = certViewerIndex,
            onClose = { certViewerIndex = -1 },
            onOpenExternal = { openRemoteDocument(context, it) },
        )
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
    onOpenPhoto: (Int) -> Unit,
    onOpenCertificate: (Int) -> Unit,
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
            item {
                OficiosPhotoMosaic(
                    items = state.gallery.map { it.toGalleryMedia() },
                    onItemClick = onOpenPhoto,
                    onDelete = if (state.isSaving) {
                        null
                    } else {
                        { index ->
                            onEvent(
                                PortfolioEvent.RequestDelete(
                                    PortfolioDeleteTarget.Gallery(state.gallery[index].id),
                                ),
                            )
                        }
                    },
                    deleteLabel = stringResource(R.string.portfolio_delete_confirm),
                )
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
            item {
                OficiosPhotoMosaic(
                    items = state.certificates.map { it.toGalleryMedia() },
                    onItemClick = onOpenCertificate,
                    onEdit = if (state.isSaving) {
                        null
                    } else {
                        { index -> onEvent(PortfolioEvent.EditCertificate(state.certificates[index])) }
                    },
                    onDelete = if (state.isSaving) {
                        null
                    } else {
                        { index ->
                            val item = state.certificates[index]
                            onEvent(
                                PortfolioEvent.RequestDelete(
                                    PortfolioDeleteTarget.Certificate(item.id, item.name),
                                ),
                            )
                        }
                    },
                    deleteLabel = stringResource(R.string.portfolio_delete_confirm),
                )
            }
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
private fun CertificateEditor(
    state: PortfolioUiState,
    onEvent: (PortfolioEvent) -> Unit,
    onPickCertificateFile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = OficiosSpacing.md),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
    ) {
        Text(stringResource(R.string.portfolio_certificate_hint), style = MaterialTheme.typography.bodyMedium)
        OficiosTextField(
            value = state.certificateName,
            onValueChange = { onEvent(PortfolioEvent.CertificateNameChanged(it)) },
            label = stringResource(R.string.portfolio_certificate_name),
            placeholder = stringResource(R.string.portfolio_certificate_name_hint),
            enabled = !state.isSaving,
        )
        val selectedPath = state.certificateFilePath
        if (selectedPath == null) {
            CertificateDropZone(
                enabled = !state.isSaving,
                onClick = onPickCertificateFile,
            )
        } else {
            CertificateFilePreview(
                path = selectedPath,
                displayName = state.certificateFileName,
                enabled = !state.isSaving,
                onChange = onPickCertificateFile,
                onRemove = { onEvent(PortfolioEvent.CertificateFileCleared) },
            )
        }
        Text(
            if (state.editingCertificateId != null && state.certificateFilePath == null) {
                stringResource(R.string.portfolio_keep_file)
            } else {
                stringResource(R.string.portfolio_file_formats)
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        state.errorMessage?.let { OficiosInlineAlert(it) }
        OficiosButton(
            text = stringResource(R.string.portfolio_save),
            onClick = { onEvent(PortfolioEvent.SaveCertificate) },
            modifier = Modifier.fillMaxWidth(),
            variant = OficiosButtonVariant.Secondary,
            size = OficiosButtonSize.Large,
            loading = state.isSaving,
            enabled = state.canSaveCertificate,
        )
    }
}

@Composable
private fun CertificateDropZone(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(OficiosRadius.lg)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .dashedBorder(MaterialTheme.colorScheme.outline, OficiosRadius.lg)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = OficiosSpacing.md, vertical = OficiosSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
    ) {
        Icon(
            imageVector = Icons.Outlined.AttachFile,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(OficiosComponentSize.iconLg),
        )
        Text(
            text = stringResource(R.string.portfolio_drop_zone),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.portfolio_file_formats),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CertificateFilePreview(
    path: String,
    displayName: String?,
    enabled: Boolean,
    onChange: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val file = File(path)
    val isPdf = file.extension.equals("pdf", ignoreCase = true)
    val label = displayName?.takeIf { it.isNotBlank() } ?: file.name
    OficiosCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
        ) {
            if (isPdf) {
                Icon(
                    imageVector = ImageVector.vectorResource(OficiosIcons.ScrollText),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(OficiosComponentSize.iconLg),
                )
            } else {
                AsyncImage(
                    model = file,
                    contentDescription = label,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(OficiosRadius.sm)),
                )
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = ImageUploadContract.formatByteSize(file.length()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(enabled = enabled, onClick = onRemove) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = stringResource(R.string.portfolio_remove_file),
                )
            }
        }
        OficiosButton(
            text = stringResource(R.string.portfolio_change_file),
            onClick = onChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = OficiosSpacing.sm),
            variant = OficiosButtonVariant.Outlined,
            enabled = enabled,
        )
    }
}
