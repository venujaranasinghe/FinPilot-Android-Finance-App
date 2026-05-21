package com.bpeople.finpilot.ui.screens.notifications

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bpeople.finpilot.data.model.NotificationItem
import com.bpeople.finpilot.data.model.NotificationPriority
import com.bpeople.finpilot.data.model.NotificationType
import com.bpeople.finpilot.data.repository.NotificationsRepository
import com.bpeople.finpilot.ui.components.FinPilotBottomNavBar
import com.bpeople.finpilot.ui.components.NavTab
import com.bpeople.finpilot.ui.components.NotificationCard
import com.bpeople.finpilot.ui.components.NotificationEmptyState
import com.bpeople.finpilot.ui.components.NotificationFilterChips
import com.bpeople.finpilot.ui.components.NotificationPreferencesSection
import com.bpeople.finpilot.ui.components.NotificationSummaryCard
import com.bpeople.finpilot.ui.components.NotificationsDisabledBanner
import com.bpeople.finpilot.ui.theme.FinPilotTheme
import kotlinx.coroutines.launch

// ── Private colour constants ──────────────────────────────────────────────────

private val ScreenOrange     = Color(0xFFF97316)
private val ScreenOrangeGlow = Color(0xFFFF8C42)
private val ScreenAmber      = Color(0xFFF59E0B)
private val ScreenRed        = Color(0xFFEF4444)
private val ScreenTeal       = Color(0xFF14B8A6)
private val ScreenIndigo     = Color(0xFF6366F1)

// ── Entry point — with ViewModel ─────────────────────────────────────────────

/**
 * Composable entry point for the Notifications screen that wires the
 * [NotificationsViewModel] to [NotificationsScreenContent].
 *
 * This thin wrapper keeps the ViewModel out of [NotificationsScreenContent] so
 * the content function can be tested and previewed with plain state.
 *
 * @param viewModel            Hilt-provided [NotificationsViewModel].
 * @param onNavigateBack       Called when the user taps the back arrow.
 * @param onNavigateToDashboard Called when the bottom nav HOME tab is tapped.
 * @param onNavigateToIncome   Called when the bottom nav TRANSACTIONS tab is tapped.
 * @param onNavigateToGoals    Called when the bottom nav GOALS tab is tapped.
 * @param onNavigateToProfile  Called when the bottom nav PROFILE tab is tapped.
 */
@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToIncome: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()

    NotificationsScreenContent(
        state                  = state,
        onNavigateBack         = onNavigateBack,
        onNavigateToDashboard  = onNavigateToDashboard,
        onNavigateToIncome     = onNavigateToIncome,
        onNavigateToGoals      = onNavigateToGoals,
        onNavigateToProfile    = onNavigateToProfile,
        onDismiss              = viewModel::dismissNotification,
        onDismissAll           = viewModel::dismissAll,
        onMarkRead             = viewModel::markNotificationRead,
        onMarkAllRead          = viewModel::markAllRead,
        onFilterSelected       = viewModel::setActiveFilter,
        onSalaryToggle         = viewModel::setSalaryReminderEnabled,
        onWeeklyToggle         = viewModel::setWeeklySummaryEnabled,
        onMilestoneToggle      = viewModel::setGoalMilestoneEnabled,
        onBudgetToggle         = viewModel::setBudgetOverspendEnabled,
        onBudgetThresholdInput = viewModel::updateBudgetThresholdInput,
        onBudgetThresholdDone  = viewModel::commitBudgetThreshold,
    )
}

// ── Pure content composable ───────────────────────────────────────────────────

/**
 * Stateless, testable content layer for the Notifications screen.
 *
 * Layout layers (back to front):
 *  1. Vertical gradient background matching the app's global glass theme.
 *  2. Three ambient radial glow blobs drawn on a [Canvas].
 *  3. [LazyColumn] with all notification sections.
 *  4. [FinPilotBottomNavBar] anchored to [Alignment.BottomCenter].
 *
 * Sections (in scroll order):
 *  - [NotificationsTopBar] — title, back button, mark-all-read action
 *  - Accent divider
 *  - [NotificationSummaryCard] — totals: all / unread / urgent / milestones
 *  - [NotificationsDisabledBanner] — shown when every category is disabled
 *  - [NotificationFilterChips] — horizontal type-filter strip
 *  - Notification list — one [NotificationCard] per item
 *  - [NotificationEmptyState] — shown when no notifications match
 *  - [NotificationPreferencesSection] — toggles + budget threshold
 *  - Spacer for bottom-nav clearance
 */
@Composable
fun NotificationsScreenContent(
    state: NotificationsViewModel.NotificationsUiState,
    onNavigateBack: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToIncome: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onDismiss: (String) -> Unit = {},
    onDismissAll: () -> Unit = {},
    onMarkRead: (String) -> Unit = {},
    onMarkAllRead: () -> Unit = {},
    onFilterSelected: (NotificationType?) -> Unit = {},
    onSalaryToggle: (Boolean) -> Unit = {},
    onWeeklyToggle: (Boolean) -> Unit = {},
    onMilestoneToggle: (Boolean) -> Unit = {},
    onBudgetToggle: (Boolean) -> Unit = {},
    onBudgetThresholdInput: (String) -> Unit = {},
    onBudgetThresholdDone: () -> Unit = {},
) {
    val isDark = isSystemInDarkTheme()
    val scope  = rememberCoroutineScope()
    val snackbarHost = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    // Derived: scroll elevation for header shadow
    val isScrolled by remember { derivedStateOf { listState.firstVisibleItemScrollOffset > 0 || listState.firstVisibleItemIndex > 0 } }

    // ── Background gradient ───────────────────────────────────────────────────
    val bgGradient = if (isDark) {
        Brush.verticalGradient(listOf(Color(0xFF0A0500), Color(0xFF000000), Color(0xFF050010)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFFFF7ED), Color(0xFFF5F3FF), Color(0xFFFFFFFF)))
    }

    // ── Derived counts for summary card and filter chips ─────────────────────
    val totalCount     = state.allNotifications.size
    val unreadCount    = state.unreadCount
    val urgentCount    = state.allNotifications.count { it.priority == NotificationPriority.URGENT }
    val milestoneCount = state.allNotifications.count { it.isGoalMilestone }

    // ── Per-type counts for filter chips ─────────────────────────────────────
    val typeCounts: Map<NotificationType, Int> = state.allNotifications
        .groupingBy { it.type }
        .eachCount()

    // ── All-disabled banner flag ──────────────────────────────────────────────
    val allDisabled = with(state.prefs) {
        !salaryReminderEnabled && !weeklySummaryEnabled &&
        !goalMilestoneEnabled  && !budgetOverspendEnabled
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHost) },
        containerColor = Color.Transparent,
    ) { pv ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGradient),
        ) {
            // ── Ambient glow blobs ────────────────────────────────────────────
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Top-right orange glow
                drawCircle(
                    brush  = Brush.radialGradient(
                        colors = listOf(ScreenOrange.copy(alpha = if (isDark) 0.22f else 0.14f), Color.Transparent),
                        center = Offset(size.width * 0.88f, size.height * 0.06f),
                        radius = 260.dp.toPx(),
                    ),
                    center = Offset(size.width * 0.88f, size.height * 0.06f),
                    radius = 260.dp.toPx(),
                )
                // Mid-left amber glow
                drawCircle(
                    brush  = Brush.radialGradient(
                        colors = listOf(ScreenAmber.copy(alpha = if (isDark) 0.13f else 0.07f), Color.Transparent),
                        center = Offset(size.width * 0.08f, size.height * 0.40f),
                        radius = 210.dp.toPx(),
                    ),
                    center = Offset(size.width * 0.08f, size.height * 0.40f),
                    radius = 210.dp.toPx(),
                )
                // Bottom-center teal glow
                drawCircle(
                    brush  = Brush.radialGradient(
                        colors = listOf(ScreenTeal.copy(alpha = if (isDark) 0.10f else 0.06f), Color.Transparent),
                        center = Offset(size.width * 0.5f, size.height * 0.86f),
                        radius = 200.dp.toPx(),
                    ),
                    center = Offset(size.width * 0.5f, size.height * 0.86f),
                    radius = 200.dp.toPx(),
                )
            }

            // ── Main scrollable content ───────────────────────────────────────
            LazyColumn(
                state         = listState,
                modifier      = Modifier
                    .fillMaxSize()
                    .padding(pv),
                contentPadding = PaddingValues(bottom = 120.dp),
            ) {

                // ─ 1. Top bar ─────────────────────────────────────────────────
                item(key = "topbar") {
                    NotificationsTopBar(
                        unreadCount     = unreadCount,
                        isScrolled      = isScrolled,
                        onNavigateBack  = onNavigateBack,
                        onMarkAllRead   = {
                            onMarkAllRead()
                            scope.launch {
                                snackbarHost.showSnackbar("All notifications marked as read")
                            }
                        },
                        onDismissAll    = {
                            onDismissAll()
                            scope.launch {
                                snackbarHost.showSnackbar("All notifications cleared")
                            }
                        },
                    )
                }

                // ─ 2. Summary card ────────────────────────────────────────────
                item(key = "summary") {
                    NotificationSummaryCard(
                        totalCount     = totalCount,
                        unreadCount    = unreadCount,
                        urgentCount    = urgentCount,
                        milestoneCount = milestoneCount,
                        onMarkAllRead  = onMarkAllRead,
                        modifier       = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }

                // ─ 3. All-disabled banner ─────────────────────────────────────
                item(key = "disabled_banner") {
                    AnimatedVisibility(
                        visible = allDisabled,
                        enter = fadeIn(tween(300)) + slideInVertically { -it / 2 },
                        exit  = fadeOut(tween(300)) + slideOutVertically { -it / 2 },
                    ) {
                        NotificationsDisabledBanner(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }
                }

                // ─ 4. Filter chips ────────────────────────────────────────────
                item(key = "filters") {
                    NotificationFilterChips(
                        activeFilter     = state.activeFilter,
                        counts           = typeCounts,
                        onFilterSelected = onFilterSelected,
                        modifier         = Modifier.padding(horizontal = 16.dp, bottom = 4.dp),
                    )
                }

                // ─ 5. Loading skeleton / empty state ─────────────────────────
                if (state.isLoading) {
                    items(count = 4, key = { "skeleton_$it" }) {
                        NotificationLoadingSkeleton(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        )
                    }
                } else if (state.notifications.isEmpty()) {
                    item(key = "empty") {
                        val headline = if (state.activeFilter != null)
                            "No ${state.activeFilter.name.lowercase().replace('_', ' ')} notifications"
                        else "You're all caught up!"
                        val subtitle = if (state.activeFilter != null)
                            "Try a different filter or wait for new activity."
                        else "New alerts will appear here when your finances trigger a rule."
                        NotificationEmptyState(
                            headline = headline,
                            subtitle = subtitle,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
                        )
                    }
                } else {
                    // ─ 6. Notification cards ──────────────────────────────────
                    itemsIndexed(
                        items = state.notifications,
                        key   = { _, item -> item.id },
                    ) { index, item ->
                        AnimatedVisibility(
                            visible = true,
                            enter   = fadeIn(tween(200, delayMillis = index * 40)) +
                                      slideInVertically(tween(220, delayMillis = index * 40)) { it / 3 },
                        ) {
                            NotificationCard(
                                item       = item,
                                modifier   = Modifier.padding(horizontal = 16.dp, vertical = 5.dp),
                                onDismiss  = onDismiss,
                                onAction   = { /* navigate based on type */ },
                                onMarkRead = onMarkRead,
                            )
                        }
                    }
                }

                // ─ 7. Section divider ─────────────────────────────────────────
                item(key = "divider") {
                    NotificationsSectionDivider(
                        label    = "Notification Settings",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                }

                // ─ 8. Preferences section ─────────────────────────────────────
                item(key = "preferences") {
                    NotificationPreferencesSection(
                        salaryEnabled     = state.prefs.salaryReminderEnabled,
                        weeklyEnabled     = state.prefs.weeklySummaryEnabled,
                        milestoneEnabled  = state.prefs.goalMilestoneEnabled,
                        budgetEnabled     = state.prefs.budgetOverspendEnabled,
                        budgetThreshold   = state.budgetThresholdInput,
                        onSalaryToggle    = onSalaryToggle,
                        onWeeklyToggle    = onWeeklyToggle,
                        onMilestoneToggle = onMilestoneToggle,
                        onBudgetToggle    = onBudgetToggle,
                        onThresholdChange = onBudgetThresholdInput,
                        onThresholdCommit = onBudgetThresholdDone,
                        modifier          = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }

                // ─ 9. Budget stats summary ────────────────────────────────────
                item(key = "budget_stats") {
                    NotificationsBudgetStats(
                        thisMonthExpenses = state.thisMonthExpenses,
                        thisWeekExpenses  = state.thisWeekExpenses,
                        thisWeekIncome    = state.thisWeekIncome,
                        budgetThreshold   = state.prefs.budgetThreshold,
                        modifier          = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }

                // ─ 10. Active goals milestone overview ────────────────────────
                if (state.activeGoals.isNotEmpty()) {
                    item(key = "goals_header") {
                        NotificationsSectionDivider(
                            label    = "Goal Progress Overview",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        )
                    }
                    itemsIndexed(
                        items = state.activeGoals,
                        key   = { _, g -> "goal_${g.goal.id}" },
                    ) { _, goalSummary ->
                        NotificationsGoalProgressRow(
                            summary  = goalSummary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp),
                        )
                    }
                }

                // ─ 11. Bottom spacer ──────────────────────────────────────────
                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // ── Bottom navigation bar overlay ─────────────────────────────────
            FinPilotBottomNavBar(
                currentTab              = NavTab.HOME,
                onNavigateToDashboard   = onNavigateToDashboard,
                onNavigateToTransactions = onNavigateToIncome,
                onNavigateToGoals       = onNavigateToGoals,
                onNavigateToProfile     = onNavigateToProfile,
                modifier                = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

/**
 * Sticky glassmorphism header for the Notifications screen.
 *
 * Shows:
 *  - Back arrow (left)
 *  - "Notifications" title with an optional orange unread badge (center)
 *  - Mark-all-read icon (right)
 *  - Clear-all icon (right, secondary)
 *  - Gradient accent divider along the bottom edge
 *
 * @param unreadCount    Number of unread notifications — drives the live badge.
 * @param isScrolled     When true a slightly more opaque glass fill is applied
 *                       to visually separate the header from the scroll content.
 * @param onNavigateBack Called when the back arrow is tapped.
 * @param onMarkAllRead  Called when the ✓✓ icon is tapped.
 * @param onDismissAll   Called when the sweep icon is tapped.
 */
@Composable
private fun NotificationsTopBar(
    unreadCount: Int,
    isScrolled: Boolean,
    onNavigateBack: () -> Unit,
    onMarkAllRead: () -> Unit,
    onDismissAll: () -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    val fillAlpha = if (isScrolled) {
        if (isDark) 0.20f else 0.15f
    } else {
        if (isDark) 0.10f else 0.08f
    }
    val borderColor = if (isDark) ScreenOrange.copy(alpha = 0.16f) else ScreenOrange.copy(alpha = 0.22f)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ScreenOrange.copy(alpha = fillAlpha))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment   = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // Back button
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Go back",
                    tint               = MaterialTheme.colorScheme.onSurface,
                )
            }

            // Title + unread badge
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector        = Icons.Default.Notifications,
                    contentDescription = null,
                    tint               = ScreenOrange,
                    modifier           = Modifier.size(22.dp),
                )
                Text(
                    text       = "Notifications",
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface,
                )
                // Live unread count badge
                AnimatedContent(
                    targetState = unreadCount,
                    transitionSpec = {
                        slideInVertically { -it } togetherWith slideOutVertically { it }
                    },
                    label = "unread_badge",
                ) { count ->
                    if (count > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(ScreenOrange)
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text       = if (count > 99) "99+" else count.toString(),
                                fontSize   = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color      = Color.White,
                                textAlign  = TextAlign.Center,
                            )
                        }
                    }
                }
            }

            // Action buttons (mark-all-read + clear-all)
            Row {
                IconButton(
                    onClick  = onMarkAllRead,
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector        = Icons.Default.DoneAll,
                        contentDescription = "Mark all as read",
                        tint               = ScreenTeal,
                        modifier           = Modifier.size(20.dp),
                    )
                }
                IconButton(
                    onClick  = onDismissAll,
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector        = Icons.Default.DeleteSweep,
                        contentDescription = "Clear all notifications",
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f),
                        modifier           = Modifier.size(20.dp),
                    )
                }
            }
        }

        // Gradient accent rule
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(ScreenOrange, ScreenOrangeGlow, ScreenAmber, Color.Transparent),
                    ),
                ),
        )
    }
}

// ── Loading skeleton ──────────────────────────────────────────────────────────

/**
 * Shimmer-style placeholder shown while notifications load on first launch.
 *
 * Renders a rounded rectangle that pulses between two alpha values using
 * [rememberInfiniteTransition], matching the shape of a real [NotificationCard].
 */
@Composable
private fun NotificationLoadingSkeleton(modifier: Modifier = Modifier) {
    val isDark  = isSystemInDarkTheme()
    val baseAlpha = if (isDark) 0.08f else 0.06f
    val shimAlpha = if (isDark) 0.18f else 0.13f

    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha by infiniteTransition.animateFloat(
        initialValue  = baseAlpha,
        targetValue   = shimAlpha,
        animationSpec = infiniteRepeatable(
            animation  = tween(900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "skeleton_alpha",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(88.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(ScreenOrange.copy(alpha = alpha))
            .border(0.8.dp, ScreenOrange.copy(alpha = alpha * 1.4f), RoundedCornerShape(18.dp)),
    )
}

// ── Section divider ───────────────────────────────────────────────────────────

/**
 * Labelled horizontal divider separating major sections in the scroll list.
 *
 * Renders a left-aligned section label with a fading horizontal rule extending
 * to the right, styled to match the app's glass theme.
 *
 * @param label    Text displayed to the left of the rule.
 * @param modifier Optional layout modifier.
 */
@Composable
private fun NotificationsSectionDivider(label: String, modifier: Modifier = Modifier) {
    val isDark = isSystemInDarkTheme()
    Row(
        modifier          = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text       = label,
            fontSize   = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f),
            letterSpacing = 0.8.sp,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            ScreenOrange.copy(alpha = if (isDark) 0.35f else 0.25f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
    }
}

// ── Budget stats card ─────────────────────────────────────────────────────────

/**
 * Glassmorphism card displaying three key financial stats used by the
 * notifications engine: this month's spend, this week's spend, and this
 * week's income, alongside the configured budget threshold.
 *
 * A thin progress bar visualises how close the current month's spend is to
 * the threshold; it glows red when over budget.
 *
 * @param thisMonthExpenses Running LKR total of expenses in the current month.
 * @param thisWeekExpenses  Running LKR total of expenses in the last 7 days.
 * @param thisWeekIncome    Running LKR total of income in the last 7 days.
 * @param budgetThreshold   Configured monthly overspend threshold in LKR.
 * @param modifier          Optional layout modifier.
 */
@Composable
private fun NotificationsBudgetStats(
    thisMonthExpenses: Double,
    thisWeekExpenses: Double,
    thisWeekIncome: Double,
    budgetThreshold: Double,
    modifier: Modifier = Modifier,
) {
    val isDark  = isSystemInDarkTheme()
    val isOver  = thisMonthExpenses > budgetThreshold
    val progress = if (budgetThreshold > 0.0) {
        (thisMonthExpenses / budgetThreshold).coerceIn(0.0, 1.5).toFloat()
    } else 1f

    val barColor = when {
        progress >= 1.0f -> ScreenRed
        progress >= 0.75f -> ScreenAmber
        else              -> ScreenTeal
    }

    val animatedProgress by animateFloatAsState(
        targetValue  = progress.coerceIn(0f, 1f),
        animationSpec = tween(1000),
        label        = "budget_progress",
    )

    // Glass card style
    val glassFill   = ScreenOrange.copy(alpha = if (isDark) 0.09f else 0.06f)
    val borderColor = ScreenOrange.copy(alpha = if (isDark) 0.18f else 0.14f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(glassFill)
            .border(0.8.dp, borderColor, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Header
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Text(
                text       = "Budget Snapshot",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurface,
            )
            if (isOver) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ScreenRed.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Text(
                        text       = "⚠️ Over budget",
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = ScreenRed,
                    )
                }
            }
        }

        // Three stat rows
        NotificationStatRow(
            label  = "This month's spend",
            value  = formatLKR(thisMonthExpenses),
            tint   = if (isOver) ScreenRed else MaterialTheme.colorScheme.onSurface,
        )
        NotificationStatRow(
            label  = "This week's spend",
            value  = formatLKR(thisWeekExpenses),
            tint   = MaterialTheme.colorScheme.onSurface,
        )
        NotificationStatRow(
            label  = "This week's income",
            value  = formatLKR(thisWeekIncome),
            tint   = Color(0xFF10B981),
        )
        NotificationStatRow(
            label  = "Budget threshold",
            value  = formatLKR(budgetThreshold),
            tint   = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Progress bar
        val trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text     = "Monthly budget used",
                    fontSize = 11.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                )
                Text(
                    text     = "${(progress * 100).toInt().coerceAtMost(999)}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color    = barColor,
                )
            }
            Canvas(modifier = Modifier.fillMaxWidth().height(8.dp)) {
                val radius = size.height / 2f
                // Track
                drawRoundRect(
                    color        = trackColor,
                    cornerRadius = CornerRadius(radius),
                )
                // Fill
                if (animatedProgress > 0f) {
                    drawRoundRect(
                        brush        = Brush.horizontalGradient(
                            listOf(barColor.copy(alpha = 0.80f), barColor),
                            endX = size.width * animatedProgress,
                        ),
                        size         = Size(size.width * animatedProgress, size.height),
                        cornerRadius = CornerRadius(radius),
                    )
                }
            }
        }
    }
}

/** Single key–value stat row used inside [NotificationsBudgetStats]. */
@Composable
private fun NotificationStatRow(label: String, value: String, tint: Color) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Text(
            text     = label,
            fontSize = 13.sp,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text       = value,
            fontSize   = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color      = tint,
        )
    }
}

// ── Goal progress row ─────────────────────────────────────────────────────────

/**
 * Compact goal progress row for the goal overview section at the bottom of the
 * notifications screen.
 *
 * Shows the goal title, an animated progress bar, the milestone chips that have
 * been reached, and the remaining amount.
 *
 * @param summary  [NotificationsViewModel.GoalProgressSummary] for one goal.
 * @param modifier Optional layout modifier.
 */
@Composable
private fun NotificationsGoalProgressRow(
    summary: NotificationsViewModel.GoalProgressSummary,
    modifier: Modifier = Modifier,
) {
    val isDark = isSystemInDarkTheme()
    val glassFill   = ScreenIndigo.copy(alpha = if (isDark) 0.08f else 0.05f)
    val borderColor = ScreenIndigo.copy(alpha = if (isDark) 0.20f else 0.14f)

    val animProgress by animateFloatAsState(
        targetValue   = summary.progressPercent.coerceIn(0f, 1f),
        animationSpec = tween(900),
        label         = "goal_prog_${summary.goal.id}",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(glassFill)
            .border(0.8.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Goal title + percentage
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Text(
                text       = summary.goal.title,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurface,
                modifier   = Modifier.weight(1f),
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text       = "${(summary.progressPercent * 100).toInt()}%",
                fontSize   = 13.sp,
                fontWeight = FontWeight.Bold,
                color      = ScreenIndigo,
            )
        }

        // Progress bar
        Canvas(modifier = Modifier.fillMaxWidth().height(6.dp)) {
            val radius = size.height / 2f
            drawRoundRect(
                color        = Color.White.copy(alpha = if (isDark) 0.06f else 0.40f),
                cornerRadius = CornerRadius(radius),
            )
            if (animProgress > 0f) {
                drawRoundRect(
                    brush        = Brush.horizontalGradient(
                        listOf(ScreenIndigo, ScreenTeal),
                        endX = size.width * animProgress,
                    ),
                    size         = Size(size.width * animProgress, size.height),
                    cornerRadius = CornerRadius(radius),
                )
            }
        }

        // Milestone chips
        if (summary.milestonesReached.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                summary.milestonesReached.forEach { m ->
                    val chipColor = when (m) {
                        25   -> ScreenAmber
                        50   -> ScreenIndigo
                        75   -> Color(0xFF8B5CF6)
                        else -> ScreenTeal
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(chipColor.copy(alpha = 0.18f))
                            .border(0.6.dp, chipColor.copy(alpha = 0.50f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text       = "🎯 $m%",
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = chipColor,
                        )
                    }
                }
            }
        }

        // Remaining amount
        Text(
            text     = "Remaining: ${formatLKR(summary.remaining)}",
            fontSize = 12.sp,
            color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
        )
    }
}

// ── Formatters ────────────────────────────────────────────────────────────────

private fun formatLKR(amount: Double): String = when {
    amount >= 1_000_000 -> "LKR %.2fM".format(amount / 1_000_000)
    amount >= 1_000     -> "LKR %.1fK".format(amount / 1_000)
    else                -> "LKR %.0f".format(amount)
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "Notifications — Dark", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun PreviewNotificationsScreenDark() {
    FinPilotTheme(darkTheme = true) {
        NotificationsScreenContent(
            state = NotificationsViewModel.NotificationsUiState(
                isLoading = false,
                unreadCount = 3,
                allNotifications = previewNotifications(),
                notifications    = previewNotifications(),
                thisMonthExpenses = 8_500.0,
                thisWeekExpenses  = 2_100.0,
                thisWeekIncome    = 15_000.0,
                prefs = NotificationsRepository.NotificationPreferences(
                    salaryReminderEnabled  = true,
                    weeklySummaryEnabled   = true,
                    goalMilestoneEnabled   = false,
                    budgetOverspendEnabled = true,
                    budgetThreshold        = 10_000.0,
                ),
                budgetThresholdInput = "10000",
            ),
        )
    }
}

@Preview(name = "Notifications — Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PreviewNotificationsScreenLight() {
    FinPilotTheme(darkTheme = false) {
        NotificationsScreenContent(
            state = NotificationsViewModel.NotificationsUiState(
                isLoading = false,
                unreadCount = 1,
                allNotifications = previewNotifications(),
                notifications    = previewNotifications().take(2),
                thisMonthExpenses = 12_000.0,
                thisWeekExpenses  = 3_200.0,
                thisWeekIncome    = 18_000.0,
                prefs = NotificationsRepository.NotificationPreferences(
                    budgetThreshold = 10_000.0,
                ),
                budgetThresholdInput = "10000",
            ),
        )
    }
}

@Preview(name = "Notifications — Empty", showBackground = true)
@Composable
private fun PreviewNotificationsScreenEmpty() {
    FinPilotTheme(darkTheme = true) {
        NotificationsScreenContent(
            state = NotificationsViewModel.NotificationsUiState(
                isLoading        = false,
                notifications    = emptyList(),
                allNotifications = emptyList(),
                unreadCount      = 0,
            ),
        )
    }
}

@Preview(name = "Notifications — Loading", showBackground = true)
@Composable
private fun PreviewNotificationsScreenLoading() {
    FinPilotTheme(darkTheme = true) {
        NotificationsScreenContent(
            state = NotificationsViewModel.NotificationsUiState(isLoading = true),
        )
    }
}

// ── Preview data factory ──────────────────────────────────────────────────────

private fun previewNotifications(): List<NotificationItem> = listOf(
    NotificationItem(
        id          = "salary_2026_4",
        type        = NotificationType.SALARY_REMINDER,
        title       = "Salary Day — LKR 85,000",
        message     = "Your salary is expected today (25th). Remember to log it so your balance stays accurate.",
        timestamp   = System.currentTimeMillis() - 3_600_000L,
        isRead      = false,
        priority    = NotificationPriority.HIGH,
        actionLabel = "Log Income",
    ),
    NotificationItem(
        id          = "budget_2026_4",
        type        = NotificationType.BUDGET_OVERSPEND,
        title       = "Budget Alert — Threshold Exceeded",
        message     = "You've spent LKR 12,340 this month, which is LKR 2,340 over your LKR 10,000 threshold.",
        timestamp   = System.currentTimeMillis() - 7_200_000L,
        isRead      = false,
        priority    = NotificationPriority.URGENT,
        actionLabel = "Review Expenses",
    ),
    NotificationItem(
        id          = "goal_abc123_50",
        type        = NotificationType.GOAL_MILESTONE_50,
        title       = "Halfway There — Emergency Fund",
        message     = "Your Emergency Fund goal just crossed the 50 % milestone. Keep going!",
        timestamp   = System.currentTimeMillis() - 86_400_000L,
        isRead      = true,
        priority    = NotificationPriority.NORMAL,
        actionLabel = "View Goal",
    ),
    NotificationItem(
        id          = "weekly_2026_21",
        type        = NotificationType.WEEKLY_SUMMARY,
        title       = "Weekly Summary — May 11–17",
        message     = "Income: LKR 15,000 · Expenses: LKR 8,500 · Net: +LKR 6,500. Great week!",
        timestamp   = System.currentTimeMillis() - 172_800_000L,
        isRead      = false,
        priority    = NotificationPriority.NORMAL,
    ),
)
