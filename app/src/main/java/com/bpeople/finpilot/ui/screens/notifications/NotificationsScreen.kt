@file:OptIn(ExperimentalMaterial3Api::class)

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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
        state                 = state,
        onNavigateBack        = onNavigateBack,
        onNavigateToDashboard = onNavigateToDashboard,
        onNavigateToIncome    = onNavigateToIncome,
        onNavigateToGoals     = onNavigateToGoals,
        onNavigateToProfile   = onNavigateToProfile,
        onDismiss             = viewModel::dismissNotification,
        onDismissAll          = viewModel::dismissAll,
        onMarkRead            = viewModel::markNotificationRead,
        onMarkAllRead         = viewModel::markAllRead,
        onFilterSelected      = viewModel::setActiveFilter,
    )
}

// ── Pure content composable ───────────────────────────────────────────────────

/**
 * Stateless, testable content layer for the Notifications screen.
 *
 * Layout layers (back to front):
 *  1. Vertical gradient background (dark: near-black; light: off-white).
 *  2. Three ambient radial glow blobs drawn on a [Canvas].
 *  3. Material3 [TopAppBar] in the Scaffold's topBar slot — handles status-bar
 *     insets automatically so the title is never hidden behind the phone's
 *     status bar.
 *  4. [LazyColumn] with all notification sections, padded by Scaffold's pv.
 *  5. [FinPilotBottomNavBar] anchored to [Alignment.BottomCenter].
 *
 * Sections (in scroll order):
 *  - [NotificationSummaryCard] — totals: all / unread / urgent / milestones
 *  - [NotificationFilterChips] — horizontal type-filter strip
 *  - Notification list — one [NotificationCard] per item
 *  - [NotificationEmptyState] — shown when no notifications match the filter
 *  - Bottom spacer for bottom-nav clearance
 *
 * Note: notification preferences (toggle switches, budget threshold) live in
 * the Settings screen, not here.
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
    val isDark       = isSystemInDarkTheme()
    val scope        = rememberCoroutineScope()
    val snackbarHost = remember { SnackbarHostState() }
    val listState    = rememberLazyListState()

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

    // ── Top app bar colour — adapts to dark / light theme ────────────────────
    // Dark  → near-black surface with orange tint border
    // Light → pure white surface with orange tint border
    val topBarContainerColor = if (isDark) Color(0xFF0D0D0D) else Color(0xFFFFFFFF)
    val topBarOnColor        = if (isDark) Color(0xFFF5F5F5) else Color(0xFF1F2937)

    Scaffold(
        // Zero contentWindowInsets so the gradient fills edge-to-edge.
        // The TopAppBar in topBar slot manages its own statusBarsPadding internally.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor      = Color.Transparent,
        snackbarHost        = { SnackbarHost(hostState = snackbarHost) },
        topBar = {
            NotificationsTopAppBar(
                unreadCount          = unreadCount,
                topBarContainerColor = topBarContainerColor,
                topBarOnColor        = topBarOnColor,
                onNavigateBack       = onNavigateBack,
                onMarkAllRead        = {
                    onMarkAllRead()
                    scope.launch {
                        snackbarHost.showSnackbar("All notifications marked as read")
                    }
                },
                onDismissAll         = {
                    onDismissAll()
                    scope.launch {
                        snackbarHost.showSnackbar("All notifications cleared")
                    }
                },
            )
        },
    ) { pv ->
        // pv.top = topBar height (which already includes the status bar height
        // because TopAppBar adds statusBarsPadding internally).
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
                        colors = listOf(
                            ScreenOrange.copy(alpha = if (isDark) 0.22f else 0.14f),
                            Color.Transparent,
                        ),
                        center = Offset(size.width * 0.88f, size.height * 0.06f),
                        radius = 260.dp.toPx(),
                    ),
                    center = Offset(size.width * 0.88f, size.height * 0.06f),
                    radius = 260.dp.toPx(),
                )
                // Mid-left amber glow
                drawCircle(
                    brush  = Brush.radialGradient(
                        colors = listOf(
                            ScreenAmber.copy(alpha = if (isDark) 0.13f else 0.07f),
                            Color.Transparent,
                        ),
                        center = Offset(size.width * 0.08f, size.height * 0.40f),
                        radius = 210.dp.toPx(),
                    ),
                    center = Offset(size.width * 0.08f, size.height * 0.40f),
                    radius = 210.dp.toPx(),
                )
                // Bottom-center teal glow
                drawCircle(
                    brush  = Brush.radialGradient(
                        colors = listOf(
                            ScreenTeal.copy(alpha = if (isDark) 0.10f else 0.06f),
                            Color.Transparent,
                        ),
                        center = Offset(size.width * 0.5f, size.height * 0.86f),
                        radius = 200.dp.toPx(),
                    ),
                    center = Offset(size.width * 0.5f, size.height * 0.86f),
                    radius = 200.dp.toPx(),
                )
            }

            // ── Main scrollable content ───────────────────────────────────────
            LazyColumn(
                state          = listState,
                modifier       = Modifier
                    .fillMaxSize()
                    .padding(pv),      // pushes content below TopAppBar
                contentPadding = PaddingValues(bottom = 120.dp),
            ) {

                // ─ 1. Summary card ─────────────────────────────────────────────
                item(key = "summary") {
                    NotificationSummaryCard(
                        totalCount     = totalCount,
                        unreadCount    = unreadCount,
                        urgentCount    = urgentCount,
                        milestoneCount = milestoneCount,
                        onMarkAllRead  = onMarkAllRead,
                        modifier       = Modifier.padding(
                            start  = 16.dp,
                            end    = 16.dp,
                            top    = 12.dp,
                            bottom = 8.dp,
                        ),
                    )
                }

                // ─ 2. Filter chips ─────────────────────────────────────────────
                item(key = "filters") {
                    NotificationFilterChips(
                        activeFilter     = state.activeFilter,
                        counts           = typeCounts,
                        onFilterSelected = onFilterSelected,
                        modifier         = Modifier.padding(
                            start  = 16.dp,
                            end    = 16.dp,
                            bottom = 4.dp,
                        ),
                    )
                }

                // ─ 3. Loading skeleton / empty state / cards ───────────────────
                if (state.isLoading) {
                    items(count = 4, key = { "skeleton_$it" }) {
                        NotificationLoadingSkeleton(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        )
                    }
                } else if (state.notifications.isEmpty()) {
                    item(key = "empty") {
                        val headline = if (state.activeFilter != null) {
                            "No ${state.activeFilter.name.lowercase().replace('_', ' ')} notifications"
                        } else {
                            "You're all caught up!"
                        }
                        val subtitle = if (state.activeFilter != null) {
                            "Try a different filter or wait for new activity."
                        } else {
                            "New alerts will appear here when your finances trigger a rule."
                        }
                        NotificationEmptyState(
                            headline = headline,
                            subtitle = subtitle,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
                        )
                    }
                } else {
                    // ─ 4. Notification cards ─────────────────────────────────
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

                // ─ 5. Bottom spacer ────────────────────────────────────────────
                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // ── Bottom navigation bar overlay ─────────────────────────────────
            FinPilotBottomNavBar(
                currentTab               = NavTab.HOME,
                onNavigateToDashboard    = onNavigateToDashboard,
                onNavigateToTransactions = onNavigateToIncome,
                onNavigateToGoals        = onNavigateToGoals,
                onNavigateToProfile      = onNavigateToProfile,
                modifier                 = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

// ── Top app bar ───────────────────────────────────────────────────────────────

/**
 * Material3 [TopAppBar] styled to match FinPilot's glassmorphism theme.
 *
 * The Scaffold places this composable in its `topBar` slot, which means
 * Material3 automatically adds [WindowInsets.statusBars] padding inside
 * the TopAppBar — the title is never hidden behind the phone's status bar.
 *
 * Colour adapts to dark / light theme:
 *  - **Dark**  → near-black container (`#0D0D0D`), near-white text
 *  - **Light** → pure-white container, dark-grey text
 *
 * The orange brand accent is applied consistently to the bell icon, the live
 * unread badge, and the gradient rule painted below the bar.
 *
 * @param unreadCount          Number of unread notifications; drives the badge.
 * @param topBarContainerColor Surface colour of the bar (varies by theme).
 * @param topBarOnColor        Text/icon colour on the bar surface.
 * @param onNavigateBack       Called when the leading back arrow is tapped.
 * @param onMarkAllRead        Called when the "mark all read" icon is tapped.
 * @param onDismissAll         Called when the "clear all" icon is tapped.
 */
@Composable
private fun NotificationsTopAppBar(
    unreadCount: Int,
    topBarContainerColor: Color,
    topBarOnColor: Color,
    onNavigateBack: () -> Unit,
    onMarkAllRead: () -> Unit,
    onDismissAll: () -> Unit,
) {
    val isDark = isSystemInDarkTheme()

    Column(modifier = Modifier.fillMaxWidth()) {
        TopAppBar(
            // Leading: back navigation
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Go back",
                        tint               = topBarOnColor,
                    )
                }
            },
            // Centre: bell icon + title + live unread badge
            title = {
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
                        color      = topBarOnColor,
                    )
                    // Animated unread badge — slides in/out as count changes
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
                        } else {
                            // Keep layout stable when there are no unread items
                            Spacer(modifier = Modifier.width(0.dp))
                        }
                    }
                }
            },
            // Trailing: mark-all-read + clear-all
            actions = {
                IconButton(onClick = onMarkAllRead) {
                    Icon(
                        imageVector        = Icons.Default.DoneAll,
                        contentDescription = "Mark all as read",
                        tint               = ScreenTeal,
                        modifier           = Modifier.size(20.dp),
                    )
                }
                IconButton(onClick = onDismissAll) {
                    Icon(
                        imageVector        = Icons.Default.DeleteSweep,
                        contentDescription = "Clear all notifications",
                        tint               = topBarOnColor.copy(alpha = 0.60f),
                        modifier           = Modifier.size(20.dp),
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor         = topBarContainerColor,
                titleContentColor      = topBarOnColor,
                navigationIconContentColor = topBarOnColor,
                actionIconContentColor     = topBarOnColor,
            ),
        )

        // Thin orange gradient rule underneath the top bar — dark and light
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            ScreenOrange.copy(alpha = if (isDark) 0.80f else 0.60f),
                            ScreenOrangeGlow.copy(alpha = if (isDark) 0.50f else 0.35f),
                            ScreenAmber.copy(alpha = if (isDark) 0.30f else 0.20f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
    }
}

// ── Loading skeleton ──────────────────────────────────────────────────────────

/**
 * Shimmer-style placeholder shown while notifications load on first launch.
 *
 * Pulses between two alpha values to signal activity without showing stale
 * content.  The rounded-rectangle shape matches a real [NotificationCard] so
 * the layout shift is minimal once real data arrives.
 */
@Composable
private fun NotificationLoadingSkeleton(modifier: Modifier = Modifier) {
    val isDark    = isSystemInDarkTheme()
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
                isLoading        = false,
                unreadCount      = 3,
                allNotifications = previewNotifications(),
                notifications    = previewNotifications(),
                prefs            = NotificationsRepository.NotificationPreferences(
                    budgetThreshold = 10_000.0,
                ),
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
                isLoading        = false,
                unreadCount      = 1,
                allNotifications = previewNotifications(),
                notifications    = previewNotifications().take(2),
                prefs            = NotificationsRepository.NotificationPreferences(
                    budgetThreshold = 10_000.0,
                ),
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
