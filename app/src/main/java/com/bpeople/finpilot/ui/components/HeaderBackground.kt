package com.bpeople.finpilot.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

private class WavyBottomShape(private val amplitudePx: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val baseline = size.height - amplitudePx
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(0f, baseline)
            // 4 half-waves → 2 complete sine cycles across the width
            val segW = size.width / 4f
            for (i in 0 until 4) {
                val x = i * segW
                val cy = if (i % 2 == 0) baseline + amplitudePx else baseline - amplitudePx
                quadraticBezierTo(x + segW * 0.5f, cy, x + segW, baseline)
            }
            lineTo(size.width, 0f)
            close()
        }
        return Outline.Generic(path)
    }
}

/**
 * Returns a Shape whose bottom edge is a smooth two-cycle sine wave instead of
 * a straight line.  Apply with Modifier.clip(wavyBottomShape()) on the header
 * background composable.
 */
@Composable
fun wavyBottomShape(amplitude: Dp = 20.dp): Shape {
    val amp = with(LocalDensity.current) { amplitude.toPx() }
    return remember(amp) { WavyBottomShape(amp) }
}

/**
 * A premium, theme-aware dynamic header background.
 * Draws custom layered wavy and curved patterns using a light yellow/orange mix.
 */
@Composable
fun DynamicHeaderBackground(
    patternType: String,
    modifier: Modifier = Modifier
) {
    // Shared color definitions
    val lightYellow = Color(0xFFFEF9C3) // Yellow 100
    val softYellow = Color(0xFFFEF08A)  // Yellow 200
    val lightOrange = Color(0xFFFFEDD5) // Orange 100
    val softOrange = Color(0xFFFED7AA)  // Orange 200
    val warmPeach = Color(0xFFFDE8E0)   // Light peach
    val accentAmber = Color(0xFFF59E0B) // Amber 500
    val accentOrange = Color(0xFFFF6B00)// Brand Orange
    val accentDeepOrange = Color(0xFFEA580C) // Deep Orange 600

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            when (patternType.lowercase()) {
                "dashboard" -> {
                    // Pattern 1: Wavy bottom with layered wave paths
                    // 1. Draw solid background gradient
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(lightOrange, warmPeach)
                        )
                    )

                    // 2. Draw Wave 1 (Deepest)
                    val path1 = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(w, 0f)
                        lineTo(w, h * 0.75f)
                        cubicTo(
                            w * 0.7f, h * 0.95f,
                            w * 0.35f, h * 0.55f,
                            0f, h * 0.70f
                        )
                        close()
                    }
                    drawPath(
                        path = path1,
                        brush = Brush.linearGradient(
                            colors = listOf(softYellow.copy(alpha = 0.5f), softOrange.copy(alpha = 0.4f)),
                            start = Offset(0f, 0f),
                            end = Offset(w, h)
                        )
                    )

                    // 3. Draw Wave 2 (Middle)
                    val path2 = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(w, 0f)
                        lineTo(w, h * 0.65f)
                        cubicTo(
                            w * 0.75f, h * 0.50f,
                            w * 0.25f, h * 0.85f,
                            0f, h * 0.60f
                        )
                        close()
                    }
                    drawPath(
                        path = path2,
                        brush = Brush.linearGradient(
                            colors = listOf(lightOrange.copy(alpha = 0.6f), softYellow.copy(alpha = 0.4f)),
                            start = Offset(w, 0f),
                            end = Offset(0f, h)
                        )
                    )

                    // 4. Draw Wave 3 (Foreground wave highlight)
                    val path3 = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(w, 0f)
                        lineTo(w, h * 0.55f)
                        cubicTo(
                            w * 0.6f, h * 0.70f,
                            w * 0.4f, h * 0.40f,
                            0f, h * 0.50f
                        )
                        close()
                    }
                    drawPath(
                        path = path3,
                        brush = Brush.linearGradient(
                            colors = listOf(accentOrange.copy(alpha = 0.15f), accentAmber.copy(alpha = 0.12f)),
                            start = Offset(0f, 0f),
                            end = Offset(w, h)
                        )
                    )
                }

                "income" -> {
                    // Pattern 2: Circular overlaps (inspired by Screen 2 circular design)
                    // 1. Draw base gradient
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(lightYellow, lightOrange)
                        )
                    )

                    // 2. Draw large top-right circular glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(accentOrange.copy(alpha = 0.25f), Color.Transparent),
                            center = Offset(w * 0.85f, h * 0.15f),
                            radius = w * 0.6f
                        ),
                        center = Offset(w * 0.85f, h * 0.15f),
                        radius = w * 0.6f
                    )

                    // 3. Draw overlapping center-left circular shape
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(accentAmber.copy(alpha = 0.20f), Color.Transparent),
                            center = Offset(w * 0.2f, h * 0.7f),
                            radius = w * 0.5f
                        ),
                        center = Offset(w * 0.2f, h * 0.7f),
                        radius = w * 0.5f
                    )

                    // 4. Layered overlapping curve path at the bottom edge
                    val bottomEdge = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(w, 0f)
                        lineTo(w, h * 0.72f)
                        cubicTo(
                            w * 0.75f, h * 0.95f,
                            w * 0.25f, h * 0.60f,
                            0f, h * 0.80f
                        )
                        close()
                    }
                    drawPath(
                        path = bottomEdge,
                        brush = Brush.linearGradient(
                            colors = listOf(softOrange.copy(alpha = 0.35f), softYellow.copy(alpha = 0.25f))
                        )
                    )
                }

                "expense" -> {
                    // Pattern 3: Diagonal waves (flowing cash flow concept)
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(warmPeach, lightYellow)
                        )
                    )

                    // 1. Diagonal wave 1
                    val p1 = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(w * 0.9f, 0f)
                        cubicTo(w * 0.7f, h * 0.4f, w * 0.3f, h * 0.7f, 0f, h * 0.95f)
                        close()
                    }
                    drawPath(
                        path = p1,
                        brush = Brush.linearGradient(
                            colors = listOf(softYellow.copy(alpha = 0.45f), softOrange.copy(alpha = 0.3f))
                        )
                    )

                    // 2. Diagonal wave 2 (shifted and intersecting)
                    val p2 = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(w, 0f)
                        lineTo(w, h * 0.55f)
                        cubicTo(w * 0.8f, h * 0.8f, w * 0.4f, h * 0.4f, 0f, h * 0.75f)
                        close()
                    }
                    drawPath(
                        path = p2,
                        brush = Brush.linearGradient(
                            colors = listOf(accentOrange.copy(alpha = 0.12f), accentAmber.copy(alpha = 0.15f))
                        )
                    )

                    // 3. Highlight line
                    val p3 = Path().apply {
                        moveTo(0f, h * 0.6f)
                        cubicTo(w * 0.35f, h * 0.3f, w * 0.7f, h * 0.9f, w, h * 0.45f)
                    }
                    drawPath(
                        path = p3,
                        color = accentOrange.copy(alpha = 0.18f),
                        style = Stroke(width = 8f)
                    )
                }

                "transaction" -> {
                    // Pattern 4: Abstract modern overlapping glassmorphic arcs
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(lightOrange, lightYellow)
                        )
                    )

                    // Arc 1 (Top Left)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(accentAmber.copy(alpha = 0.22f), Color.Transparent),
                            center = Offset(0f, 0f),
                            radius = w * 0.7f
                        ),
                        center = Offset(0f, 0f),
                        radius = w * 0.7f
                    )

                    // Arc 2 (Bottom Right)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(accentOrange.copy(alpha = 0.16f), Color.Transparent),
                            center = Offset(w, h),
                            radius = w * 0.8f
                        ),
                        center = Offset(w, h),
                        radius = w * 0.8f
                    )

                    // Curvy divider block at the bottom
                    val path = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(w, 0f)
                        lineTo(w, h * 0.85f)
                        quadraticTo(w * 0.5f, h * 0.55f, 0f, h * 0.85f)
                        close()
                    }
                    drawPath(
                        path = path,
                        brush = Brush.verticalGradient(
                            colors = listOf(softYellow.copy(alpha = 0.3f), softOrange.copy(alpha = 0.2f))
                        )
                    )
                }

                "goal" -> {
                    // Pattern 5: Target / Concentric arcs
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(warmPeach, lightOrange)
                        )
                    )

                    // Concentric rings drawn from top-right corner
                    val cx = w * 0.9f
                    val cy = h * 0.1f

                    // Ring 1 (Inner filled orb)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(accentOrange.copy(alpha = 0.25f), Color.Transparent),
                            center = Offset(cx, cy),
                            radius = w * 0.3f
                        ),
                        center = Offset(cx, cy),
                        radius = w * 0.3f
                    )

                    // Concentric stroke rings
                    drawCircle(
                        color = accentAmber.copy(alpha = 0.12f),
                        radius = w * 0.45f,
                        center = Offset(cx, cy),
                        style = Stroke(width = 6f)
                    )

                    drawCircle(
                        color = accentOrange.copy(alpha = 0.08f),
                        radius = w * 0.65f,
                        center = Offset(cx, cy),
                        style = Stroke(width = 12f)
                    )

                    drawCircle(
                        color = accentAmber.copy(alpha = 0.06f),
                        radius = w * 0.85f,
                        center = Offset(cx, cy),
                        style = Stroke(width = 18f)
                    )

                    // Clean sweeping curve edge at the bottom
                    val path = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(w, 0f)
                        lineTo(w, h * 0.70f)
                        cubicTo(w * 0.7f, h * 0.55f, w * 0.3f, h * 0.90f, 0f, h * 0.75f)
                        close()
                    }
                    drawPath(
                        path = path,
                        brush = Brush.linearGradient(
                            colors = listOf(softYellow.copy(alpha = 0.3f), softOrange.copy(alpha = 0.25f))
                        )
                    )
                }

                "profile" -> {
                    // Pattern 6: Bubble/Wave framing for user details
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(lightOrange, lightYellow)
                        )
                    )

                    // Large bubbly circle in center-left
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(accentOrange.copy(alpha = 0.16f), Color.Transparent),
                            center = Offset(w * 0.15f, h * 0.35f),
                            radius = w * 0.45f
                        ),
                        center = Offset(w * 0.15f, h * 0.35f),
                        radius = w * 0.45f
                    )

                    // Large bubbly circle in top-right
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(accentAmber.copy(alpha = 0.18f), Color.Transparent),
                            center = Offset(w * 0.85f, h * 0.2f),
                            radius = w * 0.4f
                        ),
                        center = Offset(w * 0.85f, h * 0.2f),
                        radius = w * 0.4f
                    )

                    // Wave path at the bottom
                    val path = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(w, 0f)
                        lineTo(w, h * 0.78f)
                        cubicTo(w * 0.8f, h * 0.65f, w * 0.2f, h * 0.95f, 0f, h * 0.80f)
                        close()
                    }
                    drawPath(
                        path = path,
                        brush = Brush.linearGradient(
                            colors = listOf(softOrange.copy(alpha = 0.3f), softYellow.copy(alpha = 0.2f))
                        )
                    )
                }
            }
        }
    }
}
