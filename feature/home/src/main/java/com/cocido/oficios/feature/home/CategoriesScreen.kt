package com.cocido.oficios.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cocido.oficios.core.designsystem.component.badges.OficiosNotificationBadge
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.feature.home.components.ClientHomeBottomBar
import com.cocido.oficios.feature.home.components.ClientHomeTab
import com.cocido.oficios.feature.home.components.HomeCategoriesGrid
import com.cocido.oficios.feature.home.components.HomeSearchEntryField

@Composable
fun CategoriesScreen(
    uiState: CategoriesUiState,
    selectedTab: ClientHomeTab,
    onEvent: (CategoriesEvent) -> Unit,
    onTabSelected: (ClientHomeTab) -> Unit,
    modifier: Modifier = Modifier,
    unreadNotificationCount: Int = 0,
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
            CategoriesUiState.Loading -> {
                OficiosLoadingState(
                    modifier = Modifier.padding(paddingValues),
                    message = stringResource(R.string.home_loading),
                )
            }
            is CategoriesUiState.Error -> {
                OficiosErrorState(
                    title = stringResource(R.string.home_error_title),
                    description = uiState.message,
                    onRetry = { onEvent(CategoriesEvent.Retry) },
                    modifier = Modifier.padding(paddingValues),
                )
            }
            is CategoriesUiState.Success -> {
                var searchQuery by rememberSaveable { mutableStateOf("") }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = OficiosSpacing.lg, vertical = OficiosSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = { onEvent(CategoriesEvent.Back) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = stringResource(R.string.home_back),
                            )
                        }
                        OficiosNotificationBadge(count = unreadNotificationCount) {
                            IconButton(onClick = { onEvent(CategoriesEvent.OpenNotifications) }) {
                                Icon(
                                    imageVector = Icons.Outlined.Notifications,
                                    contentDescription = stringResource(R.string.home_notifications),
                                    modifier = Modifier.size(OficiosComponentSize.iconMd),
                                )
                            }
                        }
                    }
                    HomeSearchEntryField(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onSearch = { onEvent(CategoriesEvent.SearchSubmitted(searchQuery)) },
                        placeholder = stringResource(R.string.home_search_categories_hint),
                    )
                    HomeCategoriesGrid(
                        categories = uiState.categories,
                        onCategoryClick = { onEvent(CategoriesEvent.CategorySelected(it)) },
                        showSeeAll = false,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}
