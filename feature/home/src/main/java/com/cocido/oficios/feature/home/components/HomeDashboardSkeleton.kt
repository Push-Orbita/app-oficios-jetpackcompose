package com.cocido.oficios.feature.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.cocido.oficios.core.designsystem.component.feedback.OficiosSkeleton
import com.cocido.oficios.core.designsystem.component.feedback.OficiosSkeletonCard
import com.cocido.oficios.core.designsystem.component.feedback.OficiosSkeletonLine
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing

/** Placeholder de la Home de cliente y de trabajador: header, bloques destacados y cards. */
@Composable
fun HomeDashboardSkeleton(
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = OficiosSpacing.md, vertical = OficiosSpacing.lg)
            .semantics { this.contentDescription = contentDescription },
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.lg),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OficiosSkeleton(modifier = Modifier.size(OficiosComponentSize.avatarSm), shape = CircleShape)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
            ) {
                OficiosSkeletonLine(widthFraction = 0.5f, height = OficiosComponentSize.iconSm)
                OficiosSkeletonLine(widthFraction = 0.3f)
            }
            OficiosSkeleton(modifier = Modifier.size(OficiosComponentSize.iconLg), shape = CircleShape)
        }
        OficiosSkeleton(modifier = Modifier.fillMaxWidth().height(OficiosComponentSize.textFieldHeight))
        OficiosSkeleton(modifier = Modifier.fillMaxWidth().height(OficiosComponentSize.skeletonBlockMd))
        OficiosSkeletonLine(widthFraction = 0.4f, height = OficiosComponentSize.iconSm)
        repeat(2) { OficiosSkeletonCard() }
    }
}
