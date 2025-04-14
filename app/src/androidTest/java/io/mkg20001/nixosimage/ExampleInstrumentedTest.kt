package io.mkg20001.nixosimage

import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.Rule

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    companion object {
        @BeforeClass @JvmStatic
        fun before() {
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            instrumentation.uiAutomation.executeShellCommand(
                "appops set ${BuildConfig.APPLICATION_ID} MANAGE_EXTERNAL_STORAGE allow"
            ).close()
        }
    }
    
    
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("io.mkg20001.nixosimage", appContext.packageName)
    }

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testLoadMainView() {
        composeTestRule.waitUntil(6000) {
            // Replace with your condition, e.g., UI element becomes visible after async event
            composeTestRule.onNodeWithTag("loaded_ui").isDisplayed()
        }

        composeTestRule.onNodeWithTag("install").performClick()

        Thread.sleep(2000)

        composeTestRule.waitUntil(6000) {
            // Replace with your condition, e.g., UI element becomes visible after async event
            composeTestRule.onNodeWithTag("install_ui").isDisplayed()
        }
    }

    @Test
    fun testRefresh() {
        composeTestRule.waitUntil(6000) {
            // Replace with your condition, e.g., UI element becomes visible after async event
            composeTestRule.onNodeWithTag("loaded_ui").isDisplayed()
        }

        composeTestRule.onNodeWithTag("refresh").performClick()

        composeTestRule.waitUntil(2000) {
            // Replace with your condition, e.g., UI element becomes visible after async event
            composeTestRule.onNodeWithTag("loading_ui").isDisplayed()
        }
    }

    /*

    Also test:
    - no network error

     */
}