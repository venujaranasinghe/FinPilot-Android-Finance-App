package com.bpeople.finpilot.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.bpeople.finpilot.ui.screens.auth.VerifyEmailScreen
import com.bpeople.finpilot.ui.screens.dashboard.DashboardScreen
import com.bpeople.finpilot.ui.screens.dashboard.DashboardViewModel
import com.bpeople.finpilot.ui.screens.expense.ExpenseScreen
import com.bpeople.finpilot.ui.screens.expense.ExpenseViewModel
import com.bpeople.finpilot.ui.screens.goal.GoalTrackerScreen
import com.bpeople.finpilot.ui.screens.goal.GoalViewModel
import com.bpeople.finpilot.ui.screens.income.IncomeScreen
import com.bpeople.finpilot.ui.screens.income.IncomeViewModel
import com.bpeople.finpilot.ui.screens.profile.ProfileScreen
import com.bpeople.finpilot.ui.screens.profile.ProfileViewModel
import com.bpeople.finpilot.ui.screens.profile.SettingsScreen
import com.bpeople.finpilot.ui.screens.profile.SettingsViewModel
import com.bpeople.finpilot.ui.screens.notifications.NotificationsScreen
import com.bpeople.finpilot.ui.screens.notifications.NotificationsViewModel
import com.bpeople.finpilot.ui.screens.transactions.TransactionScreen
import com.bpeople.finpilot.ui.screens.transactions.TransactionViewModel

@Composable
fun FinPilotNavGraph(
    navController: NavHostController = rememberNavController(),
) {
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
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(NavRoutes.VerifyEmail.route) {
                        popUpTo(NavRoutes.Register.route) { inclusive = true }
                    }
                },
            )
        }

        composable(NavRoutes.VerifyEmail.route) {
            val infoMessage by authViewModel.infoMessage.collectAsState()
            val errorMessage by authViewModel.errorMessage.collectAsState()
            VerifyEmailScreen(
                infoMessage = infoMessage,
                errorMessage = errorMessage,
                onResendVerification = { authViewModel.resendVerificationEmail() },
                onNavigateToLogin = {
                    navController.navigate(NavRoutes.Login.route) {
                        popUpTo(NavRoutes.VerifyEmail.route) { inclusive = true }
                    }
                },
            )
        }

        composable(NavRoutes.Dashboard.route) {
            val dashboardViewModel: DashboardViewModel = hiltViewModel()
            val dashboardState by dashboardViewModel.dashboardState.collectAsState()
            val insight = navController.currentBackStackEntry?.savedStateHandle?.get<String>("expense_insight")
            val currentUser by authViewModel.currentUser.collectAsState()
            val userName = currentUser?.displayName?.takeIf { it.isNotBlank() }
                ?: currentUser?.email?.substringBefore("@")
                    ?.replaceFirstChar { it.titlecase() }
                ?: ""

            DashboardScreen(
                state = dashboardState,
                userName = userName,
                insightMessage = insight,
                onAddExpense = {
                    navController.navigate(NavRoutes.AddExpense.route)
                },
                onNavigateToIncome = {
                    navController.navigate(NavRoutes.Income.route)
                },
                onNavigateToTransactions = {
                    navController.navigate(NavRoutes.Transactions.route)
                },
                onNavigateToGoals = {
                    navController.navigate(NavRoutes.GoalTracker.createRoute())
                },
                onNavigateToProfile = {
                    navController.navigate(NavRoutes.Profile.route)
                },
                onNavigateToNotifications = {
                    navController.navigate(NavRoutes.Notifications.route)
                },
                onNavigateToSettings = {
                    navController.navigate(NavRoutes.Settings.route)
                },
                onLogout = {
                    authViewModel.signOut()
                    navController.navigate(NavRoutes.Login.route) {
                        popUpTo(NavRoutes.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.Income.route) {
            val incomeViewModel: IncomeViewModel = hiltViewModel()
            IncomeScreen(
                viewModel = incomeViewModel,
                onNavigateToDashboard = {
                    navController.popBackStack(NavRoutes.Dashboard.route, inclusive = false)
                },
                onNavigateToExpense = {
                    navController.navigate(NavRoutes.AddExpense.route)
                },
                onNavigateToTransactions = {
                    navController.navigate(NavRoutes.Transactions.route)
                },
                onNavigateToGoals = {
                    navController.navigate(NavRoutes.GoalTracker.createRoute())
                },
                onNavigateToProfile = {
                    navController.navigate(NavRoutes.Profile.route)
                },
                onNavigateToSettings = {
                    navController.navigate(NavRoutes.Settings.route)
                },
            )
        }

        composable(NavRoutes.AddExpense.route) {
            val expenseViewModel: ExpenseViewModel = hiltViewModel()
            ExpenseScreen(
                viewModel = expenseViewModel,
                onNavigateToDashboard = {
                    navController.popBackStack(NavRoutes.Dashboard.route, inclusive = false)
                },
                onNavigateToIncome = {
                    navController.navigate(NavRoutes.Income.route)
                },
                onNavigateToTransactions = {
                    navController.navigate(NavRoutes.Transactions.route)
                },
                onNavigateToGoals = {
                    navController.navigate(NavRoutes.GoalTracker.createRoute())
                },
                onNavigateToProfile = {
                    navController.navigate(NavRoutes.Profile.route)
                },
                onNavigateToSettings = {
                    navController.navigate(NavRoutes.Settings.route)
                },
                onExpenseAdded = { insight ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("expense_insight", insight)
                    navController.popBackStack(NavRoutes.Dashboard.route, inclusive = false)
                },
            )
        }

        composable(
            route = NavRoutes.GoalTracker.route,
            arguments = listOf(
                navArgument(NavRoutes.GoalTracker.ARG_GOAL_ID) { type = NavType.StringType }
            ),
        ) {
            val goalViewModel: GoalViewModel = hiltViewModel()
            
            GoalTrackerScreen(
                viewModel = goalViewModel,
                onNavigateToDashboard = {
                    navController.navigate(NavRoutes.Dashboard.route) {
                        popUpTo(NavRoutes.Dashboard.route) { inclusive = true }
                    }
                },
                onNavigateToIncome = {
                    navController.navigate(NavRoutes.Income.route)
                },
                onNavigateToExpense = {
                    navController.navigate(NavRoutes.AddExpense.route)
                },
                onNavigateToTransactions = {
                    navController.navigate(NavRoutes.Transactions.route)
                },
                onNavigateToProfile = {
                    navController.navigate(NavRoutes.Profile.route)
                },
                onNavigateToSettings = {
                    navController.navigate(NavRoutes.Settings.route)
                },
            )
        }

        composable(NavRoutes.Transactions.route) {
            val transactionViewModel: TransactionViewModel = hiltViewModel()
            TransactionScreen(
                viewModel = transactionViewModel,
                onNavigateToDashboard = {
                    navController.navigate(NavRoutes.Dashboard.route) {
                        popUpTo(NavRoutes.Transactions.route) { inclusive = true }
                    }
                },
                onNavigateToGoals = {
                    navController.navigate(NavRoutes.GoalTracker.createRoute())
                },
                onNavigateToProfile = {
                    navController.navigate(NavRoutes.Profile.route)
                },
                onNavigateToSettings = {
                    navController.navigate(NavRoutes.Settings.route)
                },
            )
        }

        composable(NavRoutes.Profile.route) {
            val profileViewModel: ProfileViewModel = hiltViewModel()
            val currentUser by profileViewModel.currentUser.collectAsState()
            val profileUiState by profileViewModel.uiState.collectAsState()
            ProfileScreen(
                displayName = currentUser?.displayName,
                email = currentUser?.email,
                uiState = profileUiState,
                onNavigateToDashboard = {
                    navController.navigate(NavRoutes.Dashboard.route)
                },
                onNavigateToIncome = {
                    navController.navigate(NavRoutes.Income.route)
                },
                onNavigateToExpense = {
                    navController.navigate(NavRoutes.AddExpense.route)
                },
                onNavigateToTransactions = {
                    navController.navigate(NavRoutes.Transactions.route)
                },
                onNavigateToGoals = {
                    navController.navigate(NavRoutes.GoalTracker.createRoute())
                },
                onNavigateToProfile = { /* Currently on Profile */ },
                onNavigateToSettings = {
                    navController.navigate(NavRoutes.Settings.route)
                },
                onLogout = {
                    profileViewModel.signOut()
                    navController.navigate(NavRoutes.Login.route) {
                        popUpTo(NavRoutes.Profile.route) { inclusive = true }
                    }
                },
                onUpdateDisplayName = profileViewModel::updateDisplayName,
                onToggleIncomeSource = profileViewModel::toggleIncomeSource,
                onAddIncomeSource = profileViewModel::addIncomeSource,
            )
        }

        composable(NavRoutes.Settings.route) {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val settingsState by settingsViewModel.uiState.collectAsState()
            SettingsScreen(
                state = settingsState,
                events = settingsViewModel.events,
                onNavigateBack = { navController.popBackStack() },
                onNotificationsChange = settingsViewModel::onNotificationsChange,
                onThemeModeChange = settingsViewModel::onThemeModeChange,
                onCloudSyncChange = settingsViewModel::onCloudSyncChange,
                onBiometricsChange = settingsViewModel::onBiometricsChange,
                onChangePassword = settingsViewModel::onChangePassword,
                onExportData = settingsViewModel::onExportData,
                onDeleteAccount = settingsViewModel::onDeleteAccount,
                onAccountDeleted = {
                    navController.navigate(NavRoutes.Login.route) {
                        popUpTo(NavRoutes.Settings.route) { inclusive = true }
                    }
                },
                onSetUsdEnabled = settingsViewModel::setUsdEnabled,
                onSetUsdtEnabled = settingsViewModel::setUsdtEnabled,
                onSetAutoConvert = settingsViewModel::setAutoConvert,
                onNotifySalaryReminder = settingsViewModel::setNotifySalaryReminder,
                onNotifyWeeklySummary = settingsViewModel::setNotifyWeeklySummary,
                onNotifyGoalMilestone = settingsViewModel::setNotifyGoalMilestone,
                onNotifyBudgetOverspend = settingsViewModel::setNotifyBudgetOverspend,
                onBudgetThreshold = settingsViewModel::setBudgetOverspendThreshold,
                onClearCache = settingsViewModel::onClearCache,
            )
        }

        composable(NavRoutes.Notifications.route) {
            val notificationsViewModel: NotificationsViewModel = hiltViewModel()
            NotificationsScreen(
                viewModel = notificationsViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDashboard = {
                    navController.navigate(NavRoutes.Dashboard.route) {
                        popUpTo(NavRoutes.Dashboard.route) { inclusive = false }
                    }
                },
                onNavigateToIncome = {
                    navController.navigate(NavRoutes.Income.route)
                },
                onNavigateToGoals = {
                    navController.navigate(NavRoutes.GoalTracker.createRoute())
                },
                onNavigateToProfile = {
                    navController.navigate(NavRoutes.Profile.route)
                },
            )
        }
    }
}