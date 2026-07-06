package com.cocido.oficios.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocido.oficios.core.ui.intent.openSupportPhone
import com.cocido.oficios.feature.home.components.ClientHomeTab

@Composable
fun HomeRoute(
    onNavigateToSearch: () -> Unit,
    onNavigateToRubros: (Long, String) -> Unit,
    onNavigateToSearchResults: (
        rubroId: Long?,
        rubroLabel: String?,
        categoryId: Long?,
        categoryLabel: String?,
        subRubroId: Long?,
        subRubroLabel: String?,
        query: String?,
    ) -> Unit,
    onNavigateToWorker: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selectedTab by rememberSaveable { mutableStateOf(ClientHomeTab.INICIO) }
    val comingSoon = stringResource(R.string.home_coming_soon)

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                HomeEffect.NavigateToSearch -> onNavigateToSearch()
                is HomeEffect.NavigateToRubros -> onNavigateToRubros(effect.categoryId, effect.categoryName)
                is HomeEffect.NavigateToSearchResults -> onNavigateToSearchResults(
                    effect.rubroId,
                    effect.rubroLabel,
                    effect.categoryId,
                    effect.categoryLabel,
                    effect.subRubroId,
                    effect.subRubroLabel,
                    effect.query,
                )
                is HomeEffect.NavigateToWorker -> onNavigateToWorker(effect.workerId)
                HomeEffect.NavigateToSettings -> {
                    selectedTab = ClientHomeTab.AJUSTES
                    onNavigateToSettings()
                }
                HomeEffect.NavigateToProfile -> {
                    selectedTab = ClientHomeTab.PERFIL
                    onShowMessage(comingSoon)
                }
                HomeEffect.NavigateToFavorites -> {
                    selectedTab = ClientHomeTab.FAVORITOS
                    onShowMessage(comingSoon)
                }
                HomeEffect.NavigateToHelp -> {
                    selectedTab = ClientHomeTab.AYUDA
                    onShowMessage(comingSoon)
                }
                HomeEffect.ContactSupport -> openSupportPhone(context)
                is HomeEffect.ShowMessage -> onShowMessage(effect.message)
                HomeEffect.ShowComingSoon -> onShowMessage(comingSoon)
            }
        }
    }

    HomeScreen(
        uiState = uiState,
        selectedTab = selectedTab,
        onEvent = viewModel::onEvent,
        onTabSelected = { tab ->
            selectedTab = tab
            when (tab) {
                ClientHomeTab.INICIO -> Unit
                ClientHomeTab.AJUSTES -> onNavigateToSettings()
                ClientHomeTab.PERFIL -> viewModel.onEvent(HomeEvent.NavigateToProfile)
                ClientHomeTab.FAVORITOS -> viewModel.onEvent(HomeEvent.NavigateToFavorites)
                ClientHomeTab.AYUDA -> viewModel.onEvent(HomeEvent.NavigateToHelp)
            }
        },
        modifier = modifier,
    )
}

@Composable
fun CategoriesRoute(
    onNavigateBack: () -> Unit,
    onNavigateToRubros: (Long, String) -> Unit,
    onNavigateToSearchResults: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableStateOf(ClientHomeTab.INICIO) }
    val comingSoon = stringResource(R.string.home_coming_soon)

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                CategoriesEffect.NavigateBack -> onNavigateBack()
                is CategoriesEffect.NavigateToRubros -> onNavigateToRubros(effect.categoryId, effect.categoryName)
                is CategoriesEffect.NavigateToSearchResults -> onNavigateToSearchResults(effect.query)
                is CategoriesEffect.ShowMessage -> onShowMessage(effect.message)
                CategoriesEffect.ShowComingSoon -> onShowMessage(comingSoon)
            }
        }
    }

    CategoriesScreen(
        uiState = uiState,
        selectedTab = selectedTab,
        onEvent = viewModel::onEvent,
        onTabSelected = { tab ->
            selectedTab = tab
            when (tab) {
                ClientHomeTab.INICIO -> onNavigateBack()
                ClientHomeTab.AJUSTES -> onNavigateToSettings()
                else -> onShowMessage(comingSoon)
            }
        },
        modifier = modifier,
    )
}
