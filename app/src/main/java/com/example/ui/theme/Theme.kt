package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.local.ThemeMode

private val JeetLightColorScheme = lightColorScheme(
    primary = IndigoPrimary,
    onPrimary = Color.White,
    primaryContainer = IndigoSoft,
    onPrimaryContainer = IndigoPrimaryHover,
    secondary = EmeraldSecondary,
    onSecondary = Color.White,
    secondaryContainer = EmeraldSoft,
    onSecondaryContainer = Color(0xFF065F46),
    tertiary = AmberAccent,
    onTertiary = Color.White,
    tertiaryContainer = AmberSoft,
    onTertiaryContainer = Color(0xFF92400E),
    background = LightBackground,
    onBackground = LightTextMain,
    surface = LightSurface,
    onSurface = LightTextMain,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextMuted,
    outline = LightBorder,
    outlineVariant = LightBorder,
    error = RoseDanger,
    onError = Color.White,
    errorContainer = RoseSoft,
    onErrorContainer = Color(0xFF991B1B)
)

private val JeetDarkColorScheme = darkColorScheme(
    primary = IndigoLight,
    onPrimary = Color.White,
    primaryContainer = IndigoPrimary.copy(alpha = 0.25f),
    onPrimaryContainer = IndigoSoft,
    secondary = EmeraldSecondary,
    onSecondary = Color(0xFF022C22),
    secondaryContainer = EmeraldSecondary.copy(alpha = 0.22f),
    onSecondaryContainer = EmeraldSoft,
    tertiary = AmberAccent,
    onTertiary = Color(0xFF451A03),
    tertiaryContainer = AmberAccent.copy(alpha = 0.22f),
    onTertiaryContainer = AmberSoft,
    background = DarkBackground,
    onBackground = DarkTextMain,
    surface = DarkSurface,
    onSurface = DarkTextMain,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextMuted,
    outline = DarkBorder,
    outlineVariant = DarkBorder,
    error = RoseDanger,
    onError = Color.White,
    errorContainer = RoseDanger.copy(alpha = 0.22f),
    onErrorContainer = RoseSoft
)

val JeetShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun JeetXToppersTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val useDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }
    MaterialTheme(
        colorScheme = if (useDark) JeetDarkColorScheme else JeetLightColorScheme,
        typography = JeetTypography,
        shapes = JeetShapes,
        content = content
    )
}
