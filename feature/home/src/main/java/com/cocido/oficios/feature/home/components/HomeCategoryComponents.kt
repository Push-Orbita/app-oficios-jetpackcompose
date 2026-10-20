package com.cocido.oficios.feature.home.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.chips.OficiosChip
import com.cocido.oficios.core.designsystem.component.cards.WorkerCard
import com.cocido.oficios.core.designsystem.component.feedback.OficiosEmptyState
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.domain.model.HomeCategoryItem
import com.cocido.oficios.domain.model.RecentSearchEntry
import com.cocido.oficios.domain.model.WorkerSummary
import com.cocido.oficios.feature.home.R
import coil.compose.SubcomposeAsyncImage
import com.cocido.oficios.core.designsystem.component.categories.CategoryIconResolver

@Composable
fun HomeFeaturedWorkersSection(
    workers: List<WorkerSummary>,
    onViewProfile: (WorkerSummary) -> Unit,
    onContact: (WorkerSummary) -> Unit,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (workers.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.home_featured_workers_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            OficiosButton(
                text = stringResource(R.string.home_featured_workers_see_all),
                onClick = onSeeAll,
                variant = OficiosButtonVariant.Ghost,
            )
        }
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val cardWidth = maxWidth * OficiosComponentSize.carouselWidthFraction
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
                contentPadding = PaddingValues(end = OficiosSpacing.xs),
            ) {
                items(workers, key = { it.id }) { worker ->
                    WorkerCard(
                        name = worker.displayName,
                        specialty = worker.specialtyLabel,
                        rating = worker.rating,
                        avatarUrl = worker.avatarUrl,
                        isVerified = worker.isVerified,
                        contactLabel = stringResource(R.string.home_worker_contact),
                        viewProfileLabel = stringResource(R.string.home_worker_view_profile),
                        onContact = { onContact(worker) },
                        onViewProfile = { onViewProfile(worker) },
                        modifier = Modifier.width(cardWidth),
                    )
                }
            }
        }
    }
}

@Composable
fun HomeRecentSearchesSection(
    searches: List<RecentSearchEntry>,
    onSearchClick: (RecentSearchEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
    ) {
        Text(
            text = stringResource(R.string.home_recent_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
            contentPadding = PaddingValues(end = OficiosSpacing.xs),
        ) {
            items(searches, key = { "${it.type}-${it.id}" }) { entry ->
                OficiosChip(
                    label = entry.label,
                    onClick = { onSearchClick(entry) },
                )
            }
        }
    }
}

@Composable
fun HomeRecentCategoriesSection(
    categories: List<HomeCategoryItem>,
    onCategoryClick: (HomeCategoryItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    HomeRecentSearchesSection(
        searches = categories.map { category ->
            RecentSearchEntry(
                type = com.cocido.oficios.domain.model.RecentSearchType.CATEGORY,
                id = category.id,
                label = category.name,
                categoryId = category.id,
            )
        },
        onSearchClick = { entry ->
            onCategoryClick(HomeCategoryItem(entry.id, entry.label))
        },
        modifier = modifier,
    )
}

@Composable
fun HomeCategoriesGrid(
    categories: List<HomeCategoryItem>,
    onCategoryClick: (HomeCategoryItem) -> Unit,
    modifier: Modifier = Modifier,
    showSeeAll: Boolean = true,
    onSeeAllClick: () -> Unit = {},
) {
    if (categories.isEmpty()) {
        OficiosEmptyState(
            title = stringResource(R.string.home_categories_empty_title),
            description = stringResource(R.string.home_categories_empty_description),
            modifier = modifier.fillMaxWidth(),
        )
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.lg),
        contentPadding = PaddingValues(bottom = OficiosSpacing.xl),
    ) {
        items(categories, key = { it.id }) { category ->
            HomeCategoryCard(
                title = category.name,
                icon = CategoryIconResolver.iconFor(category.id, category.name),
                iconUrl = category.iconUrl,
                onClick = { onCategoryClick(category) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = OficiosComponentSize.registerRoleCardHeight),
            )
        }
        if (showSeeAll) {
            item(key = "see_all") {
                HomeCategoryCard(
                    title = stringResource(R.string.home_categories_see_all),
                    icon = CategoryIconResolver.iconFor(-1L, "todos"),
                    onClick = onSeeAllClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = OficiosComponentSize.registerRoleCardHeight),
                )
            }
        }
    }
}

@Composable
fun HomeRubrosGrid(
    rubros: List<CatalogOption>,
    onRubroClick: (CatalogOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (rubros.isEmpty()) {
        OficiosEmptyState(
            title = stringResource(R.string.home_rubros_empty_title),
            description = stringResource(R.string.home_rubros_empty_description),
            modifier = modifier.fillMaxWidth(),
        )
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.lg),
        contentPadding = PaddingValues(bottom = OficiosSpacing.xl),
    ) {
        items(rubros, key = { it.id }) { rubro ->
            HomeCategoryCard(
                title = rubro.label,
                icon = CategoryIconResolver.iconFor(rubro.id, rubro.label),
                onClick = { onRubroClick(rubro) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = OficiosComponentSize.registerRoleCardHeight),
            )
        }
    }
}

@Composable
fun HomeCategoryCard(
    category: HomeCategoryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HomeCategoryCard(
        title = category.name,
        icon = CategoryIconResolver.iconFor(category.id, category.name),
        iconUrl = category.iconUrl,
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
fun HomeCategoryCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    iconUrl: String? = null,
) {
    val titleStyle = if (compact) {
        MaterialTheme.typography.labelMedium
    } else {
        MaterialTheme.typography.bodyLarge
    }
    val titleAreaHeight = if (compact) OficiosComponentSize.ratingIcon else OficiosComponentSize.minTouchTarget

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(OficiosSpacing.md))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(OficiosSpacing.md),
            )
            .semantics(mergeDescendants = true) { contentDescription = title }
            .clickable(onClick = onClick)
            .padding(OficiosSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = titleAreaHeight),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = title,
                style = titleStyle,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Box(
            modifier = Modifier
                .weight(1f, fill = false)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            CategoryCardIcon(
                icon = icon,
                iconUrl = iconUrl,
                contentDescription = title,
            )
        }
    }
}

@Composable
private fun CategoryCardIcon(
    icon: ImageVector,
    iconUrl: String?,
    contentDescription: String,
) {
    val remoteUrl = iconUrl?.takeIf { it.isNotBlank() }
    val iconModifier = Modifier.size(OficiosComponentSize.iconLg)
    if (remoteUrl == null) {
        LocalCategoryIcon(icon = icon, modifier = iconModifier)
        return
    }
    SubcomposeAsyncImage(
        model = remoteUrl,
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        modifier = iconModifier,
        loading = { LocalCategoryIcon(icon = icon, modifier = iconModifier) },
        error = { LocalCategoryIcon(icon = icon, modifier = iconModifier) },
    )
}

@Composable
private fun LocalCategoryIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = modifier,
    )
}
