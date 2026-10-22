package com.cocido.oficios.feature.settings.qa

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.feature.settings.SettingsScreen
import com.cocido.oficios.feature.settings.SettingsUiState
import com.cocido.oficios.feature.settings.account.ChangePasswordScreen
import com.cocido.oficios.feature.settings.account.ChangePasswordUiState
import com.cocido.oficios.feature.settings.account.DeleteAccountScreen
import com.cocido.oficios.feature.settings.account.DeleteAccountUiState
import com.cocido.oficios.feature.settings.account.PersonalDataScreen
import com.cocido.oficios.feature.settings.account.PersonalDataUiState
import com.cocido.oficios.feature.settings.client.ClientDataScreen
import com.cocido.oficios.feature.settings.client.ClientDataUiState
import com.cocido.oficios.feature.settings.provider.AddServiceScreen
import com.cocido.oficios.feature.settings.provider.AddServiceUiState
import com.cocido.oficios.feature.settings.provider.CommercialLocalScreen
import com.cocido.oficios.feature.settings.provider.CommercialLocalUiState
import com.cocido.oficios.feature.settings.provider.PortfolioScreen
import com.cocido.oficios.feature.settings.provider.PortfolioUiState
import com.cocido.oficios.feature.settings.provider.ProfessionalProfileScreen
import com.cocido.oficios.feature.settings.provider.ProfessionalProfileUiState
import com.cocido.oficios.feature.settings.provider.ServicesScreen
import com.cocido.oficios.feature.settings.provider.ServicesUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ErrorCopyComposeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun settingsErrorCopy() {
        composeRule.setContent {
            OficiosTheme {
                SettingsScreen(
                    uiState = SettingsUiState(isLoading = false, errorMessage = "Sin conexión"),
                    onEvent = {},
                )
            }
        }
        assertErrorCopy("No pudimos cargar su cuenta")
    }

    @Test
    fun personalDataErrorCopy() {
        composeRule.setContent {
            OficiosTheme {
                PersonalDataScreen(
                    uiState = PersonalDataUiState(isLoading = false, errorMessage = "Sin conexión"),
                    onEvent = {},
                    onRequestPhoto = {},
                )
            }
        }
        assertErrorCopy("No pudimos cargar sus datos personales")
    }

    @Test
    fun clientDataErrorCopy() {
        composeRule.setContent {
            OficiosTheme {
                ClientDataScreen(
                    uiState = ClientDataUiState(isLoading = false, errorMessage = "Sin conexión"),
                    onEvent = {},
                )
            }
        }
        assertErrorCopy("No pudimos cargar sus contactos y domicilios")
    }

    @Test
    fun professionalProfileErrorCopy() {
        composeRule.setContent {
            OficiosTheme {
                ProfessionalProfileScreen(
                    uiState = ProfessionalProfileUiState(
                        isLoading = false,
                        loadFailed = true,
                        errorMessage = "Sin conexión",
                    ),
                    onEvent = {},
                )
            }
        }
        assertErrorCopy("No pudimos cargar su perfil profesional")
    }

    @Test
    fun portfolioErrorCopy() {
        composeRule.setContent {
            OficiosTheme {
                PortfolioScreen(
                    uiState = PortfolioUiState(isLoading = false, errorMessage = "Sin conexión"),
                    onEvent = {},
                    onPickGalleryImage = {},
                    onPickCertificateFile = {},
                )
            }
        }
        assertErrorCopy("No pudimos cargar su portfolio")
    }

    @Test
    fun servicesErrorCopy() {
        composeRule.setContent {
            OficiosTheme {
                ServicesScreen(
                    uiState = ServicesUiState(isLoading = false, errorMessage = "Sin conexión"),
                    onEvent = {},
                )
            }
        }
        assertErrorCopy("No pudimos cargar sus servicios")
    }

    @Test
    fun addServiceErrorCopy() {
        composeRule.setContent {
            OficiosTheme {
                AddServiceScreen(
                    uiState = AddServiceUiState(
                        isLoading = false,
                        loadFailed = true,
                        errorMessage = "Sin conexión",
                    ),
                    onEvent = {},
                )
            }
        }
        assertErrorCopy("No pudimos preparar el formulario")
    }

    @Test
    fun commercialLocalErrorCopy() {
        composeRule.setContent {
            OficiosTheme {
                CommercialLocalScreen(
                    uiState = CommercialLocalUiState(
                        isLoading = false,
                        loadFailed = true,
                        errorMessage = "Sin conexión",
                    ),
                    onEvent = {},
                )
            }
        }
        assertErrorCopy("No pudimos cargar su local comercial")
    }

    @Test
    fun changePasswordErrorCopy() {
        composeRule.setContent {
            OficiosTheme {
                ChangePasswordScreen(
                    uiState = ChangePasswordUiState(isLoading = false, errorMessage = "Sin conexión"),
                    onEvent = {},
                )
            }
        }
        assertErrorCopy("No pudimos validar su cuenta")
    }

    @Test
    fun deleteAccountErrorCopy() {
        composeRule.setContent {
            OficiosTheme {
                DeleteAccountScreen(
                    uiState = DeleteAccountUiState(isLoading = false, errorMessage = "Sin conexión"),
                    onEvent = {},
                )
            }
        }
        assertErrorCopy("No pudimos consultar el estado de su cuenta")
    }

    private fun assertErrorCopy(title: String) {
        composeRule.waitForIdle()
        assertTrue(composeRule.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty())
        assertTrue(composeRule.onAllNodesWithText("Reintentar").fetchSemanticsNodes().isNotEmpty())
        assertEquals(
            0,
            composeRule.onAllNodesWithText("No pudimos cargar el inicio").fetchSemanticsNodes().size,
        )
    }
}
