package com.bpeople.finpilot.ui.screens.pin

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.bpeople.finpilot.ui.screens.auth.AppLogo
import com.bpeople.finpilot.ui.screens.auth.AuthBackground
import com.bpeople.finpilot.ui.theme.BrandColor
import kotlin.math.roundToInt

@Composable
fun PinScreen(
    viewModel: PinViewModel,
    onPinVerified: () -> Unit,
    onPinSaved: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.pinVerified) {
        if (state.pinVerified) onPinVerified()
    }
    LaunchedEffect(state.pinSaved) {
        if (state.pinSaved) onPinSaved()
    }

    val title = when (state.mode) {
        PinViewModel.Mode.ENTRY -> "Enter your PIN"
        PinViewModel.Mode.SETUP_ENTER -> "Create a PIN"
        PinViewModel.Mode.SETUP_CONFIRM -> "Confirm your PIN"
    }
    val subtitle = when (state.mode) {
        PinViewModel.Mode.ENTRY -> "Enter your 4-digit PIN to continue"
        PinViewModel.Mode.SETUP_ENTER -> "Set a 4-digit PIN to secure your account"
        PinViewModel.Mode.SETUP_CONFIRM -> "Re-enter your PIN to confirm"
    }

    val shakeOffset by animateFloatAsState(
        targetValue = if (state.shakeError) 1f else 0f,
        animationSpec = if (state.shakeError) {
            keyframes {
                durationMillis = 400
                0f at 0
                -12f at 50
                12f at 100
                -12f at 150
                12f at 200
                -8f at 250
                8f at 300
                0f at 400
            }
        } else {
            spring()
        },
        finishedListener = { viewModel.clearShake() },
        label = "shake",
    )

    AuthBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 72.dp),
            ) {
                AppLogo(size = 72.dp)
                Spacer(Modifier.height(20.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.offset { IntOffset(shakeOffset.roundToInt(), 0) },
            ) {
                PinDots(filledCount = state.enteredDigits.length)
                Spacer(Modifier.height(16.dp))
                if (state.errorMessage != null) {
                    Text(
                        text = state.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            PinKeypad(
                modifier = Modifier.padding(bottom = 56.dp),
                onDigit = { viewModel.onDigit(it) },
                onBackspace = { viewModel.onBackspace() },
            )
        }
    }
}

@Composable
private fun PinDots(filledCount: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(4) { index ->
            val filled = index < filledCount
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .border(2.dp, BrandColor, CircleShape)
                    .background(if (filled) BrandColor else Color.Transparent),
            )
        }
    }
}

@Composable
private fun PinKeypad(
    modifier: Modifier = Modifier,
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "DEL"),
    )
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        rows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                row.forEach { key ->
                    when (key) {
                        "" -> Spacer(Modifier.size(80.dp))
                        "DEL" -> PinKeypadButton(
                            modifier = Modifier.size(80.dp),
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onBackspace()
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Backspace,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        else -> PinKeypadButton(
                            modifier = Modifier.size(80.dp),
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onDigit(key[0])
                            },
                        ) {
                            Text(
                                text = key,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PinKeypadButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
