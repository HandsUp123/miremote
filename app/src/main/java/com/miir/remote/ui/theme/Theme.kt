package com.miir.remote.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = MiOrange,
    onPrimary = OnDark,
    primaryContainer = MiOrangeLight,
    onPrimaryContainer = OnLight,
    secondary = MiOrangeDark,
    background = LightBackground,
    onBackground = OnLight,
    surface = LightSurface,
    onSurface = OnLight,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF666666)
)

private val DarkColors = darkColorScheme(
    primary = MiOrange,
    onPrimary = OnDark,
    primaryContainer = MiOrangeDark,
    onPrimaryContainer = OnDark,
    secondary = MiOrangeLight,
    background = DarkBackground,
    onBackground = OnDark,
    surface = DarkSurface,
    onSurface = OnDark,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFAAAAAA)
)

@Composable
fun MiIrRemoteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = MiTypography,
        content = content
    )
}
