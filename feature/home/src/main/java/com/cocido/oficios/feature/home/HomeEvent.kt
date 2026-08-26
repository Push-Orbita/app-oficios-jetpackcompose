package com.cocido.oficios.feature.home

import com.cocido.oficios.domain.model.HomeCategoryItem
import com.cocido.oficios.domain.model.RecentSearchEntry
import com.cocido.oficios.domain.model.RecentSearchType

sealed interface HomeEvent {
    data object Retry : HomeEvent
    data object OpenSearch : HomeEvent
    data object OpenNotifications : HomeEvent
    data object ContactSupport : HomeEvent
    data class SearchSubmitted(val query: String) : HomeEvent
    data object VoiceSearch : HomeEvent
    data class RecentSearchSelected(val entry: RecentSearchEntry) : HomeEvent
    data class WorkerViewProfile(val workerId: Long) : HomeEvent
    data object WorkerContact : HomeEvent
    data object SeeAllWorkers : HomeEvent
    data object OpenMyOrders : HomeEvent
    data object NavigateToSettings : HomeEvent
    data object NavigateToProfile : HomeEvent
    data object NavigateToFavorites : HomeEvent
    data object NavigateToHelp : HomeEvent
}

sealed interface CategoriesEvent {
    data object Retry : CategoriesEvent
    data object Back : CategoriesEvent
    data object OpenNotifications : CategoriesEvent
    data class CategorySelected(val category: HomeCategoryItem) : CategoriesEvent
    data class SearchSubmitted(val query: String) : CategoriesEvent
    data object VoiceSearch : CategoriesEvent
}

sealed interface HomeEffect {
    data object NavigateToSearch : HomeEffect
    data object NavigateToSettings : HomeEffect
    data object NavigateToProfile : HomeEffect
    data object NavigateToFavorites : HomeEffect
    data object NavigateToHelp : HomeEffect
    data object ContactSupport : HomeEffect
    data class ShowMessage(val message: String) : HomeEffect
    data object ShowComingSoon : HomeEffect
    data class NavigateToRubros(val categoryId: Long, val categoryName: String) : HomeEffect
    data class NavigateToSearchResults(
        val rubroId: Long? = null,
        val rubroLabel: String? = null,
        val categoryId: Long? = null,
        val categoryLabel: String? = null,
        val subRubroId: Long? = null,
        val subRubroLabel: String? = null,
        val query: String? = null,
    ) : HomeEffect
    data class NavigateToWorker(val workerId: Long) : HomeEffect
    data object NavigateToMyOrders : HomeEffect
}

sealed interface CategoriesEffect {
    data object NavigateBack : CategoriesEffect
    data class ShowMessage(val message: String) : CategoriesEffect
    data object ShowComingSoon : CategoriesEffect
    data class NavigateToRubros(val categoryId: Long, val categoryName: String) : CategoriesEffect
    data class NavigateToSearchResults(val query: String) : CategoriesEffect
}
