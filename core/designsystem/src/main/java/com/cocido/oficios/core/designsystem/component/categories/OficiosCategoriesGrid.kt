package com.cocido.oficios.core.designsystem.component.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.cocido.oficios.core.designsystem.component.cards.OficiosCard
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing

data class CategoryGridItem(
    val id: Long,
    val label: String,
    val iconUrl: String? = null,
)

@Composable
fun OficiosCategoriesGrid(
    categories: List<CategoryGridItem>,
    onCategoryClick: (CategoryGridItem) -> Unit,
    modifier: Modifier = Modifier,
    selectedCategoryId: Long? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xl),
    ) {
        categories.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xl),
            ) {
                rowItems.forEach { category ->
                    OficiosCategoryCard(
                        title = category.label,
                        categoryId = category.id,
                        iconUrl = category.iconUrl,
                        selected = category.id == selectedCategoryId,
                        onClick = { onCategoryClick(category) },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = OficiosComponentSize.authRoleCardMinHeight),
                    )
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun OficiosCategoryCard(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    categoryId: Long = 0L,
    iconUrl: String? = null,
) {
    OficiosCard(
        onClick = onClick,
        selected = selected,
        modifier = modifier,
        expandContent = true,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            CategoryGlyph(
                id = categoryId,
                label = title,
                iconUrl = iconUrl,
                modifier = Modifier.categoryVectorSize(),
                illustrationModifier = Modifier.categoryIllustrationSize(),
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}
