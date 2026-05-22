package com.bpeople.finpilot.ui.theme

import androidx.compose.ui.graphics.Color

// ── Light scheme ──────────────────────────────────────────────────────────────
val md_light_primary = Color(0xFFF97316) // Vibrant Orange
val md_light_onPrimary = Color(0xFFFFFFFF)
val md_light_primaryContainer = Color(0xFFFFEDD5)
val md_light_onPrimaryContainer = Color(0xFF7C2800)
val md_light_secondary = Color(0xFFF97316) // Match primary for consistency in this theme
val md_light_onSecondary = Color(0xFFFFFFFF)
val md_light_secondaryContainer = Color(0xFFFFF7ED)
val md_light_onSecondaryContainer = Color(0xFF431407)
val md_light_tertiary = Color(0xFFFB923C)
val md_light_onTertiary = Color(0xFFFFFFFF)
val md_light_tertiaryContainer = Color(0xFFFFEDD5)
val md_light_onTertiaryContainer = Color(0xFF7C2800)
val md_light_error = Color(0xFFEF4444)
val md_light_onError = Color(0xFFFFFFFF)
val md_light_errorContainer = Color(0xFFFFE4E4)
val md_light_onErrorContainer = Color(0xFF7F1D1D)
val md_light_background = Color(0xFFFFFFFF) // Pure White Background
val md_light_onBackground = Color(0xFF1F2937)
val md_light_surface = Color(0xFFFFFFFF)
val md_light_onSurface = Color(0xFF1F2937)
val md_light_surfaceVariant = Color(0xFFF9FAFB)
val md_light_onSurfaceVariant = Color(0xFF4B5563)
val md_light_outline = Color(0xFFE5E7EB)
val md_light_outlineVariant = Color(0xFFF3F4F6)
val md_light_scrim = Color(0xFF000000)
val md_light_inverseSurface = Color(0xFF111827)
val md_light_inverseOnSurface = Color(0xFFF9FAFB)
val md_light_inversePrimary = Color(0xFFFB923C)
val md_light_surfaceTint = Color(0xFFF97316)

// ── Dark scheme (Black + Orange) ─────────────────────────────────────────────
val md_dark_primary = Color(0xFFF97316)              // Vibrant Orange
val md_dark_onPrimary = Color(0xFF000000)             // Black text on orange
val md_dark_primaryContainer = Color(0xFF200F00)      // Very dark orange-black
val md_dark_onPrimaryContainer = Color(0xFFFFB870)    // Warm orange in container
val md_dark_secondary = Color(0xFFFB923C)             // Lighter orange
val md_dark_onSecondary = Color(0xFF1A0500)           // Very dark on secondary
val md_dark_secondaryContainer = Color(0xFF1A0800)    // Dark orange container
val md_dark_onSecondaryContainer = Color(0xFFFFCB99)  // Light orange text
val md_dark_tertiary = Color(0xFFFDBA74)              // Amber orange
val md_dark_onTertiary = Color(0xFF1A0500)            // Dark on tertiary
val md_dark_tertiaryContainer = Color(0xFF1A0800)     // Dark container
val md_dark_onTertiaryContainer = Color(0xFFFFDDBB)   // Light text
val md_dark_error = Color(0xFFFF6B6B)                 // Soft red
val md_dark_onError = Color(0xFF000000)               // Black on error
val md_dark_errorContainer = Color(0xFF3B0000)        // Very dark red
val md_dark_onErrorContainer = Color(0xFFFFCDD2)      // Light pink text
val md_dark_background = Color(0xFF000000)            // Pure black
val md_dark_onBackground = Color(0xFFFFFFFF)          // White text
val md_dark_surface = Color(0xFF0D0D0D)               // Near-black cards
val md_dark_onSurface = Color(0xFFF5F5F5)             // Near-white text
val md_dark_surfaceVariant = Color(0xFF1A1A1A)        // Slightly elevated panels
val md_dark_onSurfaceVariant = Color(0xFFAAAAAA)      // Muted text on variants
val md_dark_outline = Color(0xFF2E2E2E)               // Dark borders
val md_dark_outlineVariant = Color(0xFF1F1F1F)        // Subtle dividers
val md_dark_scrim = Color(0xFF000000)                 // Black scrim
val md_dark_inverseSurface = Color(0xFFF5F5F5)        // Light inverse surface
val md_dark_inverseOnSurface = Color(0xFF000000)      // Black on inverse
val md_dark_inversePrimary = Color(0xFFF97316)        // Orange inverse
val md_dark_surfaceTint = Color(0xFFF97316)           // Orange tint

// ── Finance semantic colours ──────────────────────────────────────────────────
val IncomeGreen = Color(0xFF10B981)
val ExpenseRed = Color(0xFFEF4444)
val SavingsBlue = Color(0xFF3B82F6)
val WarningAmber = Color(0xFFF59E0B)
val NeutralGrey = Color(0xFF6B7280)

// ── Glass / gradient UI constants ─────────────────────────────────────────────
val GradientStart = Color(0xFFFFF7ED)     // Very light orange tint (light theme)
val GradientEnd = Color(0xFFFFFFFF)       // To White (light theme)
val DarkGradientStart = Color(0xFF0A0500) // Very dark orange-tinted black (dark theme)
val DarkGradientEnd = Color(0xFF000000)   // Pure black (dark theme)
val BrandColor = Color(0xFFF97316)
val PrimaryIndigo = Color(0xFF6366F1)
val PrimaryTeal = Color(0xFF14B8A6)
val DeepText = Color(0xFF111827)
val SubtleText = Color(0xFF6B7280)

// ── Enhanced Dark Theme Colors (matching Dashboard & Transactions) ────────────
val DarkSurface = Color(0xFF1C1C1E)
val DarkSurfaceVariant = Color(0xFF2C2C2E)
val DarkBackground = Color(0xFF000000)
val DarkBorder = Color(0xFF38383A)
val DarkTextPrimary = Color(0xFFFFFFFF)
val DarkTextSecondary = Color(0xFFEBEBF5).copy(alpha = 0.6f)
val DarkTextHint = Color(0xFF636366)
val DarkGlassBg = Color(0xFF1C1C1E).copy(alpha = 0.8f)
val DarkGlassSurface = Color(0xFF2C2C2E).copy(alpha = 0.6f)
val DarkGlassBorder = Color(0xFF38383A)
val DarkGlassBorderLight = Color(0xFF38383A).copy(alpha = 0.5f)

// ── Goal-specific colors ──────────────────────────────────────────────────────
val GoalOrangeAccent = Color(0xFFFF6B00)
val GoalOrangeSoft = Color(0xFFFF8F3C)
val GoalStatusGreen = Color(0xFF10B981)
val GoalStatusAmber = Color(0xFFF59E0B)
val GoalStatusRed = Color(0xFFEF4444)