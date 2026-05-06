package com.bpeople.finpilot.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.bpeople.finpilot.ui.screens.auth.AuthViewModel
import com.bpeople.finpilot.ui.screens.auth.LoginScreen
import com.bpeople.finpilot.ui.screens.auth.RegisterScreen
import com.bpeople.finpilot.ui.screens.auth.SplashScreen
import com.bpeople.finpilot.ui.screens.dashboard.DashboardViewModel
import com.bpeople.finpilot.ui.screens.expense.ExpenseViewModel
import com.bpeople.finpilot.ui.screens.goal.GoalViewModel
import com.bpeople.finpilot.ui.screens.income.IncomeViewModel

@Composable
fun FinPilotNavGraph(
    navController: NavHostController = rememberNavController(),
) {
    // Single ViewModel instance shared across auth screens
    val authViewModel: AuthViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.Dashboard.route) {
            val dashboardViewModel: DashboardViewModel = hiltViewModel()
            val dashboardState by dashboardViewModel.dashboardState.collectAsState()
            LaunchedEffect(dashboardState) { }

            Box(modifier = Modifier.fillMaxSize()) { }
        }

        composable(Screen.IncomeList.route) {
            val incomeViewModel: IncomeViewModel = hiltViewModel()
            val incomeState by incomeViewModel.incomeState.collectAsState()
            LaunchedEffect(incomeState) { }

            Box(modifier = Modifier.fillMaxSize()) { }
        }

        composable(Screen.ExpenseList.route) {
            val expenseViewModel: ExpenseViewModel = hiltViewModel()
            val expenseState by expenseViewModel.expenseState.collectAsState()
            LaunchedEffect(expenseState) { }

            Box(modifier = Modifier.fillMaxSize()) { }
        }

        composable(Screen.Goal.route) {
            val goalViewModel: GoalViewModel = hiltViewModel()
            val goalState by goalViewModel.goalState.collectAsState()
            LaunchedEffect(goalState) { }

            Box(modifier = Modifier.fillMaxSize()) { }
        }
    }
}