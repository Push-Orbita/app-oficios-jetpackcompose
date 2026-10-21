package com.cocido.oficios.core.designsystem.component.inputs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsFocused
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
class OficiosTextFieldLabelTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun labelArriba_conTextoYFoco() = assertLabelAbove("D.N.I.", focused = true) {
        OficiosTextField(value = "ab", onValueChange = {}, label = "D.N.I.", modifier = Modifier.fillMaxWidth())
    }

    @Test
    fun labelArriba_conTextoSinFoco() = assertLabelAbove("D.N.I.", focused = false) {
        OficiosTextField(value = "ab", onValueChange = {}, label = "D.N.I.", modifier = Modifier.fillMaxWidth())
    }

    @Test
    fun labelArriba_vacioConFoco() = assertLabelAbove("Correo", focused = true) {
        OficiosTextField(value = "", onValueChange = {}, label = "Correo", placeholder = "nombre@mail.com", modifier = Modifier.fillMaxWidth())
    }

    @Test
    fun labelArriba_conError() = assertLabelAbove("D.N.I.", focused = true) {
        OficiosTextField(
            value = "ab",
            onValueChange = {},
            label = "D.N.I.",
            isError = true,
            supportingText = "Ingrese un D.N.I. válido",
            modifier = Modifier.fillMaxWidth(),
        )
    }

    @Test
    fun placeholderDentroDelCampo_sinLabelExterno() {
        composeTestRule.setContent {
            OficiosTheme {
                Column(modifier = Modifier.width(360.dp).padding(24.dp)) {
                    Box(Modifier.testTag(FIELD_TAG)) {
                        OficiosTextField(
                            value = "",
                            onValueChange = {},
                            label = "",
                            placeholder = "D.N.I.",
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
        composeTestRule.onNode(hasSetTextAction()).requestFocus()
        composeTestRule.waitForIdle()

        val placeholderNode = composeTestRule.onNode(
            hasText("D.N.I.") and !hasSetTextAction(),
            useUnmergedTree = true,
        ).fetchSemanticsNode()
        val borderNode = composeTestRule.onNode(
            hasTestTag(OFICIOS_TEXT_FIELD_BORDER_TEST_TAG) and hasAnyAncestor(hasTestTag(FIELD_TAG)),
            useUnmergedTree = true,
        ).fetchSemanticsNode()
        val placeholderTop = placeholderNode.positionInRoot.y
        assertTrue(
            "el placeholder debe quedar dentro del campo (top=$placeholderTop, borderTop=${borderNode.positionInRoot.y})",
            placeholderTop >= borderNode.positionInRoot.y - 1f,
        )
    }

    @Test
    fun labelArriba_contrasena() = assertLabelAbove("Repetir contraseña", focused = true) {
        OficiosPasswordField(value = "secreto", onValueChange = {}, label = "Repetir contraseña", modifier = Modifier.fillMaxWidth())
    }

    @Test
    fun labelArriba_fuente200() = assertLabelAbove("D.N.I.", focused = true, fontScale = 2f) {
        OficiosTextField(value = "ab", onValueChange = {}, label = "D.N.I.", modifier = Modifier.fillMaxWidth())
    }

    @Test
    fun labelYValorNoSeSolapan_fuente200() {
        composeTestRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                OficiosTheme {
                    Column(modifier = Modifier.width(360.dp).padding(24.dp)) {
                        Box(Modifier.testTag(FIELD_TAG)) {
                            OficiosTextField(
                                value = "Juan Pérez",
                                onValueChange = {},
                                label = "Nombre",
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
        composeTestRule.waitForIdle()

        val labelMatcher = hasText("Nombre") and !hasSetTextAction()
        val labelNode = composeTestRule.onNode(labelMatcher, useUnmergedTree = true).fetchSemanticsNode()
        val valueMatcher = hasText("Juan Pérez") and hasSetTextAction()
        val valueNode = composeTestRule.onNode(valueMatcher, useUnmergedTree = true).fetchSemanticsNode()
        val valueLayout = valueNode.textLayout() ?: error("no se encontró el layout del valor")
        val valueTop = valueNode.positionInRoot.y + valueLayout.getLineTop(0)
        val labelLayout = labelNode.textLayout() ?: error("no se encontró el layout del label")
        val labelBottom = labelNode.positionInRoot.y + labelLayout.getLineBottom(0)
        assertTrue(
            "label y valor se solapan (labelBottom=$labelBottom, valueTop=$valueTop)",
            valueTop + 0.5f >= labelBottom,
        )
    }

    private fun assertLabelAbove(
        label: String,
        focused: Boolean,
        fontScale: Float = 1f,
        content: @Composable () -> Unit,
    ) {
        composeTestRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                OficiosTheme {
                    Column(modifier = Modifier.width(360.dp).padding(24.dp)) {
                        Box(Modifier.testTag(FIELD_TAG)) {
                            content()
                        }
                    }
                }
            }
        }
        if (focused) {
            composeTestRule.onNode(hasSetTextAction()).requestFocus()
            composeTestRule.onNode(hasSetTextAction()).assertIsFocused()
        }
        composeTestRule.waitForIdle()

        val labelMatcher = hasText(label) and !hasSetTextAction()
        composeTestRule.onAllNodes(labelMatcher, useUnmergedTree = true).assertCountEquals(1)
        val labelNode = composeTestRule.onNode(labelMatcher, useUnmergedTree = true).fetchSemanticsNode()
        val borderNode = composeTestRule.onNode(
            hasTestTag(OFICIOS_TEXT_FIELD_BORDER_TEST_TAG) and hasAnyAncestor(hasTestTag(FIELD_TAG)),
            useUnmergedTree = true,
        ).fetchSemanticsNode()

        val layout = labelNode.textLayout() ?: error("$label: no se encontró el layout del label")
        val labelBottom = labelNode.positionInRoot.y + layout.getLineBottom(0)
        val fieldTop = borderNode.positionInRoot.y
        val minGapPx = with(composeTestRule.density) { OficiosSpacing.s10.toPx() * 0.8f }

        assertTrue("$label: el label queda recortado en alto", !layout.didOverflowHeight)
        val textWidth = (0 until layout.lineCount).maxOf { layout.getLineRight(it) }
        assertTrue(
            "$label: el label queda recortado en ancho (${textWidth}px > ${labelNode.size.width}px)",
            textWidth <= labelNode.size.width + 0.5f,
        )
        assertTrue(
            "$label: el label no está completo arriba del campo (labelBottom=$labelBottom, fieldTop=$fieldTop, gap=$minGapPx)",
            labelBottom + minGapPx <= fieldTop + 1f,
        )
    }

    private fun SemanticsNode.textLayout(): TextLayoutResult? {
        val results = mutableListOf<TextLayoutResult>()
        val action = config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action ?: return null
        return if (action(results)) results.firstOrNull() else null
    }

    private companion object {
        const val FIELD_TAG = "campo"
    }
}
