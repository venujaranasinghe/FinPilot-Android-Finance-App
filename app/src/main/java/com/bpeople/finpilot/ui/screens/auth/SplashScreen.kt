package com.bpeople.finpilot.ui.screens.auth

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bpeople.finpilot.ui.theme.BrandColor
import com.bpeople.finpilot.ui.theme.DeepText
import com.bpeople.finpilot.ui.theme.FinPilotTheme
import com.bpeople.finpilot.ui.theme.GradientEnd
import com.bpeople.finpilot.ui.theme.GradientStart
import com.bpeople.finpilot.ui.theme.PrimaryIndigo
import com.bpeople.finpilot.ui.theme.SubtleText
import kotlinx.coroutines.delay

// ─────────────────────────────────────────────────────────────────────────────
// Shared composables used across auth screens
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun AuthBackground(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(GradientStart, GradientEnd),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                )
            )
    ) {
        BlobCanvas()
        content()
    }
}

@Composable
internal fun BlobCanvas(modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Rose — top-left
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x66FDA4AF), Color.Transparent),
                center = Offset(w * 0.05f, h * 0.06f),
                radius = w * 0.58f,
            ),
            radius = w * 0.58f,
            center = Offset(w * 0.05f, h * 0.06f),
        )
        // Indigo — top-right
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x55A5B4FC), Color.Transparent),
                center = Offset(w * 0.92f, h * 0.04f),
                radius = w * 0.48f,
            ),
            radius = w * 0.48f,
            center = Offset(w * 0.92f, h * 0.04f),
        )
        // Teal — bottom-left
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x555EEAD4), Color.Transparent),
                center = Offset(w * 0.1f, h * 0.88f),
                radius = w * 0.52f,
            ),
            radius = w * 0.52f,
            center = Offset(w * 0.1f, h * 0.88f),
        )
        // Amber — center-right
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x44FCD34D), Color.Transparent),
                center = Offset(w * 0.90f, h * 0.60f),
                radius = w * 0.42f,
            ),
            radius = w * 0.42f,
            center = Offset(w * 0.90f, h * 0.60f),
        )
    }
}

@Composable
internal fun LogoBadge(size: Int, modifier: Modifier = Modifier) {
    val outerDp = size.dp
    val innerDp = (size * 0.78f).dp

    Box(
        modifier = modifier.size(outerDp),
        contentAlignment = Alignment.Center,
    ) {
        // Outer glass ring
        Box(
            modifier = Modifier
                .size(outerDp)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    spotColor = BrandColor.copy(alpha = 0.25f),
                    ambientColor = BrandColor.copy(alpha = 0.1f),
                )
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.30f))
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        listOf(Color.White.copy(alpha = 0.9f), Color.White.copy(alpha = 0.2f))
                    ),
                    shape = CircleShape,
                )
        )
        // Inner circle
        Box(
            modifier = Modifier
                .size(innerDp)
                .clip(CircleShape)
                .background(BrandColor),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "FP",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = (size * 0.22f).sp,
                    letterSpacing = 2.sp,
                ),
                color = Color.White,
            )
        }
    }
}

@Composable
internal fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 28.dp,
    topCornersOnly: Boolean = false,
    content: @Composable () -> Unit,
) {
    val shape = if (topCornersOnly)
        RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius)
    else
        RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .shadow(
                elevation = 24.dp,
                shape = shape,
                spotColor = BrandColor.copy(alpha = 0.12f),
                ambientColor = BrandColor.copy(alpha = 0.05f),
            )
            .clip(shape)
            .background(Color.White.copy(alpha = 0.88f))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.95f), Color.White.copy(alpha = 0.25f))
                ),
                shape = shape,
            )
    ) {
        content()
    }
}

@Composable
internal fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    Box(
        modifier = modifier
            .height(54.dp)
            .shadow(
                elevation = if (!isLoading) 8.dp else 0.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = BrandColor.copy(alpha = 0.35f),
            )
            .clip(RoundedCornerShape(16.dp))
            .background(if (!isLoading) BrandColor else Color(0xFFBDBDBD))
            .clickable(
                enabled = !isLoading,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color.White,
                strokeWidth = 2.5.dp,
                trackColor = Color.White.copy(alpha = 0.3f),
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                ),
                color = Color.White,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Splash screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToDashboard: () -> Unit,
) {
    var started by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (started) 1f else 0.65f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label = "scale",
    )
    val alpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(900),
        label = "alpha",
    )

    LaunchedEffect(Unit) {
        started = true
        delay(2800L)
        // TODO: replace with Firebase auth check — call onNavigateToDashboard() if signed in
        onNavigateToLogin()
    }

    AuthBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .alpha(alpha)
                .scale(scale),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            LogoBadge(size = 104)

            Spacer(Modifier.height(24.dp))

            Text(
                text = "FinPilot",
                style = MaterialTheme.typography.displaySmall.copy(
                    color = BrandColor,
                    fontWeight = FontWeight.ExtraBold,
                ),
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Navigate Your Finances",
                style = MaterialTheme.typography.bodyLarge,
                color = SubtleText,
                textAlign = TextAlign.Center,
                letterSpacing = 0.5.sp,
            )
        }

        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .align(Alignment.BottomCenter)
                .alpha(alpha),
            color = BrandColor,
            trackColor = BrandColor.copy(alpha = 0.15f),
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun SplashPreview() {
    FinPilotTheme { SplashScreen({}, {}) }
}
