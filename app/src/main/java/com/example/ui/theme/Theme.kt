package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val StudyPulseColorScheme = darkColorScheme(
    primary = PulseBlue,
    onPrimary = TextPrimary,
    primaryContainer = PulseBlue.copy(alpha = 0.22f),
    onPrimaryContainer = PulseBlueSoft,
    secondary = PulsePurple,
    onSecondary = TextPrimary,
    secondaryContainer = PulsePurple.copy(alpha = 0.22f),
    onSecondaryContainer = PulsePurpleSoft,
    tertiary = PulseEmerald,
    onTertiary = PulseEmeraldDark,
    tertiaryContainer = PulseEmerald.copy(alpha = 0.18f),
    onTertiaryContainer = PulseEmeraldLight,
    background = MidnightBg,
    onBackground = TextPrimary,
    surface = MidnightSurface,
    onSurface = TextPrimary,
    surfaceVariant = MidnightSurfaceHigh,
    onSurfaceVariant = TextMuted,
    outline = CardBorderBlue,
    outlineVariant = CardBorderSubtle,
    error = PulseRose,
    onError = TextPrimary,
    errorContainer = PulseRose.copy(alpha = 0.20f),
    onErrorContainer = PulseRoseLight
)

val StudyPulseShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun StudyPulseTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = StudyPulseColorScheme,
        typography = StudyPulseTypography,
        shapes = StudyPulseShapes,
        content = content
    )
}
