package ru.shamilkun.shader_sample.ui.components

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ru.shamilkun.shader_sample.R

/** Time is read during drawing to avoid recomposing the card on every frame. */
@Composable
fun AuroraCard(
    time: State<Float>,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val background = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Modifier.auroraBackground(time)
    } else {
        Modifier.background(Brush.linearGradient(listOf(
            Color(0xFF101C36), Color(0xFF277967), Color(0xFF594285),
        )))
    }
    Box(modifier.clip(RoundedCornerShape(28.dp)).then(background), content = content)
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun Modifier.auroraBackground(time: State<Float>): Modifier {
    val context = LocalContext.current
    val shader = remember(context) {
        val source = context.resources.openRawResource(R.raw.aurora).bufferedReader().use { it.readText() }
        RuntimeShader(source)
    }
    val brush = remember(shader) { ShaderBrush(shader) }
    return drawWithCache {
        shader.setFloatUniform("resolution", size.width, size.height)
        onDrawBehind {
            shader.setFloatUniform("time", time.value)
            drawRect(brush)
        }
    }
}
