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
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSlateBg)
            .drawBehind {
                // Subtle precision workshop blueprint grid
                val gridStep = 44.dp.toPx()
                val gridColor = Color.White.copy(alpha = 0.025f)
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

                // Ambient welding arc & molten atmosphere glows
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(ArcCyan.copy(alpha = 0.16f), Color.Transparent),
                        center = Offset(size.width * 0.14f, size.height * 0.07f),
                        radius = size.maxDimension * 0.56f
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(MoltenAmber.copy(alpha = 0.11f), Color.Transparent),
                        center = Offset(size.width * 0.90f, size.height * 0.36f),
                        radius = size.maxDimension * 0.48f
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(PassEmerald.copy(alpha = 0.08f), Color.Transparent),
                        center = Offset(size.width * 0.18f, size.height * 0.90f),
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
    cornerRadius: Dp = 20.dp,
    borderColor: Color = GlassBorder,
    accentGlow: Color? = null,
    onClick: (() -> Unit)? = null,
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val bgBrush = Brush.verticalGradient(
        colors = listOf(
            (accentGlow ?: ArcCyan).copy(alpha = if (accentGlow != null) 0.14f else 0.07f),
            SlateSurface.copy(alpha = 0.88f),
            SlateSurfaceElevated.copy(alpha = 0.72f)
        )
    )
    val rimBrush = Brush.verticalGradient(
        colors = listOf(
            if (borderColor == GlassBorder) Color.White.copy(alpha = 0.22f) else borderColor.copy(alpha = 0.72f),
            borderColor.copy(alpha = 0.28f)
        )
    )
    val baseModifier = modifier
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
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.45f))
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
    iconTint: Color = ArcCyan,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = iconTint.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, iconTint.copy(alpha = 0.40f))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
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
                color = TextPrimary
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
        if (trailing != null) {
            trailing()
        }
    }
}
