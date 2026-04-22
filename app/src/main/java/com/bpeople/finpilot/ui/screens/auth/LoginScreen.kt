package com.bpeople.finpilot.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bpeople.finpilot.ui.theme.BrandColor
import com.bpeople.finpilot.ui.theme.DeepText
import com.bpeople.finpilot.ui.theme.FinPilotTheme
import com.bpeople.finpilot.ui.theme.SubtleText

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }
    var isLoading by rememberSaveable { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    fun validateAndLogin() {
        emailError = when {
            email.isBlank() -> "Email is required"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Enter a valid email"
            else -> null
        }
        passwordError = when {
            password.isBlank() -> "Password is required"
            password.length < 6 -> "At least 6 characters"
            else -> null
        }
        if (emailError == null && passwordError == null) {
            focusManager.clearFocus()
            isLoading = true
            // TODO: wire to AuthViewModel.login(email, password)
            onLoginSuccess()
        }
    }

    AuthBackground {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Logo area (top ~38%) ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.38f)
                    .statusBarsPadding(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LogoBadge(size = 76)
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "FinPilot",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = BrandColor,
                        ),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Good to see you again",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SubtleText,
                    )
                }
            }

            // ── Glass card (bottom ~62%) ──────────────────────────────────────
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.62f),
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
                    // Decorative pull handle
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFFDDE1F0))
                    )
                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = DeepText,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Enter your credentials to continue",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SubtleText,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(Modifier.height(28.dp))

                    AuthTextField(
                        value = email,
                        onValueChange = { email = it; emailError = null },
                        label = "Email address",
                        leadingIcon = {
                            Icon(
                                Icons.Default.Email, null,
                                tint = if (email.isNotEmpty()) BrandColor else SubtleText,
                                modifier = Modifier.size(20.dp),
                            )
                        },
                        isError = emailError != null,
                        errorText = emailError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next,
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                    )

                    Spacer(Modifier.height(14.dp))

                    AuthTextField(
                        value = password,
                        onValueChange = { password = it; passwordError = null },
                        label = "Password",
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock, null,
                                tint = if (password.isNotEmpty()) BrandColor else SubtleText,
                                modifier = Modifier.size(20.dp),
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff
                                    else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = SubtleText,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        isError = passwordError != null,
                        errorText = passwordError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(onDone = { validateAndLogin() }),
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = "Forgot password?",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = BrandColor,
                        modifier = Modifier
                            .align(Alignment.End)
                            .clickable { /* TODO: forgot password flow */ }
                            .padding(4.dp),
                    )

                    Spacer(Modifier.height(24.dp))

                    GradientButton(
                        text = "Sign In",
                        onClick = { validateAndLogin() },
                        modifier = Modifier.fillMaxWidth(),
                        isLoading = isLoading,
                    )

                    Spacer(Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE8EAFF))
                        Text(
                            text = "  or  ",
                            style = MaterialTheme.typography.labelMedium,
                            color = SubtleText,
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE8EAFF))
                    }

                    Spacer(Modifier.height(20.dp))

                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = SubtleText)) {
                                append("Don't have an account?  ")
                            }
                            withStyle(SpanStyle(color = BrandColor, fontWeight = FontWeight.Bold)) {
                                append("Create one")
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .clickable { onNavigateToRegister() }
                            .padding(vertical = 8.dp),
                    )

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
internal fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isError: Boolean = false,
    errorText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        isError = isError,
        supportingText = errorText?.let { msg -> { Text(msg, color = MaterialTheme.colorScheme.error) } },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BrandColor,
            unfocusedBorderColor = Color(0xFFDDE1F0),
            focusedLabelColor = BrandColor,
            unfocusedLabelColor = SubtleText,
            cursorColor = BrandColor,
            focusedTextColor = DeepText,
            unfocusedTextColor = DeepText,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color(0xFFF8F9FF),
            errorBorderColor = MaterialTheme.colorScheme.error,
            errorLabelColor = MaterialTheme.colorScheme.error,
            errorCursorColor = MaterialTheme.colorScheme.error,
            errorContainerColor = Color(0xFFFFF5F5),
        ),
    )
}

@Preview(showSystemUi = true)
@Composable
private fun LoginPreview() {
    FinPilotTheme { LoginScreen({}, {}) }
}
