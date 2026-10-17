package ru.shamilkun.shader_sample.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Observer
import androidx.lifecycle.compose.LocalLifecycleOwner
import ru.shamilkun.shader_sample.R

@Composable
fun CameraScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    fun permissionGranted() = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    var granted by remember { mutableStateOf(permissionGranted()) }
    var denied by rememberSaveable { mutableStateOf(false) }
    var strength by rememberSaveable { mutableFloatStateOf(1f) }
    var enabled by rememberSaveable { mutableStateOf(true) }
    var front by rememberSaveable { mutableStateOf(false) }
    var canSwitch by remember { mutableStateOf(false) }
    var streaming by remember { mutableStateOf(false) }
    var cameraError by remember { mutableStateOf(false) }
    var retry by remember { mutableIntStateOf(0) }
    val supported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        granted = it
        denied = !it
    }
    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) granted = permissionGranted()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp)) { Text(stringResource(R.string.back_to_gallery)) }
        Text(stringResource(R.string.third_experiment), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(stringResource(R.string.camera_title), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.SemiBold)
        Text(stringResource(R.string.camera_description), color = MaterialTheme.colorScheme.onSurfaceVariant)

        if (!supported) {
            CameraMessage(stringResource(R.string.camera_unsupported))
        } else if (!granted) {
            CameraMessage(stringResource(if (denied) R.string.camera_permission_denied else R.string.camera_permission_description)) {
                Button(onClick = { permission.launch(Manifest.permission.CAMERA) }) {
                    Text(stringResource(R.string.camera_grant))
                }
                if (denied) {
                    TextButton(onClick = {
                        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:${context.packageName}")))
                    }) { Text(stringResource(R.string.camera_settings)) }
                }
            }
        } else {
            Box(Modifier.fillMaxWidth().height(380.dp).clip(RoundedCornerShape(28.dp)).background(Color.Black)) {
                ThermalCameraPreview(
                    front = front,
                    strength = if (enabled) strength else 0f,
                    retry = retry,
                    onStreaming = { streaming = it },
                    onAvailability = { canSwitch = it },
                    onError = { cameraError = it },
                    modifier = Modifier.fillMaxSize(),
                )
                if (cameraError) {
                    CameraMessage(stringResource(R.string.camera_error)) {
                        Button(onClick = { retry++ }) { Text(stringResource(R.string.camera_retry)) }
                    }
                } else if (!streaming) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                } else {
                    Text(stringResource(if (enabled) R.string.camera_filtered else R.string.camera_original),
                        Modifier.align(Alignment.TopStart).padding(16.dp)
                            .background(Color(0xC00B1114), RoundedCornerShape(12.dp)).padding(12.dp),
                        style = MaterialTheme.typography.labelMedium)
                }
            }
            Column(
                Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(24.dp)).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(stringResource(R.string.camera_strength), style = MaterialTheme.typography.titleMedium)
                val strengthLabel = stringResource(R.string.camera_strength)
                Slider(value = strength, onValueChange = { strength = it }, valueRange = 0f..1f,
                    enabled = enabled, modifier = Modifier.testTag("camera_strength").semantics { contentDescription = strengthLabel })
                FilledTonalButton(onClick = { enabled = !enabled }, modifier = Modifier.fillMaxWidth().testTag("camera_toggle")) {
                    Text(stringResource(if (enabled) R.string.camera_show_original else R.string.camera_show_effect))
                }
                OutlinedButton(onClick = { front = !front }, enabled = canSwitch,
                    modifier = Modifier.fillMaxWidth().testTag("camera_switch")) {
                    Text(stringResource(R.string.camera_switch))
                }
            }
            Text(stringResource(R.string.camera_note), style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CameraMessage(message: String, content: @Composable ColumnScope.() -> Unit = {}) {
    Column(
        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(24.dp)).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(message)
        content()
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun ThermalCameraPreview(
    front: Boolean,
    strength: Float,
    retry: Int,
    onStreaming: (Boolean) -> Unit,
    onAvailability: (Boolean) -> Unit,
    onError: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember(context) {
        PreviewView(context).apply {
            // A SurfaceView is composed separately and would bypass the AGSL RenderEffect.
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    val shader = remember(context) {
        RuntimeShader(context.resources.openRawResource(R.raw.thermal_camera).bufferedReader().use { it.readText() })
    }
    val description = stringResource(R.string.camera_preview_description)
    AndroidView(
        factory = { previewView },
        modifier = modifier.testTag("camera_preview").semantics { contentDescription = description },
        update = {
            shader.setFloatUniform("strength", strength)
            it.setRenderEffect(if (strength > 0f) RenderEffect.createRuntimeShaderEffect(shader, "camera") else null)
            it.invalidate()
        },
    )
    DisposableEffect(previewView, lifecycleOwner, front, retry) {
        var disposed = false
        var provider: ProcessCameraProvider? = null
        val preview = Preview.Builder().build()
        onError(false)
        onStreaming(false)
        onAvailability(false)
        val observer = Observer<PreviewView.StreamState> { onStreaming(it == PreviewView.StreamState.STREAMING) }
        previewView.previewStreamState.observe(lifecycleOwner, observer)
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            if (!disposed) {
                try {
                    val cameraProvider = future.get()
                    provider = cameraProvider
                    val rear = cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)
                    val selfie = cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
                    onAvailability(rear && selfie)
                    val selector = when {
                        front && selfie -> CameraSelector.DEFAULT_FRONT_CAMERA
                        rear -> CameraSelector.DEFAULT_BACK_CAMERA
                        selfie -> CameraSelector.DEFAULT_FRONT_CAMERA
                        else -> error("No camera available")
                    }
                    preview.setSurfaceProvider(previewView.surfaceProvider)
                    cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview)
                } catch (exception: Exception) {
                    Log.w("ThermalCamera", "Unable to start camera preview", exception)
                    onError(true)
                }
            }
        }, ContextCompat.getMainExecutor(context))
        onDispose {
            disposed = true
            previewView.previewStreamState.removeObserver(observer)
            provider?.unbind(preview)
        }
    }
}
