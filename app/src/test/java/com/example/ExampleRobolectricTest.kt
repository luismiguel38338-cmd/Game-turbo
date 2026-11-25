package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.gameturbo.ai.MainEntry
import com.gameturbo.ai.ui.theme.GameTurboTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `read app_name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Game Turbo AI", appName)
    }

    @Test
    fun `navigate across all primary tabs and verify dashboard cards`() {
        composeRule.setContent {
            GameTurboTheme {
                MainEntry(showSplashInitially = false)
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("refresh_telemetry_button").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_booster").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("scan_games_button").assertIsDisplayed()

        composeRule.onNodeWithTag("nav_monitor").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("monitor_tab_4").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("nav_profiles").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("create_profile_button").assertIsDisplayed()

        composeRule.onNodeWithTag("nav_advanced").performClick()
        composeRule.waitForIdle()
    }
}
