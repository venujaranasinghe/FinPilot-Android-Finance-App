package com.bpeople.finpilot.ui.navigation

sealed class NavRoutes(val route: String) {
    object Splash : NavRoutes("splash")
    object Login : NavRoutes("auth/login")
    object Register : NavRoutes("auth/register")
    object ForgotPassword : NavRoutes("auth/forgot_password")
    object VerifyEmail : NavRoutes("auth/verify_email")
    object PinSetup : NavRoutes("pin/setup")
    object PinEntry : NavRoutes("pin/entry")
    object Dashboard : NavRoutes("dashboard")
    object AddIncome : NavRoutes("income/add")
    object AddExpense : NavRoutes("expense/add")
    object GoalTracker : NavRoutes("goal/{goalId}") {
        const val ARG_GOAL_ID = "goalId"
        fun createRoute(goalId: String = "default"): String = "goal/$goalId"
    }
    object Transactions : NavRoutes("transactions")
    object Profile : NavRoutes("profile")
    object Settings : NavRoutes("profile/settings")
    object Income : NavRoutes("income")
    object Notifications : NavRoutes("notifications")
}
