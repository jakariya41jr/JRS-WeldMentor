package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val WeldDarkColorScheme = darkColorScheme(
    primary = ArcCyan,
    onPrimary = Color(0xFF04131E),
    primaryContainer = Color(0xFF0E334D),
    onPrimaryContainer = Color(0xFFBAE6FD),
    secondary = MoltenAmber,
    onSecondary = Color(0xFF1F1200),
    secondaryContainer = Color(0xFF3F2704),
    onSecondaryContainer = Color(0xFFFDE68A),
    tertiary = PassEmerald,
    onTertiary = Color(0xFF022015),
    tertiaryContainer = Color(0xFF0A3A2A),
    onTertiaryContainer = Color(0xFFA7F3D0),
    error = WarnRose,
    onError = Color.White,
    errorContainer = Color(0xFF4C0D1B),
    onErrorContainer = Color(0xFFFECDD3),
    background = DeepSlateBg,
    onBackground = TextPrimary,
    surface = SlateSurface,
    onSurface = TextPrimary,
    surfaceVariant = SlateSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder,
    outlineVariant = Color(0xFF1E293B)
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
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = WeldDarkColorScheme,
        typography = WeldTypography,
        shapes = WeldShapes,
        content = content
    )
}
