package com.bpeople.finpilot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.bpeople.finpilot.data.model.ThemeMode
import com.bpeople.finpilot.data.repository.SettingsRepository
import com.bpeople.finpilot.ui.navigation.FinPilotNavGraph
import com.bpeople.finpilot.ui.theme.FinPilotTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by settingsRepository.settings.collectAsState(
                initial = SettingsRepository.SettingsPreferences(
                    notificationsEnabled = true,
                    cloudSyncEnabled = true,
                    biometricsEnabled = true,
                    themeMode = ThemeMode.SYSTEM
                )
            )
            val useDarkTheme = when (settings.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            FinPilotTheme(darkTheme = useDarkTheme) {
                FinPilotNavGraph()
            }
        }
    }
}
