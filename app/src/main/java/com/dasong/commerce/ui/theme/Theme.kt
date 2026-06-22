package com.dasong.commerce.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SongLightColorScheme = lightColorScheme(
    primary = SongGold,
    onPrimary = Color.White,
    primaryContainer = SongGold.copy(alpha = 0.15f),
    onPrimaryContainer = SongInk,
    secondary = SongRed,
    onSecondary = Color.White,
    secondaryContainer = SongRed.copy(alpha = 0.15f),
    onSecondaryContainer = SongInk,
    tertiary = SongTeal,
    onTertiary = Color.White,
    background = SongPaper,
    onBackground = SongInk,
    surface = Color.White,
    onSurface = SongInk,
    surfaceVariant = SongPaper.copy(alpha = 0.7f),
    onSurfaceVariant = SongInk.copy(alpha = 0.7f),
    outline = SongBrown.copy(alpha = 0.5f)
)

@Composable
fun DaSongCommerceTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SongLightColorScheme,
        typography = SongTypography,
        content = content
    )
}
