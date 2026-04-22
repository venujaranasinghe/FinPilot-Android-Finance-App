package com.bpeople.finpilot.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("auth/login")
    object Register : Screen("auth/register")
    object Onboarding : Screen("onboarding")
    object Dashboard : Screen("dashboard")
    object IncomeList : Screen("income/list")
    object AddIncome : Screen("income/add")
    object ExpenseList : Screen("expense/list")
    object AddExpense : Screen("expense/add")
    object Goal : Screen("goal")
    object AddGoal : Screen("goal/add")
    object Reports : Screen("reports")
    object Settings : Screen("settings")
}
