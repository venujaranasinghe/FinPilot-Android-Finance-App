package com.bpeople.finpilot.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bpeople.finpilot.R
import com.bpeople.finpilot.ui.theme.BrandColor
import com.bpeople.finpilot.ui.theme.DeepText
import com.bpeople.finpilot.ui.theme.GradientEnd
import com.bpeople.finpilot.ui.theme.GradientStart
import com.bpeople.finpilot.ui.theme.SubtleText

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

        // Soft Orange — top-left
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFDBA74).copy(alpha = 0.4f), Color.Transparent),
                center = Offset(w * 0.1f, h * 0.1f),
                radius = w * 0.7f,
            ),
            radius = w * 0.7f,
            center = Offset(w * 0.1f, h * 0.1f),
        )
        // Amber — top-right
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFCD34D).copy(alpha = 0.3f), Color.Transparent),
                center = Offset(w * 0.9f, h * 0.05f),
                radius = w * 0.6f,
            ),
            radius = w * 0.6f,
            center = Offset(w * 0.9f, h * 0.05f),
        )
        // Warm Peach — bottom-left
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFDA4AF).copy(alpha = 0.3f), Color.Transparent),
                center = Offset(w * 0.15f, h * 0.9f),
                radius = w * 0.6f,
            ),
            radius = w * 0.6f,
            center = Offset(w * 0.15f, h * 0.9f),
        )
        // Light Orange — center-right
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

/**
 * Renders the application logo using pngegg.png
 */
@Composable
internal fun AppLogo(size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 20.dp,
                shape = CircleShape,
                spotColor = BrandColor.copy(alpha = 0.2f),
            )
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.6f))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.9f), Color.White.copy(alpha = 0.3f))
                ),
                shape = CircleShape,
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Use a safe check for preview to avoid crashes with certain bitmap resources that may fail to decode in layoutlib
        if (LocalInspectionMode.current) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BrandColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Logo",
                    color = BrandColor,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Image(
                painter = painterResource(id = R.drawable.pngegg),
                contentDescription = "App Logo",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
internal fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 32.dp,
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
                elevation = 30.dp,
                shape = shape,
                spotColor = BrandColor.copy(alpha = 0.15f),
                ambientColor = Color.Black.copy(alpha = 0.05f),
            )
            .clip(shape)
            .background(Color.White.copy(alpha = 0.82f))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
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
            unfocusedBorderColor = Color(0xFFE5E7EB),
            focusedLabelColor = BrandColor,
            unfocusedLabelColor = SubtleText,
            cursorColor = BrandColor,
            focusedTextColor = DeepText,
            unfocusedTextColor = DeepText,
            focusedContainerColor = Color.White.copy(alpha = 0.9f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.5f),
            errorBorderColor = MaterialTheme.colorScheme.error,
            errorLabelColor = MaterialTheme.colorScheme.error,
            errorCursorColor = MaterialTheme.colorScheme.error,
            errorContainerColor = Color(0xFFFEF2F2),
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
