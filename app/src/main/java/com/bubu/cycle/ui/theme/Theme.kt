package com.bubu.cycle.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Pink40,
    secondary = Purple40
)

private val DarkColors = darkColorScheme(
    primary = Pink80,
    secondary = Purple80
)

@Composable
fun CycleTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColors, content = content)
}
