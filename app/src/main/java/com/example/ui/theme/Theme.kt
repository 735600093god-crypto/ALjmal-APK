package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Emerald500,
    onPrimary = White,
    primaryContainer = Emerald900,
    onPrimaryContainer = Emerald100,
    secondary = Amber400,
    onSecondary = Stone950,
    secondaryContainer = Amber950,
    onSecondaryContainer = Amber200,
    background = Stone950,
    surface = Stone900,
    onBackground = Stone100,
    onSurface = Stone100,
    surfaceVariant = Stone800,
    onSurfaceVariant = Stone300,
    outline = Stone700
)

private val LightColorScheme = lightColorScheme(
    primary = Emerald800,
    onPrimary = White,
    primaryContainer = Emerald50,
    onPrimaryContainer = Emerald950,
    secondary = Amber500,
    onSecondary = Stone950,
    secondaryContainer = Amber100,
    onSecondaryContainer = Amber950,
    background = Stone100,
    surface = White,
    onBackground = Stone900,
    onSurface = Stone900,
    surfaceVariant = Stone50,
    onSurfaceVariant = Stone600,
    outline = Stone200
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Preserve custom emerald and gold brand identity across all OS versions
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
