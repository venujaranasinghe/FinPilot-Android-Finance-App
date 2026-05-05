package com.bpeople.finpilot.ui.screens.auth

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bpeople.finpilot.ui.theme.BrandColor
import com.bpeople.finpilot.ui.theme.FinPilotTheme
import com.bpeople.finpilot.ui.theme.SubtleText
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    viewModel: AuthViewModel,
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
        // Check Firebase auth state and navigate accordingly
        if (viewModel.isLoggedIn) {
            onNavigateToDashboard()
        } else {
            onNavigateToLogin()
        }
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
            AppLogo(size = 120.dp)

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
    FinPilotTheme {
        @Suppress("ViewModelConstructorInComposable")
        SplashScreen(
            viewModel = AuthViewModel(),
            onNavigateToLogin = {},
            onNavigateToDashboard = {}
        )
    }
}