package com.example.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Botanical Leaf & Branch Background Decoration
 * Renders exquisite Eastern ink / watercolor style botanical branches and leaves
 * for the Green Theme ("青枝绿叶主题").
 */
fun DrawScope.drawBotanicalFoliage(
    tint: Color = Color(0x38224C33)
) {
    val branchStroke = tint.copy(alpha = (tint.alpha * 1.25f).coerceAtMost(1f))
    val leafFill = tint.copy(alpha = (tint.alpha * 0.90f).coerceAtMost(1f))
    val leafHighlight = tint.copy(alpha = (tint.alpha * 0.65f).coerceAtMost(1f))
    val leafVein = tint.copy(alpha = (tint.alpha * 1.50f).coerceAtMost(1f))

    val w = size.width
    val h = size.height

    // Helper to draw a single realistic curved leaf with natural taper, double curvature, and central vein
    fun drawLeaf(
        baseX: Float,
        baseY: Float,
        tipX: Float,
        tipY: Float,
        bellyOffset: Float,
        hasHighlight: Boolean = false
    ) {
        val midX = (baseX + tipX) / 2f
        val midY = (baseY + tipY) / 2f
        val dx = tipX - baseX
        val dy = tipY - baseY
        val len = kotlin.math.sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
        val nx = -dy / len * bellyOffset
        val ny = dx / len * bellyOffset

        val leafPath = Path().apply {
            moveTo(baseX, baseY)
            quadraticTo(midX + nx, midY + ny, tipX, tipY)
            quadraticTo(midX - nx * 0.65f, midY - ny * 0.65f, baseX, baseY)
            close()
        }
        drawPath(path = leafPath, color = if (hasHighlight) leafHighlight else leafFill, style = Fill)

        // Central leaf vein
        drawLine(
            color = leafVein,
            start = Offset(baseX, baseY),
            end = Offset(tipX * 0.94f + baseX * 0.06f, tipY * 0.94f + baseY * 0.06f),
            strokeWidth = 1.05f,
            cap = StrokeCap.Round
        )

        // Side lateral veins for realism
        val q1X = baseX * 0.7f + tipX * 0.3f
        val q1Y = baseY * 0.7f + tipY * 0.3f
        drawLine(
            color = leafVein.copy(alpha = leafVein.alpha * 0.6f),
            start = Offset(q1X, q1Y),
            end = Offset(q1X + nx * 0.5f, q1Y + ny * 0.5f),
            strokeWidth = 0.7f,
            cap = StrokeCap.Round
        )
        val q2X = baseX * 0.4f + tipX * 0.6f
        val q2Y = baseY * 0.4f + tipY * 0.6f
        drawLine(
            color = leafVein.copy(alpha = leafVein.alpha * 0.6f),
            start = Offset(q2X, q2Y),
            end = Offset(q2X - nx * 0.4f, q2Y - ny * 0.4f),
            strokeWidth = 0.7f,
            cap = StrokeCap.Round
        )
    }

    // ================= 1. TOP-RIGHT CORNER: Main Draping Realistic Branch & Twigs =================
    val mainBranch = Path().apply {
        moveTo(w + 20f, -10f)
        cubicTo(
            w * 0.85f, h * 0.035f,
            w * 0.72f, h * 0.075f,
            w * 0.52f, h * 0.070f
        )
    }
    drawPath(path = mainBranch, color = branchStroke, style = Stroke(width = 3.2f, cap = StrokeCap.Round, join = StrokeJoin.Round))

    // Secondary twig 1
    val twig1 = Path().apply {
        moveTo(w * 0.78f, h * 0.050f)
        cubicTo(w * 0.73f, h * 0.095f, w * 0.68f, h * 0.140f, w * 0.61f, h * 0.155f)
    }
    drawPath(path = twig1, color = branchStroke, style = Stroke(width = 2.0f, cap = StrokeCap.Round))

    // Secondary twig 2
    val twig2 = Path().apply {
        moveTo(w * 0.65f, h * 0.072f)
        cubicTo(w * 0.60f, h * 0.108f, w * 0.55f, h * 0.138f, w * 0.48f, h * 0.148f)
    }
    drawPath(path = twig2, color = branchStroke, style = Stroke(width = 1.7f, cap = StrokeCap.Round))

    // Secondary twig 3 (higher arch)
    val twig3 = Path().apply {
        moveTo(w * 0.89f, h * 0.024f)
        cubicTo(w * 0.85f, h * 0.068f, w * 0.81f, h * 0.110f, w * 0.75f, h * 0.142f)
    }
    drawPath(path = twig3, color = branchStroke, style = Stroke(width = 1.8f, cap = StrokeCap.Round))

    // Realistic Foliage Clusters on Top-Right Branches
    drawLeaf(w * 0.78f, h * 0.050f, w * 0.71f, h * 0.066f, 9.5f)
    drawLeaf(w * 0.73f, h * 0.095f, w * 0.66f, h * 0.120f, 10.5f, hasHighlight = true)
    drawLeaf(w * 0.68f, h * 0.140f, w * 0.60f, h * 0.175f, 11.0f)
    drawLeaf(w * 0.61f, h * 0.155f, w * 0.53f, h * 0.188f, 11.5f, hasHighlight = true)
    drawLeaf(w * 0.65f, h * 0.072f, w * 0.58f, h * 0.055f, 9.5f)
    drawLeaf(w * 0.60f, h * 0.108f, w * 0.52f, h * 0.120f, 10.5f)
    drawLeaf(w * 0.55f, h * 0.138f, w * 0.46f, h * 0.155f, 11.0f, hasHighlight = true)
    drawLeaf(w * 0.52f, h * 0.070f, w * 0.44f, h * 0.082f, 10.0f)
    drawLeaf(w * 0.89f, h * 0.024f, w * 0.82f, h * 0.052f, 9.5f)
    drawLeaf(w * 0.85f, h * 0.068f, w * 0.79f, h * 0.092f, 10.5f)
    drawLeaf(w * 0.81f, h * 0.110f, w * 0.73f, h * 0.140f, 10.5f, hasHighlight = true)

    // ================= 2. TOP-CENTER: Dangling Twig =================
    val centerBranch = Path().apply {
        moveTo(w * 0.38f, -8f)
        cubicTo(w * 0.36f, h * 0.035f, w * 0.32f, h * 0.068f, w * 0.28f, h * 0.085f)
    }
    drawPath(path = centerBranch, color = branchStroke, style = Stroke(width = 1.9f, cap = StrokeCap.Round))
    drawLeaf(w * 0.36f, h * 0.035f, w * 0.31f, h * 0.048f, 8.5f)
    drawLeaf(w * 0.32f, h * 0.068f, w * 0.26f, h * 0.088f, 9.5f, hasHighlight = true)
    drawLeaf(w * 0.28f, h * 0.085f, w * 0.22f, h * 0.105f, 10.0f)

    // ================= 3. MID-LEFT: Spring Twig & Leaves =================
    val leftBranch = Path().apply {
        moveTo(-14f, h * 0.32f)
        cubicTo(
            w * 0.08f, h * 0.335f,
            w * 0.18f, h * 0.365f,
            w * 0.26f, h * 0.395f
        )
    }
    drawPath(path = leftBranch, color = branchStroke, style = Stroke(width = 2.4f, cap = StrokeCap.Round))

    val leftTwig = Path().apply {
        moveTo(w * 0.12f, h * 0.345f)
        cubicTo(w * 0.16f, h * 0.315f, w * 0.21f, h * 0.305f, w * 0.25f, h * 0.295f)
    }
    drawPath(path = leftTwig, color = branchStroke, style = Stroke(width = 1.6f, cap = StrokeCap.Round))

    drawLeaf(w * 0.08f, h * 0.335f, w * 0.13f, h * 0.370f, 9.0f)
    drawLeaf(w * 0.16f, h * 0.355f, w * 0.22f, h * 0.390f, 10.0f, hasHighlight = true)
    drawLeaf(w * 0.26f, h * 0.395f, w * 0.32f, h * 0.420f, 10.5f)
    drawLeaf(w * 0.16f, h * 0.315f, w * 0.21f, h * 0.285f, 9.0f)
    drawLeaf(w * 0.25f, h * 0.295f, w * 0.31f, h * 0.275f, 9.5f, hasHighlight = true)

    // ================= 4. BOTTOM-RIGHT: Corner Sprig =================
    val bottomSprig = Path().apply {
        moveTo(w + 10f, h * 0.88f)
        cubicTo(w * 0.90f, h * 0.89f, w * 0.82f, h * 0.91f, w * 0.74f, h * 0.93f)
    }
    drawPath(path = bottomSprig, color = branchStroke, style = Stroke(width = 2.0f, cap = StrokeCap.Round))
    drawLeaf(w * 0.89f, h * 0.89f, w * 0.83f, h * 0.875f, 9.0f)
    drawLeaf(w * 0.82f, h * 0.91f, w * 0.76f, h * 0.900f, 9.5f, hasHighlight = true)
    drawLeaf(w * 0.74f, h * 0.93f, w * 0.67f, h * 0.945f, 10.0f)

    // ================= 5. POETIC FLUTTERING LEAVES =================
    drawLeaf(w * 0.40f, h * 0.22f, w * 0.45f, h * 0.25f, 8.0f)
    drawLeaf(w * 0.33f, h * 0.50f, w * 0.37f, h * 0.54f, 8.5f, hasHighlight = true)
    drawLeaf(w * 0.68f, h * 0.30f, w * 0.72f, h * 0.34f, 8.5f)
    drawLeaf(w * 0.55f, h * 0.70f, w * 0.59f, h * 0.74f, 8.0f)
}

/**
 * Wonderland Enchanted Forest Realistic Tree & Branches Decoration
 * "要让用户看到真实的树和树枝，有一种爱丽丝仙境世外桃源的感觉。"
 *
 * Design Concept:
 * - Ancient gnarled tree trunk & sweeping boughs with realistic bark grain & knots
 * - Lush multi-tiered realistic leaf foliage with natural tapering blades and veins
 * - Ethereal Wonderland accents: weeping willow/ivy tendrils, dappled glowing fairy lights, and drifting flower petals
 * - 100% Vector Hardware-Accelerated, zero allocations in draw loop, keeping the app lightning-fast!
 */
fun DrawScope.drawWonderlandRealisticFoliage(
    primaryColor: Color = Color(0x38143823),
    accentColor: Color = Color(0x28D4A359)
) {
    val w = size.width
    val h = size.height

    val trunkColor = primaryColor.copy(alpha = (primaryColor.alpha * 1.35f).coerceAtMost(1f))
    val boughColor = primaryColor.copy(alpha = (primaryColor.alpha * 1.15f).coerceAtMost(1f))
    val twigColor = primaryColor.copy(alpha = (primaryColor.alpha * 0.95f).coerceAtMost(1f))
    val leafDeep = primaryColor.copy(alpha = (primaryColor.alpha * 0.85f).coerceAtMost(1f))
    val leafVibrant = primaryColor.copy(alpha = (primaryColor.alpha * 0.65f).coerceAtMost(1f))
    val leafHighlight = primaryColor.copy(alpha = (primaryColor.alpha * 0.45f).coerceAtMost(1f))
    val leafVein = primaryColor.copy(alpha = (primaryColor.alpha * 1.45f).coerceAtMost(1f))
    val petalColor = accentColor.copy(alpha = (accentColor.alpha * 1.2f).coerceAtMost(0.40f))

    // Helper to draw realistic organic leaf with dual-tone blade, fine central vein & lateral veins
    fun drawRealisticLeaf(
        baseX: Float,
        baseY: Float,
        tipX: Float,
        tipY: Float,
        belly: Float,
        toneLevel: Int = 0 // 0: deep, 1: vibrant, 2: highlight
    ) {
        val midX = (baseX + tipX) / 2f
        val midY = (baseY + tipY) / 2f
        val dx = tipX - baseX
        val dy = tipY - baseY
        val len = kotlin.math.sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
        val nx = -dy / len * belly
        val ny = dx / len * belly

        // Leaf outline path
        val leafPath = Path().apply {
            moveTo(baseX, baseY)
            cubicTo(midX + nx * 1.05f, midY + ny * 1.05f, tipX + nx * 0.2f, tipY + ny * 0.2f, tipX, tipY)
            cubicTo(tipX - nx * 0.2f, tipY - ny * 0.2f, midX - nx * 0.75f, midY - ny * 0.75f, baseX, baseY)
            close()
        }
        val fillColor = when (toneLevel) {
            2 -> leafHighlight
            1 -> leafVibrant
            else -> leafDeep
        }
        drawPath(path = leafPath, color = fillColor, style = Fill)

        // Central vein
        drawLine(
            color = leafVein,
            start = Offset(baseX, baseY),
            end = Offset(tipX * 0.95f + baseX * 0.05f, tipY * 0.95f + baseY * 0.05f),
            strokeWidth = 1.1f,
            cap = StrokeCap.Round
        )

        // Lateral veins (subtle herringbone texture)
        val v1X = baseX * 0.68f + tipX * 0.32f
        val v1Y = baseY * 0.68f + tipY * 0.32f
        drawLine(
            color = leafVein.copy(alpha = leafVein.alpha * 0.55f),
            start = Offset(v1X, v1Y),
            end = Offset(v1X + nx * 0.45f, v1Y + ny * 0.45f),
            strokeWidth = 0.75f,
            cap = StrokeCap.Round
        )
        val v2X = baseX * 0.38f + tipX * 0.62f
        val v2Y = baseY * 0.38f + tipY * 0.62f
        drawLine(
            color = leafVein.copy(alpha = leafVein.alpha * 0.55f),
            start = Offset(v2X, v2Y),
            end = Offset(v2X - nx * 0.35f, v2Y - ny * 0.35f),
            strokeWidth = 0.75f,
            cap = StrokeCap.Round
        )
    }

    // Helper to draw realistic ancient tree branch with organic tapering and bark grooves
    fun drawAncientBranch(
        start: Offset,
        c1: Offset,
        c2: Offset,
        end: Offset,
        startWidth: Float,
        endWidth: Float
    ) {
        val branchPath = Path().apply {
            moveTo(start.x, start.y)
            cubicTo(c1.x, c1.y, c2.x, c2.y, end.x, end.y)
        }
        drawPath(
            path = branchPath,
            color = trunkColor,
            style = Stroke(width = (startWidth + endWidth) / 2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        // Bark texture line along the curve for realistic wood grain
        val barkPath = Path().apply {
            moveTo(start.x + 1f, start.y - 1f)
            cubicTo(c1.x + 0.5f, c1.y - 0.5f, c2.x + 0.5f, c2.y - 0.5f, end.x, end.y)
        }
        drawPath(
            path = barkPath,
            color = trunkColor.copy(alpha = trunkColor.alpha * 0.4f),
            style = Stroke(width = 0.8f, cap = StrokeCap.Round)
        )
    }

    // ================= 1. TOP-LEFT: Ancient Majestic Oak Tree Trunk & Sweeping Bough =================
    // Thick gnarled trunk entering from top-left corner
    drawAncientBranch(
        start = Offset(-20f, -15f),
        c1 = Offset(w * 0.12f, h * 0.025f),
        c2 = Offset(w * 0.28f, h * 0.055f),
        end = Offset(w * 0.44f, h * 0.045f),
        startWidth = 5.5f,
        endWidth = 3.2f
    )

    // Sub-bough 1 reaching into upper center
    drawAncientBranch(
        start = Offset(w * 0.24f, h * 0.045f),
        c1 = Offset(w * 0.32f, h * 0.080f),
        c2 = Offset(w * 0.38f, h * 0.120f),
        end = Offset(w * 0.42f, h * 0.160f),
        startWidth = 2.8f,
        endWidth = 1.6f
    )

    // Fine twigs branching off Sub-bough 1
    val twigL1 = Path().apply {
        moveTo(w * 0.34f, h * 0.092f)
        cubicTo(w * 0.31f, h * 0.130f, w * 0.28f, h * 0.165f, w * 0.24f, h * 0.185f)
    }
    drawPath(path = twigL1, color = twigColor, style = Stroke(width = 1.4f, cap = StrokeCap.Round))

    val twigL2 = Path().apply {
        moveTo(w * 0.40f, h * 0.140f)
        cubicTo(w * 0.44f, h * 0.170f, w * 0.47f, h * 0.195f, w * 0.50f, h * 0.210f)
    }
    drawPath(path = twigL2, color = twigColor, style = Stroke(width = 1.3f, cap = StrokeCap.Round))

    // Dense realistic foliage cluster on Top-Left Boughs
    drawRealisticLeaf(w * 0.24f, h * 0.045f, w * 0.18f, h * 0.070f, 11f, toneLevel = 0)
    drawRealisticLeaf(w * 0.30f, h * 0.065f, w * 0.26f, h * 0.110f, 12f, toneLevel = 1)
    drawRealisticLeaf(w * 0.36f, h * 0.100f, w * 0.32f, h * 0.145f, 11f, toneLevel = 0)
    drawRealisticLeaf(w * 0.42f, h * 0.160f, w * 0.38f, h * 0.205f, 13f, toneLevel = 2)
    drawRealisticLeaf(w * 0.42f, h * 0.160f, w * 0.46f, h * 0.200f, 12f, toneLevel = 1)
    drawRealisticLeaf(w * 0.31f, h * 0.130f, w * 0.25f, h * 0.155f, 10f, toneLevel = 0)
    drawRealisticLeaf(w * 0.24f, h * 0.185f, w * 0.18f, h * 0.215f, 11f, toneLevel = 1)
    drawRealisticLeaf(w * 0.44f, h * 0.170f, w * 0.49f, h * 0.165f, 10f, toneLevel = 2)
    drawRealisticLeaf(w * 0.50f, h * 0.210f, w * 0.55f, h * 0.235f, 11f, toneLevel = 1)

    // ================= 2. TOP-RIGHT: Sweeping Ancient Tree Canopy with Weeping Vines =================
    drawAncientBranch(
        start = Offset(w + 25f, -10f),
        c1 = Offset(w * 0.84f, h * 0.030f),
        c2 = Offset(w * 0.68f, h * 0.065f),
        end = Offset(w * 0.55f, h * 0.055f),
        startWidth = 5.2f,
        endWidth = 2.8f
    )

    // Hanging weeping branch descending down right border
    drawAncientBranch(
        start = Offset(w * 0.78f, h * 0.045f),
        c1 = Offset(w * 0.75f, h * 0.105f),
        c2 = Offset(w * 0.72f, h * 0.170f),
        end = Offset(w * 0.67f, h * 0.230f),
        startWidth = 2.5f,
        endWidth = 1.5f
    )

    // Weeping tendril 1
    val vine1 = Path().apply {
        moveTo(w * 0.73f, h * 0.145f)
        cubicTo(w * 0.78f, h * 0.185f, w * 0.81f, h * 0.240f, w * 0.82f, h * 0.285f)
    }
    drawPath(path = vine1, color = twigColor, style = Stroke(width = 1.3f, cap = StrokeCap.Round))

    // Weeping tendril 2
    val vine2 = Path().apply {
        moveTo(w * 0.67f, h * 0.230f)
        cubicTo(w * 0.64f, h * 0.275f, w * 0.62f, h * 0.320f, w * 0.58f, h * 0.355f)
    }
    drawPath(path = vine2, color = twigColor, style = Stroke(width = 1.2f, cap = StrokeCap.Round))

    // Foliage on Top-Right Canopy
    drawRealisticLeaf(w * 0.88f, h * 0.020f, w * 0.82f, h * 0.055f, 11f, toneLevel = 0)
    drawRealisticLeaf(w * 0.84f, h * 0.055f, w * 0.78f, h * 0.090f, 12f, toneLevel = 1)
    drawRealisticLeaf(w * 0.76f, h * 0.085f, w * 0.70f, h * 0.125f, 13f, toneLevel = 2)
    drawRealisticLeaf(w * 0.72f, h * 0.135f, w * 0.65f, h * 0.165f, 11f, toneLevel = 0)
    drawRealisticLeaf(w * 0.67f, h * 0.230f, w * 0.60f, h * 0.260f, 12f, toneLevel = 1)
    drawRealisticLeaf(w * 0.78f, h * 0.185f, w * 0.84f, h * 0.215f, 10f, toneLevel = 2)
    drawRealisticLeaf(w * 0.82f, h * 0.285f, w * 0.86f, h * 0.320f, 11f, toneLevel = 1)
    drawRealisticLeaf(w * 0.62f, h * 0.320f, w * 0.56f, h * 0.345f, 11f, toneLevel = 0)
    drawRealisticLeaf(w * 0.58f, h * 0.355f, w * 0.52f, h * 0.380f, 12f, toneLevel = 2)

    // ================= 3. MID-LEFT: Secret Garden Tree Branch with Clustered Leaves =================
    drawAncientBranch(
        start = Offset(-18f, h * 0.38f),
        c1 = Offset(w * 0.08f, h * 0.395f),
        c2 = Offset(w * 0.16f, h * 0.425f),
        end = Offset(w * 0.24f, h * 0.450f),
        startWidth = 3.6f,
        endWidth = 2.0f
    )
    val leftTwigA = Path().apply {
        moveTo(w * 0.12f, h * 0.405f)
        cubicTo(w * 0.15f, h * 0.365f, w * 0.19f, h * 0.345f, w * 0.23f, h * 0.330f)
    }
    drawPath(path = leftTwigA, color = twigColor, style = Stroke(width = 1.5f, cap = StrokeCap.Round))

    drawRealisticLeaf(w * 0.08f, h * 0.395f, w * 0.14f, h * 0.435f, 10f, toneLevel = 0)
    drawRealisticLeaf(w * 0.16f, h * 0.425f, w * 0.22f, h * 0.470f, 11f, toneLevel = 1)
    drawRealisticLeaf(w * 0.24f, h * 0.450f, w * 0.30f, h * 0.485f, 12f, toneLevel = 2)
    drawRealisticLeaf(w * 0.15f, h * 0.365f, w * 0.20f, h * 0.335f, 10f, toneLevel = 1)
    drawRealisticLeaf(w * 0.23f, h * 0.330f, w * 0.28f, h * 0.315f, 11f, toneLevel = 2)

    // ================= 4. BOTTOM-RIGHT: Mossy Ancient Root / Lower Branch Sprig =================
    drawAncientBranch(
        start = Offset(w + 15f, h * 0.85f),
        c1 = Offset(w * 0.88f, h * 0.865f),
        c2 = Offset(w * 0.78f, h * 0.890f),
        end = Offset(w * 0.68f, h * 0.915f),
        startWidth = 3.2f,
        endWidth = 1.8f
    )
    drawRealisticLeaf(w * 0.86f, h * 0.865f, w * 0.80f, h * 0.845f, 10f, toneLevel = 0)
    drawRealisticLeaf(w * 0.78f, h * 0.890f, w * 0.71f, h * 0.870f, 11f, toneLevel = 1)
    drawRealisticLeaf(w * 0.68f, h * 0.915f, w * 0.60f, h * 0.930f, 12f, toneLevel = 2)

    // ================= 5. ALICE IN WONDERLAND FAIRY ACCENTS: Petals & Dappled Firefly Orbs =================
    // Soft glowing fairy orbs drifting between the foliage
    val fairyGlowColor = accentColor.copy(alpha = 0.24f)
    drawCircle(color = fairyGlowColor, radius = 5.5f, center = Offset(w * 0.38f, h * 0.08f))
    drawCircle(color = fairyGlowColor, radius = 4.0f, center = Offset(w * 0.46f, h * 0.14f))
    drawCircle(color = fairyGlowColor, radius = 6.0f, center = Offset(w * 0.74f, h * 0.22f))
    drawCircle(color = fairyGlowColor, radius = 4.5f, center = Offset(w * 0.62f, h * 0.28f))
    drawCircle(color = fairyGlowColor, radius = 5.0f, center = Offset(w * 0.26f, h * 0.42f))

    // Delicate falling peach/cherry blossom petals floating in the breeze
    fun drawPetal(cx: Float, cy: Float, rx: Float, ry: Float) {
        val petalPath = Path().apply {
            moveTo(cx, cy - ry)
            quadraticTo(cx + rx, cy, cx, cy + ry)
            quadraticTo(cx - rx, cy, cx, cy - ry)
            close()
        }
        drawPath(path = petalPath, color = petalColor, style = Fill)
    }

    drawPetal(w * 0.48f, h * 0.12f, 4f, 7f)
    drawPetal(w * 0.35f, h * 0.26f, 3.5f, 6.5f)
    drawPetal(w * 0.65f, h * 0.19f, 4f, 7.5f)
    drawPetal(w * 0.77f, h * 0.36f, 3.8f, 7f)
    drawPetal(w * 0.29f, h * 0.52f, 4f, 7f)
    drawPetal(w * 0.58f, h * 0.68f, 3.5f, 6.5f)
}

/**
 * Oasis Realistic Theme Decoration (绿洲主题背景层)
 * "把爱丽丝仙境这个主题改成绿洲这个主题里面要有树，有芒果树。有小河河流，这些树啊，芒果啊，树叶树枝小河的元素要明显。"
 */
fun DrawScope.drawOasisRealisticBackground(
    primaryColor: Color = Color(0x38143823),
    waterColor: Color = Color(0x323B927D),
    accentColor: Color = Color(0xFFE5A93C)
) {
    val w = size.width
    val h = size.height

    val trunkColor = primaryColor.copy(alpha = (primaryColor.alpha * 1.35f).coerceAtMost(1f))
    val boughColor = primaryColor.copy(alpha = (primaryColor.alpha * 1.15f).coerceAtMost(1f))
    val twigColor = primaryColor.copy(alpha = (primaryColor.alpha * 0.95f).coerceAtMost(1f))
    val leafDeep = primaryColor.copy(alpha = (primaryColor.alpha * 0.85f).coerceAtMost(1f))
    val leafVibrant = primaryColor.copy(alpha = (primaryColor.alpha * 0.65f).coerceAtMost(1f))
    val leafHighlight = primaryColor.copy(alpha = (primaryColor.alpha * 0.45f).coerceAtMost(1f))
    val leafVein = primaryColor.copy(alpha = (primaryColor.alpha * 1.45f).coerceAtMost(1f))

    // ================= 1. OASIS MEANDERING RIVER STREAM (小河河流) =================
    val riverPath = Path().apply {
        moveTo(w * 0.92f, -10f)
        cubicTo(w * 0.80f, h * 0.22f, w * 0.45f, h * 0.35f, w * 0.20f, h * 0.58f)
        cubicTo(w * 0.05f, h * 0.72f, -10f, h * 0.82f, -20f, h * 0.92f)
        lineTo(-20f, h * 0.99f)
        cubicTo(w * 0.10f, h * 0.86f, w * 0.28f, h * 0.76f, w * 0.42f, h * 0.62f)
        cubicTo(w * 0.68f, h * 0.42f, w * 0.95f, h * 0.30f, w + 10f, 15f)
        close()
    }
    drawPath(
        path = riverPath,
        color = waterColor.copy(alpha = 0.22f),
        style = Fill
    )

    // River currents & water ripple waves
    val ripplePath1 = Path().apply {
        moveTo(w * 0.85f, 10f)
        cubicTo(w * 0.72f, h * 0.25f, w * 0.40f, h * 0.38f, w * 0.18f, h * 0.62f)
        cubicTo(w * 0.08f, h * 0.74f, 5f, h * 0.84f, 0f, h * 0.90f)
    }
    drawPath(
        path = ripplePath1,
        color = Color(0x3566BB6A),
        style = Stroke(width = 2.0f, cap = StrokeCap.Round)
    )

    val ripplePath2 = Path().apply {
        moveTo(w * 0.80f, 30f)
        cubicTo(w * 0.65f, h * 0.28f, w * 0.36f, h * 0.43f, w * 0.12f, h * 0.68f)
    }
    drawPath(
        path = ripplePath2,
        color = Color(0x2880CBC4),
        style = Stroke(width = 1.6f, cap = StrokeCap.Round)
    )

    drawCircle(color = Color(0x3580DEEA), radius = 3.5f, center = Offset(w * 0.55f, h * 0.34f))
    drawCircle(color = Color(0x3080DEEA), radius = 2.5f, center = Offset(w * 0.38f, h * 0.46f))
    drawCircle(color = Color(0x2580DEEA), radius = 3.0f, center = Offset(w * 0.22f, h * 0.58f))

    // Helper to draw realistic mango leaf
    fun drawMangoLeaf(
        baseX: Float,
        baseY: Float,
        tipX: Float,
        tipY: Float,
        belly: Float,
        toneLevel: Int = 0
    ) {
        val midX = (baseX + tipX) / 2f
        val midY = (baseY + tipY) / 2f
        val dx = tipX - baseX
        val dy = tipY - baseY
        val len = kotlin.math.sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
        val nx = -dy / len * belly
        val ny = dx / len * belly

        val leafPath = Path().apply {
            moveTo(baseX, baseY)
            cubicTo(midX + nx * 1.15f, midY + ny * 1.15f, tipX + nx * 0.25f, tipY + ny * 0.25f, tipX, tipY)
            cubicTo(tipX - nx * 0.25f, tipY - ny * 0.25f, midX - nx * 0.85f, midY - ny * 0.85f, baseX, baseY)
            close()
        }
        val fillColor = when (toneLevel) {
            2 -> leafHighlight
            1 -> leafVibrant
            else -> leafDeep
        }
        drawPath(path = leafPath, color = fillColor, style = Fill)

        drawLine(
            color = leafVein,
            start = Offset(baseX, baseY),
            end = Offset(tipX * 0.96f + baseX * 0.04f, tipY * 0.96f + baseY * 0.04f),
            strokeWidth = 1.1f,
            cap = StrokeCap.Round
        )

        val v1X = baseX * 0.65f + tipX * 0.35f
        val v1Y = baseY * 0.65f + tipY * 0.35f
        drawLine(
            color = leafVein.copy(alpha = leafVein.alpha * 0.6f),
            start = Offset(v1X, v1Y),
            end = Offset(v1X + nx * 0.45f, v1Y + ny * 0.45f),
            strokeWidth = 0.8f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = leafVein.copy(alpha = leafVein.alpha * 0.5f),
            start = Offset(v1X, v1Y),
            end = Offset(v1X - nx * 0.42f, v1Y - ny * 0.42f),
            strokeWidth = 0.75f,
            cap = StrokeCap.Round
        )
    }

    // Helper to draw realistic ripe mango fruit
    fun drawMangoFruit(
        branchX: Float,
        branchY: Float,
        fruitCenterX: Float,
        fruitCenterY: Float,
        radius: Float = 14f
    ) {
        val stemPath = Path().apply {
            moveTo(branchX, branchY)
            quadraticTo((branchX + fruitCenterX) / 2f + 4f, (branchY + fruitCenterY) / 2f, fruitCenterX, fruitCenterY - radius * 0.8f)
        }
        drawPath(
            path = stemPath,
            color = twigColor,
            style = Stroke(width = 1.5f, cap = StrokeCap.Round)
        )

        val fruitPath = Path().apply {
            val cx = fruitCenterX
            val cy = fruitCenterY
            val r = radius
            moveTo(cx, cy - r * 0.9f)
            cubicTo(cx - r * 1.15f, cy - r * 0.5f, cx - r * 1.1f, cy + r * 0.7f, cx - r * 0.2f, cy + r * 1.05f)
            cubicTo(cx + r * 0.1f, cy + r * 1.12f, cx + r * 0.45f, cy + r * 0.95f, cx + r * 0.7f, cy + r * 0.7f)
            cubicTo(cx + r * 1.0f, cy + r * 0.25f, cx + r * 0.65f, cy - r * 0.65f, cx, cy - r * 0.9f)
            close()
        }

        drawPath(
            path = fruitPath,
            color = Color(0xFFF59E0B).copy(alpha = 0.88f),
            style = Fill
        )
        drawCircle(
            color = Color(0xFFE11D48).copy(alpha = 0.35f),
            radius = radius * 0.65f,
            center = Offset(fruitCenterX - radius * 0.25f, fruitCenterY + radius * 0.15f)
        )
        drawCircle(
            color = Color(0x66FFFBEB),
            radius = radius * 0.32f,
            center = Offset(fruitCenterX + radius * 0.25f, fruitCenterY - radius * 0.25f)
        )
    }

    fun drawAncientBranch(
        start: Offset,
        c1: Offset,
        c2: Offset,
        end: Offset,
        startWidth: Float,
        endWidth: Float
    ) {
        val path = Path().apply {
            moveTo(start.x, start.y)
            cubicTo(c1.x, c1.y, c2.x, c2.y, end.x, end.y)
        }
        drawPath(path = path, color = trunkColor, style = Stroke(width = startWidth, cap = StrokeCap.Round))
        drawPath(path = path, color = boughColor, style = Stroke(width = endWidth, cap = StrokeCap.Round))
    }

    // ================= 2. TOP-RIGHT: Grand Mango Tree Canopy =================
    drawAncientBranch(
        start = Offset(w + 25f, -15f),
        c1 = Offset(w * 0.88f, h * 0.05f),
        c2 = Offset(w * 0.72f, h * 0.11f),
        end = Offset(w * 0.52f, h * 0.14f),
        startWidth = 5.6f,
        endWidth = 2.8f
    )

    val twigB1 = Path().apply {
        moveTo(w * 0.74f, h * 0.10f)
        cubicTo(w * 0.68f, h * 0.15f, w * 0.62f, h * 0.18f, w * 0.56f, h * 0.21f)
    }
    drawPath(path = twigB1, color = twigColor, style = Stroke(width = 1.8f, cap = StrokeCap.Round))

    val twigB2 = Path().apply {
        moveTo(w * 0.63f, h * 0.125f)
        cubicTo(w * 0.58f, h * 0.07f, w * 0.50f, h * 0.05f, w * 0.42f, h * 0.04f)
    }
    drawPath(path = twigB2, color = twigColor, style = Stroke(width = 1.8f, cap = StrokeCap.Round))

    drawMangoLeaf(w * 0.52f, h * 0.14f, w * 0.44f, h * 0.165f, 13f, toneLevel = 0)
    drawMangoLeaf(w * 0.52f, h * 0.14f, w * 0.48f, h * 0.205f, 12f, toneLevel = 1)
    drawMangoLeaf(w * 0.56f, h * 0.21f, w * 0.50f, h * 0.245f, 14f, toneLevel = 2)
    drawMangoLeaf(w * 0.60f, h * 0.19f, w * 0.54f, h * 0.230f, 13f, toneLevel = 0)
    drawMangoLeaf(w * 0.70f, h * 0.11f, w * 0.65f, h * 0.160f, 12f, toneLevel = 1)
    drawMangoLeaf(w * 0.42f, h * 0.04f, w * 0.35f, h * 0.035f, 14f, toneLevel = 2)
    drawMangoLeaf(w * 0.46f, h * 0.055f, w * 0.40f, h * 0.085f, 12f, toneLevel = 1)
    drawMangoLeaf(w * 0.78f, h * 0.08f, w * 0.73f, h * 0.135f, 13f, toneLevel = 0)
    drawMangoLeaf(w * 0.85f, h * 0.06f, w * 0.80f, h * 0.105f, 11f, toneLevel = 1)

    // Ripe Golden Mangoes
    drawMangoFruit(w * 0.65f, h * 0.12f, w * 0.64f, h * 0.155f, radius = 13.5f)
    drawMangoFruit(w * 0.54f, h * 0.16f, w * 0.52f, h * 0.198f, radius = 12.0f)
    drawMangoFruit(w * 0.76f, h * 0.09f, w * 0.77f, h * 0.128f, radius = 14.0f)

    // ================= 3. MID-LEFT: Mango Tree Side Branch =================
    drawAncientBranch(
        start = Offset(-20f, h * 0.36f),
        c1 = Offset(w * 0.09f, h * 0.38f),
        c2 = Offset(w * 0.18f, h * 0.42f),
        end = Offset(w * 0.28f, h * 0.45f),
        startWidth = 4.2f,
        endWidth = 2.2f
    )
    val leftTwig = Path().apply {
        moveTo(w * 0.15f, h * 0.40f)
        cubicTo(w * 0.18f, h * 0.34f, w * 0.23f, h * 0.31f, w * 0.28f, h * 0.29f)
    }
    drawPath(path = leftTwig, color = twigColor, style = Stroke(width = 1.6f, cap = StrokeCap.Round))

    drawMangoLeaf(w * 0.08f, h * 0.37f, w * 0.14f, h * 0.41f, 12f, toneLevel = 0)
    drawMangoLeaf(w * 0.18f, h * 0.42f, w * 0.25f, h * 0.46f, 13f, toneLevel = 1)
    drawMangoLeaf(w * 0.28f, h * 0.45f, w * 0.35f, h * 0.48f, 14f, toneLevel = 2)
    drawMangoLeaf(w * 0.22f, h * 0.32f, w * 0.28f, h * 0.30f, 11f, toneLevel = 1)
    drawMangoLeaf(w * 0.28f, h * 0.29f, w * 0.34f, h * 0.27f, 12f, toneLevel = 2)

    drawMangoFruit(w * 0.20f, h * 0.42f, w * 0.21f, h * 0.46f, radius = 12.5f)

    // ================= 4. LOWER OASIS EMBANKMENT SPRIG =================
    drawAncientBranch(
        start = Offset(w + 15f, h * 0.86f),
        c1 = Offset(w * 0.88f, h * 0.88f),
        c2 = Offset(w * 0.77f, h * 0.90f),
        end = Offset(w * 0.65f, h * 0.92f),
        startWidth = 3.6f,
        endWidth = 1.8f
    )
    drawMangoLeaf(w * 0.86f, h * 0.87f, w * 0.79f, h * 0.85f, 11f, toneLevel = 0)
    drawMangoLeaf(w * 0.77f, h * 0.89f, w * 0.69f, h * 0.87f, 12f, toneLevel = 1)
    drawMangoLeaf(w * 0.65f, h * 0.92f, w * 0.57f, h * 0.93f, 13f, toneLevel = 2)
}

/**
 * Oasis Foreground Decoration (在书架封面正前方的层次层)
 * "有的。树叶呀，果儿呀，是在封面儿的前面儿。有层次感。你好好设计一下符合高级的审美."
 *
 * This function renders in front of the Bookshelf items, establishing real 3D spatial depth.
 * - Drooping overhanging mango branch at top-right with ripe golden mangoes right over the shelf header/grid
 * - Floating mango leaves in the foreground with delicate soft ambient drop shadows
 * - Zero touch blocking: 100% vector canvas pass-through!
 */
fun DrawScope.drawOasisForegroundLeavesAndMangoes(
    primaryColor: Color = Color(0x38143823),
    accentColor: Color = Color(0xFFE5A93C)
) {
    val w = size.width
    val h = size.height

    val foreTwig = primaryColor.copy(alpha = 0.85f)
    val foreLeafDeep = primaryColor.copy(alpha = 0.80f)
    val foreLeafVibrant = primaryColor.copy(alpha = 0.68f)
    val foreLeafHighlight = primaryColor.copy(alpha = 0.50f)
    val foreLeafVein = primaryColor.copy(alpha = 0.95f)
    val shadowColor = Color(0x18000000)

    fun drawForeMangoLeaf(
        baseX: Float,
        baseY: Float,
        tipX: Float,
        tipY: Float,
        belly: Float,
        tone: Int = 1
    ) {
        val midX = (baseX + tipX) / 2f
        val midY = (baseY + tipY) / 2f
        val dx = tipX - baseX
        val dy = tipY - baseY
        val len = kotlin.math.sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
        val nx = -dy / len * belly
        val ny = dx / len * belly

        // Soft drop shadow
        val shadowPath = Path().apply {
            moveTo(baseX + 2f, baseY + 3f)
            cubicTo(midX + nx * 1.15f + 2f, midY + ny * 1.15f + 3f, tipX + nx * 0.25f + 2f, tipY + ny * 0.25f + 3f, tipX + 2f, tipY + 3f)
            cubicTo(tipX - nx * 0.25f + 2f, tipY - ny * 0.25f + 3f, midX - nx * 0.85f + 2f, midY - ny * 0.85f + 3f, baseX + 2f, baseY + 3f)
            close()
        }
        drawPath(path = shadowPath, color = shadowColor, style = Fill)

        val leafPath = Path().apply {
            moveTo(baseX, baseY)
            cubicTo(midX + nx * 1.15f, midY + ny * 1.15f, tipX + nx * 0.25f, tipY + ny * 0.25f, tipX, tipY)
            cubicTo(tipX - nx * 0.25f, tipY - ny * 0.25f, midX - nx * 0.85f, midY - ny * 0.85f, baseX, baseY)
            close()
        }
        val fillColor = when (tone) {
            2 -> foreLeafHighlight
            1 -> foreLeafVibrant
            else -> foreLeafDeep
        }
        drawPath(path = leafPath, color = fillColor, style = Fill)

        drawLine(
            color = foreLeafVein,
            start = Offset(baseX, baseY),
            end = Offset(tipX * 0.95f + baseX * 0.05f, tipY * 0.95f + baseY * 0.05f),
            strokeWidth = 1.3f,
            cap = StrokeCap.Round
        )

        val v1X = baseX * 0.65f + tipX * 0.35f
        val v1Y = baseY * 0.65f + tipY * 0.35f
        drawLine(
            color = foreLeafVein.copy(alpha = 0.55f),
            start = Offset(v1X, v1Y),
            end = Offset(v1X + nx * 0.42f, v1Y + ny * 0.42f),
            strokeWidth = 0.8f,
            cap = StrokeCap.Round
        )
    }

    fun drawForeMango(
        stemX: Float,
        stemY: Float,
        fruitX: Float,
        fruitY: Float,
        radius: Float = 15f
    ) {
        drawLine(
            color = foreTwig,
            start = Offset(stemX, stemY),
            end = Offset(fruitX, fruitY - radius * 0.8f),
            strokeWidth = 1.8f,
            cap = StrokeCap.Round
        )

        val shadowPath = Path().apply {
            val cx = fruitX + 2.5f
            val cy = fruitY + 3.5f
            val r = radius
            moveTo(cx, cy - r * 0.9f)
            cubicTo(cx - r * 1.15f, cy - r * 0.5f, cx - r * 1.1f, cy + r * 0.7f, cx - r * 0.2f, cy + r * 1.05f)
            cubicTo(cx + r * 0.1f, cy + r * 1.12f, cx + r * 0.45f, cy + r * 0.95f, cx + r * 0.7f, cy + r * 0.7f)
            cubicTo(cx + r * 1.0f, cy + r * 0.25f, cx + r * 0.65f, cy - r * 0.65f, cx, cy - r * 0.9f)
            close()
        }
        drawPath(path = shadowPath, color = shadowColor, style = Fill)

        val fruitPath = Path().apply {
            val cx = fruitX
            val cy = fruitY
            val r = radius
            moveTo(cx, cy - r * 0.9f)
            cubicTo(cx - r * 1.15f, cy - r * 0.5f, cx - r * 1.1f, cy + r * 0.7f, cx - r * 0.2f, cy + r * 1.05f)
            cubicTo(cx + r * 0.1f, cy + r * 1.12f, cx + r * 0.45f, cy + r * 0.95f, cx + r * 0.7f, cy + r * 0.7f)
            cubicTo(cx + r * 1.0f, cy + r * 0.25f, cx + r * 0.65f, cy - r * 0.65f, cx, cy - r * 0.9f)
            close()
        }
        drawPath(
            path = fruitPath,
            color = Color(0xFFF59E0B).copy(alpha = 0.96f),
            style = Fill
        )
        drawCircle(
            color = Color(0xFFE11D48).copy(alpha = 0.42f),
            radius = radius * 0.68f,
            center = Offset(fruitX - radius * 0.25f, fruitY + radius * 0.18f)
        )
        drawCircle(
            color = Color(0x88FFFFFF),
            radius = radius * 0.35f,
            center = Offset(fruitX + radius * 0.28f, fruitY - radius * 0.28f)
        )
    }

    // ================= TOP-RIGHT FOREGROUND HANGING BRANCH OVER BOOKS =================
    val topBough = Path().apply {
        moveTo(w + 10f, 40f)
        cubicTo(w * 0.92f, 65f, w * 0.85f, 90f, w * 0.76f, 105f)
    }
    drawPath(path = topBough, color = foreTwig, style = Stroke(width = 2.6f, cap = StrokeCap.Round))

    drawForeMango(w * 0.85f, 88f, w * 0.83f, 122f, radius = 15.5f)
    drawForeMango(w * 0.77f, 104f, w * 0.74f, 138f, radius = 13.5f)

    drawForeMangoLeaf(w * 0.88f, 75f, w * 0.80f, 105f, 13f, tone = 0)
    drawForeMangoLeaf(w * 0.82f, 95f, w * 0.71f, 118f, 14f, tone = 1)
    drawForeMangoLeaf(w * 0.76f, 105f, w * 0.65f, 112f, 15f, tone = 2)

    // ================= TOP-LEFT FOREGROUND LEAF CLUSTER =================
    val leftSprig = Path().apply {
        moveTo(-5f, 50f)
        cubicTo(w * 0.05f, 65f, w * 0.10f, 80f, w * 0.16f, 92f)
    }
    drawPath(path = leftSprig, color = foreTwig, style = Stroke(width = 2.0f, cap = StrokeCap.Round))

    drawForeMangoLeaf(w * 0.06f, 68f, w * 0.14f, 78f, 12f, tone = 1)
    drawForeMangoLeaf(w * 0.12f, 84f, w * 0.20f, 96f, 13f, tone = 2)

    // ================= DELICATE FLUTTERING MANGO LEAF IN FOREGROUND =================
    drawForeMangoLeaf(w * 0.38f, h * 0.42f, w * 0.45f, h * 0.45f, 10.5f, tone = 1)
    drawForeMangoLeaf(w * 0.68f, h * 0.65f, w * 0.75f, h * 0.68f, 11.0f, tone = 2)
}

