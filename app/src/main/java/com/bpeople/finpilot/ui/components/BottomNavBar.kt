package com.bpeople.finpilot.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.CompareArrows
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.kashif_e.backdrop.drawBackdrop
import com.kashif_e.backdrop.backdrops.layerBackdrop
import com.kashif_e.backdrop.backdrops.rememberLayerBackdrop
import com.kashif_e.backdrop.effects.blur
import com.kashif_e.backdrop.effects.colorControls
import com.kashif_e.backdrop.effects.opacity
import com.kashif_e.backdrop.highlight.Highlight
import com.kashif_e.backdrop.shadow.InnerShadow
import com.kashif_e.backdrop.shadow.Shadow
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

// ─── Palette ────────────────────────────────────────────────────────────────

private val OrangePrimary = GlassTheme.Orange
private val OrangeGlow    = GlassTheme.OrangeLight
private val GlassWhite    = GlassTheme.GlassSurface
private val GlassBorder   = GlassTheme.GlassBorderLight
private val InactiveIcon  = GlassTheme.TextSecondary

// ─── Nav model ───────────────────────────────────────────────────────────────

enum class NavTab {
    HOME, TRANSACTIONS, GOALS, PROFILE,
    DASHBOARD, INCOME, EXPENSE,
}

private fun NavTab.resolved(): NavTab = when (this) {
    NavTab.DASHBOARD              -> NavTab.HOME
    NavTab.INCOME, NavTab.EXPENSE -> NavTab.TRANSACTIONS
    else                          -> this
}

private data class NavItem(
    val tab:  NavTab,
    val label: String,
    val icon: ImageVector,
)

private val navItems = listOf(
    NavItem(NavTab.HOME,         "Home",         Icons.Rounded.Home),
    NavItem(NavTab.TRANSACTIONS, "Transactions", Icons.AutoMirrored.Rounded.CompareArrows),
    NavItem(NavTab.GOALS,        "Goals",        Icons.Rounded.EmojiEvents),
    NavItem(NavTab.PROFILE,      "Profile",      Icons.Rounded.Person),
)

// ─── Swipe velocity threshold ────────────────────────────────────────────────

private const val FLING_VELOCITY_THRESHOLD = 500f  // px/s

// ─── Public composable ───────────────────────────────────────────────────────

@Composable
fun FinPilotBottomNavBar(
    modifier:                 Modifier = Modifier,
    currentTab:               NavTab,
    onNavigateToDashboard:    () -> Unit = {},
    onNavigateToIncome:       () -> Unit = {},
    onNavigateToExpense:      () -> Unit = {},
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToGoals:        () -> Unit = {},
    onNavigateToProfile:      () -> Unit = {},
) {
    val active = currentTab.resolved()

    // Map resolved active tab → index
    val activeIndex = navItems.indexOfFirst { it.tab == active }.coerceAtLeast(0)

    // Internal swipe-driven index (starts synced with prop)
    var swipeIndex by remember(activeIndex) { mutableIntStateOf(activeIndex) }

    // Raw drag accumulator (px) used for real-time pill tracking
    var dragAccumPx by remember { mutableFloatStateOf(0f) }

    // Velocity tracker for fling detection
    val velocityTracker = remember { VelocityTracker() }

    val backdrop = rememberLayerBackdrop()
    val blurRadiusPx = with(LocalDensity.current) { 20.dp.toPx() }

    // Navigate callback dispatcher
    fun navigateTo(index: Int) {
        when (navItems.getOrNull(index)?.tab) {
            NavTab.HOME         -> onNavigateToDashboard()
            NavTab.TRANSACTIONS -> onNavigateToTransactions()
            NavTab.GOALS        -> onNavigateToGoals()
            NavTab.PROFILE      -> onNavigateToProfile()
            else                -> {}
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
    ) {
        // ── Backdrop layer (captures gradient for sampling) ────────────────
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            GlassTheme.BgStart.copy(alpha = 0.08f),
                            GlassTheme.BgMid.copy(alpha = 0.12f),
                            GlassTheme.BgEnd.copy(alpha = 0.15f),
                        ),
                    ),
                )
                .layerBackdrop(backdrop),
        )

        // ── Glass shell ───────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { RoundedCornerShape(32.dp) },
                    effects = {
                        blur(blurRadiusPx) // crisp frosted blur
                        colorControls(
                            brightness = 0.06f,
                            contrast = 1.12f,
                            saturation = 1.0f,
                        )
                        opacity(0.1f) // beautifully balanced frosted glass transparency
                    },
                    highlight = { Highlight.Ambient },
                    shadow = { Shadow(radius = 6.dp) },
                    innerShadow = { InnerShadow(radius = 3.dp) },
                )
                .border(
                    width = 1.dp,
                    color = Color(0x3DFFFFFF), // elegant translucent white border highlight
                    shape = RoundedCornerShape(32.dp),
                ),
        )

        // ── Foreground content ────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(GlassWhite.copy(alpha = 0.4f))
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            dragAccumPx = 0f
                            velocityTracker.resetTracking()
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            dragAccumPx += dragAmount
                            velocityTracker.addPosition(
                                change.uptimeMillis,
                                change.position,
                            )
                        },
                        onDragEnd = {
                            val tabWidthPx = size.width.toFloat() / navItems.size
                            val velocity = velocityTracker.calculateVelocity().x

                            val newIndex = when {
                                // Fast fling rightward
                                velocity > FLING_VELOCITY_THRESHOLD ->
                                    (swipeIndex + 1).coerceIn(0, navItems.lastIndex)
                                // Fast fling leftward
                                velocity < -FLING_VELOCITY_THRESHOLD ->
                                    (swipeIndex - 1).coerceIn(0, navItems.lastIndex)
                                // Drag exceeded one slot width
                                dragAccumPx.absoluteValue >= tabWidthPx -> {
                                    val steps = (dragAccumPx / tabWidthPx).toInt()
                                    (swipeIndex + steps).coerceIn(0, navItems.lastIndex)
                                }
                                // Not enough drag — stay
                                else -> swipeIndex
                            }

                            swipeIndex = newIndex
                            dragAccumPx = 0f
                            navigateTo(newIndex)
                        },
                        onDragCancel = {
                            dragAccumPx = 0f
                        },
                    )
                },
        ) {
            // ── Icons row ────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                navItems.forEachIndexed { index, item ->
                    val isActive = index == swipeIndex

                    GlassNavBarItem(
                        item     = item,
                        isActive = isActive,
                        onClick  = {
                            swipeIndex = index
                            navigateTo(index)
                        },
                    )
                }
            }
        }

        // ── Highlight shimmer ────────────────────────────────────────────
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.verticalGradient(
                        0f    to GlassWhite.copy(alpha = 0.18f),
                        0.45f to GlassWhite.copy(alpha = 0.04f),
                        1f    to Color.Transparent,
                    ),
                ),
        )
    }
}

// ─── Individual tab item (icons only, no labels) ─────────────────────────────

@Composable
private fun GlassNavBarItem(
    item:     NavItem,
    isActive: Boolean,
    onClick:  () -> Unit,
) {
    val iconTint by animateColorAsState(
        targetValue   = if (isActive) OrangeGlow else InactiveIcon,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label         = "tint_${item.tab}",
    )
    val scale by animateFloatAsState(
        targetValue   = if (isActive) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium,
        ),
        label = "scale_${item.tab}",
    )
    Column(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector        = item.icon,
            contentDescription = item.label,
            tint               = iconTint,
            modifier           = Modifier
                .size(24.dp),
        )

        // ── Dot indicator ────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(
                    if (isActive) OrangePrimary else Color.Transparent,
                ),
        )
    }
}