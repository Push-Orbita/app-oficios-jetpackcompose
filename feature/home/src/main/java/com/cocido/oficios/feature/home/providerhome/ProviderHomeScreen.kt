package com.cocido.oficios.feature.home.providerhome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cocido.oficios.core.designsystem.component.cards.BenefitCard
import com.cocido.oficios.core.designsystem.component.cards.ProfileCompletionCard
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.domain.model.BookingStatus
import com.cocido.oficios.domain.model.ProviderHomeContent
import com.cocido.oficios.feature.home.components.ClientHomeBottomBar
import com.cocido.oficios.feature.home.components.ClientHomeTab
import com.cocido.oficios.feature.home.providerhome.components.ProviderActiveBookingBanner
import com.cocido.oficios.feature.home.providerhome.components.ProviderAgendaSection
import com.cocido.oficios.feature.home.providerhome.components.ProviderHomeHeader
import com.cocido.oficios.feature.home.R

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
            )
        },
    ) { paddingValues ->
        when (uiState) {
            ProviderHomeUiState.Loading -> {
                OficiosLoadingState(
                    modifier = Modifier.padding(paddingValues),
                    message = stringResource(R.string.home_loading),
                )
            }
            is ProviderHomeUiState.Error -> {
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = { onEvent(ProviderHomeEvent.Refresh) },
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                ) {
                    OficiosErrorState(
                        title = stringResource(R.string.home_error_title),
                        description = uiState.message,
                        onRetry = { onEvent(ProviderHomeEvent.Retry) },
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    )
                }
            }
            is ProviderHomeUiState.Success -> {
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = { onEvent(ProviderHomeEvent.Refresh) },
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                ) {
                    ProviderHomeSuccessContent(
                        content = uiState.content,
                        unreadNotificationCount = unreadNotificationCount,
                        onEvent = onEvent,
                    )
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
            .padding(horizontal = OficiosSpacing.md, vertical = OficiosSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.lg),
    ) {
        ProviderHomeHeader(
            userName = content.userDisplayName,
            isVerified = content.isVerified,
            specialtyLabel = content.primarySpecialtyLabel,
            unreadNotificationCount = unreadNotificationCount,
            onNotificationsClick = { onEvent(ProviderHomeEvent.OpenNotifications) },
        )

        // Banner "AHORA": condicional, solo si hay un servicio en curso ahora mismo
        // (booking en estado EN_CURSO entre las solicitudes del trabajador).
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

        if (content.benefits.isNotEmpty()) {
            ProviderBenefitsSection(content = content)
        }

        ProfileCompletionCard(
            completionPercent = content.profileCompletionPercent,
            onCompleteProfile = { onEvent(ProviderHomeEvent.CompleteProfileClicked) },
            missingItems = content.missingProfileItems,
        )

        ProviderAgendaSection(
            onViewFullAgenda = { onEvent(ProviderHomeEvent.ViewFullAgendaClicked) },
            upcomingOrders = content.upcomingOrders,
            onOrderClick = { onEvent(ProviderHomeEvent.OrderClicked(it)) },
        )
    }
}

@Composable
private fun ProviderBenefitsSection(
    content: ProviderHomeContent,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
    ) {
        Text(
            text = stringResource(R.string.provider_home_benefits_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val cardWidth = maxWidth * OficiosComponentSize.carouselWidthFraction
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
            ) {
                items(content.benefits) { benefit ->
                    BenefitCard(
                        title = benefit.title,
                        description = benefit.description,
                        discount = benefit.discountLabel,
                        modifier = Modifier.width(cardWidth),
                    )
                }
            }
        }
    }
}
