package ru.shamilkun.shader_sample

import android.os.Build
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Rule
import org.junit.Test

class ShaderNavigationTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun openDemo_restoreScreen_andReturnToGallery() {
        compose.mainClock.autoAdvance = false
        compose.onNodeWithText("Открыть эксперимент →").performClick()
        compose.mainClock.advanceTimeByFrame()
        compose.onNodeWithText("Свет в движении").assertIsDisplayed()

        compose.activityRule.scenario.recreate()
        compose.mainClock.advanceTimeByFrame()
        compose.onNodeWithText("Свет в движении").assertIsDisplayed()

        compose.onNodeWithText("← Все эксперименты").performScrollTo().performClick()
        compose.mainClock.advanceTimeByFrame()
        compose.onNodeWithText("Открыть эксперимент →").assertIsDisplayed()
    }

    @Test
    fun demoShowsPlaybackControls_orCompatibilityMessage() {
        compose.mainClock.autoAdvance = false
        compose.onNodeWithText("Открыть эксперимент →").performClick()
        compose.mainClock.advanceTimeByFrame()
        if (Build.VERSION.SDK_INT >= 33) {
            compose.onNodeWithText("Приостановить").performScrollTo().performClick()
            compose.mainClock.advanceTimeByFrame()
            compose.onNodeWithText("Продолжить").assertIsDisplayed().performClick()
            compose.mainClock.advanceTimeByFrame()
            compose.onNodeWithText("Приостановить").assertIsDisplayed()
        } else {
            compose.onNodeWithText(compose.activity.getString(R.string.shader_unsupported))
                .performScrollTo().assertIsDisplayed()
        }
    }
}
