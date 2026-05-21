@file:OptIn(ExperimentalMaterial3Api::class)

package com.bpeople.finpilot.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bpeople.finpilot.data.model.NotificationItem
import com.bpeople.finpilot.data.model.NotificationPriority
import com.bpeople.finpilot.data.model.NotificationType
import com.bpeople.finpilot.ui.theme.FinPilotTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// Internal palette — mirrors Color.kt semantic tokens
// ─────────────────────────────────────────────────────────────────────────────

private val NotifOrange     = Color(0xFFF97316)  // primary brand orange
private val NotifOrangeGlow = Color(0xFFFB923C)  // lighter orange tint
private val NotifGreen      = Color(0xFF10B981)  // income / success
private val NotifRed        = Color(0xFFEF4444)  // error / overspend
private val NotifAmber      = Color(0xFFF59E0B)  // warning / 25 % milestone
private val NotifIndigo     = Color(0xFF6366F1)  // 50 % milestone
private val NotifTeal       = Color(0xFF14B8A6)  // weekly summary
private val NotifPurple     = Color(0xFF8B5CF6)  // 75 % milestone
private val NotifGrey       = Color(0xFF6B7280)  // system / inactive

// ─────────────────────────────────────────────────────────────────────────────
// Colour mappings — resolve per notification type and priority
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Returns the primary accent [Color] used for icon backgrounds, type badges,
 * and border highlights for a given [NotificationType].
 */
fun accentColorFor(type: NotificationType): Color = when (type) {
    NotificationType.SALARY_REMINDER   -> NotifOrange
    NotificationType.WEEKLY_SUMMARY    -> NotifTeal
    NotificationType.GOAL_MILESTONE_25 -> NotifAmber
    NotificationType.GOAL_MILESTONE_50 -> NotifIndigo
    NotificationType.GOAL_MILESTONE_75 -> NotifPurple
    NotificationType.BUDGET_OVERSPEND  -> NotifRed
    NotificationType.SYSTEM            -> NotifGrey
}

/**
 * Returns the accent [Color] used to tint priority indicator dots and labels.
 */
fun priorityColor(priority: NotificationPriority): Color = when (priority) {
    NotificationPriority.LOW    -> NotifGrey
    NotificationPriority.NORMAL -> NotifTeal
    NotificationPriority.HIGH   -> NotifOrange
    NotificationPriority.URGENT -> NotifRed
}

// ─────────────────────────────────────────────────────────────────────────────
// 1. NotificationCard  — the primary reusable list item
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Full-width card representing a single [NotificationItem].
 *
 * Visual states:
 *  - **Unread + High/Urgent**: animated glow border
 *  - **Unread + Normal/Low**: static accent border
 *  - **Read**: muted border, lower contrast title
 *
 * The card exposes:
 *  - [onDismiss]  — called when the × button is tapped
 *  - [onAction]   — called when the [NotificationItem.actionLabel] chip is tapped
 *  - [onMarkRead] — called when the card body itself is tapped
 *
 * All callbacks default to no-ops so the composable is safe to use in Previews.
 *
 * @param item       The notification to display.
 * @param modifier   Optional layout modifier applied to the card container.
 * @param onDismiss  Called when the user taps the dismiss (×) button.
 * @param onAction   Called when the user taps the action chip.
 * @param onMarkRead Called when the user taps the card body (marks it read).
 */
@Composable
fun NotificationCard(
    item: NotificationItem,
    modifier: Modifier = Modifier,
    onDismiss: (String) -> Unit = {},
    onAction: (NotificationItem) -> Unit = {},
    onMarkRead: (String) -> Unit = {},
) {
    val isDark   = isSystemInDarkTheme()
    val accent   = accentColorFor(item.type)
    val isUnread = item.isActive

    // ── Animated pulsing border for URGENT / HIGH unread notifications ────────
    val infiniteTransition = rememberInfiniteTransition(label = "notif_pulse_${item.id}")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue   = 0.35f,
        targetValue    = 0.80f,
        animationSpec  = infiniteRepeatable(
            animation  = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_alpha_${item.id}",
    )
    val borderAlpha = when {
        isUnread && item.priority.isElevated -> pulseAlpha
        isUnread                             -> 0.50f
        else                                 -> 0.20f
    }

    // ── Background fill ───────────────────────────────────────────────────────
    val fillAlpha = if (isDark) {
        if (isUnread) 0.14f else 0.06f
    } else {
        if (isUnread) 0.09f else 0.04f
    }

    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(18.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(accent.copy(alpha = fillAlpha))
                .border(
                    width = if (isUnread && item.priority.isElevated) 1.5.dp else 0.8.dp,
                    color = accent.copy(alpha = borderAlpha),
                    shape = RoundedCornerShape(18.dp),
                )
                .clickable { onMarkRead(item.id) },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // ── Row 1: icon + title + dismiss button ──────────────────────
                Row(
                    modifier            = Modifier.fillMaxWidth(),
                    verticalAlignment   = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    NotificationIconBox(
                        emoji  = item.iconEmoji,
                        accent = accent,
                        size   = 44.dp,
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier            = Modifier.fillMaxWidth(),
                            verticalAlignment   = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text       = item.title,
                                style      = MaterialTheme.typography.titleSmall,
                                fontWeight = if (isUnread) FontWeight.Bold else FontWeight.SemiBold,
                                color      = if (isUnread) MaterialTheme.colorScheme.onSurface
                                             else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier   = Modifier.weight(1f),
                                maxLines   = 2,
                                overflow   = TextOverflow.Ellipsis,
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick  = { onDismiss(item.id) },
                                modifier = Modifier.size(28.dp),
                            ) {
                                Icon(
                                    imageVector        = Icons.Default.Close,
                                    contentDescription = "Dismiss notification",
                                    tint               = MaterialTheme.colorScheme.onSurfaceVariant
                                        .copy(alpha = 0.60f),
                                    modifier           = Modifier.size(16.dp),
                                )
                            }
                        }

                        // Priority + type badges row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                        ) {
                            NotificationTypeBadge(type = item.type, accent = accent)
                            if (item.priority != NotificationPriority.NORMAL) {
                                NotificationPriorityBadge(priority = item.priority)
                            }
                            if (isUnread) {
                                UnreadDot()
                            }
                        }
                    }
                }

                // ── Row 2: Message body ───────────────────────────────────────
                Text(
                    text      = item.message,
                    style     = MaterialTheme.typography.bodySmall,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp,
                )

                // ── Row 3: timestamp + action chip ────────────────────────────
                Row(
                    modifier            = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment   = Alignment.CenterVertically,
                ) {
                    NotificationTimestamp(millis = item.timestamp)

                    item.actionLabel?.let { label ->
                        NotificationActionChip(
                            label  = label,
                            accent = accent,
                            onClick = { onAction(item) },
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 2. NotificationIconBox  — emoji icon inside a tinted rounded circle
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Renders an emoji [emoji] centred inside a semi-transparent circle whose
 * fill and border are tinted with [accent].
 *
 * Used as the leading icon in [NotificationCard] and in empty/summary states.
 *
 * @param emoji  Unicode emoji string (single character recommended).
 * @param accent Tint colour for the circle background and border.
 * @param size   Width and height of the box; defaults to 44 dp.
 */
@Composable
fun NotificationIconBox(
    emoji: String,
    accent: Color,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
) {
    val isDark = isSystemInDarkTheme()
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(
                        accent.copy(alpha = if (isDark) 0.28f else 0.18f),
                        accent.copy(alpha = if (isDark) 0.08f else 0.05f),
                    )
                )
            )
            .border(0.8.dp, accent.copy(alpha = if (isDark) 0.40f else 0.28f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text     = emoji,
            fontSize = (size.value * 0.40f).sp,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 3. NotificationTypeBadge  — coloured chip showing the notification category
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Compact chip displaying the [NotificationItem.categoryLabel] tinted with
 * the provided [accent] colour.
 *
 * Renders as a rounded pill with semi-transparent fill and a thin accent border.
 */
@Composable
fun NotificationTypeBadge(
    type: NotificationType,
    modifier: Modifier = Modifier,
    accent: Color = accentColorFor(type),
) {
    val label = when (type) {
        NotificationType.SALARY_REMINDER   -> "Salary"
        NotificationType.WEEKLY_SUMMARY    -> "Weekly"
        NotificationType.GOAL_MILESTONE_25,
        NotificationType.GOAL_MILESTONE_50,
        NotificationType.GOAL_MILESTONE_75 -> "Goal"
        NotificationType.BUDGET_OVERSPEND  -> "Budget"
        NotificationType.SYSTEM            -> "System"
    }
    Surface(
        modifier = modifier,
        shape    = RoundedCornerShape(50.dp),
        color    = accent.copy(alpha = 0.15f),
    ) {
        Text(
            text      = label,
            modifier  = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style     = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color     = accent,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 4. NotificationPriorityBadge  — coloured priority pill
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Small pill that shows the [NotificationPriority.displayLabel] (e.g. "Urgent")
 * tinted with [priorityColor].
 *
 * Only shown when priority is not NORMAL, to reduce visual noise.
 */
@Composable
fun NotificationPriorityBadge(
    priority: NotificationPriority,
    modifier: Modifier = Modifier,
) {
    val color = priorityColor(priority)
    Surface(
        modifier = modifier,
        shape    = RoundedCornerShape(50.dp),
        color    = color.copy(alpha = 0.12f),
    ) {
        Text(
            text      = priority.displayLabel,
            modifier  = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
            style     = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color     = color,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 5. UnreadDot  — small pulsing indicator for unread notifications
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A small animated orange dot that signals an unread notification state.
 * Pulses between 0.7× and 1.0× scale using an infinite spring animation.
 */
@Composable
fun UnreadDot(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "unread_dot_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue  = 0.75f,
        targetValue   = 1.00f,
        animationSpec = infiniteRepeatable(
            animation  = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "unread_dot_scale",
    )
    Box(
        modifier = modifier
            .scale(scale)
            .size(7.dp)
            .clip(CircleShape)
            .background(NotifOrange),
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// 6. NotificationTimestamp  — human-friendly relative time label
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Renders a relative or absolute timestamp from a Unix-epoch [millis] value.
 *
 * Display rules:
 *  - < 1 minute  → "Just now"
 *  - < 60 minutes → "X minutes ago"
 *  - < 24 hours  → "X hours ago"
 *  - >= 24 hours → formatted date string ("15 Jan")
 */
@Composable
fun NotificationTimestamp(
    millis: Long,
    modifier: Modifier = Modifier,
) {
    val elapsed = System.currentTimeMillis() - millis
    val label = when {
        elapsed < 60_000L               -> "Just now"
        elapsed < 3_600_000L            -> "${elapsed / 60_000} min ago"
        elapsed < 86_400_000L           -> "${elapsed / 3_600_000} h ago"
        else -> SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(millis))
    }
    Row(
        modifier          = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Icon(
            imageVector        = Icons.Rounded.AccessTime,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
            modifier           = Modifier.size(11.dp),
        )
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 7. NotificationActionChip  — CTA chip inside the notification card footer
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A small tappable chip that renders the [NotificationItem.actionLabel]
 * (e.g. "Log Income", "View Goal", "Review Expenses").
 *
 * Tinted with [accent] to match the parent card's colour scheme.
 */
@Composable
fun NotificationActionChip(
    label: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(50.dp),
        color = accent.copy(alpha = 0.18f),
    ) {
        Text(
            text      = label,
            modifier  = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            style     = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color     = accent,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 8. NotificationSummaryCard  — stats banner at the top of the feed
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A glassmorphism card summarising the current notification feed in four
 * stat cells:
 *  - Total active notifications
 *  - Unread count
 *  - Urgent count
 *  - Goal milestone count
 *
 * @param totalCount    Number of notifications currently visible in the feed.
 * @param unreadCount   Subset of [totalCount] that have not been read.
 * @param urgentCount   Subset with [NotificationPriority.URGENT] priority.
 * @param milestoneCount Number of goal-milestone type notifications.
 * @param onMarkAllRead Called when the user taps the "Mark all read" action.
 */
@Composable
fun NotificationSummaryCard(
    totalCount: Int,
    unreadCount: Int,
    urgentCount: Int,
    milestoneCount: Int,
    modifier: Modifier = Modifier,
    onMarkAllRead: () -> Unit = {},
) {
    val isDark = isSystemInDarkTheme()
    val glassFill = if (isDark) NotifOrange.copy(alpha = 0.12f) else NotifOrange.copy(alpha = 0.07f)
    val borderBrush = Brush.linearGradient(
        listOf(
            NotifOrange.copy(alpha = if (isDark) 0.40f else 0.35f),
            Color.White.copy(alpha = if (isDark) 0.08f else 0.45f),
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(glassFill)
            .border(0.8.dp, borderBrush, RoundedCornerShape(20.dp)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier            = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment   = Alignment.CenterVertically,
            ) {
                Text(
                    text      = "Notification Overview",
                    style     = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color     = MaterialTheme.colorScheme.onSurface,
                )
                if (unreadCount > 0) {
                    TextButton(onClick = onMarkAllRead) {
                        Icon(
                            imageVector        = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint               = NotifOrange,
                            modifier           = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text      = "Mark all read",
                            style     = MaterialTheme.typography.labelMedium,
                            color     = NotifOrange,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            // Stats row
            Row(
                modifier            = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                NotificationStatCell(
                    value = totalCount.toString(),
                    label = "Total",
                    color = NotifOrange,
                )
                NotificationStatCell(
                    value = unreadCount.toString(),
                    label = "Unread",
                    color = if (unreadCount > 0) NotifAmber else NotifGrey,
                )
                NotificationStatCell(
                    value = urgentCount.toString(),
                    label = "Urgent",
                    color = if (urgentCount > 0) NotifRed else NotifGrey,
                )
                NotificationStatCell(
                    value = milestoneCount.toString(),
                    label = "Milestones",
                    color = NotifIndigo,
                )
            }
        }
    }
}

@Composable
private fun NotificationStatCell(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text       = value,
            style      = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color      = color,
        )
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 9. NotificationFilterChips  — horizontal filter strip by notification type
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A row of [FilterChip]s that let the user show only a specific notification
 * type.  Selecting the "All" chip (or tapping an active chip again) clears the
 * filter.
 *
 * @param activeFilter  Currently selected [NotificationType] filter, or null for "All".
 * @param counts        Map of [NotificationType] → count for each available type.
 * @param onFilterSelected Called with the selected type (null = show all).
 */
@Composable
fun NotificationFilterChips(
    activeFilter: NotificationType?,
    counts: Map<NotificationType, Int>,
    modifier: Modifier = Modifier,
    onFilterSelected: (NotificationType?) -> Unit = {},
) {
    val availableTypes = counts.entries
        .filter { it.value > 0 }
        .map { it.key }
        .sortedBy { it.ordinal }

    if (availableTypes.isEmpty()) return

    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        // "All" chip
        FilterChip(
            selected = activeFilter == null,
            onClick  = { onFilterSelected(null) },
            label    = { Text("All", style = MaterialTheme.typography.labelMedium) },
            colors   = FilterChipDefaults.filterChipColors(
                selectedContainerColor = NotifOrange.copy(alpha = 0.18f),
                selectedLabelColor     = NotifOrange,
            ),
        )

        // Per-type chips
        availableTypes.forEach { type ->
            val accent  = accentColorFor(type)
            val count   = counts[type] ?: 0
            FilterChip(
                selected = activeFilter == type,
                onClick  = {
                    onFilterSelected(if (activeFilter == type) null else type)
                },
                label    = {
                    Text(
                        text  = "${type.chipLabel} ($count)",
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
                colors   = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = accent.copy(alpha = 0.18f),
                    selectedLabelColor     = accent,
                ),
            )
        }
    }
}

private val NotificationType.chipLabel: String
    get() = when (this) {
        NotificationType.SALARY_REMINDER   -> "Salary"
        NotificationType.WEEKLY_SUMMARY    -> "Weekly"
        NotificationType.GOAL_MILESTONE_25,
        NotificationType.GOAL_MILESTONE_50,
        NotificationType.GOAL_MILESTONE_75 -> "Goals"
        NotificationType.BUDGET_OVERSPEND  -> "Budget"
        NotificationType.SYSTEM            -> "System"
    }

// ─────────────────────────────────────────────────────────────────────────────
// 10. NotificationEmptyState  — shown when no notifications are active
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Full-width empty state displayed when [items] is empty.
 *
 * Shows a large notification bell icon, a headline, and an optional
 * [subtitle] with context-specific guidance.
 *
 * @param headline Short heading (default: "You're all caught up!").
 * @param subtitle Optional descriptive line explaining why the feed is empty.
 */
@Composable
fun NotificationEmptyState(
    modifier: Modifier = Modifier,
    headline: String = "You're all caught up!",
    subtitle: String = "No active notifications right now. Check back later or adjust your preferences below.",
) {
    val scaleAnim = remember { Animatable(0.7f) }
    LaunchedEffect(Unit) {
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        )
    }
    Column(
        modifier            = modifier
            .fillMaxWidth()
            .padding(vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .scale(scaleAnim.value)
                .size(80.dp)
                .clip(CircleShape)
                .background(NotifOrange.copy(alpha = 0.12f))
                .border(1.dp, NotifOrange.copy(alpha = 0.30f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = Icons.Default.Notifications,
                contentDescription = null,
                tint               = NotifOrange.copy(alpha = 0.70f),
                modifier           = Modifier.size(38.dp),
            )
        }
        Text(
            text       = headline,
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onSurface,
            textAlign  = TextAlign.Center,
        )
        Text(
            text      = subtitle,
            style     = MaterialTheme.typography.bodySmall,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier  = Modifier.padding(horizontal = 24.dp),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 11. NotificationPreferenceItem  — single toggle row in the settings section
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A single preference row with an icon, [title], [subtitle], and a branded
 * orange [Switch].
 *
 * Used within [NotificationPreferencesSection] to represent each of the four
 * notification categories.
 *
 * @param icon     Material icon displayed on the left.
 * @param iconTint Accent tint for the icon background and icon itself.
 * @param title    Primary label text.
 * @param subtitle Secondary descriptive text (when/how the alert fires).
 * @param checked  Current toggle state.
 * @param onCheckedChange Called when the switch is toggled.
 * @param enabled  Whether the row is interactive (defaults to true).
 */
@Composable
fun NotificationPreferenceItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier          = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Icon box
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconTint.copy(alpha = 0.12f))
                .border(0.5.dp, iconTint.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = iconTint,
                modifier           = Modifier.size(20.dp),
            )
        }

        // Text column
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = title,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color      = if (enabled) MaterialTheme.colorScheme.onSurface
                             else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.50f),
            )
            Text(
                text  = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
                    .copy(alpha = if (enabled) 1f else 0.50f),
            )
        }

        // Orange switch
        Switch(
            checked         = checked,
            onCheckedChange = onCheckedChange,
            enabled         = enabled,
            colors          = SwitchDefaults.colors(
                checkedThumbColor   = Color.White,
                checkedTrackColor   = NotifOrange,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = MaterialTheme.colorScheme.outline,
            ),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 12. NotificationPreferencesSection  — complete settings panel
// ─────────────────────────────────────────────────────────────────────────────

/**
 * The full notification preferences panel rendered at the bottom of the
 * Notifications screen.
 *
 * Contains:
 *  - Four [NotificationPreferenceItem] toggles (salary, weekly, milestone, budget)
 *  - A collapsible [NotificationBudgetThresholdField] that appears when the
 *    budget overspend toggle is ON
 *
 * @param salaryEnabled      Salary reminder toggle state.
 * @param weeklyEnabled      Weekly summary toggle state.
 * @param milestoneEnabled   Goal milestone toggle state.
 * @param budgetEnabled      Budget overspend toggle state.
 * @param budgetThreshold    Raw string bound to the threshold text field.
 * @param onSalaryToggle     Called when salary toggle changes.
 * @param onWeeklyToggle     Called when weekly toggle changes.
 * @param onMilestoneToggle  Called when milestone toggle changes.
 * @param onBudgetToggle     Called when budget toggle changes.
 * @param onThresholdChange  Called on each keystroke in the threshold field.
 * @param onThresholdCommit  Called when the user finishes editing the field.
 */
@Composable
fun NotificationPreferencesSection(
    salaryEnabled: Boolean,
    weeklyEnabled: Boolean,
    milestoneEnabled: Boolean,
    budgetEnabled: Boolean,
    budgetThreshold: String,
    modifier: Modifier = Modifier,
    onSalaryToggle: (Boolean) -> Unit = {},
    onWeeklyToggle: (Boolean) -> Unit = {},
    onMilestoneToggle: (Boolean) -> Unit = {},
    onBudgetToggle: (Boolean) -> Unit = {},
    onThresholdChange: (String) -> Unit = {},
    onThresholdCommit: () -> Unit = {},
) {
    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text       = "Notification Preferences",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurface,
                modifier   = Modifier.padding(bottom = 8.dp),
            )

            // 1. Salary Reminder
            NotificationPreferenceItem(
                icon           = Icons.Default.AttachMoney,
                iconTint       = NotifOrange,
                title          = "Salary Received Reminder",
                subtitle       = "Alert on the 25th of every month",
                checked        = salaryEnabled,
                onCheckedChange = onSalaryToggle,
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color    = MaterialTheme.colorScheme.outline.copy(alpha = 0.30f),
            )

            // 2. Weekly Summary
            NotificationPreferenceItem(
                icon           = Icons.Default.TrendingUp,
                iconTint       = NotifTeal,
                title          = "Weekly Spend Summary",
                subtitle       = "Every Sunday with a 7-day recap",
                checked        = weeklyEnabled,
                onCheckedChange = onWeeklyToggle,
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color    = MaterialTheme.colorScheme.outline.copy(alpha = 0.30f),
            )

            // 3. Goal Milestones
            NotificationPreferenceItem(
                icon           = Icons.Default.EmojiEvents,
                iconTint       = NotifIndigo,
                title          = "Goal Milestone Alerts",
                subtitle       = "When goal reaches 25 %, 50 %, or 75 %",
                checked        = milestoneEnabled,
                onCheckedChange = onMilestoneToggle,
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color    = MaterialTheme.colorScheme.outline.copy(alpha = 0.30f),
            )

            // 4. Budget Overspend
            NotificationPreferenceItem(
                icon           = Icons.Default.Warning,
                iconTint       = NotifRed,
                title          = "Budget Overspend Alert",
                subtitle       = "When monthly spend exceeds threshold",
                checked        = budgetEnabled,
                onCheckedChange = onBudgetToggle,
            )

            // Animated threshold input — visible only when budget alert is ON
            AnimatedVisibility(
                visible = budgetEnabled,
                enter   = expandVertically() + fadeIn(),
                exit    = shrinkVertically() + fadeOut(),
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    NotificationBudgetThresholdField(
                        value    = budgetThreshold,
                        onChange = onThresholdChange,
                        onDone   = onThresholdCommit,
                    )
                    Text(
                        text     = "Default threshold: LKR 10,000",
                        style    = MaterialTheme.typography.labelSmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 13. NotificationBudgetThresholdField  — LKR amount input
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Styled [OutlinedTextField] for entering the budget overspend threshold in LKR.
 *
 * - Filters non-numeric input client-side.
 * - Fires [onDone] when the user submits via the keyboard "Done" action.
 * - Uses the brand orange focus colour to match the overall theme.
 *
 * @param value    Current raw string value (digits only).
 * @param onChange Called on each character change with the filtered value.
 * @param onDone   Called when the user confirms the value.
 */
@Composable
fun NotificationBudgetThresholdField(
    value: String,
    modifier: Modifier = Modifier,
    onChange: (String) -> Unit = {},
    onDone: () -> Unit = {},
) {
    OutlinedTextField(
        value          = value,
        onValueChange  = { raw -> onChange(raw.filter { it.isDigit() }) },
        label          = { Text("Monthly Budget Threshold (LKR)") },
        singleLine     = true,
        modifier       = modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors         = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = NotifOrange,
            focusedLabelColor    = NotifOrange,
            cursorColor          = NotifOrange,
        ),
        shape          = RoundedCornerShape(14.dp),
        leadingIcon    = {
            Text(
                text     = "LKR",
                style    = MaterialTheme.typography.labelMedium,
                color    = NotifOrange,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp),
            )
        },
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// 14. NotificationSectionHeader  — section title with optional action
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Section header row with a [title] on the left and an optional [actionLabel]
 * text button on the right (e.g. "Clear all").
 *
 * @param title        Section heading text.
 * @param actionLabel  Optional right-aligned action text.
 * @param onAction     Called when the action text is tapped.
 */
@Composable
fun NotificationSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String = "",
    onAction: () -> Unit = {},
) {
    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Text(
            text       = title,
            style      = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onSurface,
        )
        if (actionLabel.isNotBlank()) {
            Text(
                text      = actionLabel,
                style     = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color     = NotifOrange,
                modifier  = Modifier.clickable(onClick = onAction),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 15. NotificationBanner  — compact inline alert strip (for dashboard use)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A slim inline banner designed to be inserted into other screens (e.g. the
 * Dashboard) to show a single high-priority notification without opening the
 * full Notifications screen.
 *
 * Features:
 *  - Single-line emoji + message with accent tinting
 *  - Optional dismiss (×) button on the trailing edge
 *  - Tappable body that navigates to the Notifications screen
 *
 * @param item      The [NotificationItem] to display in compact form.
 * @param onTap     Called when the banner body is tapped.
 * @param onDismiss Called when the × button is tapped.
 */
@Composable
fun NotificationBanner(
    item: NotificationItem,
    modifier: Modifier = Modifier,
    onTap: () -> Unit = {},
    onDismiss: (String) -> Unit = {},
) {
    val isDark = isSystemInDarkTheme()
    val accent = accentColorFor(item.type)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(accent.copy(alpha = if (isDark) 0.16f else 0.10f))
            .border(0.8.dp, accent.copy(alpha = 0.40f), RoundedCornerShape(14.dp))
            .clickable(onClick = onTap)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(text = item.iconEmoji, fontSize = 16.sp)
        Text(
            text      = item.title,
            style     = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color     = MaterialTheme.colorScheme.onSurface,
            modifier  = Modifier.weight(1f),
            maxLines  = 1,
            overflow  = TextOverflow.Ellipsis,
        )
        IconButton(
            onClick  = { onDismiss(item.id) },
            modifier = Modifier.size(24.dp),
        ) {
            Icon(
                imageVector        = Icons.Default.Close,
                contentDescription = "Dismiss",
                tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.60f),
                modifier           = Modifier.size(14.dp),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 16. NotificationsBellBadge  — the bell icon with an unread count overlay
// ─────────────────────────────────────────────────────────────────────────────

/**
 * The notification bell icon combined with an animated unread badge.
 *
 * The badge shows the [unreadCount] (capped at "9+") and pulses with a spring
 * animation when the count changes.  If [unreadCount] is 0 the badge is hidden.
 *
 * @param unreadCount     Number of unread notifications.
 * @param onClick         Called when the bell is tapped.
 * @param tint            Icon tint colour.
 * @param containerColor  Background of the icon button circle.
 * @param borderColor     Border of the icon button circle.
 */
@Composable
fun NotificationsBellBadge(
    unreadCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    containerColor: Color = Color.Transparent,
    borderColor: Color = Color.Transparent,
) {
    val badgeScale = remember { Animatable(1f) }
    LaunchedEffect(unreadCount) {
        if (unreadCount > 0) {
            badgeScale.animateTo(1.3f, spring(Spring.DampingRatioMediumBouncy))
            badgeScale.animateTo(1.0f, spring(Spring.DampingRatioLowBouncy))
        }
    }

    Box(modifier = modifier) {
        IconButton(
            onClick  = onClick,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(containerColor)
                .border(0.8.dp, borderColor, CircleShape),
        ) {
            Icon(
                imageVector        = Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint               = tint,
                modifier           = Modifier.size(20.dp),
            )
        }

        // Badge — shown only when there are unread notifications
        if (unreadCount > 0) {
            Box(
                modifier = Modifier
                    .scale(badgeScale.value)
                    .align(Alignment.TopEnd)
                    .padding(end = 2.dp, top = 2.dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(NotifRed)
                    .border(1.dp, Color.White.copy(alpha = 0.80f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text      = if (unreadCount > 9) "9+" else unreadCount.toString(),
                    style     = MaterialTheme.typography.labelSmall,
                    color     = Color.White,
                    fontSize  = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 17. NotificationsDisabledBanner  — shown when all notifications are off
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A muted information strip shown at the top of the Notifications screen when
 * all four notification categories are disabled.
 *
 * Encourages the user to re-enable at least one category from the preferences
 * section below.
 */
@Composable
fun NotificationsDisabledBanner(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(NotifGrey.copy(alpha = 0.10f))
            .border(0.6.dp, NotifGrey.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector        = Icons.Default.NotificationsOff,
            contentDescription = null,
            tint               = NotifGrey,
            modifier           = Modifier.size(20.dp),
        )
        Text(
            text  = "All notifications are currently turned off. Enable categories below to stay on top of your finances.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "NotificationCard — Salary (unread)")
@Composable
private fun PreviewSalaryCard() {
    FinPilotTheme {
        NotificationCard(
            item = NotificationItem(
                id       = "salary_2026_4",
                type     = NotificationType.SALARY_REMINDER,
                title    = "Salary Day! 💰",
                message  = "Today is the 25th — your salary is expected.",
                priority = NotificationPriority.HIGH,
                actionLabel = "Log Income",
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, name = "NotificationCard — Budget (urgent)")
@Composable
private fun PreviewBudgetCard() {
    FinPilotTheme {
        NotificationCard(
            item = NotificationItem(
                id       = "budget_2026_4",
                type     = NotificationType.BUDGET_OVERSPEND,
                title    = "Budget Limit Exceeded ⚠️",
                message  = "Your spending in May 2026 reached LKR 14,500, exceeding your LKR 10,000 budget by LKR 4,500.",
                priority = NotificationPriority.URGENT,
                actionLabel = "Review Expenses",
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, name = "NotificationSummaryCard")
@Composable
private fun PreviewSummaryCard() {
    FinPilotTheme {
        NotificationSummaryCard(
            totalCount     = 4,
            unreadCount    = 2,
            urgentCount    = 1,
            milestoneCount = 1,
            modifier       = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, name = "NotificationPreferencesSection")
@Composable
private fun PreviewPreferencesSection() {
    FinPilotTheme {
        NotificationPreferencesSection(
            salaryEnabled    = true,
            weeklyEnabled    = true,
            milestoneEnabled = false,
            budgetEnabled    = true,
            budgetThreshold  = "10000",
            modifier         = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, name = "NotificationEmptyState")
@Composable
private fun PreviewEmptyState() {
    FinPilotTheme {
        NotificationEmptyState()
    }
}

@Preview(showBackground = true, name = "NotificationBanner — weekly")
@Composable
private fun PreviewBanner() {
    FinPilotTheme {
        NotificationBanner(
            item = NotificationItem(
                id    = "weekly_2026_21",
                type  = NotificationType.WEEKLY_SUMMARY,
                title = "Your Weekly Financial Recap 📊",
                message = "This week you earned LKR 22K and spent LKR 9K.",
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}
