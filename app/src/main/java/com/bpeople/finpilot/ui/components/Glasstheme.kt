package com.bpeople.finpilot.ui.components

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import com.bpeople.finpilot.ui.theme.DarkBackground
import com.bpeople.finpilot.ui.theme.DarkGlassBg
import com.bpeople.finpilot.ui.theme.DarkGlassBorder
import com.bpeople.finpilot.ui.theme.DarkGlassBorderLight
import com.bpeople.finpilot.ui.theme.DarkGlassSurface
import com.bpeople.finpilot.ui.theme.DarkSurface
import com.bpeople.finpilot.ui.theme.DarkTextHint
import com.bpeople.finpilot.ui.theme.DarkTextPrimary
import com.bpeople.finpilot.ui.theme.DarkTextSecondary

// ── Glass design tokens ───────────────────────────────────────────────────────
object GlassTheme {

    // Background gradient stops
    val BgStart   = Color(0xFFF9FAFB)
    val BgMid     = Color(0xFFFFFFFF)
    val BgEnd     = Color(0xFFF3F4F6)

    // Orb accents (used in header decorations)
    val OrbOrange = Color(0x2EFF6B00)
    val OrbPurple = Color(0x24534AB7)
    val OrbGreen  = Color(0x221D9E75)

    // Brand
    val Orange      = Color(0xFFFF6B00)
    val OrangeLight = Color(0xFFFF8C42)
    val OrangeDim   = Color(0x1AFF6B00)
    val OrangeGlow  = Color(0x40FF6B00)

    // Glass surfaces (light glass on white)
    val GlassBg      = Color(0xCCFFFFFF)   // ~80% white
    val GlassSurface = Color(0xE6FFFFFF)   // ~90% white
    val GlassHover   = Color(0xF2FFFFFF)   // ~95% white
    val GlassBorder  = Color(0x1A0F172A)   // ~10% slate
    val GlassBorderLight = Color(0x0F0F172A)

    // Text
    val TextPrimary   = Color(0xFF0F172A)
    val TextSecondary = Color(0xB30F172A)
    val TextHint      = Color(0x800F172A)

    // Semantic
    val Danger  = Color(0xFFE11D48)
    val Success = Color(0xFF10B981)
    val Amber   = Color(0xFFF59E0B)
    val AmberDim = Color(0x14F59E0B)
    val AmberBorder = Color(0x33F59E0B)

    // Bottom nav
    val NavBg = Color(0xE6FFFFFF)

    // Category colours
    val CatFood          = Color(0xFFFF6B00)
    val CatTransport     = Color(0xFF3B82F6)
    val CatHousing       = Color(0xFF8B5CF6)
    val CatSubscriptions = Color(0xFF10B981)
    val CatEntertainment = Color(0xFFF59E0B)
    val CatHealth        = Color(0xFFEF4444)
    val CatOther         = Color(0xFF9CA3AF)

    fun categoryColor(cat: String): Color = when (cat) {
        "Food"          -> CatFood
        "Transport"     -> CatTransport
        "Housing"       -> CatHousing
        "Subscriptions" -> CatSubscriptions
        "Entertainment" -> CatEntertainment
        "Health"        -> CatHealth
        else            -> CatOther
    }
}

// ── Theme-aware GlassTheme tokens ─────────────────────────────────────────────

/**
 * Holds all GlassTheme design tokens. Theme-sensitive values differ between
 * [LightGlassThemeColors] and [DarkGlassThemeColors]; brand/semantic/category
 * colours use the same values in both themes.
 *
 * Consume via [LocalCurrentGlassTheme].current inside composables.
 */
class GlassThemeColors(
    // ── Theme-sensitive ──────────────────────────────────────────────────────
    val BgStart: Color,
    val BgMid: Color,
    val BgEnd: Color,
    val GlassBg: Color,
    val GlassSurface: Color,
    val GlassHover: Color,
    val GlassBorder: Color,
    val GlassBorderLight: Color,
    val TextPrimary: Color,
    val TextSecondary: Color,
    val TextHint: Color,
    val NavBg: Color,
    // ── Non-theme-sensitive (same in light & dark) ────────────────────────────
    val OrbOrange: Color = Color(0x2EFF6B00),
    val OrbPurple: Color = Color(0x24534AB7),
    val OrbGreen: Color = Color(0x221D9E75),
    val Orange: Color = Color(0xFFFF6B00),
    val OrangeLight: Color = Color(0xFFFF8C42),
    val OrangeDim: Color = Color(0x1AFF6B00),
    val OrangeGlow: Color = Color(0x40FF6B00),
    val Danger: Color = Color(0xFFE11D48),
    val Success: Color = Color(0xFF10B981),
    val Amber: Color = Color(0xFFF59E0B),
    val AmberDim: Color = Color(0x14F59E0B),
    val AmberBorder: Color = Color(0x33F59E0B),
    val CatFood: Color = Color(0xFFFF6B00),
    val CatTransport: Color = Color(0xFF3B82F6),
    val CatHousing: Color = Color(0xFF8B5CF6),
    val CatSubscriptions: Color = Color(0xFF10B981),
    val CatEntertainment: Color = Color(0xFFF59E0B),
    val CatHealth: Color = Color(0xFFEF4444),
    val CatOther: Color = Color(0xFF9CA3AF),
) {
    fun categoryColor(cat: String): Color = when (cat) {
        "Food"          -> CatFood
        "Transport"     -> CatTransport
        "Housing"       -> CatHousing
        "Subscriptions" -> CatSubscriptions
        "Entertainment" -> CatEntertainment
        "Health"        -> CatHealth
        else            -> CatOther
    }
}

val LightGlassThemeColors = GlassThemeColors(
    BgStart          = Color(0xFFF9FAFB),
    BgMid            = Color(0xFFFFFFFF),
    BgEnd            = Color(0xFFF3F4F6),
    GlassBg          = Color(0xCCFFFFFF),
    GlassSurface     = Color(0xE6FFFFFF),
    GlassHover       = Color(0xF2FFFFFF),
    GlassBorder      = Color(0x1A0F172A),
    GlassBorderLight = Color(0x0F0F172A),
    TextPrimary      = Color(0xFF0F172A),
    TextSecondary    = Color(0xB30F172A),
    TextHint         = Color(0x800F172A),
    NavBg            = Color(0xE6FFFFFF),
)

val DarkGlassThemeColors = GlassThemeColors(
    BgStart          = DarkBackground,
    BgMid            = DarkSurface,
    BgEnd            = DarkBackground,
    GlassBg          = DarkGlassBg,
    GlassSurface     = DarkGlassSurface,
    GlassHover       = DarkGlassSurface,
    GlassBorder      = DarkGlassBorder,
    GlassBorderLight = DarkGlassBorderLight,
    TextPrimary      = DarkTextPrimary,
    TextSecondary    = DarkTextSecondary,
    TextHint         = DarkTextHint,
    NavBg            = DarkSurface,
)

/** Provides the active [GlassThemeColors] down the composition tree. */
val LocalCurrentGlassTheme = compositionLocalOf<GlassThemeColors> { LightGlassThemeColors }