package com.cocido.oficios.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.ContactPhone
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.cocido.oficios.core.designsystem.component.OficiosDialog
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.component.avatar.OficiosAvatar
import com.cocido.oficios.core.designsystem.component.badges.OficiosRoleChip
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.cards.OficiosCard
import com.cocido.oficios.core.designsystem.component.feedback.OficiosContentPhase
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosStateContent
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.navigation.OficiosRootTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.domain.model.ThemeMode
import com.cocido.oficios.domain.model.User
import com.cocido.oficios.domain.model.UserRole

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onEvent: (SettingsEvent) -> Unit,
    modifier: Modifier = Modifier,
    showBackNavigation: Boolean = true,
    bottomBar: @Composable () -> Unit = {},
) {
    var confirmLogout by rememberSaveable { mutableStateOf(false) }
    OficiosScaffold(
        modifier = modifier,
        topBar = {
            if (showBackNavigation) {
                OficiosBackTopBar(
                    title = stringResource(R.string.settings_title),
                    onBack = { onEvent(SettingsEvent.NavigateBack) },
                )
            } else {
                OficiosRootTopBar(title = stringResource(R.string.settings_title))
            }
        },
        bottomBar = bottomBar,
    ) { paddingValues ->
        OficiosStateContent(
            targetState = uiState,
            modifier = Modifier.padding(paddingValues),
            contentKey = { it.phase() },
        ) { state ->
            when (state.phase()) {
                OficiosContentPhase.Loading -> OficiosLoadingState(
                    message = stringResource(R.string.settings_loading),
                    modifier = Modifier.fillMaxSize(),
                )
                OficiosContentPhase.Error -> OficiosErrorState(
                    title = stringResource(R.string.settings_load_error_title),
                    description = state.errorMessage.orEmpty(),
                    onRetry = { onEvent(SettingsEvent.Retry) },
                    modifier = Modifier.fillMaxSize(),
                )
                OficiosContentPhase.Empty,
                OficiosContentPhase.Content,
                -> SettingsContent(
                    uiState = state,
                    onEvent = onEvent,
                    onRequestLogout = { confirmLogout = true },
                )
            }
        }
    }
    if (confirmLogout) {
        OficiosDialog(
            title = stringResource(R.string.settings_logout_title),
            message = stringResource(R.string.settings_logout_message),
            confirmLabel = stringResource(R.string.settings_logout_confirm),
            dismissLabel = stringResource(R.string.settings_logout_dismiss),
            onConfirm = {
                confirmLogout = false
                onEvent(SettingsEvent.Logout)
            },
            onDismiss = { confirmLogout = false },
        )
    }
}

private fun SettingsUiState.phase(): OficiosContentPhase = when {
    isLoading && profile == null -> OficiosContentPhase.Loading
    errorMessage != null && profile == null -> OficiosContentPhase.Error
    else -> OficiosContentPhase.Content
}

@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    onEvent: (SettingsEvent) -> Unit,
    onRequestLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val profile = uiState.profile ?: return
    val user = profile.user
    val displayName = "${user.firstName} ${user.lastName}".trim().ifBlank { user.email }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = OficiosSpacing.OficiosScreenPaddingAfterTopBar,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
    ) {
        item {
            SettingsProfileCard(user = user, displayName = displayName)
        }

        item { SettingsSectionTitle(stringResource(R.string.settings_theme_section)) }
        item {
            SettingsAppearanceCard(
                selected = uiState.themeMode,
                onSelect = { onEvent(SettingsEvent.SetThemeMode(it)) },
            )
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
                icon = Icons.Outlined.Article,
                title = stringResource(R.string.settings_terms),
                description = stringResource(R.string.settings_terms_description),
                onClick = { onEvent(SettingsEvent.OpenTerms) },
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

        item { SettingsSectionTitle(stringResource(R.string.settings_support_section)) }
        item {
            SettingsActionRow(
                iconRes = OficiosIcons.CircleQuestionMark,
                title = stringResource(R.string.settings_help),
                description = stringResource(R.string.settings_help_description),
                onClick = { onEvent(SettingsEvent.OpenHelp) },
            )
        }

        item { SettingsSectionTitle(stringResource(R.string.settings_session_section)) }
        item {
            OficiosButton(
                text = stringResource(R.string.settings_logout),
                onClick = onRequestLogout,
                variant = OficiosButtonVariant.Secondary,
                size = OficiosButtonSize.Large,
                loading = uiState.isLoggingOut,
                enabled = !uiState.isLoggingOut,
                modifier = Modifier.fillMaxWidth().widthIn(max = OficiosComponentSize.contentMaxWidth),
            )
        }
        item { Spacer(Modifier.size(OficiosSpacing.xl)) }
    }
}

@Composable
private fun SettingsProfileCard(
    user: User,
    displayName: String,
) {
    val roleLabel = stringResource(
        if (user.role == UserRole.PROVIDER) R.string.settings_provider_role else R.string.settings_client_role,
    )
    val verified = user.emailVerified
    val statusColor = if (verified) OficiosTheme.colors.success else OficiosTheme.colors.warning
    OficiosCard(modifier = Modifier.fillMaxWidth().widthIn(max = OficiosComponentSize.contentMaxWidth)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
        ) {
            OficiosAvatar(
                name = displayName,
                imageUrl = user.profilePhotoUrl,
                size = OficiosComponentSize.avatarSettings,
                showBorder = true,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
            ) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                OficiosRoleChip(label = roleLabel)
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(
                            if (verified) OficiosIcons.ShieldCheck else OficiosIcons.CircleAlert,
                        ),
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(OficiosComponentSize.iconSm),
                    )
                    Text(
                        text = stringResource(
                            if (verified) R.string.settings_verified_email else R.string.settings_pending_email,
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = statusColor,
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.fillMaxWidth().widthIn(max = OficiosComponentSize.contentMaxWidth).padding(top = OficiosSpacing.xs),
    )
}

@Composable
private fun SettingsActionRow(
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconRes: Int? = null,
    enabled: Boolean = true,
) {
    OficiosCard(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = OficiosComponentSize.contentMaxWidth)
            .heightIn(min = OficiosComponentSize.listRowMinHeight)
            .semantics(mergeDescendants = true) { contentDescription = "$title. $description" }
            .clickable(enabled = enabled, onClick = onClick),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            when {
                iconRes != null -> Icon(
                    imageVector = ImageVector.vectorResource(iconRes),
                    contentDescription = null,
                    tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(OficiosComponentSize.iconLg),
                )
                icon != null -> Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(OficiosComponentSize.iconLg),
                )
            }
            Spacer(Modifier.width(OficiosSpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (enabled) {
                Icon(
                    imageVector = ImageVector.vectorResource(OficiosIcons.ChevronRight),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SettingsAppearanceCard(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
) {
    val options = listOf(
        ThemeMode.LIGHT to stringResource(R.string.settings_theme_light),
        ThemeMode.DARK to stringResource(R.string.settings_theme_dark),
        ThemeMode.SYSTEM to stringResource(R.string.settings_theme_system),
    )
    OficiosCard(modifier = Modifier.fillMaxWidth().widthIn(max = OficiosComponentSize.contentMaxWidth)) {
        Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xs)) {
            options.forEach { (mode, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = OficiosComponentSize.listRowMinHeight)
                        .clickable { onSelect(mode) }
                        .semantics(mergeDescendants = true) { contentDescription = label },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = selected == mode,
                        onClick = { onSelect(mode) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary,
                            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                    Spacer(Modifier.width(OficiosSpacing.sm))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (selected == mode) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                }
            }
        }
    }
}
