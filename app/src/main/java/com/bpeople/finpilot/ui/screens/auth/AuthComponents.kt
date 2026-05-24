package com.bpeople.finpilot.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import com.bpeople.finpilot.ui.theme.LocalAppDarkTheme
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bpeople.finpilot.R
import com.bpeople.finpilot.ui.theme.BrandColor
import com.bpeople.finpilot.ui.theme.DarkGradientEnd
import com.bpeople.finpilot.ui.theme.DarkGradientStart
import com.bpeople.finpilot.ui.theme.DeepText
import com.bpeople.finpilot.ui.theme.GradientEnd
import com.bpeople.finpilot.ui.theme.GradientStart
import com.bpeople.finpilot.ui.theme.SubtleText

@Composable
internal fun AuthBackground(content: @Composable BoxScope.() -> Unit) {
    val isDark = LocalAppDarkTheme.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = if (isDark) listOf(DarkGradientStart, DarkGradientEnd)
                             else listOf(GradientStart, GradientEnd),
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
    val isDark = LocalAppDarkTheme.current
    androidx.compose.foundation.Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        if (isDark) {
            // Dark theme — subtle orange glow on black
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFF97316).copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(w * 0.1f, h * 0.1f),
                    radius = w * 0.7f,
                ),
                radius = w * 0.7f,
                center = Offset(w * 0.1f, h * 0.1f),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFB923C).copy(alpha = 0.08f), Color.Transparent),
                    center = Offset(w * 0.9f, h * 0.05f),
                    radius = w * 0.6f,
                ),
                radius = w * 0.6f,
                center = Offset(w * 0.9f, h * 0.05f),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFF97316).copy(alpha = 0.06f), Color.Transparent),
                    center = Offset(w * 0.15f, h * 0.9f),
                    radius = w * 0.6f,
                ),
                radius = w * 0.6f,
                center = Offset(w * 0.15f, h * 0.9f),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFDBA74).copy(alpha = 0.10f), Color.Transparent),
                    center = Offset(w * 0.85f, h * 0.65f),
                    radius = w * 0.5f,
                ),
                radius = w * 0.5f,
                center = Offset(w * 0.85f, h * 0.65f),
            )
        } else {
            // Light theme — original warm blobs
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFDBA74).copy(alpha = 0.4f), Color.Transparent),
                    center = Offset(w * 0.1f, h * 0.1f),
                    radius = w * 0.7f,
                ),
                radius = w * 0.7f,
                center = Offset(w * 0.1f, h * 0.1f),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFCD34D).copy(alpha = 0.3f), Color.Transparent),
                    center = Offset(w * 0.9f, h * 0.05f),
                    radius = w * 0.6f,
                ),
                radius = w * 0.6f,
                center = Offset(w * 0.9f, h * 0.05f),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFDA4AF).copy(alpha = 0.3f), Color.Transparent),
                    center = Offset(w * 0.15f, h * 0.9f),
                    radius = w * 0.6f,
                ),
                radius = w * 0.6f,
                center = Offset(w * 0.15f, h * 0.9f),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFEDD5).copy(alpha = 0.5f), Color.Transparent),
                    center = Offset(w * 0.85f, h * 0.65f),
                    radius = w * 0.5f,
                ),
                radius = w * 0.5f,
                center = Offset(w * 0.85f, h * 0.65f),
            )
        }
    }
}

/**
 * Renders the application logo using the orange vector asset.
 */
@Composable
internal fun AppLogo(size: Dp, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.finpilot_logo),
        contentDescription = "App Logo",
        modifier = modifier.size(size)
    )
}

@Composable
internal fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 32.dp,
    topCornersOnly: Boolean = false,
    content: @Composable () -> Unit,
) {
    val isDark = LocalAppDarkTheme.current
    val shape = if (topCornersOnly)
        RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius)
    else
        RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .shadow(
                elevation = 30.dp,
                shape = shape,
                spotColor = BrandColor.copy(alpha = if (isDark) 0.25f else 0.15f),
                ambientColor = if (isDark) BrandColor.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f),
            )
            .clip(shape)
            .background(
                if (isDark) Color(0xFF161616).copy(alpha = 0.95f)
                else Color.White.copy(alpha = 0.82f)
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    if (isDark)
                        listOf(Color(0xFFF97316).copy(alpha = 0.25f), Color(0xFFF97316).copy(alpha = 0.05f))
                    else
                        listOf(Color.White.copy(alpha = 0.9f), Color.White.copy(alpha = 0.3f))
                ),
                shape = shape,
            )
    ) {
        content()
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
    val isDark = LocalAppDarkTheme.current
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
            unfocusedBorderColor = if (isDark) Color(0xFF2E2E2E) else Color(0xFFE5E7EB),
            focusedLabelColor = BrandColor,
            unfocusedLabelColor = SubtleText,
            cursorColor = BrandColor,
            focusedTextColor = if (isDark) Color(0xFFF5F5F5) else DeepText,
            unfocusedTextColor = if (isDark) Color(0xFFD1D1D1) else DeepText,
            focusedContainerColor = if (isDark) Color(0xFF1A1A1A) else Color.White.copy(alpha = 0.9f),
            unfocusedContainerColor = if (isDark) Color(0xFF111111) else Color.White.copy(alpha = 0.5f),
            errorBorderColor = MaterialTheme.colorScheme.error,
            errorLabelColor = MaterialTheme.colorScheme.error,
            errorCursorColor = MaterialTheme.colorScheme.error,
            errorContainerColor = if (isDark) Color(0xFF3B0000) else Color(0xFFFEF2F2),
        ),
    )
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
            .height(56.dp)
            .shadow(
                elevation = if (!isLoading) 12.dp else 0.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = BrandColor.copy(alpha = 0.4f),
            )
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (!isLoading) {
                    Brush.linearGradient(
                        colors = listOf(BrandColor, Color(0xFFFB923C))
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFD1D5DB), Color(0xFF9CA3AF))
                    )
                }
            )
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
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 3.dp,
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

@Composable
internal fun GoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    val isDark = LocalAppDarkTheme.current
    Box(
        modifier = modifier
            .height(56.dp)
            .border(
                width = 1.5.dp,
                color = if (isDark) Color(0xFF2E2E2E) else Color(0xFFE5E7EB),
                shape = RoundedCornerShape(16.dp),
            )
            .clip(RoundedCornerShape(16.dp))
            .background(if (isDark) Color(0xFF1A1A1A) else Color.White)
            .clickable(
                enabled = !isLoading,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = BrandColor,
                strokeWidth = 3.dp,
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_google),
                    contentDescription = "Google Logo",
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(24.dp)
                )
                Text(
                    text = "Sign in with Google",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Color(0xFFF5F5F5) else DeepText,
                    ),
                )
            }
        }
    }
}
