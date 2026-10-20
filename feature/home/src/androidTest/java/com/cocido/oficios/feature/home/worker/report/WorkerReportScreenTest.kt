package com.cocido.oficios.feature.home.worker.report

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.domain.model.ReportReason
import com.cocido.oficios.feature.home.TestHostActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkerReportScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<TestHostActivity>()

    @Test
    fun reportScreen_submitDisabledUntilFormIsComplete() {
        composeTestRule.setContent {
            OficiosTheme {
                WorkerReportScreen(
                    uiState = WorkerReportUiState(
                        workerName = "Matias",
                        isLoadingReasons = false,
                        reasons = listOf(ReportReason("ESTAFA", "Estafa o cobro indebido")),
                    ),
                    onEvent = {},
                    onNavigateBack = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Denunciar perfil").assertIsDisplayed()
        composeTestRule.onNodeWithText("Enviar denuncia").assertIsNotEnabled()
    }

    @Test
    fun reportScreen_submitEnabledWhenReasonAndDescriptionAreValid() {
        composeTestRule.setContent {
            OficiosTheme {
                WorkerReportScreen(
                    uiState = WorkerReportUiState(
                        workerName = "Matias",
                        isLoadingReasons = false,
                        reasons = listOf(ReportReason("ESTAFA", "Estafa o cobro indebido")),
                        selectedReasonCode = "ESTAFA",
                        description = "Cobró y no se presentó al domicilio acordado en el pedido.",
                    ),
                    onEvent = {},
                    onNavigateBack = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Enviar denuncia").assertIsEnabled()
    }
}
