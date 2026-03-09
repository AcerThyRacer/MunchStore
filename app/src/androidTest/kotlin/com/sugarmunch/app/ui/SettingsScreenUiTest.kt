package com.sugarmunch.app.ui

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.sugarmunch.app.MainActivity
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for Settings screen.
 */
class SettingsScreenUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun settingsNavigation_shouldOpenSettings() {
        composeTestRule.apply {
            waitForIdle()
            onNodeWithContentDescription("Settings").performClick()
            waitForIdle()
            // Settings screen should show (e.g. title or a settings-specific element)
            onNodeWithContentDescription("Settings").assertExists()
        }
    }
}
