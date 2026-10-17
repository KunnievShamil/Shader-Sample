package ru.shamilkun.shader_sample.ui

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.isActive
import ru.shamilkun.shader_sample.R
import ru.shamilkun.shader_sample.ui.components.AuroraCard
import ru.shamilkun.shader_sample.ui.components.LiquidMetalCard
import ru.shamilkun.shader_sample.ui.theme.ShaderSampleTheme

private enum class Demo { Aurora, LiquidMetal, ThermalCamera }

@Composable
fun ShaderApp() {
    var selectedDemo by rememberSaveable { mutableStateOf<Demo?>(null) }
    BackHandler(enabled = selectedDemo != null) { selectedDemo = null }
    Surface(modifier = Modifier.fillMaxSize()) {
        val demo = selectedDemo
        if (demo != null) {
            key(demo) {
                if (demo == Demo.ThermalCamera) CameraScreen(onBack = { selectedDemo = null })
                else DemoScreen(demo = demo, onBack = { selectedDemo = null })
            }
        } else {
            GalleryScreen(onOpenDemo = { selectedDemo = it })
        }
    }
}

@Composable
private fun GalleryScreen(onOpenDemo: (Demo) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().testTag("gallery"),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Eyebrow(stringResource(R.string.lab_label))
                Text(stringResource(R.string.gallery_title), style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.SemiBold)
                Text(stringResource(R.string.gallery_description), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item(key = "aurora") {
            Card(
                onClick = { onOpenDemo(Demo.Aurora) },
                modifier = Modifier.testTag("demo_aurora"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                AuroraCard(remember { mutableFloatStateOf(0f) }, Modifier.fillMaxWidth().height(180.dp)) {
                    Text("01 / AGSL", Modifier.padding(20.dp), style = MaterialTheme.typography.labelMedium)
                }
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.aurora_title), style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.aurora_description), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(stringResource(R.string.open_demo), Modifier.padding(top = 8.dp),
                        color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        item(key = "liquid_metal") {
            Card(
                onClick = { onOpenDemo(Demo.LiquidMetal) },
                modifier = Modifier.testTag("demo_metal"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                LiquidMetalCard(
                    time = remember { mutableFloatStateOf(0f) },
                    distortion = remember { mutableFloatStateOf(1f) },
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    interactive = false,
                ) {
                    Text("02 / AGSL", Modifier.padding(20.dp), style = MaterialTheme.typography.labelMedium)
                }
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.metal_title), style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.metal_description), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(stringResource(R.string.open_demo), Modifier.padding(top = 8.dp),
                        color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        item(key = "thermal_camera") {
            Card(
                onClick = { onOpenDemo(Demo.ThermalCamera) },
                modifier = Modifier.testTag("demo_camera"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Box(Modifier.fillMaxWidth().height(180.dp).background(
                    androidx.compose.ui.graphics.Brush.linearGradient(listOf(
                        androidx.compose.ui.graphics.Color(0xFF080A30), androidx.compose.ui.graphics.Color(0xFF154DF2),
                        androidx.compose.ui.graphics.Color(0xFFCB0A73), androidx.compose.ui.graphics.Color(0xFFFF500A),
                        androidx.compose.ui.graphics.Color(0xFFFFFA8C),
                    )))) {
                    Text("03 / LIVE CAMERA", Modifier.padding(20.dp), style = MaterialTheme.typography.labelMedium)
                }
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.camera_title), style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.camera_gallery_description), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(stringResource(R.string.open_demo), Modifier.padding(top = 8.dp),
                        color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        item {
            Text(stringResource(R.string.gallery_footer), style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DemoScreen(demo: Demo, onBack: () -> Unit) {
    val isMetal = demo == Demo.LiquidMetal
    var playing by rememberSaveable { mutableStateOf(true) }
    var speed by rememberSaveable { mutableFloatStateOf(1f) }
    val distortion = rememberSaveable { mutableFloatStateOf(1f) }
    val time = rememberSaveable { mutableFloatStateOf(0f) }
    val supported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val currentSpeed by rememberUpdatedState(speed)

    LaunchedEffect(playing, supported, lifecycle) {
        if (playing && supported) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                var previousFrame = withFrameNanos { it }
                while (isActive) {
                    withFrameNanos { frame ->
                        time.floatValue += ((frame - previousFrame) / 1_000_000_000f) * currentSpeed
                        previousFrame = frame
                    }
                }
            }
        }
    }

    Column(
        Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp)) {
            Text(stringResource(R.string.back_to_gallery))
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Eyebrow(stringResource(if (isMetal) R.string.second_experiment else R.string.first_experiment))
            Text(stringResource(if (isMetal) R.string.metal_title else R.string.aurora_title), style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold)
            Text(stringResource(if (isMetal) R.string.metal_demo_description else R.string.demo_description), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (isMetal) {
            LiquidMetalCard(time, distortion, Modifier.fillMaxWidth().height(300.dp).testTag("metal_surface")) {
                // A scrim keeps ordinary Compose text legible over moving reflections.
                Column(Modifier.align(Alignment.BottomStart)
                    .background(androidx.compose.ui.graphics.Color(0xB30B1114), RoundedCornerShape(topEnd = 24.dp))
                    .padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("CHROME / TOUCH & FLOW", style = MaterialTheme.typography.labelMedium)
                    Text(stringResource(R.string.metal_card_title), style = MaterialTheme.typography.headlineMedium)
                }
            }
        } else AuroraCard(time, Modifier.fillMaxWidth().height(300.dp)) {
            Column(Modifier.align(Alignment.BottomStart).padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.card_label), style = MaterialTheme.typography.labelMedium)
                Text(stringResource(R.string.card_title), style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Medium)
            }
        }
        if (supported) {
            Column(
                Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(24.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.speed), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.speed_value, speed), color = MaterialTheme.colorScheme.primary)
                }
                Slider(value = speed, onValueChange = { speed = it }, valueRange = 0.25f..2f)
                if (isMetal) {
                    Text(stringResource(R.string.distortion), style = MaterialTheme.typography.titleMedium)
                    Slider(value = distortion.floatValue, onValueChange = { distortion.floatValue = it },
                        valueRange = 0.25f..2f, modifier = Modifier.testTag("metal_distortion"))
                }
                FilledTonalButton(onClick = { playing = !playing }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(if (playing) R.string.pause else R.string.resume))
                }
            }
        }
        Text(
            stringResource(if (!supported) R.string.shader_unsupported else if (isMetal) R.string.metal_explanation else R.string.shader_explanation),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun Eyebrow(text: String) {
    Text(text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
}

@Preview(showBackground = true)
@Composable
private fun GalleryPreview() {
    ShaderSampleTheme { ShaderApp() }
}
