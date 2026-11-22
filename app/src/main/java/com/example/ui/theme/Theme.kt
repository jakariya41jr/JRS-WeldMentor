package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.local.AppThemeMode

val LocalWeldPalette = staticCompositionLocalOf { WeldDarkPalette }

object WeldTheme {
    val colors: WeldPalette
        @Composable
        @ReadOnlyComposable
        get() = LocalWeldPalette.current
}

private val WeldDarkColorScheme = darkColorScheme(
    primary = WeldDarkPalette.arcCyan,
    onPrimary = Color(0xFF04131E),
    primaryContainer = Color(0xFF0E334D),
    onPrimaryContainer = Color(0xFFBAE6FD),
    secondary = WeldDarkPalette.moltenAmber,
    onSecondary = Color(0xFF1F1200),
    secondaryContainer = Color(0xFF3F2704),
    onSecondaryContainer = Color(0xFFFDE68A),
    tertiary = WeldDarkPalette.passEmerald,
    onTertiary = Color(0xFF022015),
    tertiaryContainer = Color(0xFF0A3A2A),
    onTertiaryContainer = Color(0xFFA7F3D0),
    error = WeldDarkPalette.warnRose,
    onError = Color.White,
    errorContainer = Color(0xFF4C0D1B),
    onErrorContainer = Color(0xFFFECDD3),
    background = WeldDarkPalette.bg,
    onBackground = WeldDarkPalette.textPrimary,
    surface = WeldDarkPalette.surface,
    onSurface = WeldDarkPalette.textPrimary,
    surfaceVariant = WeldDarkPalette.surfaceElevated,
    onSurfaceVariant = WeldDarkPalette.textSecondary,
    outline = WeldDarkPalette.glassBorder,
    outlineVariant = Color(0xFF1E293B)
)

private val WeldLightColorScheme = lightColorScheme(
    primary = WeldLightPalette.arcCyan,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0C4A6E),
    secondary = WeldLightPalette.moltenAmber,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = WeldLightPalette.passEmerald,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD1FAE5),
    onTertiaryContainer = Color(0xFF064E3B),
    error = WeldLightPalette.warnRose,
    onError = Color.White,
    errorContainer = Color(0xFFFFE4E6),
    onErrorContainer = Color(0xFF881337),
    background = WeldLightPalette.bg,
    onBackground = WeldLightPalette.textPrimary,
    surface = WeldLightPalette.surface,
    onSurface = WeldLightPalette.textPrimary,
    surfaceVariant = WeldLightPalette.surfaceElevated,
    onSurfaceVariant = WeldLightPalette.textSecondary,
    outline = WeldLightPalette.glassBorder,
    outlineVariant = Color(0xFFCBD5E1)
)

val WeldShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun JrsWeldMentorTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val isLight = themeMode == AppThemeMode.LIGHT
    val palette = if (isLight) WeldLightPalette else WeldDarkPalette
    val colorScheme = if (isLight) WeldLightColorScheme else WeldDarkColorScheme

    CompositionLocalProvider(LocalWeldPalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = WeldTypography,
            shapes = WeldShapes,
            content = content
        )
    }
}
