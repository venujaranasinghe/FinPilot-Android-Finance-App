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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
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
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
) {
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var nameError by rememberSaveable { mutableStateOf<String?>(null) }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmPasswordError by rememberSaveable { mutableStateOf<String?>(null) }

    val isLoading by viewModel.isLoading.collectAsState()
    val authSuccess by viewModel.authSuccess.collectAsState()
    val viewModelError by viewModel.errorMessage.collectAsState()

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Navigate on registration success
    LaunchedEffect(authSuccess) {
        if (authSuccess) {
            viewModel.clearAuthSuccess()
            onRegisterSuccess()
        }
    }

    // Surface ViewModel errors
    LaunchedEffect(viewModelError) {
        viewModelError?.let { msg ->
            emailError = msg
        }
    }

    fun validateAndRegister() {
        nameError = when {
            fullName.isBlank() -> "Name is required"
            fullName.trim().length < 2 -> "Enter your full name"
            else -> null
        }
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
        confirmPasswordError = when {
            confirmPassword.isBlank() -> "Please confirm your password"
            confirmPassword != password -> "Passwords do not match"
            else -> null
        }
        if (nameError == null && emailError == null && passwordError == null && confirmPasswordError == null) {
            focusManager.clearFocus()
            viewModel.onFullNameChange(fullName)
            viewModel.onEmailChange(email)
            viewModel.onPasswordChange(password)
            viewModel.onConfirmPasswordChange(confirmPassword)
            viewModel.register()
        }
    }

    AuthBackground {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Logo area (top ~30%) ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.30f)
                    .statusBarsPadding(),
            ) {
                // Back button
                IconButton(
                    onClick = onNavigateToLogin,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.4f)),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = BrandColor,
                        modifier = Modifier.size(20.dp),
                    )
                }

                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AppLogo(size = 64.dp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "FinPilot",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = BrandColor,
                        ),
                    )
                }
            }

            // ── Glass card (bottom ~70%) ──────────────────────────────────────
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.70f),
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
                            .background(Color(0xFFE5E7EB))
                    )
                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = "Create Account",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = DeepText,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Fill in your details to get started",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SubtleText,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(Modifier.height(24.dp))

                    AuthTextField(
                        value = fullName,
                        onValueChange = { fullName = it; nameError = null; viewModel.clearError() },
                        label = "Full name",
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person, null,
                                tint = if (fullName.isNotEmpty()) BrandColor else SubtleText,
                                modifier = Modifier.size(20.dp),
                            )
                        },
                        isError = nameError != null,
                        errorText = nameError,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next,
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                    )

                    Spacer(Modifier.height(14.dp))

                    AuthTextField(
                        value = email,
                        onValueChange = { email = it; emailError = null; viewModel.clearError() },
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
                        onValueChange = { password = it; passwordError = null; viewModel.clearError() },
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
                            imeAction = ImeAction.Next,
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                    )

                    Spacer(Modifier.height(14.dp))

                    AuthTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; confirmPasswordError = null; viewModel.clearError() },
                        label = "Confirm password",
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock, null,
                                tint = if (confirmPassword.isNotEmpty()) {
                                    if (confirmPassword == password) BrandColor else MaterialTheme.colorScheme.error
                                } else SubtleText,
                                modifier = Modifier.size(20.dp),
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff
                                    else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = SubtleText,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        isError = confirmPasswordError != null,
                        errorText = confirmPasswordError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(onDone = { validateAndRegister() }),
                    )

                    Spacer(Modifier.height(28.dp))

                    GradientButton(
                        text = "Create Account",
                        onClick = { validateAndRegister() },
                        modifier = Modifier.fillMaxWidth(),
                        isLoading = isLoading,
                    )

                    Spacer(Modifier.height(20.dp))

                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = SubtleText)) {
                                append("Already have an account?  ")
                            }
                            withStyle(SpanStyle(color = BrandColor, fontWeight = FontWeight.Bold)) {
                                append("Sign In")
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .clickable { onNavigateToLogin() }
                            .padding(vertical = 8.dp),
                    )

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun RegisterPreview() {
    FinPilotTheme {
        @Suppress("ViewModelConstructorInComposable")
        RegisterScreen(
            viewModel = AuthViewModel(),
            onNavigateToLogin = {},
            onRegisterSuccess = {}
        )
    }
}