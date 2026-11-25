package com.gameturbo.ai.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val GameTurboColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = ObsidianDeep,
    primaryContainer = DeepRoyalBlue,
    onPrimaryContainer = NeonCyanGlow,
    secondary = ElectricRoyalBlue,
    onSecondary = Color.White,
    secondaryContainer = RoyalSurfaceElevated,
    onSecondaryContainer = TextPrimary,
    tertiary = StatusInfo,
    onTertiary = ObsidianDeep,
    background = ObsidianDeep,
    onBackground = TextPrimary,
    surface = RoyalMidnight,
    onSurface = TextPrimary,
    surfaceVariant = RoyalSurface,
    onSurfaceVariant = TextSecondary,
    outline = RoyalBorder,
    outlineVariant = Color(0xFF172554),
    error = StatusHigh,
    onError = Color.White
)

val GameTurboShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun GameTurboTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GameTurboColorScheme,
        typography = GameTurboTypography,
        shapes = GameTurboShapes,
        content = content
    )
}
