package com.cocido.oficios.feature.home.providerhome

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import com.cocido.oficios.core.designsystem.component.cards.HomeBenefitTile
import com.cocido.oficios.core.designsystem.component.cards.ProfileMilestoneTile
import com.cocido.oficios.core.designsystem.component.cards.ProfileProgressBar
import com.cocido.oficios.core.designsystem.component.cards.emphasizedBody
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosStateContent
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosColorWhite
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.domain.model.ProviderHomeContent
import com.cocido.oficios.feature.home.R
import com.cocido.oficios.feature.home.components.ClientHomeBottomBar
import com.cocido.oficios.feature.home.components.ClientHomeTab
import com.cocido.oficios.feature.home.components.HomeDashboardSkeleton
import com.cocido.oficios.feature.home.providerhome.components.ProviderAgendaSection
import com.cocido.oficios.feature.home.providerhome.components.ProviderHomeHeader
import java.time.ZonedDateTime

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
                            now = state.now,
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
    now: ZonedDateTime,
    onEvent: (ProviderHomeEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
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

        ProviderBenefitsSection()

        if (content.showProfileCompletion) {
            ProviderProfileCompletionSection(
                percent = content.profileCompletionPercent,
                hasGallery = content.hasGalleryPhotos,
                hasLocal = content.hasCommercialLocal,
                hasCertificate = content.hasCertificate,
                onCompleteProfile = { onEvent(ProviderHomeEvent.CompleteProfileClicked) },
            )
        }

        ProviderAgendaSection(
            upcomingOrders = content.upcomingOrders,
            now = now,
            onViewFullAgenda = {
                if (content.hasAgendaConfigured) {
                    onEvent(ProviderHomeEvent.ViewFullAgendaClicked)
                } else {
                    onEvent(ProviderHomeEvent.ConfigureAgendaClicked)
                }
            },
            onOrderClick = { onEvent(ProviderHomeEvent.OrderClicked(it)) },
        )
    }
}

@Composable
private fun ProviderBenefitsSection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.s10),
    ) {
        Text(
            text = stringResource(R.string.provider_home_benefits_title),
            style = MaterialTheme.typography.headlineLarge,
            color = OficiosColorWhite,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.s10),
        ) {
            HomeBenefitTile(
                text = emphasizedBody(
                    stringResource(R.string.provider_home_benefit_services),
                    stringResource(R.string.provider_home_benefit_services_emphasis),
                ),
                modifier = Modifier.weight(1f),
            )
            HomeBenefitTile(
                text = emphasizedBody(
                    stringResource(R.string.provider_home_benefit_reach),
                    stringResource(R.string.provider_home_benefit_reach_emphasis),
                ),
                modifier = Modifier.weight(1f),
            )
            HomeBenefitTile(
                text = emphasizedBody(
                    stringResource(R.string.provider_home_benefit_efficient),
                    stringResource(R.string.provider_home_benefit_efficient_emphasis),
                ),
                modifier = Modifier.weight(1f),
            )
        }
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
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.s10),
    ) {
        Text(
            text = stringResource(R.string.provider_home_complete_profile_title),
            style = MaterialTheme.typography.headlineLarge,
            color = OficiosColorWhite,
        )
        ProfileProgressBar(percent = percent)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.s10),
        ) {
            ProfileMilestoneTile(
                label = stringResource(R.string.provider_home_tile_gallery),
                done = hasGallery,
                onClick = onCompleteProfile,
                modifier = Modifier.weight(1f),
            )
            ProfileMilestoneTile(
                label = stringResource(R.string.provider_home_tile_local),
                done = hasLocal,
                onClick = onCompleteProfile,
                modifier = Modifier.weight(1f),
            )
            ProfileMilestoneTile(
                label = stringResource(R.string.provider_home_tile_certificate),
                done = hasCertificate,
                onClick = onCompleteProfile,
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.provider_home_complete_profile_link),
                style = MaterialTheme.typography.bodyLarge,
                color = OficiosColorWhite,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable(onClick = onCompleteProfile),
            )
        }
    }
}
