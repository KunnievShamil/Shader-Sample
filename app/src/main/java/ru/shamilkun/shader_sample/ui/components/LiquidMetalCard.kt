package ru.shamilkun.shader_sample.ui.components

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ru.shamilkun.shader_sample.R

/** A metallic shader surface. The touch position is normalized so it survives size changes. */
@Composable
fun LiquidMetalCard(
    time: State<Float>,
    distortion: State<Float>,
    modifier: Modifier = Modifier,
    interactive: Boolean = true,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val touch = remember { mutableStateOf(Offset(0.5f, 0.5f)) }
    val supported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    val background = if (supported) {
        Modifier.metalBackground(time, distortion, touch)
    } else {
        Modifier.background(Brush.linearGradient(listOf(
            Color(0xFF18232E), Color(0xFFB0C6D0), Color(0xFF344D5D), Color(0xFF142029),
        )))
    }
    val interaction = if (interactive && supported) {
        Modifier.pointerInput(Unit) {
            awaitEachGesture {
                val down = awaitFirstDown()
                fun updateTouch(position: Offset) {
                    touch.value = Offset(
                        (position.x / size.width.coerceAtLeast(1)).coerceIn(0f, 1f),
                        (position.y / size.height.coerceAtLeast(1)).coerceIn(0f, 1f),
                    )
                }
                updateTouch(down.position)
                down.consume()
                do {
                    val event = awaitPointerEvent()
                    event.changes.firstOrNull { it.id == down.id }?.let {
                        updateTouch(it.position)
                    }
                    event.changes.forEach { it.consume() }
                } while (event.changes.any { it.pressed })
            }
        }
    } else Modifier
    Box(modifier.clip(RoundedCornerShape(28.dp)).then(background).then(interaction), content = content)
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun Modifier.metalBackground(
    time: State<Float>,
    distortion: State<Float>,
    touch: State<Offset>,
): Modifier {
    val context = LocalContext.current
    val shader = remember(context) {
        RuntimeShader(context.resources.openRawResource(R.raw.liquid_metal).bufferedReader().use { it.readText() })
    }
    val brush = remember(shader) { ShaderBrush(shader) }
    return drawWithCache {
        shader.setFloatUniform("resolution", size.width, size.height)
        onDrawBehind {
            shader.setFloatUniform("time", time.value)
            shader.setFloatUniform("distortion", distortion.value)
            shader.setFloatUniform("touch", touch.value.x, touch.value.y)
            drawRect(brush)
        }
    }
}
