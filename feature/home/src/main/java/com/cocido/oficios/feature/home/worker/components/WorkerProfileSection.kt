package com.cocido.oficios.feature.home.worker.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cocido.oficios.core.designsystem.component.cards.OficiosCard
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing

@Composable
fun WorkerProfileSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = OficiosSpacing.md)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        OficiosCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = OficiosSpacing.sm),
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(OficiosSpacing.md),
        ) {
            Column(content = content)
        }
    }
}
