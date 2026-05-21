package com.bpeople.finpilot.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bpeople.finpilot.ui.theme.BrandColor
import com.bpeople.finpilot.ui.theme.DeepText
import com.bpeople.finpilot.ui.theme.FinPilotTheme
import com.bpeople.finpilot.ui.theme.SubtleText
import kotlinx.coroutines.delay

@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }

    val resetState by viewModel.resetState.collectAsState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(resetState) {
        when (resetState) {
            is AuthViewModel.ResetState.Success -> {
                emailError = null
                delay(1500)
                viewModel.clearResetState()
                onNavigateBack()
            }
            is AuthViewModel.ResetState.Error -> {
                emailError = (resetState as AuthViewModel.ResetState.Error).message
            }
            AuthViewModel.ResetState.Idle -> Unit
        }
    }

    fun submitReset() {
        focusManager.clearFocus()
        viewModel.forgotPassword(email)
    }

    ForgotPasswordContent(
        email = email,
        emailError = emailError,
        resetState = resetState,
        onEmailChange = {
            email = it
            emailError = null
            viewModel.clearResetState()
        },
        onSubmit = { submitReset() },
        onNavigateBack = onNavigateBack,
    )
}

@Composable
internal fun ForgotPasswordContent(
    email: String,
    emailError: String?,
    resetState: AuthViewModel.ResetState,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val scrollState = rememberScrollState()

    AuthBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.32f)
                    .statusBarsPadding(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AppLogo(size = 72.dp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Reset Password",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = BrandColor,
                        ),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "We will email you a reset link",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SubtleText,
                    )
                }
            }

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.68f),
                cornerRadius = 32.dp,
                topCornersOnly = true,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 28.dp)
                        .imePadding()
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFFE5E7EB))
                    )
                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = "Enter your email address",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = DeepText,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "We will send you a link to reset your password.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SubtleText,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(Modifier.height(28.dp))

                    AuthTextField(
                        value = email,
                        onValueChange = onEmailChange,
                        label = "Email address",
                        leadingIcon = {
                            Icon(
                                Icons.Default.Email,
                                null,
                                tint = if (email.isNotEmpty()) BrandColor else SubtleText,
                                modifier = Modifier.size(20.dp),
                            )
                        },
                        isError = emailError != null,
                        errorText = emailError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                    )

                    Spacer(Modifier.height(18.dp))

                    GradientButton(
                        text = "Send reset link",
                        onClick = { onSubmit() },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    if (resetState is AuthViewModel.ResetState.Success) {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Password reset email sent. Check your inbox.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = BrandColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    Text(
                        text = "Back to login",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = BrandColor,
                        modifier = Modifier
                            .clickable { onNavigateBack() }
                            .padding(vertical = 6.dp),
                    )

                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun ForgotPasswordPreview() {
    FinPilotTheme {
        ForgotPasswordContent(
            email = "jane@finpilot.app",
            emailError = null,
            resetState = AuthViewModel.ResetState.Success,
            onEmailChange = {},
            onSubmit = {},
            onNavigateBack = {},
        )
    }
}
