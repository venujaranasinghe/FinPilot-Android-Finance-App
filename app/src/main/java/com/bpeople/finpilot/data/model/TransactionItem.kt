package com.bpeople.finpilot.data.model

enum class TransactionType { INCOME, EXPENSE }

enum class Period { WEEK, MONTH, YEAR }

data class TransactionItem(
    val id: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val source: String = "",
    val displayName: String = "",
    val amount: Double = 0.0,
    val currency: String = "LKR",
    val amountInLKR: Double = 0.0,
    val exchangeRate: Double? = null,
    val note: String? = null,
    val paymentMethod: String = "cash",
    val isRecurring: Boolean = false,
    val timestampMillis: Long = 0L,
)

data class MonthlyBarData(
    val month: String,
    val income: Double,
    val expenses: Double,
)
