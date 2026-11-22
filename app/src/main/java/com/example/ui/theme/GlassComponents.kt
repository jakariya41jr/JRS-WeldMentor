package com.example.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun WeldGlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val palette = WeldTheme.colors
    val cyanAlpha = if (palette.isLight) 0.11f else 0.16f
    val amberAlpha = if (palette.isLight) 0.08f else 0.11f
    val emeraldAlpha = if (palette.isLight) 0.07f else 0.08f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(palette.bg, palette.bgSecondary)
                )
            )
            .drawBehind {
                val gridStep = 44.dp.toPx()
                val gridColor = palette.gridLine
                var x = gridStep
                while (x < size.width) {
                    drawLine(
                        color = gridColor,
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 1f
                    )
                    x += gridStep
                }
                var y = gridStep
                while (y < size.height) {
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f
                    )
                    y += gridStep
                }

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(palette.arcCyan.copy(alpha = cyanAlpha), Color.Transparent),
                        center = Offset(size.width * 0.16f, size.height * 0.07f),
                        radius = size.maxDimension * 0.56f
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(palette.moltenAmber.copy(alpha = amberAlpha), Color.Transparent),
                        center = Offset(size.width * 0.90f, size.height * 0.36f),
                        radius = size.maxDimension * 0.48f
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(palette.passEmerald.copy(alpha = emeraldAlpha), Color.Transparent),
                        center = Offset(size.width * 0.20f, size.height * 0.90f),
                        radius = size.maxDimension * 0.44f
                    )
                )
            },
        content = content
    )
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 22.dp,
    borderColor: Color? = null,
    accentGlow: Color? = null,
    onClick: (() -> Unit)? = null,
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val palette = WeldTheme.colors
    val shape = RoundedCornerShape(cornerRadius)
    val resolvedBorder = borderColor ?: palette.glassBorder
    val accentTintAlpha = if (palette.isLight) {
        if (accentGlow != null) 0.08f else 0.02f
    } else {
        if (accentGlow != null) 0.14f else 0.06f
    }

    val bgBrush = Brush.verticalGradient(
        colors = listOf(
            if (palette.isLight) {
                (accentGlow ?: palette.arcCyan).copy(alpha = accentTintAlpha)
                    .let { tint ->
                        Color(
                            red = (palette.glassFillTop.red * (1f - accentTintAlpha) + tint.red * accentTintAlpha).coerceIn(0f, 1f),
                            green = (palette.glassFillTop.green * (1f - accentTintAlpha) + tint.green * accentTintAlpha).coerceIn(0f, 1f),
                            blue = (palette.glassFillTop.blue * (1f - accentTintAlpha) + tint.blue * accentTintAlpha).coerceIn(0f, 1f),
                            alpha = palette.glassFillTop.alpha
                        )
                    }
            } else {
                (accentGlow ?: palette.arcCyan).copy(alpha = accentTintAlpha)
            },
            palette.glassFillTop,
            palette.glassFillBottom
        )
    )

    val rimBrush = Brush.verticalGradient(
        colors = listOf(
            if (borderColor == null) palette.glassRimHighlight else resolvedBorder.copy(alpha = 0.72f),
            resolvedBorder.copy(alpha = if (palette.isLight) 0.85f else 0.34f)
        )
    )

    val shadowModifier = if (palette.isLight) {
        modifier.shadow(
            elevation = 8.dp,
            shape = shape,
            ambientColor = palette.cardShadow,
            spotColor = palette.cardShadow
        )
    } else {
        modifier
    }

    val baseModifier = shadowModifier
        .clip(shape)
        .background(brush = bgBrush, shape = shape)
        .border(width = 1.dp, brush = rimBrush, shape = shape)
        .then(
            if (onClick != null) {
                Modifier
                    .minimumInteractiveComponentSize()
                    .clickable(onClick = onClick)
            } else {
                Modifier
            }
        )
        .padding(contentPadding)

    Column(modifier = baseModifier, content = content)
}

@Composable
fun ArcStatusPill(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val isLight = WeldTheme.colors.isLight
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = if (isLight) 0.12f else 0.16f),
        border = BorderStroke(1.dp, color.copy(alpha = if (isLight) 0.32f else 0.45f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun GlassSectionHeader(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconTint: Color? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    val palette = WeldTheme.colors
    val tint = iconTint ?: palette.arcCyan

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = tint.copy(alpha = if (palette.isLight) 0.12f else 0.16f),
                border = BorderStroke(1.dp, tint.copy(alpha = if (palette.isLight) 0.28f else 0.40f))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = palette.textPrimary
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary
                )
            }
        }
        if (trailing != null) {
            trailing()
        }
    }
}
