package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.ui.theme.GlassCard
import com.example.ui.theme.WeldTheme
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

enum class CenterpiecePhase {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING
}

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

    val transition = rememberInfiniteTransition(label = "weldEnergyOrb")
    val wavePhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (phase) {
                    CenterpiecePhase.IDLE -> 3200
                    CenterpiecePhase.THINKING -> 1300
                    else -> 1100
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
                durationMillis = if (phase == CenterpiecePhase.THINKING) 1700 else 6800,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbitDeg"
    )
    val breatheScale by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = when (phase) {
            CenterpiecePhase.IDLE -> 1.03f
            CenterpiecePhase.LISTENING -> 1.10f
            CenterpiecePhase.THINKING -> 1.05f
            CenterpiecePhase.SPEAKING -> 1.11f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (phase == CenterpiecePhase.IDLE) 2000 else 850,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breatheScale"
    )
    val rippleProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleProgress"
    )

    val activeScale by animateFloatAsState(
        targetValue = if (phase == CenterpiecePhase.IDLE) 1f else 1.03f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 260f),
        label = "activeScale"
    )

    if (compact) {
        CompactEnergyOrbBanner(
            phase = phase,
            title = title,
            subtitle = subtitle,
            badgeText = badgeText,
            statusText = statusText,
            accentColor = accentColor,
            rimColor = rimColor,
            wavePhase = wavePhase,
            orbitDeg = orbitDeg,
            breatheScale = breatheScale,
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
            // Top bar inside card: title + model/mode pill
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

            // Prominent Central Energy Orb
            val orbInteraction = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .size(152.dp)
                    .scale(activeScale)
                    .clip(CircleShape)
                    .then(
                        if (onOrbClick != null) {
                            Modifier
                                .minimumInteractiveComponentSize()
                                .clickable(
                                    interactionSource = orbInteraction,
                                    indication = ripple(bounded = true, color = accentColor),
                                    role = Role.Button,
                                    onClick = onOrbClick
                                )
                        } else {
                            Modifier
                        }
                    )
                    .testTag("mentor_central_orb"),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val maxRadius = size.minDimension / 2f

                    // Expanding energy ripples during active speech/listen
                    if (phase == CenterpiecePhase.LISTENING || phase == CenterpiecePhase.SPEAKING) {
                        for (ring in 0..1) {
                            val t = (rippleProgress + ring * 0.5f) % 1f
                            val ringRadius = maxRadius * (0.55f + 0.43f * t)
                            drawCircle(
                                color = accentColor.copy(alpha = (1f - t) * 0.38f),
                                radius = ringRadius,
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                    }

                    // Soft radial aura
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = if (palette.isLight) 0.24f else 0.34f),
                                rimColor.copy(alpha = if (palette.isLight) 0.12f else 0.16f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = maxRadius * breatheScale.coerceAtMost(1.0f)
                        ),
                        radius = maxRadius
                    )

                    // Outer swirling orbital arc
                    rotate(degrees = orbitDeg, pivot = center) {
                        val inset = 12.dp.toPx()
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(accentColor, rimColor, Color.Transparent, accentColor)
                            ),
                            startAngle = 0f,
                            sweepAngle = 265f,
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = Size(size.width - inset * 2, size.height - inset * 2),
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    // Counter-rotating plasma filament + orbiting spark dot
                    rotate(degrees = -orbitDeg * 1.45f, pivot = center) {
                        val inset = 22.dp.toPx()
                        drawArc(
                            color = rimColor.copy(alpha = 0.72f),
                            startAngle = 25f,
                            sweepAngle = 125f,
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = Size(size.width - inset * 2, size.height - inset * 2),
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                        )
                        val dotAngle = Math.toRadians(25.0)
                        val dotR = (size.minDimension / 2f) - inset
                        drawCircle(
                            color = accentColor,
                            radius = 3.5.dp.toPx(),
                            center = Offset(
                                center.x + (dotR * cos(dotAngle)).toFloat(),
                                center.y + (dotR * sin(dotAngle)).toFloat()
                            )
                        )
                    }
                }

                // Luminous Core Emblem
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(breatheScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    accentColor.copy(alpha = 0.28f),
                                    palette.surface
                                )
                            )
                        )
                        .border(2.dp, accentColor.copy(alpha = 0.85f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_weldmentor_symbol),
                        contentDescription = statusText,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Prominent Status Text (TAP TO START / LISTENING... / THINKING... / SPEAKING...)
            Text(
                text = statusText,
                style = MaterialTheme.typography.titleMedium,
                color = accentColor,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = MaterialTheme.typography.labelLarge.letterSpacing,
                modifier = Modifier.testTag("orb_status_text")
            )

            // Interactive Interrupt / Tap Cue when SPEAKING or LISTENING
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
                wavePhase = wavePhase,
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
    orbitDeg: Float,
    breatheScale: Float,
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
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .then(
                        if (onOrbClick != null) {
                            Modifier
                                .minimumInteractiveComponentSize()
                                .clickable(onClick = onOrbClick)
                        } else {
                            Modifier
                        }
                    )
                    .testTag("mentor_central_orb"),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val radius = size.minDimension / 2f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(accentColor.copy(alpha = 0.34f * breatheScale), Color.Transparent),
                            center = center,
                            radius = radius
                        )
                    )
                    rotate(degrees = orbitDeg, pivot = center) {
                        drawArc(
                            brush = Brush.sweepGradient(listOf(accentColor, rimColor, Color.Transparent, accentColor)),
                            startAngle = 0f,
                            sweepAngle = 250f,
                            useCenter = false,
                            topLeft = Offset(3.dp.toPx(), 3.dp.toPx()),
                            size = Size(size.width - 6.dp.toPx(), size.height - 6.dp.toPx()),
                            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(palette.surface)
                        .border(1.5.dp, accentColor.copy(alpha = 0.78f), CircleShape),
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
