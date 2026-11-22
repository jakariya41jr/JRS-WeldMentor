package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.WeldTheme
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

enum class CenterpiecePhase {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING
}

typealias EnergyOrbState = CenterpiecePhase

data class OrbPulseProfile(
    val minScale: Float,
    val maxScale: Float,
    val peakHoldScale: Float,
    val expandDurationMs: Int,
    val contractDurationMs: Int,
    val orbitDurationMs: Int,
    val rippleDurationMs: Int,
    val rippleCount: Int,
    val maxRippleAlpha: Float,
    val haloAlpha: Float,
    val ringStrokeDp: Float
)

fun pulseProfileFor(state: CenterpiecePhase): OrbPulseProfile = when (state) {
    CenterpiecePhase.IDLE -> OrbPulseProfile(
        minScale = 0.96f,
        maxScale = 1.03f,
        peakHoldScale = 1.01f,
        expandDurationMs = 1850,
        contractDurationMs = 2000,
        orbitDurationMs = 7200,
        rippleDurationMs = 2800,
        rippleCount = 1,
        maxRippleAlpha = 0.22f,
        haloAlpha = 0.28f,
        ringStrokeDp = 1.6f
    )
    CenterpiecePhase.LISTENING -> OrbPulseProfile(
        minScale = 0.95f,
        maxScale = 1.11f,
        peakHoldScale = 1.06f,
        expandDurationMs = 460,
        contractDurationMs = 640,
        orbitDurationMs = 4200,
        rippleDurationMs = 1550,
        rippleCount = 2,
        maxRippleAlpha = 0.44f,
        haloAlpha = 0.40f,
        ringStrokeDp = 2.4f
    )
    CenterpiecePhase.THINKING -> OrbPulseProfile(
        minScale = 0.93f,
        maxScale = 1.07f,
        peakHoldScale = 1.04f,
        expandDurationMs = 340,
        contractDurationMs = 380,
        orbitDurationMs = 1500,
        rippleDurationMs = 1200,
        rippleCount = 2,
        maxRippleAlpha = 0.36f,
        haloAlpha = 0.36f,
        ringStrokeDp = 2.2f
    )
    CenterpiecePhase.SPEAKING -> OrbPulseProfile(
        minScale = 0.94f,
        maxScale = 1.14f,
        peakHoldScale = 1.08f,
        expandDurationMs = 310,
        contractDurationMs = 410,
        orbitDurationMs = 3000,
        rippleDurationMs = 1350,
        rippleCount = 3,
        maxRippleAlpha = 0.48f,
        haloAlpha = 0.44f,
        ringStrokeDp = 2.6f
    )
}

/**
 * Custom animated Energy Orb Composable driven by Compose [Animatable] instances for
 * smooth, phase-choreographed pulsing across IDLE, LISTENING, THINKING, and SPEAKING states.
 */
@Composable
fun EnergyOrb(
    state: CenterpiecePhase,
    accentColor: Color,
    rimColor: Color,
    contentDescription: String,
    modifier: Modifier = Modifier,
    orbSize: Dp = 156.dp,
    coreSize: Dp = 82.dp,
    tag: String = "mentor_central_orb",
    onClick: (() -> Unit)? = null
) {
    val palette = WeldTheme.colors
    val profile = remember(state) { pulseProfileFor(state) }

    val animatedAccent by animateColorAsState(
        targetValue = accentColor,
        animationSpec = tween(durationMillis = 280),
        label = "energyOrbAccent"
    )
    val animatedRim by animateColorAsState(
        targetValue = rimColor,
        animationSpec = tween(durationMillis = 280),
        label = "energyOrbRim"
    )

    // Explicit Animatable drivers for core breathing, atmospheric aura, outward ripples, and seamless orbit
    val corePulse = remember { Animatable(1f) }
    val auraPulse = remember { Animatable(1f) }
    val rippleProgress = remember { Animatable(0f) }
    val orbitDegrees = remember { Animatable(0f) }
    val activationScale = remember { Animatable(1f) }

    LaunchedEffect(state) {
        // Tactile spring impulse on state transitions
        launch {
            if (state == CenterpiecePhase.IDLE) {
                activationScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            } else {
                activationScale.animateTo(
                    targetValue = 1.06f,
                    animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing)
                )
                activationScale.animateTo(
                    targetValue = 1.03f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
        }

        // Core pulse loop tailored to idle / listening / thinking / speaking
        launch {
            var beat = 0
            while (isActive) {
                val targetPeak = when (state) {
                    CenterpiecePhase.SPEAKING -> if (beat % 2 == 0) profile.maxScale else profile.peakHoldScale
                    CenterpiecePhase.THINKING -> if (beat % 3 == 0) profile.maxScale else profile.peakHoldScale
                    else -> profile.maxScale
                }
                corePulse.animateTo(
                    targetValue = targetPeak,
                    animationSpec = tween(durationMillis = profile.expandDurationMs, easing = FastOutSlowInEasing)
                )
                corePulse.animateTo(
                    targetValue = profile.minScale,
                    animationSpec = tween(durationMillis = profile.contractDurationMs, easing = FastOutSlowInEasing)
                )
                beat++
            }
        }

        // Atmospheric aura pulse loop slightly decoupled from core for depth
        launch {
            while (isActive) {
                auraPulse.animateTo(
                    targetValue = (profile.maxScale + 0.04f).coerceAtMost(1.18f),
                    animationSpec = tween(
                        durationMillis = (profile.expandDurationMs * 1.15f).toInt(),
                        easing = FastOutSlowInEasing
                    )
                )
                auraPulse.animateTo(
                    targetValue = (profile.minScale - 0.03f).coerceAtLeast(0.86f),
                    animationSpec = tween(
                        durationMillis = (profile.contractDurationMs * 1.15f).toInt(),
                        easing = FastOutSlowInEasing
                    )
                )
            }
        }

        // Expanding ripple rings
        launch {
            while (isActive) {
                rippleProgress.snapTo(0f)
                rippleProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = profile.rippleDurationMs, easing = LinearEasing)
                )
            }
        }

        // Seamless orbital rotation preserving current angle across phase switches
        launch {
            while (isActive) {
                val startAngle = orbitDegrees.value % 360f
                orbitDegrees.snapTo(startAngle)
                orbitDegrees.animateTo(
                    targetValue = startAngle + 360f,
                    animationSpec = tween(durationMillis = profile.orbitDurationMs, easing = LinearEasing)
                )
            }
        }
    }

    val interactionSource = remember { MutableInteractionSource() }
    val showDualOrbitNodes = state == CenterpiecePhase.THINKING || state == CenterpiecePhase.SPEAKING
    val isCompactOrb = orbSize < 100.dp
    val outerInsetDp = if (isCompactOrb) 3.dp else 12.dp
    val innerInsetDp = if (isCompactOrb) 8.dp else 22.dp

    Box(
        modifier = modifier
            .size(orbSize)
            .scale(activationScale.value)
            .clip(CircleShape)
            .then(
                if (onClick != null) {
                    Modifier
                        .minimumInteractiveComponentSize()
                        .clickable(
                            interactionSource = interactionSource,
                            indication = ripple(bounded = true, color = animatedAccent),
                            role = Role.Button,
                            onClick = onClick
                        )
                } else {
                    Modifier
                }
            )
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val maxRadius = size.minDimension / 2f
            val baseHaloAlpha = if (palette.isLight) profile.haloAlpha * 0.72f else profile.haloAlpha

            // Concentric expanding ripples choreographed per state
            val ringCount = profile.rippleCount
            for (ring in 0 until ringCount) {
                val phaseOffset = ring.toFloat() / ringCount.coerceAtLeast(1)
                val t = (rippleProgress.value + phaseOffset) % 1f
                val ringRadius = maxRadius * (0.52f + 0.46f * t)
                val ringAlpha = (1f - t) * profile.maxRippleAlpha
                drawCircle(
                    color = animatedAccent.copy(alpha = ringAlpha),
                    radius = ringRadius,
                    style = Stroke(width = profile.ringStrokeDp.dp.toPx())
                )
            }

            // Soft breathing radial aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        animatedAccent.copy(alpha = baseHaloAlpha),
                        animatedRim.copy(alpha = baseHaloAlpha * 0.45f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = (maxRadius * (auraPulse.value * 0.92f)).coerceAtMost(maxRadius)
                ),
                radius = maxRadius
            )

            // Outer sweeping energy arc
            val currentOrbit = orbitDegrees.value
            rotate(degrees = currentOrbit, pivot = center) {
                val inset = outerInsetDp.toPx()
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(animatedAccent, animatedRim, Color.Transparent, animatedAccent)
                    ),
                    startAngle = 0f,
                    sweepAngle = if (state == CenterpiecePhase.THINKING) 220f else 265f,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - inset * 2, size.height - inset * 2),
                    style = Stroke(
                        width = (if (isCompactOrb) 2.4.dp else 3.2.dp).toPx(),
                        cap = StrokeCap.Round
                    )
                )
            }

            // Counter-rotating plasma filament + orbiting spark dot(s)
            if (!isCompactOrb || state != CenterpiecePhase.IDLE) {
                rotate(degrees = -currentOrbit * 1.45f, pivot = center) {
                    val inset = innerInsetDp.toPx()
                    val arcSpan = size.width - inset * 2
                    if (arcSpan > 4f) {
                        drawArc(
                            color = animatedRim.copy(alpha = 0.74f),
                            startAngle = 25f,
                            sweepAngle = if (state == CenterpiecePhase.THINKING) 145f else 125f,
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = Size(arcSpan, size.height - inset * 2),
                            style = Stroke(
                                width = (if (isCompactOrb) 1.6.dp else 2.2.dp).toPx(),
                                cap = StrokeCap.Round
                            )
                        )
                        val dotRadius = (size.minDimension / 2f) - inset
                        val primaryRad = Math.toRadians(25.0)
                        drawCircle(
                            color = animatedAccent,
                            radius = (if (isCompactOrb) 2.4.dp else 3.6.dp).toPx(),
                            center = Offset(
                                center.x + (dotRadius * cos(primaryRad)).toFloat(),
                                center.y + (dotRadius * sin(primaryRad)).toFloat()
                            )
                        )
                        if (showDualOrbitNodes && !isCompactOrb) {
                            val secondaryRad = Math.toRadians(205.0)
                            drawCircle(
                                color = animatedRim,
                                radius = 3.dp.toPx(),
                                center = Offset(
                                    center.x + (dotRadius * cos(secondaryRad)).toFloat(),
                                    center.y + (dotRadius * sin(secondaryRad)).toFloat()
                                )
                            )
                        }
                    }
                }
            }
        }

        // Luminous pulsating core emblem
        val emblemSize = (coreSize.value * 0.88f).dp
        Box(
            modifier = Modifier
                .size(coreSize)
                .scale(corePulse.value)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            animatedAccent.copy(alpha = 0.30f),
                            palette.surface
                        )
                    )
                )
                .border(
                    width = if (isCompactOrb) 1.5.dp else 2.2.dp,
                    color = animatedAccent.copy(alpha = 0.88f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_weldmentor_symbol),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(emblemSize)
                    .clip(CircleShape)
            )
        }
    }
}
