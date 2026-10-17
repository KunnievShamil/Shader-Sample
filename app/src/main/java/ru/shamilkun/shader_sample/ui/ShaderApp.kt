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
import ru.shamilkun.shader_sample.ui.theme.ShaderSampleTheme

@Composable
fun ShaderApp() {
    var showAurora by rememberSaveable { mutableStateOf(false) }
    BackHandler(enabled = showAurora) { showAurora = false }
    Surface(modifier = Modifier.fillMaxSize()) {
        if (showAurora) {
            AuroraScreen(onBack = { showAurora = false })
        } else {
            GalleryScreen(onOpenAurora = { showAurora = true })
        }
    }
}

@Composable
private fun GalleryScreen(onOpenAurora: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().safeDrawingPadding(),
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
                onClick = onOpenAurora,
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
        item {
            Text(stringResource(R.string.gallery_footer), style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AuroraScreen(onBack: () -> Unit) {
    var playing by rememberSaveable { mutableStateOf(true) }
    var speed by rememberSaveable { mutableFloatStateOf(1f) }
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
            Eyebrow(stringResource(R.string.first_experiment))
            Text(stringResource(R.string.aurora_title), style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.demo_description), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        AuroraCard(time, Modifier.fillMaxWidth().height(300.dp)) {
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
                FilledTonalButton(onClick = { playing = !playing }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(if (playing) R.string.pause else R.string.resume))
                }
            }
        }
        Text(
            stringResource(if (supported) R.string.shader_explanation else R.string.shader_unsupported),
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
