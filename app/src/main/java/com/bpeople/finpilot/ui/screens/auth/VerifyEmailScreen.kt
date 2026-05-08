package com.bpeople.finpilot.ui.screens.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bpeople.finpilot.ui.theme.BrandColor
import com.bpeople.finpilot.ui.theme.DeepText
import com.bpeople.finpilot.ui.theme.FinPilotTheme
import com.bpeople.finpilot.ui.theme.SubtleText

@Composable
fun VerifyEmailScreen(
    infoMessage: String?,
    errorMessage: String?,
    onResendVerification: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    AuthBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            GlassCard(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 28.dp),
                cornerRadius = 28.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AppLogo(size = 64.dp)

                    Spacer(Modifier.height(18.dp))

                    Text(
                        text = "Verification email sent",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = DeepText,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = "We sent a verification link to your email address. Please verify your email to activate your account, then sign in to continue.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SubtleText,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.height(20.dp))

                    GradientButton(
                        text = "Back to Login",
                        onClick = onNavigateToLogin,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Resend verification email",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = BrandColor,
                        modifier = Modifier
                            .clickable { onResendVerification() }
                            .padding(4.dp),
                    )

                    if (!infoMessage.isNullOrBlank()) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = infoMessage,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = BrandColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    if (!errorMessage.isNullOrBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    val helpText = buildAnnotatedString {
                        withStyle(SpanStyle(color = SubtleText)) {
                            append("Did not receive the email? ")
                        }
                        withStyle(SpanStyle(color = BrandColor, fontWeight = FontWeight.SemiBold)) {
                            append("Check spam or try again later.")
                        }
                    }
                    Text(
                        text = helpText,
                        style = MaterialTheme.typography.labelMedium.copy(textAlign = TextAlign.Center),
                    )
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun VerifyEmailPreview() {
    FinPilotTheme {
        VerifyEmailScreen(
            infoMessage = "Verification email sent. Please check your inbox.",
            errorMessage = null,
            onResendVerification = {},
            onNavigateToLogin = {},
        )
    }
}
