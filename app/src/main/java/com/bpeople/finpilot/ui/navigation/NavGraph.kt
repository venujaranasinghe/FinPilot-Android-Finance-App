package com.bpeople.finpilot.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.hilt.navigation.compose.hiltViewModel
import com.bpeople.finpilot.ui.screens.auth.AuthViewModel
import com.bpeople.finpilot.ui.screens.auth.ForgotPasswordScreen
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
        startDestination = NavRoutes.Splash.route,
    ) {
        composable(NavRoutes.Splash.route) {
            SplashScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    navController.navigate(NavRoutes.Login.route) {
                        popUpTo(NavRoutes.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(NavRoutes.Dashboard.route) {
                        popUpTo(NavRoutes.Splash.route) { inclusive = true }
                    }
                },
            )
        }

        composable(NavRoutes.Login.route) {
            val currentUser by authViewModel.currentUser.collectAsState()
            LaunchedEffect(currentUser) {
                if (currentUser != null) {
                    navController.navigate(NavRoutes.Dashboard.route) {
                        popUpTo(NavRoutes.Login.route) { inclusive = true }
                    }
                }
            }

            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = {
                    navController.navigate(NavRoutes.Register.route)
                },
                onLoginSuccess = {
                    navController.navigate(NavRoutes.Dashboard.route) {
                        popUpTo(NavRoutes.Login.route) { inclusive = true }
                    }
                },
                onNavigateToForgotPassword = {
                    navController.navigate(NavRoutes.ForgotPassword.route)
                },
            )
        }

        composable(NavRoutes.ForgotPassword.route) {
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(NavRoutes.Register.route) {
            val currentUser by authViewModel.currentUser.collectAsState()
            LaunchedEffect(currentUser) {
                if (currentUser != null) {
                    navController.navigate(NavRoutes.Dashboard.route) {
                        popUpTo(NavRoutes.Register.route) { inclusive = true }
                    }
                }
            }

            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(NavRoutes.Dashboard.route) {
                        popUpTo(NavRoutes.Login.route) { inclusive = true }
                    }
                },
            )
        }

        composable(NavRoutes.Dashboard.route) {
            val dashboardViewModel: DashboardViewModel = hiltViewModel()
            val dashboardState by dashboardViewModel.dashboardState.collectAsState()
            LaunchedEffect(dashboardState) { }

            Box(modifier = Modifier.fillMaxSize()) { }
        }

        composable(NavRoutes.AddIncome.route) {
            val incomeViewModel: IncomeViewModel = hiltViewModel()
            val incomeState by incomeViewModel.incomeState.collectAsState()
            LaunchedEffect(incomeState) { }

            Box(modifier = Modifier.fillMaxSize()) { }
        }

        composable(NavRoutes.AddExpense.route) {
            val expenseViewModel: ExpenseViewModel = hiltViewModel()
            val expenseState by expenseViewModel.expenseState.collectAsState()
            LaunchedEffect(expenseState) { }

            Box(modifier = Modifier.fillMaxSize()) { }
        }

        composable(
            route = NavRoutes.GoalTracker.route,
            arguments = listOf(
                navArgument(NavRoutes.GoalTracker.ARG_GOAL_ID) { type = NavType.StringType }
            ),
        ) { backStackEntry ->
            val goalId = backStackEntry.arguments?.getString(NavRoutes.GoalTracker.ARG_GOAL_ID)
            val goalViewModel: GoalViewModel = hiltViewModel()
            val goalState by goalViewModel.goalState.collectAsState()
            LaunchedEffect(goalState, goalId) { }

            Box(modifier = Modifier.fillMaxSize()) { }
        }
    }
}