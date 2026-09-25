package com.cocido.oficios.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.ContactPhone
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MiscellaneousServices
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.component.avatar.OficiosAvatar
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.cards.OficiosCard
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.navigation.OficiosTopAppBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosColorSecondaryText
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.domain.model.UserRole

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onEvent: (SettingsEvent) -> Unit,
    modifier: Modifier = Modifier,
    showBackNavigation: Boolean = true,
    bottomBar: @Composable () -> Unit = {},
) {
    OficiosScaffold(
        modifier = modifier,
        topBar = {
            if (showBackNavigation) {
                OficiosBackTopBar(
                    title = stringResource(R.string.settings_title),
                    onBack = { onEvent(SettingsEvent.NavigateBack) },
                )
            } else {
                OficiosTopAppBar(title = stringResource(R.string.settings_title))
            }
        },
        bottomBar = bottomBar,
    ) { paddingValues ->
        when {
            uiState.isLoading && uiState.profile == null -> OficiosLoadingState(
                message = stringResource(R.string.settings_loading),
                modifier = Modifier.fillMaxSize().padding(paddingValues),
            )
            uiState.errorMessage != null && uiState.profile == null -> OficiosErrorState(
                title = stringResource(R.string.settings_load_error_title),
                description = uiState.errorMessage,
                onRetry = { onEvent(SettingsEvent.Retry) },
                modifier = Modifier.fillMaxSize().padding(paddingValues),
            )
            else -> SettingsContent(
                uiState = uiState,
                onEvent = onEvent,
                modifier = Modifier.padding(paddingValues),
            )
        }
    }
}

@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    onEvent: (SettingsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val profile = uiState.profile ?: return
    val user = profile.user
    val displayName = "${user.firstName} ${user.lastName}".trim().ifBlank { user.email }
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = OficiosSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
    ) {
        item {
            OficiosCard(modifier = Modifier.fillMaxWidth().widthIn(max = OficiosComponentSize.contentMaxWidth)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OficiosAvatar(
                        name = displayName,
                        imageUrl = user.profilePhotoUrl,
                        size = 72.dp,
                        showBorder = true,
                    )
                    Spacer(Modifier.width(OficiosSpacing.md))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = OficiosColorSecondaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
                        ) {
                            Icon(
                                imageVector = if (user.emailVerified) Icons.Outlined.Verified else Icons.Outlined.Badge,
                                contentDescription = null,
                                tint = if (user.emailVerified) MaterialTheme.colorScheme.tertiary else OficiosColorSecondaryText,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = stringResource(
                                    if (user.emailVerified) R.string.settings_verified_email else R.string.settings_pending_email,
                                ),
                                style = MaterialTheme.typography.labelMedium,
                                color = OficiosColorSecondaryText,
                            )
                        }
                        Text(
                            text = stringResource(
                                if (user.role == UserRole.PROVIDER) R.string.settings_provider_role else R.string.settings_client_role,
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }

        item { SettingsSectionTitle(stringResource(R.string.settings_profile_section)) }
        item {
            SettingsActionRow(
                icon = Icons.Outlined.Person,
                title = stringResource(R.string.settings_personal_data),
                description = stringResource(R.string.settings_personal_data_description),
                onClick = { onEvent(SettingsEvent.OpenPersonalData) },
            )
        }
        if (user.role == UserRole.CLIENT) {
            item {
                SettingsActionRow(
                    icon = Icons.Outlined.ContactPhone,
                    title = stringResource(R.string.settings_client_data),
                    description = stringResource(R.string.settings_client_data_description),
                    onClick = { onEvent(SettingsEvent.OpenClientData) },
                )
            }
        }
        if (user.role == UserRole.PROVIDER) {
            item {
                SettingsActionRow(
                    icon = Icons.Outlined.WorkOutline,
                    title = stringResource(R.string.settings_professional_data),
                    description = stringResource(R.string.settings_professional_data_description),
                    onClick = { onEvent(SettingsEvent.OpenProfessionalData) },
                )
            }
            item {
                SettingsActionRow(
                    icon = Icons.Outlined.MiscellaneousServices,
                    title = stringResource(R.string.settings_services),
                    description = stringResource(R.string.settings_services_description),
                    onClick = { onEvent(SettingsEvent.OpenServices) },
                )
            }
            item {
                SettingsActionRow(
                    icon = Icons.Outlined.PhotoLibrary,
                    title = stringResource(R.string.settings_portfolio),
                    description = stringResource(R.string.settings_portfolio_description),
                    onClick = { onEvent(SettingsEvent.OpenPortfolio) },
                )
            }
        }

        item { SettingsSectionTitle(stringResource(R.string.settings_security_section)) }
        item {
            SettingsActionRow(
                icon = Icons.Outlined.Lock,
                title = stringResource(R.string.settings_change_password),
                description = stringResource(R.string.settings_change_password_description),
                onClick = { onEvent(SettingsEvent.OpenChangePassword) },
            )
        }
        item {
            SettingsActionRow(
                icon = Icons.Outlined.DeleteForever,
                title = stringResource(R.string.settings_delete_account),
                description = stringResource(R.string.settings_delete_account_description),
                onClick = { onEvent(SettingsEvent.OpenDeleteAccount) },
            )
        }

        item { SettingsSectionTitle(stringResource(R.string.settings_session_section)) }
        item {
            OficiosButton(
                text = stringResource(R.string.settings_logout),
                onClick = { onEvent(SettingsEvent.Logout) },
                variant = OficiosButtonVariant.Ghost,
                loading = uiState.isLoggingOut,
                enabled = !uiState.isLoggingOut,
                modifier = Modifier.fillMaxWidth().widthIn(max = OficiosComponentSize.contentMaxWidth),
            )
        }
        item { Spacer(Modifier.size(OficiosSpacing.xl)) }
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.fillMaxWidth().widthIn(max = OficiosComponentSize.contentMaxWidth).padding(top = OficiosSpacing.sm),
    )
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    OficiosCard(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = OficiosComponentSize.contentMaxWidth)
            .clickable(enabled = enabled, onClick = onClick),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.primary else OficiosColorSecondaryText,
                modifier = Modifier.size(28.dp),
            )
            Spacer(Modifier.width(OficiosSpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else OficiosColorSecondaryText,
                )
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = OficiosColorSecondaryText)
            }
            if (enabled) {
                Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = OficiosColorSecondaryText)
            }
        }
    }
}
