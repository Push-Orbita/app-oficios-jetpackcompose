package com.cocido.oficios.feature.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performScrollTo
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.domain.model.AccountProfile
import com.cocido.oficios.domain.model.ManagedProviderService
import com.cocido.oficios.domain.model.ProviderAccountDetails
import com.cocido.oficios.domain.model.User
import com.cocido.oficios.domain.model.UserRole
import com.cocido.oficios.domain.model.WorkCertificateItem
import com.cocido.oficios.feature.settings.client.ClientDataMode
import com.cocido.oficios.feature.settings.client.ClientDataScreen
import com.cocido.oficios.feature.settings.client.ClientDataUiState
import com.cocido.oficios.feature.settings.provider.PortfolioScreen
import com.cocido.oficios.feature.settings.provider.PortfolioUiState
import com.cocido.oficios.feature.settings.provider.ServicesScreen
import com.cocido.oficios.feature.settings.provider.ServicesUiState
import org.junit.Rule
import org.junit.Test

class ProfileScreensTest {
    @get:Rule val composeRule = createAndroidComposeRule<TestHostActivity>()

    @Test
    fun settings_providerShowsProfessionalSectionsAndDeleteAccount() {
        composeRule.setContent {
            OficiosTheme {
                SettingsScreen(
                    uiState = SettingsUiState(
                        isLoading = false,
                        profile = AccountProfile(
                            user = User(9, "trabajador@test.com", "Trabajador", "Prueba", UserRole.PROVIDER, true),
                            providerDetails = ProviderAccountDetails(9, "VALIDADO", "99887766", "Descripción", "Experiencia", emptyList(), emptyList(), emptyList(), emptyList()),
                        ),
                    ),
                    onEvent = {},
                )
            }
        }
        composeRule.onNodeWithText("Trabajador Prueba").assertIsDisplayed()
        composeRule.onNodeWithText("Servicios que ofrece").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Galería y certificados").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Eliminar cuenta").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun clientContactEditor_explainsArgentinaWhatsappFormat() {
        composeRule.setContent {
            OficiosTheme {
                ClientDataScreen(
                    uiState = ClientDataUiState(isLoading = false, mode = ClientDataMode.CONTACT),
                    onEvent = {},
                )
            }
        }
        composeRule.onNodeWithText("El número se valida por WhatsApp. Argentina usa +54 y usted ingresa solo el número local, por ejemplo 9 370 5219109.").assertIsDisplayed()
        composeRule.onNodeWithText("11 dígitos con el 9").assertIsDisplayed()
        composeRule.onNodeWithText("Guardar y verificar").assertIsDisplayed()
    }

    @Test
    fun clientAddressEditor_showsMapAndConfirmedCoordinates() {
        composeRule.setContent {
            OficiosTheme {
                ClientDataScreen(
                    uiState = ClientDataUiState(
                        isLoading = false,
                        mode = ClientDataMode.DOMICILIO,
                        addressLatitude = -26.18490,
                        addressLongitude = -58.17310,
                    ),
                    onEvent = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("Mapa para seleccionar la ubicación")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText("Ubicación confirmada", substring = true)
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText("Usar mi ubicación")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun services_primaryServiceExplainsWhyItCannotBeDeleted() {
        composeRule.setContent {
            OficiosTheme {
                ServicesScreen(
                    uiState = ServicesUiState(
                        isLoading = false,
                        services = listOf(ManagedProviderService(1, 2, "Electricidad", true, emptyList(), emptyList())),
                    ),
                    onEvent = {},
                )
            }
        }
        composeRule.onNodeWithText("Servicio principal").assertIsDisplayed()
        composeRule.onNodeWithText("El servicio principal se conserva para proteger su perfil y sus pedidos activos.").assertIsDisplayed()
    }

    @Test
    fun portfolio_certificateNeverClaimsAdministrativeVerification() {
        composeRule.setContent {
            OficiosTheme {
                PortfolioScreen(
                    uiState = PortfolioUiState(
                        isLoading = false,
                        certificates = listOf(WorkCertificateItem(1, "Matrícula profesional", "https://example.com/a.pdf")),
                    ),
                    onEvent = {},
                    onPickGalleryImage = {},
                    onPickCertificateFile = {},
                )
            }
        }
        composeRule.onNodeWithText("Documento cargado · sin validación administrativa informada").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun deleteAccount_formAsksPasswordAndEliminar() {
        composeRule.setContent {
            OficiosTheme {
                com.cocido.oficios.feature.settings.account.DeleteAccountScreen(
                    uiState = com.cocido.oficios.feature.settings.account.DeleteAccountUiState(isLoading = false),
                    onEvent = {},
                )
            }
        }
        composeRule.onNodeWithText("Eliminar cuenta").assertIsDisplayed()
        composeRule.onNodeWithText("Contraseña actual").assertIsDisplayed()
        composeRule.onNodeWithText("Escriba ELIMINAR").assertIsDisplayed()
        composeRule.onNodeWithText("Eliminar mi cuenta").assertIsDisplayed()
    }
}
