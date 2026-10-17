package ru.shamilkun.shader_sample

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.content.ContextCompat
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test

@SdkSuppress(minSdkVersion = 33)
class CameraShaderTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun cameraStreams_switchesEffect_andRestoresSettings() {
        compose.onNodeWithTag("gallery").performScrollToNode(hasTestTag("demo_camera"))
        compose.onNodeWithTag("demo_camera").performClick()
        if (ContextCompat.checkSelfPermission(compose.activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            compose.onNodeWithText("Разрешить камеру").assertIsDisplayed()
            InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(
                compose.activity.packageName, Manifest.permission.CAMERA,
            )
            compose.activityRule.scenario.recreate()
        }
        waitForStream("THERMAL / LIVE")
        compose.onNodeWithTag("camera_strength").performScrollTo()
            .performSemanticsAction(SemanticsActions.SetProgress) { it(0.6f) }
        compose.onNodeWithTag("camera_toggle").performScrollTo().performClick()
        waitForStream("ORIGINAL / LIVE")

        compose.onNodeWithTag("camera_switch").performScrollTo().assertIsEnabled().performClick()
        waitForStream("ORIGINAL / LIVE")
        compose.onNodeWithTag("camera_toggle").performScrollTo().performClick()
        waitForStream("THERMAL / LIVE")

        compose.activityRule.scenario.recreate()
        waitForStream("THERMAL / LIVE")
        compose.onNodeWithTag("camera_strength").performScrollTo()
            .assertRangeInfoEquals(ProgressBarRangeInfo(0.6f, 0f..1f))
        compose.onNodeWithText("← Все эксперименты").performScrollTo().performClick()
        compose.onNodeWithTag("demo_aurora").assertIsDisplayed()
    }

    private fun waitForStream(label: String) {
        compose.waitUntil(timeoutMillis = 30_000) {
            compose.onAllNodesWithText(label).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
