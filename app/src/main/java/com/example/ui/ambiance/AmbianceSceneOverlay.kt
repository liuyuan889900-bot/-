package com.example.ui.ambiance

import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private class Particle(
    var x: Float = 0f,
    var y: Float = 0f,
    var speedY: Float = 0f,
    var speedX: Float = 0f,
    var sizeDp: Float = 0f,
    var baseAlpha: Float = 0f,
    var phase: Float = 0f,
    var swayAmpDp: Float = 0f,
    var swayFreq: Float = 0f,
    var rotation: Float = 0f,
    var rotationSpeed: Float = 0f,
    var type: Int = 0,       // 0, 1, or 2 for shape variation
    var depthLayer: Int = 0, // 0=far, 1=mid, 2=near
    var rollSpeed: Float = 0f,
    var rollPhase: Float = 0f,
    var pitchSpeed: Float = 0f,
    var pitchPhase: Float = 0f,
    var colorVariant: Int = 0
)

/**
 * Task 5: Masterpiece Poetic Atmospheric Scene Physics Engine.
 * 
 * Features:
 * 1. RAIN (听雨读书): Realistic tapered teardrop water droplets with gradient transparency,
 *    quiet serene descent (slower speed with 3-tier depth parallax), and delicate splash ripples.
 * 2. WINDOW_RAIN (隔窗听雨 - NEW): Condensation water beads on glass with 3D specular highlights
 *    and subtle shadow; stationary beads + slow sliding trickles with fading condensation tracks.
 * 3. SNOW (踏雪寻章): Pure bright white, dual morphology: 6-pointed hexagonal crystal flakes
 *    with delicate branchlets + soft distant snow pellets, fluttering with horizontal harmonic sway.
 * 4. STARS (星夜伴读): Brilliant warm celestial white/gold stars, dual morphology: 4-pointed
 *    cross lens-flare stars + 5-pointed classic stars, pulsating with out-of-phase respiratory twinkling.
 * 5. PETALS (落英缤纷): Spiraling delicate peach blossom petals drifting in the breeze.
 */
@Composable
fun AmbianceSceneOverlay(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false
) {
    val currentScene by AmbianceManager.currentScene.collectAsState()
    val isLowPower by AmbianceManager.isLowPowerMode.collectAsState()
    val density = LocalDensity.current

    if (currentScene == ReadingAmbiance.NONE) {
        return
    }

    val particleCount = when (currentScene) {
        ReadingAmbiance.AI_FLOW -> 0
        ReadingAmbiance.RAIN -> if (isLowPower) 22 else 38
        ReadingAmbiance.WINDOW_RAIN -> if (isLowPower) 38 else 68
        ReadingAmbiance.SNOW -> if (isLowPower) 20 else 36
        ReadingAmbiance.STARS -> if (isLowPower) 22 else 40
        ReadingAmbiance.PETALS -> if (isLowPower) 16 else 26
        ReadingAmbiance.NONE -> 0
    }

    // Pre-allocate fixed array of particles per scene configuration (zero allocations in draw loop)
    val particles = remember(currentScene, particleCount) {
        val rnd = Random(System.currentTimeMillis())
        Array<Particle>(particleCount) { idx ->
            when (currentScene) {
                ReadingAmbiance.RAIN -> {
                    val layer = idx % 3
                    val (speed, len, alpha) = when (layer) {
                        0 -> Triple(0.18f + rnd.nextFloat() * 0.06f, 10f + rnd.nextFloat() * 4f, 0.22f + rnd.nextFloat() * 0.15f) // Far
                        1 -> Triple(0.24f + rnd.nextFloat() * 0.08f, 16f + rnd.nextFloat() * 6f, 0.38f + rnd.nextFloat() * 0.20f) // Mid
                        else -> Triple(0.30f + rnd.nextFloat() * 0.08f, 22f + rnd.nextFloat() * 8f, 0.55f + rnd.nextFloat() * 0.25f) // Near
                    }
                    Particle(
                        x = rnd.nextFloat(),
                        y = rnd.nextFloat(),
                        speedY = speed,
                        speedX = -0.025f - rnd.nextFloat() * 0.015f,
                        sizeDp = len,
                        baseAlpha = alpha,
                        phase = rnd.nextFloat() * 6.28f,
                        type = if (idx % 4 == 0) 1 else 0,
                        depthLayer = layer
                    )
                }
                ReadingAmbiance.WINDOW_RAIN -> {
                    // Apple iPhone 4S Lockscreen Water Droplets Distribution:
                    // type 1: sliding droplets with natural cleared trail (10%)
                    // type 0: classic medium water droplets (40%)
                    // type 2: tiny fine condensation beads (35%)
                    // type 3: large sagging water domes with gravity elongation (10%)
                    // type 4: twin merged double-bubble water beads (5%)
                    val rVal = rnd.nextFloat()
                    val (type, size, speed) = when {
                        rVal < 0.10f -> Triple(1, 4.8f + rnd.nextFloat() * 2.8f, 0.016f + rnd.nextFloat() * 0.024f)
                        rVal < 0.50f -> Triple(0, 3.2f + rnd.nextFloat() * 2.5f, 0f)
                        rVal < 0.85f -> Triple(2, 1.3f + rnd.nextFloat() * 1.3f, 0f)
                        rVal < 0.95f -> Triple(3, 7.0f + rnd.nextFloat() * 3.5f, 0f)
                        else -> Triple(4, 4.0f + rnd.nextFloat() * 2.0f, 0f)
                    }
                    val stretch = if (type == 3) 1.25f + rnd.nextFloat() * 0.18f else 1.05f + rnd.nextFloat() * 0.16f
                    Particle(
                        x = rnd.nextFloat(),
                        y = rnd.nextFloat(),
                        speedY = speed,
                        speedX = 0f,
                        sizeDp = size,
                        baseAlpha = 0.70f + rnd.nextFloat() * 0.30f,
                        swayAmpDp = stretch,
                        phase = rnd.nextFloat() * 6.28f,
                        type = type
                    )
                }
                ReadingAmbiance.SNOW -> {
                    val isHexCrystal = idx % 3 != 0 // 66% hexagonal crystalline flakes, 34% soft snow pellets
                    Particle(
                        x = rnd.nextFloat(),
                        y = rnd.nextFloat(),
                        speedY = 0.055f + rnd.nextFloat() * 0.065f,
                        speedX = 0f,
                        sizeDp = if (isHexCrystal) 5.5f + rnd.nextFloat() * 4.5f else 2.5f + rnd.nextFloat() * 3.0f,
                        baseAlpha = 0.65f + rnd.nextFloat() * 0.35f,
                        swayAmpDp = 12f + rnd.nextFloat() * 16f,
                        swayFreq = 0.8f + rnd.nextFloat() * 1.2f,
                        rotation = rnd.nextFloat() * 360f,
                        rotationSpeed = 10f + rnd.nextFloat() * 25f,
                        phase = rnd.nextFloat() * 6.28f,
                        type = if (isHexCrystal) 1 else 0
                    )
                }
                ReadingAmbiance.STARS -> {
                    val isCrossGlance = idx % 2 == 0 // 50% 4-point cross starburst, 50% 5-point star
                    Particle(
                        x = rnd.nextFloat(),
                        y = rnd.nextFloat() * 0.85f,
                        speedY = 0f,
                        speedX = 0f,
                        sizeDp = if (isCrossGlance) 3.5f + rnd.nextFloat() * 3.5f else 2.8f + rnd.nextFloat() * 2.5f,
                        baseAlpha = 0.60f + rnd.nextFloat() * 0.40f,
                        swayFreq = 1.2f + rnd.nextFloat() * 2.4f, // Unique breathing frequency
                        phase = rnd.nextFloat() * 6.28f,          // Out-of-phase twinkling
                        type = if (isCrossGlance) 1 else 0
                    )
                }
                ReadingAmbiance.PETALS -> {
                    val layer = idx % 3
                    val isFullFlower = (idx % 6 == 0) // ~17% full 5-petal flower
                    val (baseSpeed, size, alpha) = when (layer) {
                        0 -> Triple(0.038f + rnd.nextFloat() * 0.018f, 9.5f + rnd.nextFloat() * 2.5f, 0.45f + rnd.nextFloat() * 0.18f) // Far: smaller, gentle depth
                        1 -> Triple(0.052f + rnd.nextFloat() * 0.022f, 15.0f + rnd.nextFloat() * 3.5f, 0.70f + rnd.nextFloat() * 0.18f) // Mid: standard, balanced
                        else -> Triple(0.068f + rnd.nextFloat() * 0.026f, 21.5f + rnd.nextFloat() * 5.0f, 0.88f + rnd.nextFloat() * 0.10f) // Near: large foreground, crisp, drop shadow
                    }
                    Particle(
                        x = rnd.nextFloat(),
                        y = rnd.nextFloat(),
                        speedY = baseSpeed,
                        speedX = 0.015f + rnd.nextFloat() * 0.025f,
                        sizeDp = if (isFullFlower) size * 1.15f else size,
                        baseAlpha = alpha,
                        swayAmpDp = 22f + rnd.nextFloat() * 24f,
                        swayFreq = 0.85f + rnd.nextFloat() * 0.90f,
                        rotation = rnd.nextFloat() * 360f,
                        rotationSpeed = (if (rnd.nextBoolean()) 1 else -1) * (12f + rnd.nextFloat() * 18f),
                        phase = rnd.nextFloat() * 6.28f,
                        type = if (isFullFlower) 3 else (idx % 3), // 0=notched sakura, 1=curved peach, 2=flutter twist, 3=5-petal blossom
                        depthLayer = layer,
                        rollSpeed = 1.3f + rnd.nextFloat() * 1.5f,
                        rollPhase = rnd.nextFloat() * 6.28f,
                        pitchSpeed = 1.0f + rnd.nextFloat() * 1.2f,
                        pitchPhase = rnd.nextFloat() * 6.28f,
                        colorVariant = idx % 3
                    )
                }
                ReadingAmbiance.AI_FLOW, ReadingAmbiance.NONE -> Particle()
            }
        }
    }

    var animMillis by remember { mutableLongStateOf(0L) }

    LaunchedEffect(currentScene) {
        var prevTime = 0L
        while (true) {
            withInfiniteAnimationFrameMillis { frameTime ->
                if (prevTime == 0L) prevTime = frameTime
                val delta = (frameTime - prevTime).coerceIn(8L, 34L)
                prevTime = frameTime
                animMillis += delta
            }
        }
    }

    // Color tones tailored for highest aesthetic in both cream-paper and dark reader environments
    val rainDropTintLight = Color(0xFF4A6B88)
    val rainDropTintDark = Color(0xFF90BFE6)

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        val timeSec = animMillis / 1000f

        when (currentScene) {
            ReadingAmbiance.AI_FLOW -> {
                // AI Brain Flowing Light Bar (Conic Gradient + Gaussian Blur Glow + Stream Packets)
                val barHeight = with(density) { 3.5.dp.toPx() }
                val centerY = with(density) { 6.dp.toPx() }

                // Conic gradient cyber color spectrum
                val cyberColors = listOf(
                    Color(0xFF4285F4), // Google Blue
                    Color(0xFF00E5FF), // Cyan
                    Color(0xFF00E676), // Neon Green
                    Color(0xFFFFEE58), // Amber
                    Color(0xFFFF4081), // Pink
                    Color(0xFF7C4DFF), // Purple
                    Color(0xFF4285F4)  // Seamless loop
                )

                // Conic / Sweep gradient moving with rotating focal center across the bar
                val sweepBrush = Brush.sweepGradient(
                    colors = cyberColors,
                    center = Offset(w * 0.5f + sin(timeSec * 1.5f) * w * 0.35f, centerY)
                )

                // High-speed neural traveling wave displacement
                val waveOffset = (timeSec * w * 0.75f) % w

                // Multi-layered Gaussian blur glow simulation
                // 1. Outermost soft ambient glow
                drawRoundRect(
                    brush = sweepBrush,
                    topLeft = Offset(0f, centerY - with(density) { 14.dp.toPx() }),
                    size = Size(w, with(density) { 28.dp.toPx() }),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(with(density) { 14.dp.toPx() }),
                    alpha = 0.15f
                )

                // 2. Mid-level diffuse glow
                drawRoundRect(
                    brush = sweepBrush,
                    topLeft = Offset(0f, centerY - with(density) { 7.dp.toPx() }),
                    size = Size(w, with(density) { 14.dp.toPx() }),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(with(density) { 7.dp.toPx() }),
                    alpha = 0.35f
                )

                // 3. Inner radiant core halo
                drawRoundRect(
                    brush = sweepBrush,
                    topLeft = Offset(0f, centerY - with(density) { 3.5.dp.toPx() }),
                    size = Size(w, with(density) { 7.dp.toPx() }),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(with(density) { 3.5.dp.toPx() }),
                    alpha = 0.70f
                )

                // 4. Slender focused filament beam
                drawRoundRect(
                    brush = sweepBrush,
                    topLeft = Offset(0f, centerY - barHeight * 0.5f),
                    size = Size(w, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(barHeight * 0.5f),
                    alpha = 0.95f
                )

                // 5. Dynamic synapse packet nodes streaming across the bar
                val packetCount = if (isLowPower) 3 else 6
                for (i in 0 until packetCount) {
                    val pX = (waveOffset + (i.toFloat() / packetCount) * w) % w
                    val pulse = (sin(timeSec * 6f + i * 1.8f) * 0.5f + 0.5f)
                    val packetRadius = with(density) { (2.5f + pulse * 2.5f).dp.toPx() }

                    // Synapse core burst
                    drawCircle(
                        color = Color.White.copy(alpha = 0.90f),
                        radius = packetRadius * 0.7f,
                        center = Offset(pX, centerY)
                    )
                    // Synapse luminous aura
                    drawCircle(
                        color = Color(0xFF00E5FF).copy(alpha = 0.40f),
                        radius = packetRadius * 2.2f,
                        center = Offset(pX, centerY)
                    )
                }
            }
            ReadingAmbiance.RAIN -> {
                // 1. 听雨读书 (庭院细雨 · 水滴形态 · 渐变半透明 · 底部微涟漪)
                val baseTint = if (isDarkTheme) rainDropTintDark else rainDropTintLight

                for (p in particles) {
                    val progressY = (p.y + timeSec * p.speedY) % 1.0f
                    val progressX = ((p.x + timeSec * p.speedX) % 1.0f + 1.0f) % 1.0f

                    val pxX = progressX * w
                    val pxY = progressY * h
                    val lengthPx = with(density) { p.sizeDp.dp.toPx() }
                    val headRadiusPx = with(density) { (if (p.depthLayer == 2) 1.8f else 1.2f).dp.toPx() }
                    val slantX = lengthPx * 0.12f

                    // Raindrop teardrop shape with gradient: sharp top, rounded specular head at bottom
                    val topX = pxX
                    val topY = pxY
                    val botX = pxX - slantX
                    val botY = pxY + lengthPx

                    // Motion streak body
                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                baseTint.copy(alpha = 0.04f),
                                baseTint.copy(alpha = p.baseAlpha * 0.65f),
                                baseTint.copy(alpha = p.baseAlpha)
                            ),
                            start = Offset(topX, topY),
                            end = Offset(botX, botY)
                        ),
                        start = Offset(topX, topY),
                        end = Offset(botX, botY),
                        strokeWidth = headRadiusPx * 1.6f,
                        cap = StrokeCap.Round
                    )

                    // Rounded droplet head highlight
                    drawCircle(
                        color = Color.White.copy(alpha = p.baseAlpha * 0.75f),
                        radius = headRadiusPx * 0.8f,
                        center = Offset(botX, botY)
                    )

                    // Brief subtle splash ripple as droplet reaches bottom
                    if (progressY > 0.92f) {
                        val rippleAge = (progressY - 0.92f) / 0.08f
                        val rippleW = with(density) { (8f + rippleAge * 14f).dp.toPx() }
                        val rippleH = rippleW * 0.35f
                        val rippleAlpha = ((1f - rippleAge) * p.baseAlpha * 0.38f).coerceAtLeast(0f)
                        if (rippleAlpha > 0.02f) {
                            drawOval(
                                color = baseTint.copy(alpha = rippleAlpha),
                                topLeft = Offset(botX - rippleW * 0.5f, h - rippleH - 4f),
                                size = Size(rippleW, rippleH),
                                style = Stroke(width = with(density) { 1.dp.toPx() })
                            )
                        }
                    }
                }
            }

            ReadingAmbiance.WINDOW_RAIN -> {
                // ================= 1. DISTANT OUTDOOR RAIN (Behind the Glass Pane) =================
                // "像看玻璃后面下雨一样" - Distant soft diagonal rain streaks falling outside behind the window
                val distantRainCount = if (isLowPower) 16 else 28
                val rainStreakTint = if (isDarkTheme) Color(0x3888A8C8) else Color(0x28406080)
                val slantRatio = 0.16f // 10 degree natural gentle diagonal wind slant

                for (i in 0 until distantRainCount) {
                    val seed1 = ((i * 137.3f) % 1000f) / 1000f
                    val seed2 = ((i * 269.7f) % 1000f) / 1000f
                    val layerSpeed = 0.20f + (i % 3) * 0.07f
                    val streakProgY = (seed2 + timeSec * layerSpeed) % 1.0f
                    val streakProgX = ((seed1 + timeSec * (-0.022f)) % 1.0f + 1.0f) % 1.0f

                    val streakX = streakProgX * w
                    val streakY = streakProgY * h
                    val streakLen = with(density) { (14f + (i % 4) * 6f).dp.toPx() }
                    val streakDx = streakLen * slantRatio

                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                rainStreakTint.copy(alpha = rainStreakTint.alpha * 0.35f),
                                rainStreakTint
                            ),
                            start = Offset(streakX, streakY),
                            end = Offset(streakX - streakDx, streakY + streakLen)
                        ),
                        start = Offset(streakX, streakY),
                        end = Offset(streakX - streakDx, streakY + streakLen),
                        strokeWidth = with(density) { (0.9f + (i % 2) * 0.4f).dp.toPx() },
                        cap = StrokeCap.Round
                    )
                }

                // ================= 2. GLASS WINDOW CONDENSATION & VIGNETTE =================
                // Very subtle cool glass mist border simulating looking through a glass pane
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x066088B0),
                            Color(0x18182E48)
                        ),
                        center = Offset(w * 0.5f, h * 0.5f),
                        radius = w * 0.95f
                    )
                )

                // ================= 3. IPHONE 4S RETINA WATER DROPLETS (On the Glass Surface) =================
                for (p in particles) {
                    val baseR = with(density) { p.sizeDp.dp.toPx() }
                    val stretch = p.swayAmpDp
                    val rx = baseR
                    val ry = baseR * stretch

                    val (curX, curY) = if (p.type == 1) {
                        val cycleY = (p.y + timeSec * p.speedY) % 1.0f
                        Pair(p.x * w, cycleY * h)
                    } else {
                        Pair(p.x * w, p.y * h)
                    }

                    // For sliding droplet: draw the clean, transparent trail where water wiped the glass
                    if (p.type == 1) {
                        val trailLenPx = with(density) { (38f + rx * 2.5f).dp.toPx() }
                        val startTrailY = (curY - trailLenPx).coerceAtLeast(0f)
                        
                        // Wet trail body
                        drawLine(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0x156AA0C8),
                                    Color(0x356AA0C8)
                                ),
                                startY = startTrailY,
                                endY = curY
                            ),
                            start = Offset(curX, startTrailY),
                            end = Offset(curX, curY),
                            strokeWidth = rx * 0.85f,
                            cap = StrokeCap.Round
                        )
                        // Faint left and right meniscus boundary lines along the trail
                        val trailEdgeColor = Color(0x30FFFFFF)
                        val edgeOffset = rx * 0.45f
                        drawLine(
                            color = trailEdgeColor,
                            start = Offset(curX - edgeOffset, startTrailY + trailLenPx * 0.3f),
                            end = Offset(curX - edgeOffset, curY - ry * 0.5f),
                            strokeWidth = 0.5f
                        )
                        drawLine(
                            color = trailEdgeColor,
                            start = Offset(curX + edgeOffset, startTrailY + trailLenPx * 0.3f),
                            end = Offset(curX + edgeOffset, curY - ry * 0.5f),
                            strokeWidth = 0.5f
                        )
                    }

                    // For twin merged droplets (type 4): draw companion adjacent droplet
                    if (p.type == 4) {
                        val twinOffset = rx * 1.35f
                        val twinR = rx * 0.75f
                        val twinRy = ry * 0.75f
                        val tX = curX + twinOffset * 0.7f
                        val tY = curY + twinOffset * 0.4f
                        
                        // Twin drop shadow
                        drawOval(
                            color = Color(0x38000000),
                            topLeft = Offset(tX - twinR + twinR * 0.25f, tY - twinRy + twinR * 0.35f),
                            size = Size(twinR * 2f, twinRy * 2f)
                        )
                        // Twin water body
                        drawOval(
                            color = Color(0x284A7090),
                            topLeft = Offset(tX - twinR, tY - twinRy),
                            size = Size(twinR * 2f, twinRy * 2f)
                        )
                        // Twin caustic glow
                        drawArc(
                            color = Color(0x95FFFFFF),
                            startAngle = 20f,
                            sweepAngle = 135f,
                            useCenter = false,
                            topLeft = Offset(tX - twinR * 0.95f, tY - twinRy * 0.95f),
                            size = Size(twinR * 1.9f, twinRy * 1.9f),
                            style = Stroke(width = with(density) { 0.8.dp.toPx() })
                        )
                        // Twin glare
                        drawOval(
                            color = Color(0xE8FFFFFF),
                            topLeft = Offset(tX - twinR * 0.60f, tY - twinRy * 0.65f),
                            size = Size(twinR * 0.65f, twinRy * 0.35f)
                        )
                    }

                    // 1. Contact Drop Shadow on the glass (Ambient sky light from top-left, casting soft dark shadow to bottom-right)
                    val shadowOffset = rx * 0.24f
                    drawOval(
                        color = Color(0x45000000),
                        topLeft = Offset(curX - rx + shadowOffset, curY - ry + shadowOffset * 1.40f),
                        size = Size(rx * 2f, ry * 2f)
                    )

                    // 2. Translucent Convex Water Body (Convex refraction lens)
                    drawOval(
                        color = Color(0x2A4E7A9C),
                        topLeft = Offset(curX - rx, curY - ry),
                        size = Size(rx * 2f, ry * 2f)
                    )

                    // 3. Lower-right Caustic Glow (Light focused through droplet lens onto inner bottom-right rim - classic 4S glowing rim)
                    drawArc(
                        color = Color(0xB5FFFFFF),
                        startAngle = 20f,
                        sweepAngle = 140f,
                        useCenter = false,
                        topLeft = Offset(curX - rx * 0.92f, curY - ry * 0.92f),
                        size = Size(rx * 1.84f, ry * 1.84f),
                        style = Stroke(width = with(density) { (if (p.type == 3) 1.3f else 0.9f).dp.toPx() })
                    )

                    // 4. Subtle perimeter glass-water refraction meniscus rim
                    drawOval(
                        color = Color(0x48FFFFFF),
                        topLeft = Offset(curX - rx, curY - ry),
                        size = Size(rx * 2f, ry * 2f),
                        style = Stroke(width = with(density) { 0.6.dp.toPx() })
                    )

                    // 5. Signature iPhone 4S Curved Specular Glare (Top-Left brilliant white reflection crescent)
                    val glareW = rx * (if (p.type == 3) 0.78f else 0.70f)
                    val glareH = ry * (if (p.type == 3) 0.42f else 0.36f)
                    drawOval(
                        color = Color(0xFAFFFFFF),
                        topLeft = Offset(curX - rx * 0.62f, curY - ry * 0.68f),
                        size = Size(glareW, glareH)
                    )

                    // 6. Secondary subtle pinpoint reflection dot (for natural 3D depth)
                    if (p.type != 2) {
                        drawCircle(
                            color = Color(0xC5FFFFFF),
                            radius = rx * 0.17f,
                            center = Offset(curX - rx * 0.20f, curY - ry * 0.28f)
                        )
                    }
                }
            }

            ReadingAmbiance.SNOW -> {
                // 3. 踏雪寻章 (纯白亮白 · 六角形结晶花纹 + 远景小圆雪点 · 左右摇摆漂浮)
                val snowColor = Color.White

                for (p in particles) {
                    val progressY = (p.y + timeSec * p.speedY) % 1.0f
                    val swayPx = with(density) { (sin(timeSec * p.swayFreq + p.phase) * p.swayAmpDp).dp.toPx() }
                    val pxX = ((p.x * w + swayPx) % w + w) % w
                    val pxY = progressY * h
                    val radiusPx = with(density) { p.sizeDp.dp.toPx() }
                    val curRotation = (p.rotation + timeSec * p.rotationSpeed) % 360f

                    if (p.type == 1) {
                        // Crystalline Hexagonal Snowflake (六角结晶花纹)
                        rotate(degrees = curRotation, pivot = Offset(pxX, pxY)) {
                            // Soft ambient crystal glow
                            drawCircle(
                                color = snowColor.copy(alpha = (p.baseAlpha * 0.32f).coerceIn(0.12f, 0.40f)),
                                radius = radiusPx * 1.35f,
                                center = Offset(pxX, pxY)
                            )
                            // 6 Symmetric spokes with delicate branchlets
                            val strokeW = with(density) { 1.1.dp.toPx() }
                            for (spoke in 0 until 6) {
                                val angleRad = (spoke * 60f) * (Math.PI / 180f).toFloat()
                                val cosA = cos(angleRad)
                                val sinA = sin(angleRad)

                                val endX = pxX + radiusPx * cosA
                                val endY = pxY + radiusPx * sinA

                                // Main spoke
                                drawLine(
                                    color = snowColor.copy(alpha = p.baseAlpha),
                                    start = Offset(pxX, pxY),
                                    end = Offset(endX, endY),
                                    strokeWidth = strokeW,
                                    cap = StrokeCap.Round
                                )

                                // Symmetrical branchlet at 55% radius
                                val branchStartX = pxX + radiusPx * 0.55f * cosA
                                val branchStartY = pxY + radiusPx * 0.55f * sinA
                                val branchLen = radiusPx * 0.32f

                                val branch1Angle = angleRad + 0.78f // +45 deg
                                val branch2Angle = angleRad - 0.78f // -45 deg

                                drawLine(
                                    color = snowColor.copy(alpha = p.baseAlpha * 0.85f),
                                    start = Offset(branchStartX, branchStartY),
                                    end = Offset(branchStartX + branchLen * cos(branch1Angle), branchStartY + branchLen * sin(branch1Angle)),
                                    strokeWidth = strokeW * 0.8f,
                                    cap = StrokeCap.Round
                                )
                                drawLine(
                                    color = snowColor.copy(alpha = p.baseAlpha * 0.85f),
                                    start = Offset(branchStartX, branchStartY),
                                    end = Offset(branchStartX + branchLen * cos(branch2Angle), branchStartY + branchLen * sin(branch2Angle)),
                                    strokeWidth = strokeW * 0.8f,
                                    cap = StrokeCap.Round
                                )
                            }
                            // Center jewel core
                            drawCircle(
                                color = snowColor,
                                radius = radiusPx * 0.22f,
                                center = Offset(pxX, pxY)
                            )
                        }
                    } else {
                        // Soft snow powder pellet (远景轻盈小圆点)
                        drawCircle(
                            color = snowColor.copy(alpha = (p.baseAlpha * 0.28f).coerceIn(0.12f, 0.40f)),
                            radius = radiusPx * 1.6f,
                            center = Offset(pxX, pxY)
                        )
                        drawCircle(
                            color = snowColor.copy(alpha = p.baseAlpha.coerceIn(0.40f, 0.95f)),
                            radius = radiusPx,
                            center = Offset(pxX, pxY)
                        )
                    }
                }
            }

            ReadingAmbiance.STARS -> {
                // 4. 星夜伴读 (明耀暖白/浅金 · 四角十字星芒 + 经典五角星 · 独立呼吸闪烁)
                val starWhite = if (isDarkTheme) Color(0xFFFFFEEB) else Color(0xFF9E7E36)
                val starGlance = if (isDarkTheme) Color(0xFFFFF0B8) else Color(0xFF806424)

                for (p in particles) {
                    val twinkle = (sin(timeSec * p.swayFreq + p.phase) * 0.5f + 0.5f) // 0..1 smooth respiratory cycle
                    val starAlpha = (p.baseAlpha * (0.28f + twinkle * 0.72f)).coerceIn(0.15f, 0.98f)
                    val starX = p.x * w
                    val starY = p.y * h
                    val currentRadiusPx = with(density) { (p.sizeDp * (0.75f + twinkle * 0.45f)).dp.toPx() }

                    // Luminous starry aura
                    drawCircle(
                        color = starWhite.copy(alpha = starAlpha * 0.25f),
                        radius = currentRadiusPx * 2.5f,
                        center = Offset(starX, starY)
                    )

                    if (p.type == 1) {
                        // Four-pointed cross flare star (四角星 / 十字光芒样式)
                        val rayLen = currentRadiusPx * 3.4f
                        val rayW = with(density) { 1.2.dp.toPx() }

                        // Horizontal and vertical diamond-tapered rays
                        drawLine(
                            color = starGlance.copy(alpha = starAlpha * 0.85f),
                            start = Offset(starX - rayLen, starY),
                            end = Offset(starX + rayLen, starY),
                            strokeWidth = rayW,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = starGlance.copy(alpha = starAlpha * 0.85f),
                            start = Offset(starX, starY - rayLen),
                            end = Offset(starX, starY + rayLen),
                            strokeWidth = rayW,
                            cap = StrokeCap.Round
                        )
                        // Diagonal faint micro-glints
                        val diagLen = rayLen * 0.45f
                        drawLine(
                            color = starGlance.copy(alpha = starAlpha * 0.45f),
                            start = Offset(starX - diagLen, starY - diagLen),
                            end = Offset(starX + diagLen, starY + diagLen),
                            strokeWidth = rayW * 0.7f,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = starGlance.copy(alpha = starAlpha * 0.45f),
                            start = Offset(starX - diagLen, starY + diagLen),
                            end = Offset(starX + diagLen, starY - diagLen),
                            strokeWidth = rayW * 0.7f,
                            cap = StrokeCap.Round
                        )
                        // Bright central jewel
                        drawCircle(
                            color = Color.White.copy(alpha = starAlpha),
                            radius = currentRadiusPx * 0.7f,
                            center = Offset(starX, starY)
                        )
                    } else {
                        // Five-pointed classic star (经典五角星)
                        val starPath = Path()
                        val outerR = currentRadiusPx * 1.2f
                        val innerR = outerR * 0.45f
                        val angleStep = (Math.PI / 5.0).toFloat()

                        for (i in 0 until 10) {
                            val r = if (i % 2 == 0) outerR else innerR
                            val a = i * angleStep - (Math.PI / 2.0).toFloat()
                            val vx = starX + r * cos(a)
                            val vy = starY + r * sin(a)
                            if (i == 0) starPath.moveTo(vx, vy) else starPath.lineTo(vx, vy)
                        }
                        starPath.close()

                        drawPath(
                            path = starPath,
                            color = starWhite.copy(alpha = starAlpha)
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = starAlpha * 0.9f),
                            radius = innerR * 0.7f,
                            center = Offset(starX, starY)
                        )
                    }
                }
            }

            ReadingAmbiance.PETALS -> {
                // 5. 落英缤纷 (真质感飘落花瓣 · 3D空中翻转 · 阳光透射渐变 · 双正弦微风摆动 · 真实植物形态)
                for (p in particles) {
                    // Continuous smooth aerodynamic motion (no sudden jerks or stutter)
                    val roll = cos((timeSec * p.rollSpeed + p.rollPhase).toDouble()).toFloat()
                    val absRoll = abs(roll)
                    val isFront = roll >= 0f

                    // Smooth continuous vertical descent
                    val progressY = (p.y + timeSec * p.speedY) % 1.0f

                    // Organic wind sway: superposition of primary breeze and secondary flutter
                    val primarySway = sin((timeSec * p.swayFreq + p.phase).toDouble()).toFloat() * p.swayAmpDp
                    val secondaryFlutter = sin((timeSec * (p.swayFreq * 0.47f) + p.phase * 1.7f).toDouble()).toFloat() * (p.swayAmpDp * 0.35f)
                    val swayPx = with(density) { (primarySway + secondaryFlutter).dp.toPx() }
                    val driftX = timeSec * p.speedX * w * 0.35f

                    val pxX = ((p.x * w + swayPx + driftX) % w + w) % w
                    val pxY = progressY * h

                    // Aerodynamic tilting: petal tilts naturally along horizontal glide direction
                    val tiltAngle = cos((timeSec * p.swayFreq + p.phase).toDouble()).toFloat() * 16f
                    val curRotation = (p.rotation + timeSec * p.rotationSpeed + tiltAngle) % 360f

                    // 3D pitch & roll transform
                    val pitch = sin((timeSec * p.pitchSpeed + p.pitchPhase).toDouble()).toFloat()
                    val pitchScale = 0.82f + 0.18f * cos(pitch.toDouble()).toFloat()

                    val petalHeightPx = with(density) { p.sizeDp.dp.toPx() }
                    val petalWidthPx = petalHeightPx * 0.76f
                    val hw = petalWidthPx * 0.5f
                    val hh = petalHeightPx * 0.5f

                    val curAlpha = (p.baseAlpha * if (isDarkTheme) 0.90f else 0.82f).coerceIn(0.20f, 0.96f)

                    // Multi-stop Photorealistic Gradient Colors:
                    // Base: Deep rouge/carmine, Mid: Soft peach blush, Tip: Translucent milk snow-pink
                    val baseColor = when (p.colorVariant) {
                        0 -> Color(0xFFD83266) // Deep carmine
                        1 -> Color(0xFFDE3B68) // Rose rouge
                        else -> Color(0xFFC72457) // Imperial magenta
                    }
                    val midColor = when (p.colorVariant) {
                        0 -> Color(0xFFFF8EA2) // Peach blossom
                        1 -> Color(0xFFFFA6BA) // Soft blush
                        else -> Color(0xFFFF9BB1) // Rose quartz
                    }
                    val tipColor = when (p.colorVariant) {
                        0 -> Color(0xFFFFF2F5) // Snow-white pink
                        1 -> Color(0xFFFFFFFF) // Pure pearl highlight
                        else -> Color(0xFFFFF7F9)
                    }

                    // When flipped to backside, light is more diffuse and luminous
                    val finalBase = if (isFront) baseColor else Color(0xFFE56285)
                    val finalMid = if (isFront) midColor else Color(0xFFFFBAC7)
                    val finalTip = if (isFront) tipColor else Color(0xFFFFFDFC)

                    if (p.type == 3) {
                        // =================================================================
                        // Type 3: Intact 5-Petal Flower Blossom (自然完整五瓣落花)
                        // =================================================================
                        val flowerRadius = petalHeightPx * 0.55f
                        val singlePetalH = flowerRadius * 0.85f
                        val singlePetalW = singlePetalH * 0.65f
                        val shw = singlePetalW * 0.5f

                        // 5 individual petals radiating outwards with distinct cleft notches
                        val singlePetalPath = Path().apply {
                            moveTo(0f, 0f)
                            cubicTo(-shw * 0.5f, -singlePetalH * 0.35f, -shw, -singlePetalH * 0.65f, -shw * 0.65f, -singlePetalH * 0.92f)
                            cubicTo(-shw * 0.40f, -singlePetalH * 1.02f, -shw * 0.15f, -singlePetalH * 0.95f, -shw * 0.05f, -singlePetalH * 0.82f)
                            lineTo(0f, -singlePetalH * 0.70f) // Distinct sakura cleft notch
                            lineTo(shw * 0.05f, -singlePetalH * 0.82f)
                            cubicTo(shw * 0.15f, -singlePetalH * 0.95f, shw * 0.40f, -singlePetalH * 1.02f, shw * 0.65f, -singlePetalH * 0.92f)
                            cubicTo(shw, -singlePetalH * 0.65f, shw * 0.5f, -singlePetalH * 0.35f, 0f, 0f)
                            close()
                        }

                        val blossomPetalBrush = Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to finalTip.copy(alpha = curAlpha * 0.85f),
                                0.45f to finalMid.copy(alpha = curAlpha * 0.90f),
                                1.0f to finalBase.copy(alpha = curAlpha * 0.98f)
                            ),
                            startY = -singlePetalH,
                            endY = 0f
                        )

                        // Soft ground drop shadow (near & mid layers)
                        if (p.depthLayer > 0) {
                            val shadowOffset = with(density) { (if (p.depthLayer == 2) 3.5.dp else 2.0.dp).toPx() }
                            withTransform({
                                translate(left = pxX + shadowOffset, top = pxY + shadowOffset)
                                rotate(degrees = curRotation, pivot = Offset.Zero)
                                scale(scaleX = roll * 0.92f, scaleY = pitchScale * 0.92f, pivot = Offset.Zero)
                            }) {
                                for (i in 0 until 5) {
                                    rotate(degrees = i * 72f, pivot = Offset.Zero) {
                                        drawPath(
                                            path = singlePetalPath,
                                            color = Color.Black.copy(alpha = if (p.depthLayer == 2) 0.08f else 0.04f)
                                        )
                                    }
                                }
                            }
                        }

                        // Blossom body with 5 petals + floral carpel & golden stamens
                        withTransform({
                            translate(left = pxX, top = pxY)
                            rotate(degrees = curRotation, pivot = Offset.Zero)
                            scale(scaleX = roll, scaleY = pitchScale, pivot = Offset.Zero)
                        }) {
                            for (i in 0 until 5) {
                                rotate(degrees = i * 72f, pivot = Offset.Zero) {
                                    drawPath(path = singlePetalPath, brush = blossomPetalBrush)
                                    // Delicate rim highlight
                                    drawPath(
                                        path = singlePetalPath,
                                        color = Color.White.copy(alpha = curAlpha * 0.30f),
                                        style = Stroke(width = with(density) { 0.5.dp.toPx() })
                                    )
                                }
                            }

                            // Floral Center: Deep ruby carpel disc
                            val centerDiscR = flowerRadius * 0.16f
                            drawCircle(
                                color = Color(0xFFB71C1C).copy(alpha = curAlpha * 0.95f),
                                radius = centerDiscR,
                                center = Offset.Zero
                            )
                            // Delicate radiating stamens with golden anther dots
                            val stamenLen = flowerRadius * 0.28f
                            for (s in 0 until 8) {
                                val sAngle = s * (360f / 8f) * (Math.PI / 180f).toFloat()
                                val tipX = cos(sAngle) * stamenLen
                                val tipY = sin(sAngle) * stamenLen
                                drawLine(
                                    color = Color(0xFFFFCDD2).copy(alpha = curAlpha * 0.75f),
                                    start = Offset.Zero,
                                    end = Offset(tipX, tipY),
                                    strokeWidth = with(density) { 0.6.dp.toPx() }
                                )
                                drawCircle(
                                    color = Color(0xFFFFD54F).copy(alpha = curAlpha * 0.90f),
                                    radius = with(density) { 0.9.dp.toPx() },
                                    center = Offset(tipX, tipY)
                                )
                            }
                        }
                    } else {
                        // =================================================================
                        // Single Petals: Type 0, 1, 2
                        // Type 0: Classic Sakura with natural cleft notch (自然缺口樱花瓣)
                        // Type 1: Natural curved Peach/Plum petal (优雅自然微曲桃花瓣)
                        // Type 2: Asymmetric fluttering twist petal (随风微翻卷瓣)
                        // =================================================================
                        val path = Path().apply {
                            moveTo(0f, hh * 0.92f)
                            when (p.type) {
                                0 -> {
                                    // Realistic Sakura Petal with smooth organic curvature & V-cleft apex
                                    cubicTo(-hw * 0.35f, hh * 0.75f, -hw * 0.85f, hh * 0.35f, -hw * 0.98f, -hh * 0.05f)
                                    cubicTo(-hw * 1.05f, -hh * 0.45f, -hw * 0.68f, -hh * 0.88f, -hw * 0.22f, -hh * 0.98f)
                                    lineTo(-hw * 0.12f, -hh * 1.0f)
                                    // Organic V-notch cleft
                                    cubicTo(-hw * 0.06f, -hh * 0.88f, -hw * 0.02f, -hh * 0.68f, 0f, -hh * 0.65f)
                                    cubicTo(hw * 0.02f, -hh * 0.68f, hw * 0.06f, -hh * 0.88f, hw * 0.12f, -hh * 1.0f)
                                    lineTo(hw * 0.22f, -hh * 0.98f)
                                    cubicTo(hw * 0.68f, -hh * 0.88f, hw * 1.05f, -hh * 0.45f, hw * 0.98f, -hh * 0.05f)
                                    cubicTo(hw * 0.85f, hh * 0.35f, hw * 0.35f, hh * 0.75f, 0f, hh * 0.92f)
                                }
                                1 -> {
                                    // Natural curved Peach/Plum petal with organic undulating rim
                                    cubicTo(-hw * 0.40f, hh * 0.72f, -hw * 0.95f, hh * 0.30f, -hw * 0.90f, -hh * 0.15f)
                                    cubicTo(-hw * 0.85f, -hh * 0.58f, -hw * 0.52f, -hh * 0.92f, -hw * 0.15f, -hh * 0.98f)
                                    cubicTo(-hw * 0.05f, -hh * 1.0f, hw * 0.10f, -hh * 0.95f, hw * 0.22f, -hh * 0.88f)
                                    cubicTo(hw * 0.60f, -hh * 0.82f, hw * 0.98f, -hh * 0.42f, hw * 0.88f, -hh * 0.05f)
                                    cubicTo(hw * 0.80f, hh * 0.35f, hw * 0.35f, hh * 0.72f, 0f, hh * 0.92f)
                                }
                                else -> {
                                    // Fluttering twist petal with organic asymmetrical undulation
                                    cubicTo(-hw * 0.50f, hh * 0.70f, -hw * 1.05f, hh * 0.20f, -hw * 0.85f, -hh * 0.25f)
                                    cubicTo(-hw * 0.70f, -hh * 0.65f, -hw * 0.35f, -hh * 0.92f, 0f, -hh * 0.75f)
                                    cubicTo(hw * 0.25f, -hh * 0.88f, hw * 0.75f, -hh * 0.78f, hw * 0.92f, -hh * 0.35f)
                                    cubicTo(hw * 1.02f, hh * 0.08f, hw * 0.65f, hh * 0.60f, 0f, hh * 0.92f)
                                }
                            }
                            close()
                        }

                        val petalBrush = Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to finalTip.copy(alpha = curAlpha * 0.80f),
                                0.45f to finalMid.copy(alpha = curAlpha * 0.88f),
                                1.0f to finalBase.copy(alpha = curAlpha * 0.98f)
                            ),
                            startY = -hh,
                            endY = hh
                        )

                        // 1. Soft ground drop shadow (near & mid layers)
                        if (p.depthLayer > 0) {
                            val shadowOffsetX = with(density) { (if (p.depthLayer == 2) 2.5.dp else 1.5.dp).toPx() }
                            val shadowOffsetY = with(density) { (if (p.depthLayer == 2) 3.5.dp else 2.0.dp).toPx() }
                            withTransform({
                                translate(left = pxX + shadowOffsetX, top = pxY + shadowOffsetY)
                                rotate(degrees = curRotation, pivot = Offset.Zero)
                                scale(scaleX = roll * 0.92f, scaleY = pitchScale * 0.92f, pivot = Offset.Zero)
                            }) {
                                drawPath(
                                    path = path,
                                    color = Color.Black.copy(alpha = if (p.depthLayer == 2) 0.08f else 0.04f)
                                )
                            }
                        }

                        // 2. Realistic Petal with 3D Flip & Vein Architecture
                        withTransform({
                            translate(left = pxX, top = pxY)
                            rotate(degrees = curRotation, pivot = Offset.Zero)
                            scale(scaleX = roll, scaleY = pitchScale, pivot = Offset.Zero)
                        }) {
                            // Petal Body
                            drawPath(path = path, brush = petalBrush)

                            // Central Delicate Translucent Vein
                            val veinColor = Color.White.copy(alpha = curAlpha * 0.28f)
                            val centralVein = Path().apply {
                                moveTo(0f, hh * 0.85f)
                                quadraticBezierTo(-hw * 0.04f, 0f, 0f, -hh * 0.42f)
                            }
                            drawPath(
                                path = centralVein,
                                color = veinColor,
                                style = Stroke(width = with(density) { 0.7.dp.toPx() }, cap = StrokeCap.Round)
                            )

                            // Secondary Micro-Vein Branches (微细脉络)
                            val lateralVeins = Path().apply {
                                moveTo(0f, hh * 0.35f)
                                quadraticBezierTo(-hw * 0.25f, hh * 0.15f, -hw * 0.45f, -hh * 0.10f)
                                moveTo(0f, hh * 0.25f)
                                quadraticBezierTo(hw * 0.25f, hh * 0.05f, hw * 0.50f, -hh * 0.18f)
                            }
                            drawPath(
                                path = lateralVeins,
                                color = veinColor.copy(alpha = curAlpha * 0.18f),
                                style = Stroke(width = with(density) { 0.5.dp.toPx() }, cap = StrokeCap.Round)
                            )

                            // Outer translucent rim highlight for crisp natural light
                            drawPath(
                                path = path,
                                color = Color.White.copy(alpha = curAlpha * 0.30f),
                                style = Stroke(width = with(density) { 0.5.dp.toPx() })
                            )
                        }
                    }
                }
            }

            ReadingAmbiance.NONE -> {}
        }
    }
}
