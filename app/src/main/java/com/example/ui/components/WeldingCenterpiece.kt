package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.DeepSlateBg
import com.example.ui.theme.GlassCard
import com.example.ui.theme.MoltenAmber
import com.example.ui.theme.PassEmerald
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletSpark
import com.example.ui.theme.WarnRose
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

enum class CenterpiecePhase {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING
}

@Composable
fun WeldingArcCenterpiece(
    phase: CenterpiecePhase,
    title: String,
    subtitle: String,
    badgeText: String,
    modifier: Modifier = Modifier
) {
    val targetAccent = when (phase) {
        CenterpiecePhase.IDLE -> ArcCyan
        CenterpiecePhase.LISTENING -> WarnRose
        CenterpiecePhase.THINKING -> MoltenAmber
        CenterpiecePhase.SPEAKING -> PassEmerald
    }
    val secondaryAccent = when (phase) {
        CenterpiecePhase.IDLE -> MoltenAmber
        CenterpiecePhase.LISTENING -> ArcCyan
        CenterpiecePhase.THINKING -> VioletSpark
        CenterpiecePhase.SPEAKING -> ArcCyan
    }

    val accentColor by animateColorAsState(
        targetValue = targetAccent,
        animationSpec = tween(durationMillis = 280),
        label = "centerpieceAccent"
    )
    val rimColor by animateColorAsState(
        targetValue = secondaryAccent,
        animationSpec = tween(durationMillis = 280),
        label = "centerpieceRim"
    )

    val transition = rememberInfiniteTransition(label = "weldArcPulse")
    val wavePhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (phase) {
                    CenterpiecePhase.IDLE -> 3200
                    CenterpiecePhase.THINKING -> 1400
                    else -> 1150
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )
    val orbitDeg by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (phase == CenterpiecePhase.THINKING) 1800 else 7200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbitDeg"
    )
    val pulseScale by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 22.dp,
        accentGlow = accentColor,
        borderColor = accentColor.copy(alpha = 0.45f),
        contentPadding = 14.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Precision Welding Arc & Emblem Ring
            Box(
                modifier = Modifier.size(74.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val radius = size.minDimension / 2f
                    // Ambient radial arc glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(accentColor.copy(alpha = 0.34f * pulseScale), Color.Transparent),
                            center = center,
                            radius = radius
                        )
                    )
                    // Outer segmented telemetry ring
                    rotate(degrees = orbitDeg, pivot = center) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(accentColor, rimColor, Color.Transparent, accentColor)
                            ),
                            startAngle = 0f,
                            sweepAngle = 250f,
                            useCenter = false,
                            topLeft = Offset(3.dp.toPx(), 3.dp.toPx()),
                            size = Size(size.width - 6.dp.toPx(), size.height - 6.dp.toPx()),
                            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    // Inner counter-rotating arc spark ring
                    rotate(degrees = -orbitDeg * 1.4f, pivot = center) {
                        drawArc(
                            color = rimColor.copy(alpha = 0.65f),
                            startAngle = 30f,
                            sweepAngle = 110f,
                            useCenter = false,
                            topLeft = Offset(8.dp.toPx(), 8.dp.toPx()),
                            size = Size(size.width - 16.dp.toPx(), size.height - 16.dp.toPx()),
                            style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(DeepSlateBg)
                        .border(1.5.dp, accentColor.copy(alpha = 0.7f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_weldmentor_symbol),
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = accentColor.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f))
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Smooth Harmonic Arc / Voice Waveform Visualizer
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                ) {
                    val midY = size.height / 2f
                    val barCount = 26
                    val barStep = size.width / barCount
                    val barWidth = (barStep * 0.48f).coerceAtLeast(2.dp.toPx())
                    val amplitudeMultiplier = when (phase) {
                        CenterpiecePhase.IDLE -> 0.28f
                        CenterpiecePhase.THINKING -> 0.55f
                        CenterpiecePhase.LISTENING -> 0.92f
                        CenterpiecePhase.SPEAKING -> 0.88f
                    }

                    // Equalizer bars
                    for (i in 0 until barCount) {
                        val normX = i.toFloat() / (barCount - 1).coerceAtLeast(1)
                        val envelope = sin(normX * PI).toFloat()
                        val rawWave = abs(sin(wavePhase + i * 0.52f) * 0.65f + sin(wavePhase * 1.7f - i * 0.31f) * 0.35f)
                        val barHalfHeight = (size.height * 0.46f * amplitudeMultiplier * envelope * (0.35f + 0.65f * rawWave))
                            .coerceAtLeast(2.dp.toPx())

                        val xPos = i * barStep + (barStep - barWidth) / 2f
                        drawRoundRect(
                            brush = Brush.verticalGradient(listOf(accentColor, rimColor)),
                            topLeft = Offset(xPos, midY - barHalfHeight),
                            size = Size(barWidth, barHalfHeight * 2f),
                            cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
                            alpha = 0.4f + 0.6f * envelope
                        )
                    }

                    // Smooth harmonic glow filament along center
                    val wavePath = Path()
                    val stepPx = (size.width / 48f).coerceAtLeast(2f)
                    var x = 0f
                    while (x <= size.width) {
                        val nx = x / size.width.coerceAtLeast(1f)
                        val env = sin(nx * PI).toFloat()
                        val yOffset = sin(wavePhase * 1.4f + nx * 10f) * (size.height * 0.34f) * amplitudeMultiplier * env
                        if (x == 0f) wavePath.moveTo(x, midY + yOffset)
                        else wavePath.lineTo(x, midY + yOffset)
                        x += stepPx
                    }
                    drawPath(
                        path = wavePath,
                        brush = Brush.horizontalGradient(listOf(Color.Transparent, accentColor, rimColor, Color.Transparent)),
                        style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
}
