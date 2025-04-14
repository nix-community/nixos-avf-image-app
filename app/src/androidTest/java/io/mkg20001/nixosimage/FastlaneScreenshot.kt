package io.mkg20001.nixosimage

import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.mkg20001.nixosimage.ui.install.Install
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.cleanstatusbar.CleanStatusBar
import tools.fastlane.screengrab.locale.LocaleTestRule


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class FastlaneScreenshot {
    companion object {
        @BeforeClass @JvmStatic
        fun beforeAll() {
            CleanStatusBar.enableWithDefaults()

            val instrumentation = InstrumentationRegistry.getInstrumentation()
            instrumentation.uiAutomation.executeShellCommand(
                "appops set ${BuildConfig.APPLICATION_ID} MANAGE_EXTERNAL_STORAGE allow"
            ).close()
        }

        @AfterClass @JvmStatic
        fun afterAll() {
            CleanStatusBar.disable()
        }
    }

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("io.mkg20001.nixosimage", appContext.packageName)
    }

    @Rule @JvmField
    val localeTestRule = LocaleTestRule()

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    @get:Rule
    val composeTestRuleTwo = createAndroidComposeRule<Install>()

    @Test
    fun testTakeScreenshot() {
        composeTestRule.waitUntil(6000) {
            // Replace with your condition, e.g., UI element becomes visible after async event
            composeTestRule.onNodeWithTag("loaded_ui").isDisplayed()
        }

        Screengrab.screenshot("loaded")

        composeTestRule.onNodeWithTag("install").performClick()

        Thread.sleep(2000)

        composeTestRule.waitUntil(6000) {
            // Replace with your condition, e.g., UI element becomes visible after async event
            composeTestRule.onNodeWithTag("install_ui").isDisplayed()
        }

        Screengrab.screenshot("installing")
    }
}