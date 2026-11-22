package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class WeldPalette(
    val isLight: Boolean,
    val bg: Color,
    val bgSecondary: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val glassFillTop: Color,
    val glassFillBottom: Color,
    val glassBorder: Color,
    val glassRimHighlight: Color,
    val arcCyan: Color,
    val arcCyanDeep: Color,
    val moltenAmber: Color,
    val moltenOrange: Color,
    val passEmerald: Color,
    val warnRose: Color,
    val violetSpark: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val gridLine: Color,
    val progressTrack: Color,
    val cardShadow: Color
)

// Dark Glassmorphism Industrial Palette
val DeepSlateBg = Color(0xFF070B12)
val SlateSurface = Color(0xFF0F1724)
val SlateSurfaceElevated = Color(0xFF162133)
val GlassCardFill = Color(0xB8131E2E)
val GlassCardHighlight = Color(0x22FFFFFF)
val GlassBorder = Color(0x26E2E8F0)
val GlassBorderBright = Color(0x4D38BDF8)

// Welding Arc & Molten Accents
val ArcCyan = Color(0xFF38BDF8)
val ArcCyanDeep = Color(0xFF0284C7)
val MoltenAmber = Color(0xFFF59E0B)
val MoltenOrange = Color(0xFFEA580C)
val PassEmerald = Color(0xFF10B981)
val WarnRose = Color(0xFFF43F5E)
val VioletSpark = Color(0xFFA78BFA)

// Typography Contrast
val TextPrimary = Color(0xFFF1F5F9)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)

val WeldDarkPalette = WeldPalette(
    isLight = false,
    bg = DeepSlateBg,
    bgSecondary = Color(0xFF0B111C),
    surface = SlateSurface,
    surfaceElevated = SlateSurfaceElevated,
    glassFillTop = SlateSurface.copy(alpha = 0.88f),
    glassFillBottom = SlateSurfaceElevated.copy(alpha = 0.74f),
    glassBorder = GlassBorder,
    glassRimHighlight = Color.White.copy(alpha = 0.22f),
    arcCyan = ArcCyan,
    arcCyanDeep = ArcCyanDeep,
    moltenAmber = MoltenAmber,
    moltenOrange = MoltenOrange,
    passEmerald = PassEmerald,
    warnRose = WarnRose,
    violetSpark = VioletSpark,
    textPrimary = TextPrimary,
    textSecondary = TextSecondary,
    textMuted = TextMuted,
    gridLine = Color.White.copy(alpha = 0.026f),
    progressTrack = Color.White.copy(alpha = 0.09f),
    cardShadow = Color.Black.copy(alpha = 0.45f)
)

// Clean Light Glass Surface Aesthetic (soft shadows, crisp borders, high-contrast ink)
val WeldLightPalette = WeldPalette(
    isLight = true,
    bg = Color(0xFFF3F6FB),
    bgSecondary = Color(0xFFE9EFF8),
    surface = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFF8FAFC),
    glassFillTop = Color(0xFAFFFFFF),
    glassFillBottom = Color(0xEBF4F8FD),
    glassBorder = Color(0xFFDCE5F1),
    glassRimHighlight = Color(0xFFFFFFFF),
    arcCyan = Color(0xFF0284C7),
    arcCyanDeep = Color(0xFF0369A1),
    moltenAmber = Color(0xFFD97706),
    moltenOrange = Color(0xFFC2410C),
    passEmerald = Color(0xFF059669),
    warnRose = Color(0xFFE11D48),
    violetSpark = Color(0xFF7C3AED),
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF475569),
    textMuted = Color(0xFF64748B),
    gridLine = Color(0xFF0F172A).copy(alpha = 0.032f),
    progressTrack = Color(0xFF0F172A).copy(alpha = 0.08f),
    cardShadow = Color(0xFF0F172A).copy(alpha = 0.08f)
)
