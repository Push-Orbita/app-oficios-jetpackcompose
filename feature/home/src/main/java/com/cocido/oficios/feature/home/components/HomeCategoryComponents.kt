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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.cards.WorkerCard
import com.cocido.oficios.core.designsystem.component.categories.CategoryGlyph
import com.cocido.oficios.core.designsystem.component.categories.OficiosCategoryCard
import com.cocido.oficios.core.designsystem.component.feedback.OficiosEmptyState
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.domain.model.HomeCategoryItem
import com.cocido.oficios.domain.model.RecentSearchEntry
import com.cocido.oficios.domain.model.WorkerSummary
import com.cocido.oficios.feature.home.R

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
                        reviewCount = worker.reviewCount,
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
    onSeeAllClick: () -> Unit = {},
) {
    HomeRecentRubrosSection(
        searches = searches,
        onSearchClick = onSearchClick,
        onSeeAllClick = onSeeAllClick,
        modifier = modifier,
    )
}

@Composable
fun HomeRecentRubrosSection(
    searches: List<RecentSearchEntry>,
    onSearchClick: (RecentSearchEntry) -> Unit,
    modifier: Modifier = Modifier,
    onSeeAllClick: () -> Unit = {},
) {
    val fontScale = LocalConfiguration.current.fontScale
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = OficiosComponentSize.homeRecentsAfterSearchExtra)
            .heightIn(min = OficiosComponentSize.homeRecentsSectionMinHeight),
        verticalArrangement = Arrangement.spacedBy(OficiosComponentSize.homeRecentsTitleToCards),
    ) {
        Text(
            text = stringResource(R.string.home_recent_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.s10),
        ) {
            searches.take(3).forEach { entry ->
                val cardHeightModifier = if (fontScale >= 1.3f) {
                    Modifier.heightIn(min = OficiosComponentSize.categoryCardSize)
                } else {
                    Modifier
                        .heightIn(min = OficiosComponentSize.categoryCardSize)
                        .height(OficiosComponentSize.categoryCardSize)
                }
                HomeCategoryCard(
                    title = entry.label,
                    categoryId = entry.id,
                    iconUrl = null,
                    onClick = { onSearchClick(entry) },
                    compact = true,
                    modifier = Modifier
                        .weight(1f)
                        .then(cardHeightModifier),
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
    lazy: Boolean = true,
) {
    if (categories.isEmpty()) {
        OficiosEmptyState(
            title = stringResource(R.string.home_categories_empty_title),
            description = stringResource(R.string.home_categories_empty_description),
            modifier = modifier.fillMaxWidth(),
        )
        return
    }
    val seeAllLabel = stringResource(R.string.home_categories_see_all)
    if (!lazy) {
        val cells = buildList {
            addAll(categories)
            if (showSeeAll) {
                add(HomeCategoryItem(id = -1L, name = seeAllLabel))
            }
        }
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xl),
        ) {
            cells.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xl),
                ) {
                    rowItems.forEach { category ->
                        HomeCategoryCard(
                            title = category.name,
                            categoryId = category.id,
                            iconUrl = category.iconUrl,
                            onClick = {
                                if (category.id < 0) onSeeAllClick() else onCategoryClick(category)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = OficiosComponentSize.authRoleCardMinHeight),
                        )
                    }
                    if (rowItems.size == 1) {
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xl),
        contentPadding = PaddingValues(bottom = OficiosSpacing.xl),
    ) {
        items(categories, key = { it.id }) { category ->
            HomeCategoryCard(
                title = category.name,
                categoryId = category.id,
                iconUrl = category.iconUrl,
                onClick = { onCategoryClick(category) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = OficiosComponentSize.authRoleCardMinHeight),
            )
        }
        if (showSeeAll) {
            item(key = "see_all") {
                HomeCategoryCard(
                    title = stringResource(R.string.home_categories_see_all),
                    categoryId = -1L,
                    onClick = onSeeAllClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = OficiosComponentSize.authRoleCardMinHeight),
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
        horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xl),
        contentPadding = PaddingValues(bottom = OficiosSpacing.xl),
    ) {
        items(rubros, key = { it.id }) { rubro ->
            HomeCategoryCard(
                title = rubro.label,
                categoryId = rubro.id,
                iconUrl = rubro.iconUrl,
                onClick = { onRubroClick(rubro) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = OficiosComponentSize.authRoleCardMinHeight),
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
        categoryId = category.id,
        iconUrl = category.iconUrl,
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
fun HomeCategoryCard(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    categoryId: Long = 0L,
    iconUrl: String? = null,
) {
    if (!compact) {
        OficiosCategoryCard(
            title = title,
            onClick = onClick,
            modifier = modifier,
            categoryId = categoryId,
            iconUrl = iconUrl,
        )
        return
    }

    Column(
        modifier = modifier
            .heightIn(min = OficiosComponentSize.categoryCardSize)
            .clip(RoundedCornerShape(OficiosSpacing.md))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(OficiosSpacing.md),
            )
            .semantics(mergeDescendants = true) { contentDescription = title }
            .clickable(onClick = onClick)
            .padding(
                start = OficiosSpacing.md,
                end = OficiosSpacing.md,
                top = OficiosComponentSize.homeRecentCardLabelTop,
                bottom = OficiosSpacing.md,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
    ) {
        AutoshrinkCategoryLabel(
            title = title,
            style = MaterialTheme.typography.bodyLarge,
            compact = true,
        )
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            val compactGlyph = Modifier.size(
                width = OficiosComponentSize.categoryCardCompactGlyphWidth,
                height = OficiosComponentSize.categoryCardCompactGlyphHeight,
            )
            CategoryGlyph(
                id = categoryId,
                label = title,
                iconUrl = iconUrl,
                contentDescription = title,
                modifier = compactGlyph,
                illustrationModifier = compactGlyph,
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Composable
private fun AutoshrinkCategoryLabel(
    title: String,
    style: androidx.compose.ui.text.TextStyle,
    compact: Boolean,
) {
    val fontScale = LocalConfiguration.current.fontScale
    val allowsWordWrap = title.trim().contains(' ')
    val maxLines = if (allowsWordWrap) 2 else 1
    val minSize = if (compact) 10.sp else 12.sp
    var fontSize by remember(title, fontScale, compact) { mutableStateOf(style.fontSize) }
    Text(
        text = title,
        style = style.copy(
            fontSize = fontSize,
            lineBreak = LineBreak.Simple,
            hyphens = Hyphens.None,
        ),
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center,
        maxLines = maxLines,
        softWrap = allowsWordWrap,
        overflow = TextOverflow.Clip,
        onTextLayout = { result ->
            val overflowed = result.hasVisualOverflow ||
                result.lineCount > maxLines ||
                (result.lineCount > 0 && result.getLineEnd(0, visibleEnd = true) < title.length && !allowsWordWrap)
            if (overflowed && fontSize > minSize) {
                fontSize = (fontSize.value - 1f).coerceAtLeast(minSize.value).sp
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 20.dp),
    )
}

