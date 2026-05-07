package com.bpeople.finpilot.ui.navigation

sealed class NavRoutes(val route: String) {
    object Splash : NavRoutes("splash")
    object Login : NavRoutes("auth/login")
    object Register : NavRoutes("auth/register")
    object Dashboard : NavRoutes("dashboard")
    object AddIncome : NavRoutes("income/add")
    object AddExpense : NavRoutes("expense/add")
    object GoalTracker : NavRoutes("goal/{goalId}") {
        const val ARG_GOAL_ID = "goalId"
        fun createRoute(goalId: String): String = "goal/$goalId"
    }
}
