package com.bpeople.finpilot.ui.screens.notifications

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.bpeople.finpilot.ui.components.NotificationSummaryCard
import com.bpeople.finpilot.ui.components.NotificationsDisabledBanner
import com.bpeople.finpilot.ui.theme.FinPilotTheme
import com.bpeople.finpilot.ui.theme.LocalAppDarkTheme
import kotlinx.coroutines.launch

// ── Private colour constants ──────────────────────────────────────────────────

private val ScreenOrange     = Color(0xFFF97316)
private val ScreenOrangeGlow = Color(0xFFFF8C42)
private val ScreenAmber      = Color(0xFFF59E0B)
private val ScreenTeal       = Color(0xFF14B8A6)

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
) {
    val isDark = LocalAppDarkTheme.current
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
                        modifier         = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 4.dp),
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

                // ─ 7. Bottom spacer ───────────────────────────────────────────
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
    val isDark = LocalAppDarkTheme.current
    val topBarBrush = if (isDark) {
        Brush.horizontalGradient(
            listOf(
                Color(0xFF0A0500).copy(alpha = if (isScrolled) 0.98f else 0.92f),
                Color(0xFF1A0800).copy(alpha = if (isScrolled) 0.95f else 0.88f),
            )
        )
    } else {
        Brush.horizontalGradient(
            listOf(
                Color(0xFFFFFFFF).copy(alpha = if (isScrolled) 0.98f else 0.92f),
                Color(0xFFFFF0E0).copy(alpha = if (isScrolled) 0.95f else 0.88f),
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(topBarBrush)
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
    val isDark  = LocalAppDarkTheme.current
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
