package com.bpeople.finpilot.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

// ─────────────────────────────────────────────────────────────────────────────
// DataStore instance — one per process, shared across all callers
// ─────────────────────────────────────────────────────────────────────────────

private const val NOTIFICATIONS_DATASTORE_NAME = "finpilot_notifications_v1"

private val Context.notificationsDataStore: DataStore<Preferences>
    by preferencesDataStore(name = NOTIFICATIONS_DATASTORE_NAME)

// ─────────────────────────────────────────────────────────────────────────────
// Repository
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Single source of truth for all notification-related user preferences and
 * persisted state, backed by Jetpack DataStore (Preferences variant).
 *
 * All writes are `suspend` functions; reads are exposed as [kotlinx.coroutines.flow.Flow]s
 * so the ViewModel layer can observe changes reactively without polling.
 *
 * ### Four notification categories
 *
 * | Category           | Trigger condition                                    |
 * |--------------------|------------------------------------------------------|
 * | Salary Reminder    | Day-of-month == 25                                   |
 * | Weekly Summary     | Day-of-week  == Sunday (with 7-day income/expense)   |
 * | Goal Milestones    | Goal progress first crosses 25 %, 50 %, or 75 %     |
 * | Budget Overspend   | Monthly spend > [NotificationPreferences.budgetThreshold] |
 *
 * ### Persistence model
 *
 * - Each toggle preference is stored as a [Boolean].
 * - [NotificationPreferences.budgetThreshold] is stored as a [String] so we
 *   avoid floating-point serialisation issues.
 * - [NotificationPreferences.dismissedIds] tracks which notification IDs the
 *   user has explicitly swiped/dismissed.
 * - [NotificationPreferences.readIds] tracks which notifications have been
 *   opened (reduces the unread badge without full dismissal).
 * - [NotificationPreferences.shownMilestones] tracks tokens of the form
 *   `"<goalId>_<milestone%>"` so a milestone is only triggered once per goal.
 */
@Singleton
class NotificationsRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {

    // ─── Inner data class ─────────────────────────────────────────────────────

    /**
     * Immutable snapshot of all notification preferences read from DataStore.
     * Emitted by [preferences] whenever any stored key changes.
     */
    data class NotificationPreferences(
        /** Show in-app salary reminder on the 25th of each month. */
        val salaryReminderEnabled: Boolean = true,

        /** Show weekly spend/income recap every Sunday. */
        val weeklySummaryEnabled: Boolean = true,

        /** Show a card each time goal progress crosses 25 %, 50 %, or 75 %. */
        val goalMilestoneEnabled: Boolean = true,

        /** Show a warning when monthly spending exceeds [budgetThreshold]. */
        val budgetOverspendEnabled: Boolean = true,

        /**
         * Monthly spending threshold (LKR) for the overspend alert.
         * Defaults to 10,000 LKR as specified in the project requirements.
         */
        val budgetThreshold: Double = DEFAULT_BUDGET_THRESHOLD,

        /**
         * Set of notification IDs explicitly dismissed by the user.
         * Dismissed notifications are filtered out permanently until the
         * DataStore is cleared.
         */
        val dismissedIds: Set<String> = emptySet(),

        /**
         * Tokens of the form "<goalId>_<milestone%>" (e.g. "abc123_50")
         * recording which goal milestones have already been surfaced so the
         * same milestone is not shown twice for the same goal.
         */
        val shownMilestones: Set<String> = emptySet(),

        /**
         * Set of notification IDs the user has opened / marked-as-read.
         * These still appear in the list but do not contribute to the badge.
         */
        val readIds: Set<String> = emptySet(),
    )

    // ─── DataStore preference keys ────────────────────────────────────────────

    private object Keys {
        val SALARY_REMINDER_ENABLED  = booleanPreferencesKey("notif_salary_reminder")
        val WEEKLY_SUMMARY_ENABLED   = booleanPreferencesKey("notif_weekly_summary")
        val GOAL_MILESTONE_ENABLED   = booleanPreferencesKey("notif_goal_milestone")
        val BUDGET_OVERSPEND_ENABLED = booleanPreferencesKey("notif_budget_overspend")
        val BUDGET_THRESHOLD         = stringPreferencesKey("notif_budget_threshold")
        val DISMISSED_IDS            = stringSetPreferencesKey("notif_dismissed_ids")
        val SHOWN_MILESTONES         = stringSetPreferencesKey("notif_shown_milestones")
        val READ_IDS                 = stringSetPreferencesKey("notif_read_ids")
    }

    // ─── Public observable flow ───────────────────────────────────────────────

    /**
     * Continuously emits the latest [NotificationPreferences] whenever any
     * stored value changes.  On an [IOException] (e.g. corrupted store) the
     * flow falls back to default values rather than crashing.
     */
    val preferences: Flow<NotificationPreferences> = context.notificationsDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs ->
            NotificationPreferences(
                salaryReminderEnabled  = prefs[Keys.SALARY_REMINDER_ENABLED]  ?: true,
                weeklySummaryEnabled   = prefs[Keys.WEEKLY_SUMMARY_ENABLED]   ?: true,
                goalMilestoneEnabled   = prefs[Keys.GOAL_MILESTONE_ENABLED]   ?: true,
                budgetOverspendEnabled = prefs[Keys.BUDGET_OVERSPEND_ENABLED] ?: true,
                budgetThreshold        = prefs[Keys.BUDGET_THRESHOLD]?.toDoubleOrNull()
                    ?: DEFAULT_BUDGET_THRESHOLD,
                dismissedIds           = prefs[Keys.DISMISSED_IDS]    ?: emptySet(),
                shownMilestones        = prefs[Keys.SHOWN_MILESTONES]  ?: emptySet(),
                readIds                = prefs[Keys.READ_IDS]          ?: emptySet(),
            )
        }

    // ─── Preference setters ───────────────────────────────────────────────────

    /**
     * Enable or disable the 25th-of-the-month salary reminder.
     */
    suspend fun setSalaryReminderEnabled(enabled: Boolean) {
        context.notificationsDataStore.edit { prefs ->
            prefs[Keys.SALARY_REMINDER_ENABLED] = enabled
        }
    }

    /**
     * Enable or disable the Sunday weekly spend-summary notification.
     */
    suspend fun setWeeklySummaryEnabled(enabled: Boolean) {
        context.notificationsDataStore.edit { prefs ->
            prefs[Keys.WEEKLY_SUMMARY_ENABLED] = enabled
        }
    }

    /**
     * Enable or disable goal-progress milestone alerts (25 %, 50 %, 75 %).
     */
    suspend fun setGoalMilestoneEnabled(enabled: Boolean) {
        context.notificationsDataStore.edit { prefs ->
            prefs[Keys.GOAL_MILESTONE_ENABLED] = enabled
        }
    }

    /**
     * Enable or disable the budget-overspend alert.
     */
    suspend fun setBudgetOverspendEnabled(enabled: Boolean) {
        context.notificationsDataStore.edit { prefs ->
            prefs[Keys.BUDGET_OVERSPEND_ENABLED] = enabled
        }
    }

    /**
     * Update the monthly spending threshold (in LKR) that triggers the
     * overspend alert.  Values less than zero are silently rejected.
     */
    suspend fun setBudgetThreshold(threshold: Double) {
        if (threshold < 0.0) return
        context.notificationsDataStore.edit { prefs ->
            prefs[Keys.BUDGET_THRESHOLD] = threshold.toString()
        }
    }

    // ─── Dismissal / read-state management ───────────────────────────────────

    /**
     * Record that the user has dismissed notification [id].
     *
     * Once dismissed a notification will no longer appear in the feed, even
     * after app restarts, unless [clearAllDismissed] is called.
     */
    suspend fun dismissNotification(id: String) {
        context.notificationsDataStore.edit { prefs ->
            val current = prefs[Keys.DISMISSED_IDS] ?: emptySet()
            prefs[Keys.DISMISSED_IDS] = current + id
        }
    }

    /**
     * Batch-dismiss all notification IDs in [ids] in a single DataStore write
     * to minimise I/O overhead when the user taps "Clear all".
     */
    suspend fun dismissAll(ids: Collection<String>) {
        if (ids.isEmpty()) return
        context.notificationsDataStore.edit { prefs ->
            val current = prefs[Keys.DISMISSED_IDS] ?: emptySet()
            prefs[Keys.DISMISSED_IDS] = current + ids
        }
    }

    /**
     * Mark notification [id] as read.
     *
     * The notification remains visible but stops contributing to the unread
     * badge count shown on the bell icon in the Dashboard header.
     */
    suspend fun markRead(id: String) {
        context.notificationsDataStore.edit { prefs ->
            val current = prefs[Keys.READ_IDS] ?: emptySet()
            prefs[Keys.READ_IDS] = current + id
        }
    }

    /**
     * Mark all supplied notification IDs as read in a single DataStore write.
     * Called when the user taps "Mark all as read" in the Notifications screen.
     */
    suspend fun markAllRead(ids: Collection<String>) {
        if (ids.isEmpty()) return
        context.notificationsDataStore.edit { prefs ->
            val current = prefs[Keys.READ_IDS] ?: emptySet()
            prefs[Keys.READ_IDS] = current + ids
        }
    }

    /**
     * Persist the token `"<goalId>_<milestone>"` so the same milestone alert
     * is never generated twice for the same goal.
     *
     * Call this immediately after constructing the milestone [NotificationItem]
     * so subsequent snapshot recomputations suppress the duplicate.
     *
     * @param goalId    Firestore document ID of the goal.
     * @param milestone The percentage threshold just crossed (25, 50, or 75).
     */
    suspend fun recordMilestoneShown(goalId: String, milestone: Int) {
        context.notificationsDataStore.edit { prefs ->
            val current = prefs[Keys.SHOWN_MILESTONES] ?: emptySet()
            prefs[Keys.SHOWN_MILESTONES] = current + "${goalId}_${milestone}"
        }
    }

    /**
     * Remove all dismissed and read IDs from persistence.
     *
     * Intended for developer/testing use or a future "restore all dismissed
     * notifications" feature in Settings.
     */
    suspend fun clearAllDismissed() {
        context.notificationsDataStore.edit { prefs ->
            prefs[Keys.DISMISSED_IDS]   = emptySet()
            prefs[Keys.READ_IDS]        = emptySet()
        }
    }

    /**
     * Remove all shown-milestone tokens so the same milestone alerts can fire
     * again on next app launch.  Useful when a goal is deleted and re-created.
     */
    suspend fun clearShownMilestones() {
        context.notificationsDataStore.edit { prefs ->
            prefs[Keys.SHOWN_MILESTONES] = emptySet()
        }
    }

    // ─── Companion ────────────────────────────────────────────────────────────

    companion object {
        /** Default LKR budget threshold (10,000) matching the project spec. */
        const val DEFAULT_BUDGET_THRESHOLD = 10_000.0

        /** Milestone percentages tracked for goal alerts, in ascending order. */
        val MILESTONE_THRESHOLDS = listOf(25, 50, 75)
    }
}
