package com.cocido.oficios.feature.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.domain.model.AccountProfile
import com.cocido.oficios.domain.model.User
import com.cocido.oficios.domain.model.UserRole
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SettingsProfileCardTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun clientCardShowsNameEmailPendingStatusAndRoleChip() {
        composeRule.setContent {
            OficiosTheme {
                SettingsScreen(
                    uiState = SettingsUiState(
                        isLoading = false,
                        profile = AccountProfile(
                            user = User(
                                id = 1,
                                email = "devmatiasbritez+oficiosqa@gmail.com",
                                firstName = "Daniel",
                                lastName = "Prueba",
                                role = UserRole.CLIENT,
                                emailVerified = false,
                                profilePhotoUrl = "https://cdn.example/daniel.jpg",
                            ),
                        ),
                    ),
                    onEvent = {},
                )
            }
        }
        composeRule.onNodeWithText("Daniel Prueba").assertIsDisplayed()
        composeRule.onNodeWithText("devmatiasbritez+oficiosqa@gmail.com").assertIsDisplayed()
        composeRule.onNodeWithText("Correo pendiente de verificación").assertIsDisplayed()
        composeRule.onNodeWithText("Cliente").assertIsDisplayed()
    }

    @Test
    fun providerCardShowsWorkerRoleChipAndVerifiedStatus() {
        composeRule.setContent {
            OficiosTheme {
                SettingsScreen(
                    uiState = SettingsUiState(
                        isLoading = false,
                        profile = AccountProfile(
                            user = User(
                                id = 2,
                                email = "trabajador@test.com",
                                firstName = "Ana",
                                lastName = "Lopez",
                                role = UserRole.PROVIDER,
                                emailVerified = true,
                            ),
                        ),
                    ),
                    onEvent = {},
                )
            }
        }
        composeRule.onNodeWithText("Ana Lopez").assertIsDisplayed()
        composeRule.onNodeWithText("trabajador@test.com").assertIsDisplayed()
        composeRule.onNodeWithText("Correo verificado").assertIsDisplayed()
        composeRule.onNodeWithText("Trabajador").assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "w360dp-h800dp-mdpi")
    fun longNameIsFullyVisibleNextToRoleChip() {
        composeRule.setContent {
            OficiosTheme {
                SettingsScreen(
                    uiState = SettingsUiState(
                        isLoading = false,
                        profile = AccountProfile(
                            user = User(
                                id = 3,
                                email = "matias@oficios.test",
                                firstName = "Matías Federico",
                                lastName = "Britez Fernández",
                                role = UserRole.PROVIDER,
                                emailVerified = true,
                            ),
                        ),
                    ),
                    onEvent = {},
                )
            }
        }
        composeRule.onNodeWithText("Matías Federico Britez Fernández").assertIsDisplayed()
        composeRule.onNodeWithText("Trabajador").assertIsDisplayed()
        composeRule.onNodeWithText("Correo verificado").assertIsDisplayed()
    }
}
