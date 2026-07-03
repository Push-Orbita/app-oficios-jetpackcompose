package com.cocido.oficios.feature.home.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material.icons.outlined.ElectricalServices
import androidx.compose.material.icons.outlined.Handyman
import androidx.compose.material.icons.outlined.HomeRepairService
import androidx.compose.material.icons.outlined.Plumbing
import androidx.compose.material.icons.outlined.Yard
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

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
