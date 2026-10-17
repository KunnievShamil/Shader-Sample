package ru.shamilkun.shader_sample.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ShaderColors = darkColorScheme(
    primary = Color(0xFFA4F4CE),
    onPrimary = Color(0xFF07382A),
    background = Color(0xFF0B1114),
    surface = Color(0xFF0B1114),
    surfaceVariant = Color(0xFF182328),
    onBackground = Color(0xFFEEF5F2),
    onSurface = Color(0xFFEEF5F2),
    onSurfaceVariant = Color(0xFFA5B7B4),
    outline = Color(0xFF354741),
)

@Composable
fun ShaderSampleTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ShaderColors, content = content)
}
