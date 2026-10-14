package com.cocido.oficios.feature.settings.provider

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.cocido.oficios.core.designsystem.component.OficiosDialog
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.cards.OficiosCard
import com.cocido.oficios.core.designsystem.component.cards.WorkGalleryCard
import com.cocido.oficios.core.designsystem.component.feedback.OficiosEmptyState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosInlineAlert
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.inputs.OficiosTextField
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosColorSecondaryText
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
        if (uiState.editingCertificateId == null) "Nuevo certificado" else "Editar certificado"
    } else "Galería y certificados"
    OficiosScaffold(
        topBar = {
            OficiosBackTopBar(
                title,
                onBack = { onEvent(if (uiState.certificateEditorVisible) PortfolioEvent.CloseCertificateEditor else PortfolioEvent.Back) },
            )
        },
    ) { padding ->
        when {
            uiState.isLoading && uiState.gallery.isEmpty() && uiState.certificates.isEmpty() -> OficiosLoadingState(Modifier.fillMaxSize().padding(padding))
            uiState.errorMessage != null && uiState.gallery.isEmpty() && uiState.certificates.isEmpty() && !uiState.certificateEditorVisible -> OficiosErrorState(
                title = "No pudimos cargar tu portfolio",
                description = uiState.errorMessage,
                onRetry = { onEvent(PortfolioEvent.Retry) },
                modifier = Modifier.fillMaxSize().padding(padding),
            )
            uiState.certificateEditorVisible -> CertificateEditor(uiState, onEvent, onPickCertificateFile, Modifier.padding(padding))
            else -> PortfolioList(uiState, onEvent, onPickGalleryImage, Modifier.padding(padding))
        }
    }
    uiState.pendingDelete?.let { target ->
        OficiosDialog(
            title = when (target) {
                is PortfolioDeleteTarget.Gallery -> "Eliminar foto"
                is PortfolioDeleteTarget.Certificate -> "Eliminar ${target.name}"
            },
            message = "Este contenido dejará de verse en tu perfil público.",
            confirmLabel = "Eliminar",
            isDangerous = true,
            onConfirm = { onEvent(PortfolioEvent.ConfirmDelete) },
            onDismiss = { onEvent(PortfolioEvent.DismissDelete) },
        )
    }
}

@Composable
private fun PortfolioList(
    state: PortfolioUiState,
    onEvent: (PortfolioEvent) -> Unit,
    onPickGalleryImage: () -> Unit,
    modifier: Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = OficiosSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
    ) {
        item {
            Text(
                "Usá fotos reales y documentación vigente. Un perfil claro genera más confianza que una galería extensa sin contexto.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        state.errorMessage?.let { item { OficiosInlineAlert(it) } }
        item { PortfolioHeader("Trabajos realizados", "Agregar foto", onPickGalleryImage) }
        if (state.gallery.isEmpty()) {
            item {
                OficiosEmptyState(
                    title = "Tu galería está vacía",
                    description = "Sumá fotos nítidas que muestren el resultado de tu trabajo.",
                    ctaLabel = "Agregar foto",
                    onCta = onPickGalleryImage,
                )
            }
        } else {
            items(state.gallery.chunked(2), key = { row -> row.joinToString { it.id.toString() } }) { rowItems ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm)) {
                    rowItems.forEach { item ->
                        Column(Modifier.weight(1f)) {
                            WorkGalleryCard(item.imageUrl, "Trabajo realizado", modifier = Modifier.fillMaxWidth())
                            OficiosButton(
                                text = "Eliminar",
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
        item { PortfolioHeader("Certificados", "Agregar", { onEvent(PortfolioEvent.AddCertificate) }) }
        if (state.certificates.isEmpty()) {
            item {
                OficiosEmptyState(
                    title = "No cargaste certificados",
                    description = "Podés adjuntar matrículas, cursos o habilitaciones en PDF o imagen.",
                    ctaLabel = "Agregar certificado",
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
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        OficiosButton(action, onAction, variant = OficiosButtonVariant.Ghost, leadingIcon = Icons.Outlined.AddAPhoto)
    }
}

@Composable
private fun CertificateItem(item: WorkCertificateItem, disabled: Boolean, onEvent: (PortfolioEvent) -> Unit) {
    OficiosCard(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.WorkspacePremium, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
            Column(Modifier.weight(1f).padding(horizontal = OficiosSpacing.sm)) {
                Text(item.name, style = MaterialTheme.typography.titleMedium)
                Text("Documento cargado · sin validación administrativa informada", style = MaterialTheme.typography.bodySmall, color = OficiosColorSecondaryText)
            }
            IconButton(enabled = !disabled, onClick = { onEvent(PortfolioEvent.EditCertificate(item)) }) {
                Icon(Icons.Outlined.Edit, contentDescription = "Editar ${item.name}")
            }
            IconButton(enabled = !disabled, onClick = { onEvent(PortfolioEvent.RequestDelete(PortfolioDeleteTarget.Certificate(item.id, item.name))) }) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = "Eliminar ${item.name}")
            }
        }
    }
}

@Composable
private fun CertificateEditor(
    state: PortfolioUiState,
    onEvent: (PortfolioEvent) -> Unit,
    onPickCertificateFile: () -> Unit,
    modifier: Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(OficiosSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
    ) {
        Text("Identificá el documento con un nombre que el cliente pueda entender.", style = MaterialTheme.typography.bodyMedium)
        OficiosTextField(
            value = state.certificateName,
            onValueChange = { onEvent(PortfolioEvent.CertificateNameChanged(it)) },
            label = "Nombre del certificado",
            placeholder = "Ej: Electricista matriculado",
        )
        OficiosButton(
            text = if (state.certificateFilePath == null) "Seleccionar archivo" else "Archivo seleccionado",
            onClick = onPickCertificateFile,
            modifier = Modifier.fillMaxWidth(),
            variant = OficiosButtonVariant.Outlined,
            leadingIcon = Icons.Outlined.AttachFile,
        )
        Text(
            if (state.editingCertificateId != null && state.certificateFilePath == null) "Si no elegís otro archivo, se conservará el actual." else "Formatos admitidos: PDF, JPG, PNG o WEBP. Máximo 10 MB.",
            style = MaterialTheme.typography.bodySmall,
            color = OficiosColorSecondaryText,
        )
        state.errorMessage?.let { OficiosInlineAlert(it) }
        OficiosButton(
            text = "Guardar certificado",
            onClick = { onEvent(PortfolioEvent.SaveCertificate) },
            modifier = Modifier.fillMaxWidth(),
            variant = OficiosButtonVariant.Secondary,
            size = OficiosButtonSize.Large,
            loading = state.isSaving,
            enabled = !state.isSaving,
        )
    }
}
