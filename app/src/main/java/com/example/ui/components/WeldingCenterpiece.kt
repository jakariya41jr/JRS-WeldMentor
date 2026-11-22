package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.ui.theme.GlassCard
import com.example.ui.theme.WeldTheme
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun WeldEnergyOrbCenterpiece(
    phase: CenterpiecePhase,
    title: String,
    subtitle: String,
    badgeText: String,
    lang: AppLanguage,
    compact: Boolean = false,
    onOrbClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val palette = WeldTheme.colors

    val targetAccent = when (phase) {
        CenterpiecePhase.IDLE -> palette.arcCyan
        CenterpiecePhase.LISTENING -> palette.warnRose
        CenterpiecePhase.THINKING -> palette.moltenAmber
        CenterpiecePhase.SPEAKING -> palette.passEmerald
    }
    val secondaryAccent = when (phase) {
        CenterpiecePhase.IDLE -> palette.violetSpark
        CenterpiecePhase.LISTENING -> palette.arcCyan
        CenterpiecePhase.THINKING -> palette.violetSpark
        CenterpiecePhase.SPEAKING -> palette.arcCyan
    }

    val accentColor by animateColorAsState(
        targetValue = targetAccent,
        animationSpec = tween(durationMillis = 300),
        label = "orbAccent"
    )
    val rimColor by animateColorAsState(
        targetValue = secondaryAccent,
        animationSpec = tween(durationMillis = 300),
        label = "orbRim"
    )

    val statusText = when (phase) {
        CenterpiecePhase.IDLE -> AppStrings.orbTapToStart.get(lang)
        CenterpiecePhase.LISTENING -> AppStrings.orbListening.get(lang)
        CenterpiecePhase.THINKING -> AppStrings.orbThinking.get(lang)
        CenterpiecePhase.SPEAKING -> AppStrings.orbSpeaking.get(lang)
    }

    val wavePhaseAnim = remember { Animatable(0f) }
    LaunchedEffect(phase) {
        val cycleMs = when (phase) {
            CenterpiecePhase.IDLE -> 3200
            CenterpiecePhase.THINKING -> 1300
            CenterpiecePhase.LISTENING -> 1050
            CenterpiecePhase.SPEAKING -> 950
        }
        val twoPi = (2.0 * PI).toFloat()
        while (isActive) {
            val start = wavePhaseAnim.value % twoPi
            wavePhaseAnim.snapTo(start)
            wavePhaseAnim.animateTo(
                targetValue = start + twoPi,
                animationSpec = tween(durationMillis = cycleMs, easing = LinearEasing)
            )
        }
    }

    if (compact) {
        CompactEnergyOrbBanner(
            phase = phase,
            title = title,
            subtitle = subtitle,
            badgeText = badgeText,
            statusText = statusText,
            accentColor = accentColor,
            rimColor = rimColor,
            wavePhase = wavePhaseAnim.value,
            lang = lang,
            onOrbClick = onOrbClick,
            modifier = modifier
        )
        return
    }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 26.dp,
        accentGlow = accentColor,
        borderColor = accentColor.copy(alpha = if (palette.isLight) 0.34f else 0.45f),
        contentPadding = 16.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = palette.textPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = accentColor.copy(alpha = if (palette.isLight) 0.12f else 0.16f),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = if (palette.isLight) 0.34f else 0.45f))
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            EnergyOrb(
                state = phase,
                accentColor = accentColor,
                rimColor = rimColor,
                contentDescription = statusText,
                orbSize = 156.dp,
                coreSize = 82.dp,
                onClick = onOrbClick
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = statusText,
                style = MaterialTheme.typography.titleMedium,
                color = accentColor,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = MaterialTheme.typography.labelLarge.letterSpacing,
                modifier = Modifier.testTag("orb_status_text")
            )

            AnimatedVisibility(visible = phase == CenterpiecePhase.SPEAKING && onOrbClick != null) {
                Surface(
                    onClick = { onOrbClick?.invoke() },
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .testTag("orb_tap_to_interrupt_btn"),
                    shape = RoundedCornerShape(50),
                    color = palette.moltenAmber.copy(alpha = if (palette.isLight) 0.14f else 0.18f),
                    border = BorderStroke(1.dp, palette.moltenAmber.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PanTool,
                            contentDescription = null,
                            tint = palette.moltenAmber,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = AppStrings.orbTapToInterrupt.get(lang),
                            style = MaterialTheme.typography.labelMedium,
                            color = palette.moltenAmber,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            AnimatedVisibility(visible = phase == CenterpiecePhase.LISTENING && onOrbClick != null) {
                Surface(
                    onClick = { onOrbClick?.invoke() },
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .testTag("orb_tap_to_stop_btn"),
                    shape = RoundedCornerShape(50),
                    color = palette.warnRose.copy(alpha = if (palette.isLight) 0.12f else 0.16f),
                    border = BorderStroke(1.dp, palette.warnRose.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Stop,
                            contentDescription = null,
                            tint = palette.warnRose,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = AppStrings.orbTapToStop.get(lang),
                            style = MaterialTheme.typography.labelMedium,
                            color = palette.warnRose
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = palette.textSecondary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            HarmonicWaveBar(
                phase = phase,
                accentColor = accentColor,
                rimColor = rimColor,
                wavePhase = wavePhaseAnim.value,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
            )
        }
    }
}

@Composable
private fun CompactEnergyOrbBanner(
    phase: CenterpiecePhase,
    title: String,
    subtitle: String,
    badgeText: String,
    statusText: String,
    accentColor: Color,
    rimColor: Color,
    wavePhase: Float,
    lang: AppLanguage,
    onOrbClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val palette = WeldTheme.colors
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 22.dp,
        accentGlow = accentColor,
        borderColor = accentColor.copy(alpha = if (palette.isLight) 0.34f else 0.45f),
        contentPadding = 12.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            EnergyOrb(
                state = phase,
                accentColor = accentColor,
                rimColor = rimColor,
                contentDescription = title,
                orbSize = 70.dp,
                coreSize = 46.dp,
                onClick = onOrbClick
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (phase == CenterpiecePhase.SPEAKING) AppStrings.orbTapToInterrupt.get(lang) else statusText,
                        style = MaterialTheme.typography.labelLarge,
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = accentColor.copy(alpha = if (palette.isLight) 0.12f else 0.16f),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = if (palette.isLight) 0.32f else 0.45f))
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

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))
                HarmonicWaveBar(
                    phase = phase,
                    accentColor = accentColor,
                    rimColor = rimColor,
                    wavePhase = wavePhase,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                )
            }
        }
    }
}

@Composable
private fun HarmonicWaveBar(
    phase: CenterpiecePhase,
    accentColor: Color,
    rimColor: Color,
    wavePhase: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val midY = size.height / 2f
        val barCount = 26
        val barStep = size.width / barCount
        val barWidth = (barStep * 0.48f).coerceAtLeast(2.dp.toPx())
        val amplitudeMultiplier = when (phase) {
            CenterpiecePhase.IDLE -> 0.28f
            CenterpiecePhase.THINKING -> 0.58f
            CenterpiecePhase.LISTENING -> 0.94f
            CenterpiecePhase.SPEAKING -> 0.90f
        }

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
                alpha = 0.42f + 0.58f * envelope
            )
        }

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
