package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DarkIndigo,
    secondary = DarkCyan,
    tertiary = DarkEmerald,
    background = DarkBg,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onPrimary = Color.White,
    onSecondary = DarkBg,
    onTertiary = DarkBg,
    onBackground = LightText,
    onSurface = LightText,
    onSurfaceVariant = LightText
)

private val LightColorScheme = lightColorScheme(
    primary = DeepIndigo,
    secondary = CyanBlue,
    tertiary = EmeraldGreen,
    background = SoftWhite,
    surface = Color.White,
    surfaceVariant = SoftSlate,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = DarkSlate,
    onSurface = DarkSlate,
    onSurfaceVariant = CharcoalGray
)

@Composable
fun CareeronixTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
