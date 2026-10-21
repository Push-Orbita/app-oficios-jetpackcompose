package com.cocido.oficios.core.designsystem.component.inputs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.theme.OficiosAlpha
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

// ---------------------------------------------------------------------------
// OficiosSearchField
// Figma 25:633 / 25:219 / 40:335 — hint a la izquierda, mic + search a la derecha.
// ---------------------------------------------------------------------------

@Composable
fun OficiosSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    onSearchClick: (() -> Unit)? = null,
    onMicClick: (() -> Unit)? = null,
    onFilterClick: (() -> Unit)? = null,
    filterContentDescription: String? = null,
    activeFilterCount: Int = 0,
    placeholderMaxLines: Int = 1,
    enabled: Boolean = true,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val textColor = MaterialTheme.colorScheme.onSurface
    val placeholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = OficiosAlpha.medium)
    val iconTint = MaterialTheme.colorScheme.onBackground

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = textColor),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = keyboardActions,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = OficiosComponentSize.textFieldHeight)
            .clip(MaterialTheme.shapes.small)
            .border(
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = MaterialTheme.shapes.small,
            )
            .semantics(mergeDescendants = true) {
                contentDescription = placeholder
            },
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = OficiosComponentSize.textFieldHeight)
                    .padding(start = OficiosSpacing.md, end = OficiosSpacing.lg, top = OficiosSpacing.sm, bottom = OficiosSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = placeholderColor,
                            maxLines = placeholderMaxLines,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    innerTextField()
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (onMicClick != null) {
                        SearchFieldIcon(
                            icon = OficiosIcons.Mic,
                            description = stringResource(com.cocido.oficios.core.designsystem.R.string.ds_search_voice),
                            onClick = onMicClick,
                            tint = iconTint,
                        )
                    }
                    if (onSearchClick != null) {
                        SearchFieldIcon(
                            icon = OficiosIcons.Search,
                            description = stringResource(com.cocido.oficios.core.designsystem.R.string.ds_search_action),
                            onClick = onSearchClick,
                            tint = iconTint,
                        )
                    }
                    if (onFilterClick != null) {
                        val filterCd = filterContentDescription
                            ?: stringResource(com.cocido.oficios.core.designsystem.R.string.ds_search_filters)
                        BadgedBox(
                            badge = {
                                if (activeFilterCount > 0) {
                                    Badge { Text(activeFilterCount.toString()) }
                                }
                            },
                        ) {
                            SearchFieldIcon(
                                icon = OficiosIcons.SlidersHorizontal,
                                description = filterCd,
                                onClick = onFilterClick,
                                tint = iconTint,
                            )
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun SearchFieldIcon(
    icon: Int,
    description: String,
    onClick: () -> Unit,
    tint: androidx.compose.ui.graphics.Color,
) {
    Box(
        modifier = Modifier.size(OficiosComponentSize.iconMd),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(icon),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(OficiosComponentSize.iconMd),
        )
        Box(
            modifier = Modifier
                .requiredSize(OficiosComponentSize.minTouchTarget)
                .clickable(onClick = onClick)
                .semantics {
                    role = Role.Button
                    contentDescription = description
                },
        )
    }
}

@Preview(name = "Search Field", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun SearchFieldPreview() {
    OficiosTheme {
        OficiosSearchField(
            value = "",
            onValueChange = {},
            placeholder = "¿Qué oficio está buscando?",
            onMicClick = {},
            onSearchClick = {},
        )
    }
}
