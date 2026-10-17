package ru.shamilkun.shader_sample

import android.os.Build
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertRangeInfoEquals
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ShaderNavigationTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun metalRespondsToTouchWhilePaused_andRestoresSettings() {
        compose.onNodeWithTag("demo_metal").performScrollTo()
        compose.mainClock.autoAdvance = false
        compose.onNodeWithTag("demo_metal").performClick()
        compose.mainClock.advanceTimeByFrame()
        compose.onNodeWithText("Прикоснись к свету").assertIsDisplayed()
        if (Build.VERSION.SDK_INT >= 33) {
            compose.onNodeWithTag("metal_distortion").performScrollTo()
                .performSemanticsAction(SemanticsActions.SetProgress) { it(1.7f) }
            compose.mainClock.advanceTimeByFrame()
            compose.onNodeWithText("Приостановить").performScrollTo().performClick()
            compose.mainClock.advanceTimeByFrame()

            val surface = compose.onNodeWithTag("metal_surface").performScrollTo()
            val before = surface.captureToImage().toPixelMap()
            surface.performTouchInput { click(Offset(width * 0.8f, height * 0.2f)) }
            compose.mainClock.advanceTimeByFrame()
            val after = surface.captureToImage().toPixelMap()
            val changed = (1..8).any { x -> (1..6).any { y ->
                before[before.width * x / 10, before.height * y / 10] !=
                    after[after.width * x / 10, after.height * y / 10]
            } }
            assertTrue("Touch must change the rendered metal while time is paused", changed)

            compose.activityRule.scenario.recreate()
            compose.mainClock.advanceTimeByFrame()
            compose.onNodeWithTag("metal_distortion").performScrollTo()
                .assertRangeInfoEquals(ProgressBarRangeInfo(1.7f, 0.25f..2f))
            compose.onNodeWithText("Продолжить").performScrollTo().assertIsDisplayed()
        }
        compose.onNodeWithText("← Все эксперименты").performScrollTo().performClick()
        compose.mainClock.advanceTimeByFrame()
        compose.onNodeWithTag("demo_aurora").assertIsDisplayed()
    }

    @Test
    fun openDemo_restoreScreen_andReturnToGallery() {
        compose.mainClock.autoAdvance = false
        compose.onNodeWithTag("demo_aurora").performClick()
        compose.mainClock.advanceTimeByFrame()
        compose.onNodeWithText("Свет в движении").assertIsDisplayed()

        compose.activityRule.scenario.recreate()
        compose.mainClock.advanceTimeByFrame()
        compose.onNodeWithText("Свет в движении").assertIsDisplayed()

        compose.onNodeWithText("← Все эксперименты").performScrollTo().performClick()
        compose.mainClock.advanceTimeByFrame()
        compose.onNodeWithTag("demo_aurora").assertIsDisplayed()
    }

    @Test
    fun demoShowsPlaybackControls_orCompatibilityMessage() {
        compose.mainClock.autoAdvance = false
        compose.onNodeWithTag("demo_aurora").performClick()
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
