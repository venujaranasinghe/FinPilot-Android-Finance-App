package com.bpeople.finpilot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.bpeople.finpilot.data.model.ThemeMode
import com.bpeople.finpilot.data.repository.SettingsRepository
import com.bpeople.finpilot.ui.navigation.FinPilotNavGraph
import com.bpeople.finpilot.ui.screens.pin.PinScreen
import com.bpeople.finpilot.ui.screens.pin.PinViewModel
import com.bpeople.finpilot.ui.theme.FinPilotTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var settingsRepository: SettingsRepository

    // Activity-scoped: survives config changes, shared with NavGraph's top-level hiltViewModel()
    private val pinViewModel: PinViewModel by viewModels()

    override fun onStop() {
        super.onStop()
        // isChangingConfigurations is true during screen rotation — don't lock in that case
        if (!isChangingConfigurations) {
            pinViewModel.lock()
        }
    }

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

            val isLocked by pinViewModel.isLocked.collectAsState()

            FinPilotTheme(darkTheme = useDarkTheme) {
                Box(modifier = Modifier.fillMaxSize()) {
                    FinPilotNavGraph()

                    if (isLocked) {
                        // Intercept back navigation so the user can't bypass the lock screen
                        BackHandler {}

                        LaunchedEffect(Unit) {
                            pinViewModel.initMode(PinViewModel.Mode.ENTRY)
                        }

                        PinScreen(
                            viewModel = pinViewModel,
                            onPinVerified = { pinViewModel.unlock() },
                            onPinSaved = {},
                        )
                    }
                }
            }
        }
    }
}

