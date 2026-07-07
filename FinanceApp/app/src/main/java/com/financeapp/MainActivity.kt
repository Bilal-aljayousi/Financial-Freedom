package com.financeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.financeapp.ui.navigation.FinanceAppNavigation
import com.financeapp.ui.screens.goals.GoalsViewModel
import com.financeapp.ui.screens.home.HomeViewModel
import com.financeapp.ui.screens.settings.SettingsViewModel
import com.financeapp.ui.theme.FinanceAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinanceAppTheme {
                val homeViewModel: HomeViewModel = viewModel()
                val goalsViewModel: GoalsViewModel = viewModel()
                val settingsViewModel: SettingsViewModel = viewModel()

                FinanceAppNavigation(
                    homeViewModel = homeViewModel,
                    goalsViewModel = goalsViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}
