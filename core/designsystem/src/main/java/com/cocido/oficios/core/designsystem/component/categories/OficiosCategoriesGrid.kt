package com.cocido.oficios.core.designsystem.component.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material.icons.outlined.ElectricalServices
import androidx.compose.material.icons.outlined.Handyman
import androidx.compose.material.icons.outlined.HomeRepairService
import androidx.compose.material.icons.outlined.Plumbing
import androidx.compose.material.icons.outlined.Yard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.component.cards.OficiosCard
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing

data class CategoryGridItem(
    val id: Long,
    val label: String,
)

object CategoryIconResolver {

    @Composable
    fun iconFor(categoryId: Long, categoryName: String): ImageVector {
        val key = categoryName.lowercase()
        return when {
            key.contains("plom") -> Icons.Outlined.Plumbing
            key.contains("electr") -> Icons.Outlined.ElectricalServices
            key.contains("alba") || key.contains("construc") -> Icons.Outlined.Construction
            key.contains("jardin") || key.contains("paisaj") -> Icons.Outlined.Yard
            key.contains("limpie") -> Icons.Outlined.CleaningServices
            key.contains("repar") || key.contains("manten") -> Icons.Outlined.HomeRepairService
            categoryId % 3L == 0L -> Icons.Outlined.Handyman
            categoryId % 2L == 0L -> Icons.Outlined.Build
            else -> Icons.Outlined.Handyman
        }
    }
}

@Composable
fun OficiosCategoriesGrid(
    categories: List<CategoryGridItem>,
    onCategoryClick: (CategoryGridItem) -> Unit,
    modifier: Modifier = Modifier,
    selectedCategoryId: Long? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.lg),
    ) {
        categories.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.lg),
            ) {
                rowItems.forEach { category ->
                    OficiosCategoryCard(
                        title = category.label,
                        icon = CategoryIconResolver.iconFor(category.id, category.label),
                        selected = category.id == selectedCategoryId,
                        onClick = { onCategoryClick(category) },
                        modifier = Modifier
                            .weight(1f)
                            .height(172.dp),
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
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
) {
    OficiosCard(
        onClick = onClick,
        selected = selected,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(OficiosSpacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
