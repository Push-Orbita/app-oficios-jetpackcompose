package com.cocido.oficios.core.designsystem.component.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h3000dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class OficiosTextFieldLabelClipTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private data class ClipCase(
        val tag: String,
        val label: String,
        val checkValueOverlap: Boolean = false,
        val valueForOverlap: String? = null,
        val content: @Composable () -> Unit,
    )

    @Test
    fun labelBoundsVisiblesCoincidenConNodo_sinRecortePorClip() {
        val cases = listOf(
            ClipCase(
                tag = "fuente100",
                label = "Nombre",
                content = {
                    OficiosTextField(
                        value = "Juan",
                        onValueChange = {},
                        label = "Nombre",
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
            ),
            ClipCase(
                tag = "fuente130",
                label = "Apellido",
                content = {
                    OficiosTextField(
                        value = "García",
                        onValueChange = {},
                        label = "Apellido",
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
            ),
            ClipCase(
                tag = "fuente200",
                label = "Nombre grande",
                checkValueOverlap = true,
                valueForOverlap = "Juan Pérez",
                content = {
                    OficiosTextField(
                        value = "Juan Pérez",
                        onValueChange = {},
                        label = "Nombre grande",
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
            ),
            ClipCase(
                tag = "vacio_foco",
                label = "Correo electrónico",
                content = {
                    OficiosTextField(
                        value = "",
                        onValueChange = {},
                        label = "Correo electrónico",
                        placeholder = "nombre@mail.com",
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
            ),
            ClipCase(
                tag = "error",
                label = "Correo error",
                content = {
                    OficiosTextField(
                        value = "mal@",
                        onValueChange = {},
                        label = "Correo error",
                        isError = true,
                        supportingText = "Email inválido",
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
            ),
            ClipCase(
                tag = "deshabilitado",
                label = "Nombre off",
                content = {
                    OficiosTextField(
                        value = "Juan",
                        onValueChange = {},
                        label = "Nombre off",
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
            ),
            ClipCase(
                tag = "trailing",
                label = "Rubro",
                content = {
                    OficiosTextField(
                        value = "Plomería",
                        onValueChange = {},
                        label = "Rubro",
                        modifier = Modifier.fillMaxWidth(),
                        trailingContent = { Icon(Icons.Default.Search, contentDescription = null) },
                    )
                },
            ),
            ClipCase(
                tag = "multilinea",
                label = "Comentario",
                content = {
                    OficiosTextField(
                        value = "Primera línea\nSegunda línea",
                        onValueChange = {},
                        label = "Comentario",
                        singleLine = false,
                        minLines = 3,
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
            ),
        )

        composeTestRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                OficiosTheme {
                    Column(
                        modifier = Modifier
                            .width(360.dp)
                            .background(Color(0xFF00358B))
                            .padding(24.dp),
                    ) {
                        cases.forEach { case ->
                            Box(Modifier.testTag(case.tag)) {
                                case.content()
                            }
                        }
                    }
                }
            }
        }
        composeTestRule.onNode(
            hasSetTextAction() and hasAnyAncestor(hasTestTag("vacio_foco")),
            useUnmergedTree = true,
        ).requestFocus()
        composeTestRule.waitForIdle()

        cases.forEach { case ->
            val labelMatcher = hasText(case.label) and !hasSetTextAction()
            val labelNode = composeTestRule.onNode(labelMatcher, useUnmergedTree = true).fetchSemanticsNode()
            val layout = labelNode.textLayout() ?: error("${case.tag}: sin layout de label")

            val borderNode = composeTestRule.onNode(
                hasTestTag(OFICIOS_TEXT_FIELD_BORDER_TEST_TAG) and hasAnyAncestor(hasTestTag(case.tag)),
                useUnmergedTree = true,
            ).fetchSemanticsNode()
            val fieldTop = borderNode.positionInRoot.y
            val labelTop = labelNode.positionInRoot.y
            val textBottom = labelTop + layout.getLineBottom(0)

            assertTrue("${case.tag}: overflow vertical en layout", !layout.didOverflowHeight)
            val textWidth = (0 until layout.lineCount).maxOf { layout.getLineRight(it) }
            assertTrue(
                "${case.tag}: ancho insuficiente ($textWidth > ${labelNode.size.width})",
                textWidth <= labelNode.size.width + 0.5f,
            )
            assertTrue(
                "${case.tag}: alto del nodo menor que el layout (${labelNode.size.height} < ${layout.size.height})",
                labelNode.size.height.toFloat() >= layout.size.height,
            )
            val minGapPx = with(composeTestRule.density) { OficiosSpacing.s10.toPx() * 0.8f }
            assertTrue(
                "${case.tag}: el label queda recortado por el borde (textBottom=$textBottom, fieldTop=$fieldTop)",
                textBottom + minGapPx <= fieldTop + 1f,
            )
            assertTrue(
                "${case.tag}: el label no está completo arriba del campo (labelTop=$labelTop, fieldTop=$fieldTop)",
                labelTop + labelNode.size.height <= fieldTop + 1f,
            )

            if (case.checkValueOverlap && case.valueForOverlap != null) {
                assertLabelValueNoOverlap(case.tag, case.label, case.valueForOverlap)
            }
        }
    }

    private fun assertLabelValueNoOverlap(caseName: String, label: String, value: String) {
        val labelMatcher = hasText(label) and !hasSetTextAction()
        val labelNode = composeTestRule.onNode(labelMatcher, useUnmergedTree = true).fetchSemanticsNode()
        val valueMatcher = hasText(value) and hasSetTextAction()
        val valueNode = composeTestRule.onNode(valueMatcher, useUnmergedTree = true).fetchSemanticsNode()
        val valueLayout = valueNode.textLayout() ?: error("$caseName: sin layout del valor")
        val valueTop = valueNode.positionInRoot.y + valueLayout.getLineTop(0)
        val labelLayout = labelNode.textLayout() ?: error("$caseName: sin layout del label")
        val labelBottom = labelNode.positionInRoot.y + labelLayout.getLineBottom(0)
        assertTrue(
            "$caseName: label y valor se solapan (labelBottom=$labelBottom, valueTop=$valueTop)",
            valueTop + 0.5f >= labelBottom,
        )
    }

    private fun SemanticsNode.textLayout(): TextLayoutResult? {
        val results = mutableListOf<TextLayoutResult>()
        val action = config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action ?: return null
        return if (action(results)) results.firstOrNull() else null
    }

}
