package com.cocido.oficios.core.designsystem.component.categories

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.R
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.component.cards.OficiosCard
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing

data class CategoryGridItem(
    val id: Long,
    val label: String,
)

object CategoryIconResolver {

    @Composable
    fun iconFor(categoryId: Long, categoryName: String): ImageVector {
        return ImageVector.vectorResource(vectorRes(categoryId, categoryName))
    }

    @DrawableRes
    fun illustrationRes(name: String): Int? {
        val key = name.lowercase()
        return when {
            key.contains("alba") -> R.drawable.ill_albanil
            key.contains("plom") -> R.drawable.ill_plomero
            key.contains("instal") -> R.drawable.ill_instalador
            key.contains("pintor") -> R.drawable.ill_pintor
            key.contains("carpin") -> R.drawable.ill_carpintero
            key.contains("gas") -> R.drawable.ill_gasista
            key.contains("electr") -> R.drawable.ill_electricista
            key.contains("ingen") -> R.drawable.ill_ingeniero
            key.contains("construc") -> R.drawable.ill_construccion
            key.contains("manten") || key.contains("refacc") -> R.drawable.ill_mantenimiento
            key.contains("bienestar") -> R.drawable.ill_bienestar
            // Seguridad / Personales: el export de Figma llegó recortado mal.
            // Caen a Lucide (shield-check / users) hasta que haya asset bueno.
            else -> null
        }
    }

    @DrawableRes
    fun vectorRes(categoryId: Long, categoryName: String): Int {
        val key = categoryName.lowercase()
        return when {
            key.contains("todo") -> OficiosIcons.BookUser
            key.contains("segur") -> OficiosIcons.ShieldCheck
            key.contains("personal") || key.contains("user") -> OficiosIcons.Users
            key.contains("cerr") -> OficiosIcons.Asterisk
            else -> if (categoryId % 2L == 0L) OficiosIcons.Hammer else OficiosIcons.House
        }
    }

    fun isIllustration(name: String): Boolean = illustrationRes(name) != null
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
    icon: ImageVector? = null,
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
            val illustration = CategoryIconResolver.illustrationRes(title)
            if (illustration != null) {
                Image(
                    painter = painterResource(illustration),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(width = 100.dp, height = 74.dp),
                )
            } else {
                Icon(
                    imageVector = icon ?: CategoryIconResolver.iconFor(0L, title),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}
