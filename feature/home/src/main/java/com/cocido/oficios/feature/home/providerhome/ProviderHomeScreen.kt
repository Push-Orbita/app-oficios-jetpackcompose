package com.cocido.oficios.feature.home.providerhome

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.cards.OficiosCard
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosStateContent
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosColorOutlineFill
import com.cocido.oficios.core.designsystem.theme.OficiosColorSecondaryTextFigma
import com.cocido.oficios.core.designsystem.theme.OficiosColorWhite
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosRadius
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.domain.model.BookingStatus
import com.cocido.oficios.domain.model.ProviderHomeContent
import com.cocido.oficios.feature.home.R
import com.cocido.oficios.feature.home.components.ClientHomeBottomBar
import com.cocido.oficios.feature.home.components.ClientHomeTab
import com.cocido.oficios.feature.home.components.HomeDashboardSkeleton
import com.cocido.oficios.feature.home.providerhome.components.ProviderActiveBookingBanner
import com.cocido.oficios.feature.home.providerhome.components.ProviderAgendaSection
import com.cocido.oficios.feature.home.providerhome.components.ProviderHomeHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderHomeScreen(
    uiState: ProviderHomeUiState,
    unreadNotificationCount: Int,
    selectedTab: ClientHomeTab,
    onEvent: (ProviderHomeEvent) -> Unit,
    onTabSelected: (ClientHomeTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    OficiosScaffold(
        modifier = modifier,
        bottomBar = {
            ClientHomeBottomBar(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                showFavorites = false,
            )
        },
    ) { paddingValues ->
        OficiosStateContent(targetState = uiState, modifier = Modifier.padding(paddingValues)) { state ->
            when (state) {
                ProviderHomeUiState.Loading -> {
                    HomeDashboardSkeleton(contentDescription = stringResource(R.string.home_loading))
                }
                is ProviderHomeUiState.Error -> {
                    PullToRefreshBox(
                        isRefreshing = state.isRefreshing,
                        onRefresh = { onEvent(ProviderHomeEvent.Refresh) },
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        OficiosErrorState(
                            title = stringResource(R.string.home_error_title),
                            description = state.message,
                            onRetry = { onEvent(ProviderHomeEvent.Retry) },
                            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                        )
                    }
                }
                is ProviderHomeUiState.Success -> {
                    PullToRefreshBox(
                        isRefreshing = state.isRefreshing,
                        onRefresh = { onEvent(ProviderHomeEvent.Refresh) },
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        ProviderHomeSuccessContent(
                            content = state.content,
                            unreadNotificationCount = unreadNotificationCount,
                            onEvent = onEvent,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProviderHomeSuccessContent(
    content: ProviderHomeContent,
    unreadNotificationCount: Int,
    onEvent: (ProviderHomeEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(OficiosSpacing.OficiosScreenPadding),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.screenGap),
    ) {
        ProviderHomeHeader(
            userName = content.userDisplayName,
            isVerified = content.isVerified,
            specialtyLabel = content.primarySpecialtyLabel,
            unreadNotificationCount = unreadNotificationCount,
            onNotificationsClick = { onEvent(ProviderHomeEvent.OpenNotifications) },
            profilePhotoUrl = content.profilePhotoUrl,
        )

        if (content.hasActiveBooking) {
            val activeOrder = content.upcomingOrders.firstOrNull { it.bookingStatus == BookingStatus.IN_PROGRESS }
            if (activeOrder != null) {
                ProviderActiveBookingBanner(
                    clientName = activeOrder.counterpartName,
                    serviceTitle = activeOrder.subRubroLabel,
                    onClick = { onEvent(ProviderHomeEvent.OrderClicked(activeOrder.id)) },
                )
            }
        }

        ProviderBenefitsSection()

        if (content.profileCompletionPercent < 100) {
            ProviderProfileCompletionSection(
                percent = content.profileCompletionPercent,
                hasGallery = content.hasGalleryPhotos,
                hasLocal = content.hasCommercialLocal,
                hasCertificate = content.hasCertificate,
                onCompleteProfile = { onEvent(ProviderHomeEvent.CompleteProfileClicked) },
            )
        }

        if (content.hasAgendaConfigured) {
            ProviderAgendaSection(
                onViewFullAgenda = { onEvent(ProviderHomeEvent.ViewFullAgendaClicked) },
                upcomingOrders = content.upcomingOrders,
                onOrderClick = { onEvent(ProviderHomeEvent.OrderClicked(it)) },
            )
        } else {
            ProviderCalendarSetupCard(
                onConfigure = { onEvent(ProviderHomeEvent.ViewFullAgendaClicked) },
            )
        }
    }
}

@Composable
private fun ProviderBenefitsSection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
    ) {
        Text(
            text = stringResource(R.string.provider_home_benefits_title),
            style = MaterialTheme.typography.headlineLarge,
            color = OficiosColorWhite,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
        ) {
            ProviderBenefitTile(
                text = stringResource(R.string.provider_home_benefit_visibility),
                modifier = Modifier.weight(1f),
            )
            ProviderBenefitTile(
                text = stringResource(R.string.provider_home_benefit_agenda),
                modifier = Modifier.weight(1f),
            )
            ProviderBenefitTile(
                text = stringResource(R.string.provider_home_benefit_trust),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ProviderBenefitTile(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(118.dp)
            .clip(RoundedCornerShape(OficiosRadius.sm))
            .background(OficiosColorOutlineFill)
            .padding(OficiosSpacing.xs),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = OficiosColorWhite,
        )
    }
}

@Composable
private fun ProviderProfileCompletionSection(
    percent: Int,
    hasGallery: Boolean,
    hasLocal: Boolean,
    hasCertificate: Boolean,
    onCompleteProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
    ) {
        Text(
            text = stringResource(R.string.provider_home_complete_profile_title),
            style = MaterialTheme.typography.headlineLarge,
            color = OficiosColorWhite,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(OficiosRadius.pill))
                .border(1.dp, OficiosColorOutlineFill, RoundedCornerShape(OficiosRadius.pill)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percent.coerceIn(0, 100) / 100f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(OficiosRadius.pill))
                    .background(OficiosColorOutlineFill),
            )
        }
        Text(
            text = stringResource(R.string.provider_home_complete_profile_percent, percent),
            style = MaterialTheme.typography.titleMedium,
            color = OficiosColorWhite,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
        ) {
            ProfileMilestoneTile(
                label = stringResource(R.string.provider_home_tile_gallery),
                done = hasGallery,
                modifier = Modifier.weight(1f),
            )
            ProfileMilestoneTile(
                label = stringResource(R.string.provider_home_tile_local),
                done = hasLocal,
                modifier = Modifier.weight(1f),
            )
            ProfileMilestoneTile(
                label = stringResource(R.string.provider_home_tile_certificate),
                done = hasCertificate,
                modifier = Modifier.weight(1f),
            )
        }
        Text(
            text = stringResource(R.string.provider_home_complete_profile_link),
            style = MaterialTheme.typography.bodyLarge,
            color = OficiosColorWhite,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable(onClick = onCompleteProfile),
        )
    }
}

@Composable
private fun ProfileMilestoneTile(
    label: String,
    done: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(OficiosRadius.sm))
            .background(OficiosColorOutlineFill)
            .padding(OficiosSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(
                if (done) OficiosIcons.CircleCheck else OficiosIcons.CircleDashed,
            ),
            contentDescription = null,
            tint = OficiosColorWhite,
            modifier = Modifier.size(OficiosComponentSize.iconMd),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = OficiosColorWhite,
        )
    }
}

@Composable
private fun ProviderCalendarSetupCard(
    onConfigure: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
    ) {
        Text(
            text = stringResource(R.string.provider_home_calendar_title),
            style = MaterialTheme.typography.headlineLarge,
            color = OficiosColorWhite,
        )
        OficiosCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm)) {
                Text(
                    text = stringResource(R.string.provider_home_calendar_setup_body),
                    style = MaterialTheme.typography.bodyLarge,
                    color = OficiosColorSecondaryTextFigma,
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OficiosButton(
                        text = stringResource(R.string.provider_home_configure_agenda),
                        onClick = onConfigure,
                        variant = OficiosButtonVariant.Secondary,
                    )
                }
            }
        }
    }
}
