package com.bpeople.finpilot.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private const val SETTINGS_DATASTORE_NAME = "settings"

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = SETTINGS_DATASTORE_NAME
)

@Singleton
class SettingsRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    data class SettingsPreferences(
        val notificationsEnabled: Boolean,
        val darkModeEnabled: Boolean,
        val cloudSyncEnabled: Boolean,
        val biometricsEnabled: Boolean,
    )

    private object Keys {
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
        val CLOUD_SYNC_ENABLED = booleanPreferencesKey("cloud_sync_enabled")
        val BIOMETRICS_ENABLED = booleanPreferencesKey("biometrics_enabled")
    }

    val settings: Flow<SettingsPreferences> = context.settingsDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            SettingsPreferences(
                notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
                darkModeEnabled = prefs[Keys.DARK_MODE_ENABLED] ?: false,
                cloudSyncEnabled = prefs[Keys.CLOUD_SYNC_ENABLED] ?: true,
                biometricsEnabled = prefs[Keys.BIOMETRICS_ENABLED] ?: true,
            )
        }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setDarkModeEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.DARK_MODE_ENABLED] = enabled
        }
    }

    suspend fun setCloudSyncEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.CLOUD_SYNC_ENABLED] = enabled
        }
    }

    suspend fun setBiometricsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.BIOMETRICS_ENABLED] = enabled
        }
    }
}
