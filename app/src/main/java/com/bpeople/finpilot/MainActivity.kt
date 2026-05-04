package com.bpeople.finpilot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.bpeople.finpilot.ui.navigation.FinPilotNavGraph
import com.bpeople.finpilot.ui.theme.FinPilotTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinPilotTheme {
                FinPilotNavGraph()
            }
        }
    }
}
