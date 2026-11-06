package com.cocido.oficios.feature.auth.wizard

import androidx.compose.material3.Text
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cocido.oficios.feature.auth.TestHostActivity
import com.cocido.oficios.feature.auth.navigation.AuthDestination
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class RegistrationWizardNavigationTest {
    @get:Rule val rule = createAndroidComposeRule<TestHostActivity>()
    private val events = Channel<RegistrationWizardEffect>(Channel.BUFFERED)
    private val version = mutableIntStateOf(0)
    private lateinit var nav: NavHostController

    private fun scene(restoration: StateRestorationTester = StateRestorationTester(rule)): StateRestorationTester {
        restoration.setContent {
            nav = rememberNavController()
            val effects = remember { events.receiveAsFlow() }
            RegistrationWizardEffectHandler(effects, LocalLifecycleOwner.current, nav, {}, {}, {})
            Text("version ${version.intValue}")
            NavHost(nav, startDestination = AuthDestination.WIZARD_PERSONAL) {
                composable(AuthDestination.WIZARD_PERSONAL) { Text("Personal") }
                composable(AuthDestination.WIZARD_PROFILE) { Text("Contact") }
            }
        }
        rule.waitForIdle()
        return restoration
    }

    private fun advance() {
        rule.runOnIdle { assertTrue(events.trySend(RegistrationWizardEffect.GoToProfile).isSuccess) }
        rule.waitForIdle()
        rule.runOnIdle { assertEquals(AuthDestination.WIZARD_PROFILE, nav.currentDestination?.route) }
    }

    @Test fun duplicateEventIsSingleTopAndBackDoesNotRenavigate() {
        scene()
        advance()
        advance()
        rule.runOnIdle {
            assertEquals(AuthDestination.WIZARD_PERSONAL, nav.previousBackStackEntry?.destination?.route)
            assertTrue(nav.popBackStack())
        }
        rule.waitForIdle()
        rule.runOnIdle { assertEquals(AuthDestination.WIZARD_PERSONAL, nav.currentDestination?.route) }
    }

    @Test fun recompositionDoesNotRepeatNavigation() {
        scene()
        advance()
        var entry = ""
        rule.runOnIdle { entry = nav.currentBackStackEntry!!.id; version.intValue++ }
        rule.waitForIdle()
        rule.runOnIdle {
            assertEquals(entry, nav.currentBackStackEntry!!.id)
            nav.popBackStack()
        }
        rule.waitForIdle()
        rule.runOnIdle { assertEquals(AuthDestination.WIZARD_PERSONAL, nav.currentDestination?.route) }
    }

    @Test fun savedStateRecreationDoesNotReplayConsumedEvent() {
        val restoration = scene()
        advance()
        restoration.emulateSavedInstanceStateRestore()
        rule.waitForIdle()
        rule.runOnIdle {
            assertEquals(AuthDestination.WIZARD_PROFILE, nav.currentDestination?.route)
            assertTrue(nav.popBackStack())
        }
        rule.waitForIdle()
        rule.runOnIdle { assertEquals(AuthDestination.WIZARD_PERSONAL, nav.currentDestination?.route) }
    }
}
