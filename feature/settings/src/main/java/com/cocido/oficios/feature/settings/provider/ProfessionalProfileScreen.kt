package com.cocido.oficios.feature.settings.provider

import com.cocido.oficios.feature.settings.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.cards.DashedAddTile
import com.cocido.oficios.core.designsystem.component.feedback.OficiosContentPhase
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosStateContent
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosColorAction
import com.cocido.oficios.core.designsystem.theme.OficiosColorOutlineFill
import com.cocido.oficios.core.designsystem.theme.OficiosColorWhite
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosRadius
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing

/** Editar Perfil — no está en Flujos; fuente Rediseño `231:15`. */
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
    val fieldShape = RoundedCornerShape(OficiosRadius.sm)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(OficiosSpacing.OficiosScreenPadding),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.screenGap),
    ) {
        Text(
            text = stringResource(R.string.professional_profile_oficios),
            style = MaterialTheme.typography.titleMedium,
            color = OficiosColorWhite,
        )
        ReadOnlyField(value = state.oficiosLabel, shape = fieldShape)
        Text(
            text = stringResource(R.string.professional_profile_specialized),
            style = MaterialTheme.typography.titleMedium,
            color = OficiosColorWhite,
        )
        ReadOnlyField(value = state.specializedLabel, shape = fieldShape, minLines = 3)
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            OficiosButton(
                text = stringResource(R.string.professional_profile_edit_rubro),
                onClick = { onEvent(ProfessionalProfileEvent.EditRubro) },
                variant = OficiosButtonVariant.Primary,
                leadingIcon = ImageVector.vectorResource(OficiosIcons.Pencil),
            )
        }
        Text(
            text = stringResource(R.string.professional_profile_works),
            style = MaterialTheme.typography.headlineMedium,
            color = OficiosColorWhite,
        )
        MediaRow(
            items = state.gallery.map { it.id to it.imageUrl },
            addLabel = stringResource(R.string.professional_profile_add),
            onAdd = { onEvent(ProfessionalProfileEvent.AddWork) },
        )
        Text(
            text = stringResource(R.string.professional_profile_certificates),
            style = MaterialTheme.typography.headlineMedium,
            color = OficiosColorWhite,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm)) {
            state.certificates.take(2).forEach { cert ->
                CertificateThumb(
                    item = cert,
                    modifier = Modifier.size(OficiosComponentSize.profileMediaTile),
                )
            }
            DashedAddTile(
                label = stringResource(R.string.professional_profile_add),
                onClick = { onEvent(ProfessionalProfileEvent.AddCertificate) },
            )
        }
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            OficiosButton(
                text = stringResource(R.string.professional_profile_delete_account),
                onClick = { onEvent(ProfessionalProfileEvent.DeleteAccount) },
                variant = OficiosButtonVariant.Primary,
                size = OficiosButtonSize.Small,
                leadingIcon = ImageVector.vectorResource(OficiosIcons.CircleAlert),
            )
        }
    }
}

@Composable
private fun ReadOnlyField(
    value: String,
    shape: RoundedCornerShape,
    minLines: Int = 1,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(OficiosColorOutlineFill)
            .padding(horizontal = OficiosSpacing.md, vertical = OficiosSpacing.sm),
    ) {
        Text(
            text = value.ifBlank { "—" },
            style = MaterialTheme.typography.bodyLarge,
            color = OficiosColorWhite,
            minLines = minLines,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun MediaRow(
    items: List<Pair<Long, String?>>,
    addLabel: String,
    onAdd: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm)) {
        items.take(2).forEach { (_, url) ->
            val shape = RoundedCornerShape(OficiosRadius.lg)
            Box(
                modifier = Modifier
                    .size(OficiosComponentSize.profileMediaTile)
                    .clip(shape)
                    .background(OficiosColorOutlineFill),
            ) {
                if (!url.isNullOrBlank()) {
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
        DashedAddTile(label = addLabel, onClick = onAdd)
    }
}

@Composable
private fun CertificateThumb(
    item: ProfileCertificateUi,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(OficiosRadius.lg)
    Box(
        modifier = modifier
            .clip(shape)
            .background(OficiosColorOutlineFill),
    ) {
        if (!item.fileUrl.isNullOrBlank()) {
            AsyncImage(
                model = item.fileUrl,
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(OficiosColorAction.copy(alpha = 0.92f))
                .padding(horizontal = OficiosSpacing.xs, vertical = OficiosSpacing.xxs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xxs),
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(
                    if (item.validated) OficiosIcons.CircleCheck else OficiosIcons.CircleDashed,
                ),
                contentDescription = null,
                tint = OficiosColorWhite,
                modifier = Modifier.size(OficiosComponentSize.iconSm),
            )
            Text(
                text = stringResource(
                    if (item.validated) {
                        R.string.professional_profile_validated
                    } else {
                        R.string.professional_profile_pending
                    },
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = OficiosColorWhite,
            )
        }
    }
}
