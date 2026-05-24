package com.bpeople.finpilot.data.model

import java.io.Serializable

// ─────────────────────────────────────────────────────────────────────────────
// Notification Type
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Semantic classification of every in-app notification FinPilot can produce.
 *
 * Each variant maps to a distinct icon, accent colour, and message template so
 * the UI layer can render notifications without any switch logic inside
 * composables.
 *
 * Variants:
 *  - [SALARY_REMINDER]     – fires on the 25th of every month
 *  - [WEEKLY_SUMMARY]      – fires every Sunday with a 7-day recap
 *  - [GOAL_MILESTONE_25]   – goal progress crossed the 25 % mark
 *  - [GOAL_MILESTONE_50]   – goal progress crossed the 50 % mark
 *  - [GOAL_MILESTONE_75]   – goal progress crossed the 75 % mark
 *  - [BUDGET_OVERSPEND]    – monthly spending exceeded the user threshold
 *  - [SYSTEM]              – general informational or diagnostic alerts
 */
enum class NotificationType {
    SALARY_REMINDER,
    WEEKLY_SUMMARY,
    GOAL_MILESTONE_25,
    GOAL_MILESTONE_50,
    GOAL_MILESTONE_75,
    BUDGET_OVERSPEND,
    SYSTEM,
}

// ─────────────────────────────────────────────────────────────────────────────
// Notification Priority
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Controls the visual prominence and sort order of a notification card.
 *
 * Notifications are sorted descending by priority (URGENT first), then by
 * timestamp descending.  The [displayLabel] is shown as a chip in the UI.
 */
enum class NotificationPriority(val displayLabel: String) {
    LOW("Low"),
    NORMAL("Normal"),
    HIGH("High"),
    URGENT("Urgent");

    /**
     * Returns true when this priority level is strictly higher than [other],
     * letting callers compare without relying on [ordinal] arithmetic.
     */
    fun isHigherThan(other: NotificationPriority): Boolean = ordinal > other.ordinal

    /**
     * Returns true when this priority is HIGH or URGENT — used by the UI to
     * decide whether to apply an animated pulsing border on the notification card.
     */
    val isElevated: Boolean get() = this == HIGH || this == URGENT
}

// ─────────────────────────────────────────────────────────────────────────────
// Notification Item  (Domain Model)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Immutable snapshot of a single in-app notification.
 *
 * IDs are intentionally deterministic so the ViewModel can deduplicate without
 * keeping local state:
 *
 * | Type               | ID pattern                          |
 * |--------------------|-------------------------------------|
 * | Salary reminder    | `salary_<YYYY>_<MM>`                |
 * | Weekly summary     | `weekly_<YYYY>_<WW>`                |
 * | Goal milestone     | `goal_<goalId>_<milestone%>`        |
 * | Budget overspend   | `budget_<YYYY>_<MM>`                |
 *
 * Any notification whose [id] appears in the DataStore `dismissed_ids` set
 * is filtered out by the repository before it reaches the ViewModel.
 *
 * @property id            Stable, deterministic identifier.
 * @property type          Semantic classification — drives icon, colour, copy.
 * @property title         Short headline (max ~40 chars) shown at the top of the card.
 * @property message       Full body text explaining the event and any action to take.
 * @property timestamp     Unix-epoch millis when the notification was generated.
 * @property isRead        True after the user has opened/acknowledged this card.
 * @property isDismissed   True after the user explicitly dismissed this card.
 * @property priority      Visual weight and sort position.
 * @property actionLabel   Optional call-to-action chip label (e.g. "Log Income").
 * @property extraData     Type-specific key-value pairs used by expanded card views.
 */
data class NotificationItem(
    val id: String,
    val type: NotificationType,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val isDismissed: Boolean = false,
    val priority: NotificationPriority = NotificationPriority.NORMAL,
    val actionLabel: String? = null,
    val extraData: Map<String, String> = emptyMap(),
) : Serializable {

    // ── Derived display helpers ───────────────────────────────────────────────

    /**
     * Emoji used as the leading icon on the notification card.
     * Chosen per [type] for instant visual recognition.
     */
    val iconEmoji: String
        get() = when (type) {
            NotificationType.SALARY_REMINDER   -> "💰"
            NotificationType.WEEKLY_SUMMARY    -> "📊"
            NotificationType.GOAL_MILESTONE_25 -> "🎯"
            NotificationType.GOAL_MILESTONE_50 -> "🏅"
            NotificationType.GOAL_MILESTONE_75 -> "🔥"
            NotificationType.BUDGET_OVERSPEND  -> "⚠️"
            NotificationType.SYSTEM            -> "🔔"
        }

    /**
     * Short human-readable category label rendered inside a chip on the card.
     */
    val categoryLabel: String
        get() = when (type) {
            NotificationType.SALARY_REMINDER              -> "Salary"
            NotificationType.WEEKLY_SUMMARY               -> "Weekly"
            NotificationType.GOAL_MILESTONE_25,
            NotificationType.GOAL_MILESTONE_50,
            NotificationType.GOAL_MILESTONE_75            -> "Goal"
            NotificationType.BUDGET_OVERSPEND             -> "Budget"
            NotificationType.SYSTEM                       -> "System"
        }

    /**
     * True when the notification is both unread and not dismissed — the state
     * that contributes to the unread badge count shown on the bell icon.
     */
    val isActive: Boolean get() = !isRead && !isDismissed

    /**
     * Returns the milestone percentage (25, 50, or 75) for goal-type
     * notifications, or null for all other types.
     */
    val goalMilestonePercent: Int?
        get() = when (type) {
            NotificationType.GOAL_MILESTONE_25 -> 25
            NotificationType.GOAL_MILESTONE_50 -> 50
            NotificationType.GOAL_MILESTONE_75 -> 75
            else                               -> null
        }

    /**
     * Convenience: true when this is any kind of goal-milestone notification.
     */
    val isGoalMilestone: Boolean
        get() = type == NotificationType.GOAL_MILESTONE_25 ||
                type == NotificationType.GOAL_MILESTONE_50 ||
                type == NotificationType.GOAL_MILESTONE_75

    // ── Companion — stable ID factories ──────────────────────────────────────

    companion object {
        /**
         * Deterministic ID for the monthly salary reminder.
         * [month] is 0-based (Calendar.MONTH convention).
         */
        fun salaryId(year: Int, month: Int): String = "salary_${year}_${month}"

        /**
         * Deterministic ID for the Sunday weekly summary.
         * [week] is the Calendar.WEEK_OF_YEAR value.
         */
        fun weeklyId(year: Int, week: Int): String = "weekly_${year}_${week}"

        /**
         * Deterministic ID for a goal-progress milestone notification.
         * [milestone] is one of 25, 50, or 75.
         */
        fun milestoneId(goalId: String, milestone: Int): String =
            "goal_${goalId}_${milestone}"

        /**
         * Deterministic ID for the monthly budget-overspend notification.
         * [month] is 0-based (Calendar.MONTH convention).
         */
        fun budgetId(year: Int, month: Int): String = "budget_${year}_${month}"

        /**
         * All milestone percentages that FinPilot tracks, in ascending order.
         */
        val TRACKED_MILESTONES: List<Int> = listOf(25, 50, 75)
    }
}
